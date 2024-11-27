#!/bin/bash

# Source configuration file
CONFIG_FILE="$HOME/.google/vm-config"
if [[ ! -f "$CONFIG_FILE" ]]; then
    echo "Error: Configuration file not found: $CONFIG_FILE"
    echo "Please create the configuration file with the following required variables:"
    echo "PROJECT"
    echo "ZONE"
    echo "INSTANCE_NAME"
    echo "MACHINE_TYPE"
    echo "DISK_SIZE"
    echo "MAX_RUN_DURATION"
    echo "SERVICE_ACCOUNT"
    exit 1
fi

# Source the configuration
source "$CONFIG_FILE"

# Verify required variables are set
required_vars=(PROJECT ZONE INSTANCE_NAME MACHINE_TYPE DISK_SIZE MAX_RUN_DURATION SERVICE_ACCOUNT)
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

create_startup_script() {
    cat << 'EOF' > /tmp/startup-script.sh
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

# Install net-tools for netstat (might be needed on some systems)
apt-get update && apt-get install -y net-tools

# Log the status and configuration for debugging
echo "Ollama installation completed at $(date)" >> /var/log/ollama-install.log
echo "Checking Ollama service status:" >> /var/log/ollama-install.log
systemctl status ollama >> /var/log/ollama-install.log
echo "Checking network listeners:" >> /var/log/ollama-install.log
netstat -tulpn | grep 11434 >> /var/log/ollama-install.log
EOF
    chmod +x /tmp/startup-script.sh
}

create_firewall_rule() {
    # Check if firewall rule already exists
    if ! gcloud compute firewall-rules describe "allow-ollama" --project="$PROJECT" &>/dev/null; then
        echo "Creating firewall rule for Ollama..."
        gcloud compute firewall-rules create "allow-ollama" \
            --project="$PROJECT" \
            --direction=INGRESS \
            --priority=1000 \
            --network=default \
            --action=ALLOW \
            --rules=tcp:11434 \
            --source-ranges=0.0.0.0/0 \
            --target-tags=ollama-server
    fi
}

verify_installation() {
    local external_ip=$1
    echo "Verifying Ollama installation..."

    # Try to SSH and check Ollama status
    gcloud compute ssh "$INSTANCE_NAME" --zone="$ZONE" --command='
        echo "Checking Ollama service status..."
        sudo systemctl status ollama
        echo "Checking Ollama service logs..."
        sudo journalctl -u ollama -n 50
        echo "Checking install logs..."
        sudo cat /var/log/ollama-install.log
    ' || echo "Failed to verify installation via SSH"
}

wait_for_ollama() {
    local external_ip=$1
    local max_attempts=30
    local attempt=1

    echo "Waiting for Ollama to be ready..."
    while [[ $attempt -le $max_attempts ]]; do
        if curl -s "http://$external_ip:11434/api/version" &>/dev/null; then
            return 0
        fi
        echo "Attempt $attempt/$max_attempts: Ollama not ready yet, waiting..."
        if [[ $attempt -eq 5 ]]; then
            echo "Verifying installation status..."
            verify_installation "$external_ip"
        fi
        sleep 10
        ((attempt++))
    done
    echo "Warning: Ollama service not responding after $max_attempts attempts"
    verify_installation "$external_ip"
    return 1
}

check_instance_exists() {
    gcloud compute instances describe "$INSTANCE_NAME" --zone="$ZONE" &>/dev/null
}

check_instance_status() {
    gcloud compute instances describe "$INSTANCE_NAME" --zone="$ZONE" --format='get(status)'
}

