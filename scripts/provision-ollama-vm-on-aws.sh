#!/usr/bin/env bash

export AWS_PAGER=""

# Get the directory where the script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# Get the parent (root) directory
ROOT_DIR="$(dirname "$SCRIPT_DIR")"

# Change to the root directory
cd "$ROOT_DIR" || exit 1

# Source configuration file
CONFIG_FILE="$HOME/.ollama/aws/config"
if [[ ! -f "$CONFIG_FILE" ]]; then
    echo "Error: Configuration file not found: $CONFIG_FILE"
    echo "Please create the configuration file with the following required variables:"
    echo "REGION"
    echo "INSTANCE_NAME"
    echo "INSTANCE_TYPE"
    echo "GPU_INSTANCE_TYPE"
    echo "USE_GPU"
    echo "VOLUME_SIZE"
    echo "KEY_NAME"
    exit 1
fi

# Source the configuration
source "$CONFIG_FILE"

# Terraform working directory
TERRAFORM_DIR="$HOME/.ollama/aws/terraform"
mkdir -p "$TERRAFORM_DIR"

# Initialize Terraform variables
init_terraform() {
    # Create terraform.tfvars file
    cat > "$TERRAFORM_DIR/terraform.tfvars" << EOF
region           = "$REGION"
instance_name    = "$INSTANCE_NAME"
instance_type    = "$INSTANCE_TYPE"
gpu_instance_type = "$GPU_INSTANCE_TYPE"
use_gpu         = $USE_GPU
volume_size     = $VOLUME_SIZE
key_name        = "$KEY_NAME"
EOF

    # Copy Terraform configuration files; even if they don't exist
    cp terraform/aws/*.tf "$TERRAFORM_DIR/"
    cp terraform/user_data.tpl "$TERRAFORM_DIR/"

    # Initialize Terraform
    (cd "$TERRAFORM_DIR" && terraform init)
}

usage() {
    echo "Usage: $0 [create|start|stop|destroy|status]"
    echo "  create  - Creates and starts a new Ollama EC2 instance"
    echo "  start   - Starts a stopped EC2 instance"
    echo "  stop    - Stops the running EC2 instance"
    echo "  destroy - Completely removes the EC2 instance and associated infrastructure"
    echo "  status  - Shows the current status of the EC2 instance"
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
    echo "To connect to the instance:"
    key_name=$(cd "$TERRAFORM_DIR" && terraform output -raw key_name)
    echo "ssh -i ~/.ssh/$key_name.pem ubuntu@$INSTANCE_IP -o IdentitiesOnly=yes"
}

create_instance() {
    echo "Creating infrastructure and EC2 instance..."
    init_terraform
    (cd "$TERRAFORM_DIR" && terraform apply -auto-approve)
    connect_to_instance
}

start_instance() {
    local instance_id=$(cd "$TERRAFORM_DIR" && terraform output -raw instance_id)
    if [[ -n "$instance_id" ]]; then
        aws ec2 start-instances --instance-ids "$instance_id" --region "$REGION"
        echo "Starting instance..."
        aws ec2 wait instance-running --instance-ids "$instance_id" --region "$REGION"
        INSTANCE_IP=$(aws ec2 describe-instances --instance-ids "$instance_id" --region "$REGION" --query 'Reservations[0].Instances[0].PublicIpAddress' --output text)
        echo "Instance is running at IP: $INSTANCE_IP"
    else
        echo "No instance found. Use 'create' first."
    fi
    connect_to_instance
}

stop_instance() {
    local instance_id=$(cd "$TERRAFORM_DIR" && terraform output -raw instance_id)
    if [[ -n "$instance_id" ]]; then
        aws ec2 stop-instances --instance-ids "$instance_id" --region "$REGION"
        echo "Stopping instance..."
    else
        echo "No instance found"
    fi
}

destroy_instance() {
    echo "Destroying infrastructure..."
    (cd "$TERRAFORM_DIR" && terraform destroy -auto-approve)
}

get_status() {
    local instance_id=$(cd "$TERRAFORM_DIR" && terraform output -raw instance_id 2>/dev/null)
    if [[ -n "$instance_id" ]]; then
        local state=$(aws ec2 describe-instances --instance-ids "$instance_id" --region "$REGION" --query 'Reservations[0].Instances[0].State.Name' --output text)
        echo "Instance status: $state"

        if [[ "$state" == "running" ]]; then
            INSTANCE_IP=$(aws ec2 describe-instances --instance-ids "$instance_id" --region "$REGION" --query 'Reservations[0].Instances[0].PublicIpAddress' --output text)
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
