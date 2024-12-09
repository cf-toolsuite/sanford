#!/usr/bin/env bash

# Get the directory where the script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# Get the parent (root) directory
ROOT_DIR="$(dirname "$SCRIPT_DIR")"

# Change to the root directory
cd "$ROOT_DIR" || exit 1

# Source configuration file
CONFIG_FILE="$HOME/.ollama/googlecloud/config"
if [[ ! -f "$CONFIG_FILE" ]]; then
    echo "Error: Configuration file not found: $CONFIG_FILE"
    echo "Please create the configuration file with the following required variables:"
    echo "PROJECT_ID"
    echo "REGION"
    echo "ZONE"
    echo "INSTANCE_NAME"
    echo "MACHINE_TYPE"
    echo "GPU_MACHINE_TYPE"
    echo "USE_GPU"
    echo "GPU_TYPE"
    echo "GPU_COUNT"
    echo "VOLUME_SIZE"
    echo "SERVICE_ACCOUNT"
    exit 1
fi

# Source the configuration
source "$CONFIG_FILE"

# Terraform working directory
TERRAFORM_DIR="$HOME/.ollama/googlecloud/terraform"
mkdir -p "$TERRAFORM_DIR"

# Initialize Terraform variables
init_terraform() {
    # Create terraform.tfvars file
    cat > "$TERRAFORM_DIR/terraform.tfvars" << EOF
project_id       = "$PROJECT_ID"
region           = "$REGION"
zone             = "$ZONE"
instance_name    = "$INSTANCE_NAME"
machine_type     = "$MACHINE_TYPE"
gpu_machine_type = "$GPU_MACHINE_TYPE"
use_gpu          = "$USE_GPU"
gpu_type         = "$GPU_TYPE"
gpu_count        = "$GPU_COUNT"
volume_size      = "$VOLUME_SIZE"
service_account   = "$SERVICE_ACCOUNT"
EOF

    # Copy Terraform configuration files; even if they don't exist
    cp terraform/googlecloud/*.tf "$TERRAFORM_DIR/"
    cp terraform/user_data.tpl "$TERRAFORM_DIR/"

    # Initialize Terraform
    (cd "$TERRAFORM_DIR" && terraform init)
}

usage() {
    echo "Usage: $0 [create|start|stop|destroy|status]"
    echo "  create  - Creates and starts a new Ollama GCP instance"
    echo "  start   - Starts a stopped GCP instance"
    echo "  stop    - Stops the running GCP instance"
    echo "  destroy - Completely removes the GCP instance and associated infrastructure"
    echo "  status  - Shows the current status of the GCP instance"
    echo ""
    echo "Configuration file: $CONFIG_FILE"
    exit 1
}

connect_to_instance() {
    # Get instance IP
    INSTANCE_IP=$(cd "$TERRAFORM_DIR" && terraform output -raw public_ip)
    echo "Instance is running at IP: $INSTANCE_IP"
    echo "Ollama will be available at: http://$INSTANCE_IP:11434"
    echo ""
    echo "To connect to the VM, execute:"
    echo "gcloud compute ssh $INSTANCE_NAME --zone=$ZONE"
}

create_instance() {
    echo "Creating infrastructure and GCP instance..."
    init_terraform
    (cd "$TERRAFORM_DIR" && terraform apply -auto-approve)
    connect_to_instance
}

start_instance() {
    echo "Starting instance..."
    gcloud compute instances start "$INSTANCE_NAME" \
        --zone="$ZONE" \
        --project="$PROJECT_ID"

    # Wait for instance to be running and get IP
    INSTANCE_IP=$(gcloud compute instances describe "$INSTANCE_NAME" \
        --zone="$ZONE" \
        --project="$PROJECT_ID" \
        --format='get(networkInterfaces[0].accessConfigs[0].natIP)')

    connect_to_instance
}

stop_instance() {
    echo "Stopping instance..."
    gcloud compute instances stop "$INSTANCE_NAME" \
        --zone="$ZONE" \
        --project="$PROJECT_ID"
}

destroy_instance() {
    echo "Destroying infrastructure..."
    (cd "$TERRAFORM_DIR" && terraform destroy -auto-approve)
}

get_status() {
    local status=$(gcloud compute instances describe "$INSTANCE_NAME" \
        --zone="$ZONE" \
        --project="$PROJECT_ID" \
        --format='get(status)' 2>/dev/null)

    if [[ -n "$status" ]]; then
        echo "Instance status: $status"

        if [[ "$status" == "RUNNING" ]]; then
            INSTANCE_IP=$(gcloud compute instances describe "$INSTANCE_NAME" \
                --zone="$ZONE" \
                --project="$PROJECT_ID" \
                --format='get(networkInterfaces[0].accessConfigs[0].natIP)')

            echo "External IP: $INSTANCE_IP"
            echo "Ollama URL: http://$INSTANCE_IP:11434"
        fi
    else
        echo "No instance exists"
    fi
}

# Main script logic
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
