#!/bin/bash

# Source configuration file
CONFIG_FILE="$HOME/.ollama/azure/config"
if [[ ! -f "$CONFIG_FILE" ]]; then
    echo "Error: Configuration file not found: $CONFIG_FILE"
    echo "Please create the configuration file with the following required variables:"
    echo "RESOURCE_GROUP"
    echo "LOCATION"
    echo "VM_NAME"
    echo "VM_SIZE"
    echo "GPU_VM_SIZE"
    echo "USE_GPU"
    echo "DISK_SIZE"
    echo "MAX_RUN_DURATION"
    echo "ADMIN_USERNAME"
    exit 1
fi

# Source the configuration
source "$CONFIG_FILE"

# Verify required variables are set
required_vars=(RESOURCE_GROUP LOCATION VM_NAME VM_SIZE DISK_SIZE MAX_RUN_DURATION ADMIN_USERNAME)
missing_vars=()

for var in "${required_vars[@]}"; do
    if [[ -z "${!var}" ]]; then
        missing_vars+=("$var")
    fi
done

if [[ ${#missing_vars[@]} -gt 0 ]]; then
    echo "Error: The following required variables are missing in $CONFIG_FILE:"
    printf '%s\n' "${missing_vars[@]}"
    exit 1
fi

usage() {
    echo "Usage: $0 [create|start|stop|destroy|status]"
    echo "  create  - Creates and starts a new Ollama VM instance"
    echo "  start   - Starts a stopped VM instance"
    echo "  stop    - Stops the running VM instance"
    echo "  destroy - Completely removes the VM instance"
    echo "  status  - Shows the current status of the VM instance"
    echo ""
    echo "Configuration file: $CONFIG_FILE"
    exit 1
}

create_custom_data_script() {
    cat << 'EOF' > /tmp/custom-data.sh
#!/bin/bash

# Install Ollama
curl -fsSL https://ollama.com/install.sh | sh

# Create directory for Ollama service override
mkdir -p /etc/systemd/system/ollama.service.d

# Configure Ollama to listen on all interfaces
cat << 'END' > /etc/systemd/system/ollama.service.d/override.conf
[Service]
Environment="OLLAMA_HOST=0.0.0.0"
END

# Reload systemd configs
systemctl daemon-reload

# Enable and start Ollama service
systemctl enable ollama
systemctl start ollama

# Install net-tools for netstat
apt-get update && apt-get install -y net-tools

# Log the status and configuration for debugging
echo "Ollama installation completed at $(date)" >> /var/log/ollama-install.log
echo "Checking Ollama service status:" >> /var/log/ollama-install.log
systemctl status ollama >> /var/log/ollama-install.log
echo "Checking network listeners:" >> /var/log/ollama-install.log
netstat -tulpn | grep 11434 >> /var/log/ollama-install.log
EOF
    chmod +x /tmp/custom-data.sh
    # Convert to base64 for Azure custom data
    base64 /tmp/custom-data.sh > /tmp/custom-data-base64
}

create_network_resources() {
    local vnet_name="${VM_NAME}-vnet"
    local subnet_name="${VM_NAME}-subnet"
    local nsg_name="${VM_NAME}-nsg"
    local public_ip_name="${VM_NAME}-ip"

    # Create Network Security Group
    echo "Creating Network Security Group..."
    az network nsg create \
        --resource-group "$RESOURCE_GROUP" \
        --name "$nsg_name" \
        --location "$LOCATION"

    # Add NSG rule for Ollama
    az network nsg rule create \
        --resource-group "$RESOURCE_GROUP" \
        --nsg-name "$nsg_name" \
        --name "AllowOllama" \
        --priority 1000 \
        --protocol tcp \
        --destination-port-range 11434 \
        --access allow

    # Add NSG rule for SSH
    az network nsg rule create \
        --resource-group "$RESOURCE_GROUP" \
        --nsg-name "$nsg_name" \
        --name "AllowSSH" \
        --priority 1001 \
        --protocol tcp \
        --destination-port-range 22 \
        --access allow

    # Create VNet and Subnet
    echo "Creating VNet and Subnet..."
    az network vnet create \
        --resource-group "$RESOURCE_GROUP" \
        --name "$vnet_name" \
        --subnet-name "$subnet_name" \
        --location "$LOCATION"

    # Create Public IP
    echo "Creating Public IP..."
    az network public-ip create \
        --resource-group "$RESOURCE_GROUP" \
        --name "$public_ip_name" \
        --sku Standard \
        --location "$LOCATION"

    # Create NIC
    echo "Creating Network Interface..."
    az network nic create \
        --resource-group "$RESOURCE_GROUP" \
        --name "${VM_NAME}-nic" \
        --vnet-name "$vnet_name" \
        --subnet "$subnet_name" \
        --network-security-group "$nsg_name" \
        --public-ip-address "$public_ip_name"
}

verify_installation() {
    local public_ip=$1
    echo "Verifying Ollama installation..."

    # Try to SSH and check Ollama status
    ssh -i "$HOME/.ssh/${VM_NAME}_key" -o StrictHostKeyChecking=no "$ADMIN_USERNAME@$public_ip" '
        echo "Checking Ollama service status..."
        sudo systemctl status ollama
        echo "Checking Ollama service logs..."
        sudo journalctl -u ollama -n 50
        echo "Checking install logs..."
        sudo cat /var/log/ollama-install.log
    ' || echo "Failed to verify installation via SSH"
}

wait_for_ollama() {
    local public_ip=$1
    local max_attempts=30
    local attempt=1

    echo "Waiting for Ollama to be ready..."
    while [[ $attempt -le $max_attempts ]]; do
        if curl -s "http://$public_ip:11434/api/version" &>/dev/null; then
            return 0
        fi
        echo "Attempt $attempt/$max_attempts: Ollama not ready yet, waiting..."
        if [[ $attempt -eq 5 ]]; then
            echo "Verifying installation status..."
            verify_installation "$public_ip"
        fi
        sleep 10
        ((attempt++))
    done
    echo "Warning: Ollama service not responding after $max_attempts attempts"
    verify_installation "$public_ip"
    return 1
}

check_vm_exists() {
    az vm show \
        --resource-group "$RESOURCE_GROUP" \
        --name "$VM_NAME" \
        --show-details \
        --query "name" \
        --output tsv 2>/dev/null
}

get_vm_status() {
    az vm show \
        --resource-group "$RESOURCE_GROUP" \
        --name "$VM_NAME" \
        --show-details \
        --query "powerState" \
        --output tsv 2>/dev/null
}

get_public_ip() {
    az vm show \
        --resource-group "$RESOURCE_GROUP" \
        --name "$VM_NAME" \
        --show-details \
        --query "publicIps" \
        --output tsv 2>/dev/null
}

create_instance() {
    echo "Creating Azure VM instance..."

    # Check if VM already exists
    if check_vm_exists; then
        echo "Error: VM '$VM_NAME' already exists. Use 'start' to start a stopped VM or 'destroy' to remove it first."
        exit 1
    fi

    # Generate SSH key pair if it doesn't exist
    if [[ ! -f "$HOME/.ssh/${VM_NAME}_key" ]]; then
        ssh-keygen -t rsa -b 4096 -f "$HOME/.ssh/${VM_NAME}_key" -N ""
    fi
    SSH_PUB_KEY=$(cat "$HOME/.ssh/${VM_NAME}_key.pub")

    # Create custom data script
    create_custom_data_script

    # Create network resources
    create_network_resources

    # Select VM size based on GPU configuration
    local vm_size="$VM_SIZE"
    if [[ "$USE_GPU" == "true" && -n "$GPU_VM_SIZE" ]]; then
        vm_size="$GPU_VM_SIZE"

        # Add GPU driver installation to custom data script
        cat << 'EOF' >> /tmp/custom-data.sh
# Install NVIDIA driver and CUDA toolkit
apt-get update
apt-get install -y nvidia-driver-525 nvidia-cuda-toolkit
EOF
        # Convert to base64 for Azure custom data
        base64 /tmp/custom-data.sh > /tmp/custom-data-base64
    fi

    # Create the VM
    echo "Creating VM..."
    az vm create \
        --resource-group "$RESOURCE_GROUP" \
        --name "$VM_NAME" \
        --image "Ubuntu:0001-com-ubuntu-server-noble:24_04-lts-gen2:latest" \
        --size "$vm_size" \
        --admin-username "$ADMIN_USERNAME" \
        --ssh-key-values "$SSH_PUB_KEY" \
        --nics "${VM_NAME}-nic" \
        --os-disk-size-gb "$DISK_SIZE" \
        --custom-data "@/tmp/custom-data-base64"

    # Get the public IP
    PUBLIC_IP=$(get_public_ip)

    echo "VM is being created at IP: $PUBLIC_IP"
    echo "Waiting for VM to be fully initialized..."
    sleep 30

    # Wait for Ollama to be ready
    wait_for_ollama "$PUBLIC_IP"

    echo "VM setup completed!"
    echo "Public IP: $PUBLIC_IP"
    echo "Ollama will be available at: http://$PUBLIC_IP:11434"
    echo ""
    echo "To connect to the VM:"
    echo "ssh -i ~/.ssh/${VM_NAME}_key $ADMIN_USERNAME@$PUBLIC_IP"

    # Cleanup custom data script
    rm -f /tmp/custom-data.sh /tmp/custom-data-base64
}

start_instance() {
    if ! check_vm_exists; then
        echo "Error: VM '$VM_NAME' does not exist. Use 'create' to create a new VM."
        exit 1
    fi

    local status=$(get_vm_status)
    if [[ "$status" == "VM running" ]]; then
        echo "VM '$VM_NAME' is already running."
        exit 0
    fi

    echo "Starting Azure VM..."
    az vm start \
        --resource-group "$RESOURCE_GROUP" \
        --name "$VM_NAME"

    # Get the public IP
    PUBLIC_IP=$(get_public_ip)

    echo "VM is running at IP: $PUBLIC_IP"
    echo "Ollama will be available at: http://$PUBLIC_IP:11434"
    echo ""
    echo "To connect to the VM:"
    echo "ssh -i ~/.ssh/${VM_NAME}_key $ADMIN_USERNAME@$PUBLIC_IP"
}

stop_instance() {
    echo "Stopping Azure VM..."
    az vm stop \
        --resource-group "$RESOURCE_GROUP" \
        --name "$VM_NAME"
}

destroy_instance() {
    echo "Destroying Azure VM and related resources..."

    # Delete the VM and its managed disks
    az vm delete \
        --resource-group "$RESOURCE_GROUP" \
        --name "$VM_NAME" \
        --yes

    # Delete the NIC
    az network nic delete \
        --resource-group "$RESOURCE_GROUP" \
        --name "${VM_NAME}-nic"

    # Delete the Public IP
    az network public-ip delete \
        --resource-group "$RESOURCE_GROUP" \
        --name "${VM_NAME}-ip"

    # Delete the NSG
    az network nsg delete \
        --resource-group "$RESOURCE_GROUP" \
        --name "${VM_NAME}-nsg"

    # Delete the VNet
    az network vnet delete \
        --resource-group "$RESOURCE_GROUP" \
        --name "${VM_NAME}-vnet"
}

get_status() {
    if check_vm_exists; then
        local status=$(get_vm_status)
        echo "VM status: $status"

        if [[ "$status" == "VM running" ]]; then
            PUBLIC_IP=$(get_public_ip)
            echo "Public IP: $PUBLIC_IP"
            echo "Ollama URL: http://$PUBLIC_IP:11434"
            echo ""
            echo "To connect to the VM:"
            echo "ssh -i ~/.ssh/${VM_NAME}_key $ADMIN_USERNAME@$PUBLIC_IP"

            # Try to verify Ollama status
            verify_installation "$PUBLIC_IP"
        fi
    else
        echo "VM does not exist"
    fi
}

# Main script logic
case "$1" in
    create)
        create_instance
        ;;
    start)
        start_instance
        ;;
    stop)
        stop_instance
        ;;
    destroy)
        destroy_instance
        ;;
    status)
        get_status
        ;;
    *)
        usage
        ;;
esac