create_instance() {
    echo "Creating VM instance..."

    # Check if instance already exists
    if check_instance_exists; then
        echo "Error: Instance '$INSTANCE_NAME' already exists. Use 'start' to start a stopped instance or 'destroy' to remove it first."
        exit 1
    }

    # Create startup script
    create_startup_script

    # Create firewall rule if it doesn't exist
    create_firewall_rule

    gcloud compute instances create "$INSTANCE_NAME" \
        --project="$PROJECT" \
        --zone="$ZONE" \
        --machine-type="$MACHINE_TYPE" \
        --network-interface=network-tier=PREMIUM,stack-type=IPV4_ONLY,subnet=default \
        --no-restart-on-failure \
        --maintenance-policy=TERMINATE \
        --provisioning-model=SPOT \
        --instance-termination-action=STOP \
        --max-run-duration="$MAX_RUN_DURATION" \
        --service-account="$SERVICE_ACCOUNT" \
        --scopes=https://www.googleapis.com/auth/devstorage.read_only,https://www.googleapis.com/auth/logging.write,https://www.googleapis.com/auth/monitoring.write,https://www.googleapis.com/auth/service.management.readonly,https://www.googleapis.com/auth/servicecontrol,https://www.googleapis.com/auth/trace.append \
        --create-disk=auto-delete=yes,boot=yes,device-name="$INSTANCE_NAME",image=projects/ubuntu-os-cloud/global/images/ubuntu-2404-noble-amd64-v20241115,mode=rw,size="$DISK_SIZE",type=pd-standard \
        --no-shielded-secure-boot \
        --shielded-vtpm \
        --shielded-integrity-monitoring \
        --tags=ollama-server \
        --labels=goog-ec-src=vm_add-gcloud \
        --metadata-from-file=startup-script=/tmp/startup-script.sh \
        --reservation-affinity=any

    # Wait for instance to be ready
    echo "Waiting for instance to be ready..."
    while [[ $(check_instance_status) != "RUNNING" ]]; do
        sleep 5
    done

    # Get the external IP
    EXTERNAL_IP=$(gcloud compute instances describe "$INSTANCE_NAME" --zone="$ZONE" --format='get(networkInterfaces[0].accessConfigs[0].natIP)')

    echo "Instance is running at IP: $EXTERNAL_IP"
    echo "Waiting for instance to be fully initialized..."
    sleep 30  # Give startup script time to begin execution

    # Wait for Ollama to be ready
    wait_for_ollama "$EXTERNAL_IP"

    echo "Instance setup completed!"
    echo "External IP: $EXTERNAL_IP"
    echo "Ollama will be available at: http://$EXTERNAL_IP:11434"
    echo ""
    echo "To connect to the VM:"
    echo "gcloud command: gcloud compute ssh $INSTANCE_NAME --zone=$ZONE"

    # Cleanup startup script
    rm -f /tmp/startup-script.sh
}

start_instance() {
    # Check if instance exists
    if ! check_instance_exists; then
        echo "Error: Instance '$INSTANCE_NAME' does not exist. Use 'create' to create a new instance."
        exit 1
    }

    # Get current status
    local status=$(check_instance_status)

    # Check if already running
    if [[ "$status" == "RUNNING" ]]; then
        echo "Instance '$INSTANCE_NAME' is already running."
        exit 0
    fi

    # Start the instance
    echo "Starting VM instance..."
    gcloud compute instances start "$INSTANCE_NAME" --zone="$ZONE"

    # Wait for instance to be ready
    echo "Waiting for instance to be ready..."
    while [[ $(check_instance_status) != "RUNNING" ]]; do
        sleep 5
    done

    # Get the external IP
    EXTERNAL_IP=$(gcloud compute instances describe "$INSTANCE_NAME" --zone="$ZONE" --format='get(networkInterfaces[0].accessConfigs[0].natIP)')

    echo "Instance is running at IP: $EXTERNAL_IP"
    echo "Ollama will be available at: http://$EXTERNAL_IP:11434"
    echo ""
    echo "To connect to the VM:"
    echo "gcloud command: gcloud compute ssh $INSTANCE_NAME --zone=$ZONE"
}

stop_instance() {
    echo "Stopping VM instance..."
    gcloud compute instances stop "$INSTANCE_NAME" --zone="$ZONE"
}

destroy_instance() {
    echo "Destroying VM instance..."
    gcloud compute instances delete "$INSTANCE_NAME" --zone="$ZONE" --quiet
}

get_status() {
    if check_instance_exists; then
        STATUS=$(check_instance_status)
        EXTERNAL_IP=$(gcloud compute instances describe "$INSTANCE_NAME" --zone="$ZONE" --format='get(networkInterfaces[0].accessConfigs[0].natIP)')
        echo "Instance status: $STATUS"
        if [[ $STATUS == "RUNNING" ]]; then
            echo "External IP: $EXTERNAL_IP"
            echo "Ollama URL: http://$EXTERNAL_IP:11434"
            echo ""
            echo "To connect to the VM:"
            echo "gcloud command: gcloud compute ssh $INSTANCE_NAME --zone=$ZONE"

            # Try to verify Ollama status
            verify_installation "$EXTERNAL_IP"
        fi
    else
        echo "Instance does not exist"
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