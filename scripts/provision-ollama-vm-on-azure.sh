#!/usr/bin/env bash

# Get the directory where the script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"

cd "$ROOT_DIR" || exit 1

# Source configuration file
CONFIG_FILE="$HOME/.ollama/azure/config"
if [[ ! -f "$CONFIG_FILE" ]]; then
    echo "Error: Configuration file not found: $CONFIG_FILE"
    echo "Required variables: RESOURCE_GROUP LOCATION VM_NAME VM_SIZE GPU_VM_SIZE USE_GPU DISK_SIZE ADMIN_USERNAME"
    exit 1
fi

source "$CONFIG_FILE"

# Terraform working directory
TERRAFORM_DIR="$HOME/.ollama/azure/terraform"
mkdir -p "$TERRAFORM_DIR"

# Initialize Terraform variables
init_terraform() {
    cat > "$TERRAFORM_DIR/terraform.tfvars" << EOF
resource_group     = "$RESOURCE_GROUP"
location          = "$LOCATION"
vm_name           = "$VM_NAME"
vm_size           = "$VM_SIZE"
gpu_vm_size      = "$GPU_VM_SIZE"
use_gpu          = ${USE_GPU:-false}
disk_size        = $DISK_SIZE
admin_username   = "$ADMIN_USERNAME"
EOF

    cp terraform/azure/*.tf "$TERRAFORM_DIR/"
    cp terraform/user_data.tpl "$TERRAFORM_DIR/"

    (cd "$TERRAFORM_DIR" && terraform init)
}

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

connect_to_instance() {
    INSTANCE_IP=$(cd "$TERRAFORM_DIR" && terraform output -raw public_ip)
    echo "VM is running at IP: $INSTANCE_IP"
    echo "Ollama will be available at: http://$INSTANCE_IP:11434"
    echo ""
    echo "To connect to the VM:"
    admin_user=$(cd "$TERRAFORM_DIR" && terraform output -raw admin_username)
    echo "ssh -i ~/.ssh/${VM_NAME}_key $admin_user@$INSTANCE_IP"
}

create_instance() {
    if [[ ! -f "$HOME/.ssh/${VM_NAME}_key" ]]; then
        ssh-keygen -t rsa -b 4096 -f "$HOME/.ssh/${VM_NAME}_key" -N ""
    fi

    echo "Creating Azure infrastructure..."
    init_terraform
    (cd "$TERRAFORM_DIR" && terraform apply -auto-approve)
    connect_to_instance
}

start_instance() {
    local vm_id=$(cd "$TERRAFORM_DIR" && terraform output -raw vm_id)
    if [[ -n "$vm_id" ]]; then
        az vm start --ids "$vm_id"
        connect_to_instance
    else
        echo "No VM found. Use 'create' first."
    fi
}

stop_instance() {
    local vm_id=$(cd "$TERRAFORM_DIR" && terraform output -raw vm_id)
    if [[ -n "$vm_id" ]]; then
        az vm stop --ids "$vm_id"
    else
        echo "No VM found"
    fi
}

destroy_instance() {
    echo "Destroying infrastructure..."
    (cd "$TERRAFORM_DIR" && terraform destroy -auto-approve)
}

get_status() {
    local vm_id=$(cd "$TERRAFORM_DIR" && terraform output -raw vm_id 2>/dev/null)
    if [[ -n "$vm_id" ]]; then
        local state=$(az vm get-instance-view --ids "$vm_id" --query "instanceView.statuses[1].displayStatus" -o tsv)
        echo "VM status: $state"

        if [[ "$state" == "VM running" ]]; then
            INSTANCE_IP=$(cd "$TERRAFORM_DIR" && terraform output -raw public_ip)
            echo "Public IP: $INSTANCE_IP"
            echo "Ollama URL: http://$INSTANCE_IP:11434"
        fi
    else
        echo "No VM exists"
    fi
}

case "$1" in
    connect)
        connect_to_instance
        ;;
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