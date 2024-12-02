#!/usr/bin/env bash

# Source configuration file
CONFIG_FILE="$HOME/.ollama/aws/config"
if [[ ! -f "$CONFIG_FILE" ]]; then
    echo "Error: Configuration file not found: $CONFIG_FILE"
    echo "Please create the configuration file with the following required variables:"
    echo "REGION"
    echo "VPC_ID"
    echo "SUBNET_ID"
    echo "KEY_NAME"
    echo "INSTANCE_NAME"
    echo "INSTANCE_TYPE"
    echo "GPU_INSTANCE_TYPE"
    echo "USE_GPU"
    echo "VOLUME_SIZE"
    echo "MAX_RUN_DURATION"
    exit 1
fi

# Source the configuration
source "$CONFIG_FILE"

# Verify required variables are set
required_vars=(INSTANCE_TYPE INSTANCE_NAME VOLUME_SIZE MAX_RUN_DURATION VPC_ID SUBNET_ID KEY_NAME REGION)
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
    echo "  create  - Creates and starts a new Ollama EC2 instance"
    echo "  start   - Starts a stopped EC2 instance"
    echo "  stop    - Stops the running EC2 instance"
    echo "  destroy - Completely removes the EC2 instance"
    echo "  status  - Shows the current status of the EC2 instance"
    echo ""
    echo "Configuration file: $CONFIG_FILE"
    exit 1
}

create_user_data_script() {
    cat << 'EOF' > /tmp/user-data.sh
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
    chmod +x /tmp/user-data.sh
}

create_security_group() {
    local sg_name="ollama-security-group"

    # Check if security group exists
    if ! aws ec2 describe-security-groups --group-names "$sg_name" --region "$REGION" &>/dev/null; then
        echo "Creating security group for Ollama..."
        SECURITY_GROUP_ID=$(aws ec2 create-security-group \
            --group-name "$sg_name" \
            --description "Security group for Ollama server" \
            --vpc-id "$VPC_ID" \
            --region "$REGION" \
            --output text --query 'GroupId')

        # Add inbound rule for Ollama
        aws ec2 authorize-security-group-ingress \
            --group-id "$SECURITY_GROUP_ID" \
            --protocol tcp \
            --port 11434 \
            --cidr 0.0.0.0/0 \
            --region "$REGION"

        # Add SSH access
        aws ec2 authorize-security-group-ingress \
            --group-id "$SECURITY_GROUP_ID" \
            --protocol tcp \
            --port 22 \
            --cidr 0.0.0.0/0 \
            --region "$REGION"
    else
        SECURITY_GROUP_ID=$(aws ec2 describe-security-groups \
            --group-names "$sg_name" \
            --region "$REGION" \
            --query 'SecurityGroups[0].GroupId' \
            --output text)
    fi
    echo "$SECURITY_GROUP_ID"
}

verify_installation() {
    local instance_ip=$1
    echo "Verifying Ollama installation..."

    # Try to SSH and check Ollama status
    ssh -i "$HOME/.ssh/$KEY_NAME.pem" -o StrictHostKeyChecking=no ubuntu@"$instance_ip" '
        echo "Checking Ollama service status..."
        sudo systemctl status ollama
        echo "Checking Ollama service logs..."
        sudo journalctl -u ollama -n 50
        echo "Checking install logs..."
        sudo cat /var/log/ollama-install.log
    ' || echo "Failed to verify installation via SSH"
}

wait_for_ollama() {
    local instance_ip=$1
    local max_attempts=30
    local attempt=1

    echo "Waiting for Ollama to be ready..."
    while [[ $attempt -le $max_attempts ]]; do
        if curl -s "http://$instance_ip:11434/api/version" &>/dev/null; then
            return 0
        fi
        echo "Attempt $attempt/$max_attempts: Ollama not ready yet, waiting..."
        if [[ $attempt -eq 5 ]]; then
            echo "Verifying installation status..."
            verify_installation "$instance_ip"
        fi
        sleep 10
        ((attempt++))
    done
    echo "Warning: Ollama service not responding after $max_attempts attempts"
    verify_installation "$instance_ip"
    return 1
}

get_instance_id() {
    aws ec2 describe-instances \
        --filters "Name=tag:Name,Values=$INSTANCE_NAME" "Name=instance-state-name,Values=running,stopped" \
        --region "$REGION" \
        --query 'Reservations[0].Instances[0].InstanceId' \
        --output text
}

get_instance_state() {
    local instance_id=$1
    aws ec2 describe-instances \
        --instance-ids "$instance_id" \
        --region "$REGION" \
        --query 'Reservations[0].Instances[0].State.Name' \
        --output text
}

create_instance() {
    echo "Creating EC2 instance..."

    # Check if instance already exists
    local existing_id=$(get_instance_id)
    if [[ -n "$existing_id" ]]; then
        echo "Error: Instance with name '$INSTANCE_NAME' already exists. Use 'start' to start a stopped instance or 'destroy' to remove it first."
        exit 1
    fi

    # Create user data script
    create_user_data_script

    # Create security group
    SECURITY_GROUP_ID=$(create_security_group)

    # Get latest Ubuntu 24.04 AMI ID
    AMI_ID=$(aws ec2 describe-images \
        --owners 099720109477 \
        --filters "Name=name,Values=ubuntu/images/hvm-ssd/ubuntu-noble-24.04-amd64-server-*" \
        --query 'sort_by(Images, &CreationDate)[-1].ImageId' \
        --region "$REGION" \
        --output text)

    # Select instance type based on GPU configuration
    local instance_type="$INSTANCE_TYPE"
    if [[ "$USE_GPU" == "true" && -n "$GPU_INSTANCE_TYPE" ]]; then
        instance_type="$GPU_INSTANCE_TYPE"

        # Add GPU driver installation to user data script
        cat << 'EOF' >> /tmp/user-data.sh
# Install NVIDIA driver and CUDA toolkit
apt-get update
apt-get install -y nvidia-driver-525 nvidia-cuda-toolkit
EOF
    fi

    # Create the instance
    INSTANCE_ID=$(aws ec2 run-instances \
        --image-id "$AMI_ID" \
        --instance-type "$instance_type" \
        --key-name "$KEY_NAME" \
        --security-group-ids "$SECURITY_GROUP_ID" \
        --subnet-id "$SUBNET_ID" \
        --user-data file:///tmp/user-data.sh \
        --tag-specifications "ResourceType=instance,Tags=[{Key=Name,Value=$INSTANCE_NAME}]" \
        --block-device-mappings "[{\"DeviceName\":\"/dev/sda1\",\"Ebs\":{\"VolumeSize\":$VOLUME_SIZE,\"DeleteOnTermination\":true}}]" \
        --region "$REGION" \
        --query 'Instances[0].InstanceId' \
        --output text)

    echo "Waiting for instance to be running..."
    aws ec2 wait instance-running --instance-ids "$INSTANCE_ID" --region "$REGION"

    # Get the public IP
    INSTANCE_IP=$(aws ec2 describe-instances \
        --instance-ids "$INSTANCE_ID" \
        --region "$REGION" \
        --query 'Reservations[0].Instances[0].PublicIpAddress' \
        --output text)

    echo "Instance is running at IP: $INSTANCE_IP"
    echo "Waiting for instance to be fully initialized..."
    sleep 30

    # Wait for Ollama to be ready
    wait_for_ollama "$INSTANCE_IP"

    echo "Instance setup completed!"
    echo "External IP: $INSTANCE_IP"
    echo "Ollama will be available at: http://$INSTANCE_IP:11434"
    echo ""
    echo "To connect to the instance:"
    echo "ssh -i ~/.ssh/$KEY_NAME.pem ubuntu@$INSTANCE_IP"

    # Cleanup user data script
    rm -f /tmp/user-data.sh
}

start_instance() {
    local instance_id=$(get_instance_id)
    if [[ -z "$instance_id" ]]; then
        echo "Error: Instance '$INSTANCE_NAME' does not exist. Use 'create' to create a new instance."
        exit 1
    fi

    local state=$(get_instance_state "$instance_id")
    if [[ "$state" == "running" ]]; then
        echo "Instance '$INSTANCE_NAME' is already running."
        exit 0
    fi

    echo "Starting EC2 instance..."
    aws ec2 start-instances --instance-ids "$instance_id" --region "$REGION"

    echo "Waiting for instance to be running..."
    aws ec2 wait instance-running --instance-ids "$instance_id" --region "$REGION"

    INSTANCE_IP=$(aws ec2 describe-instances \
        --instance-ids "$instance_id" \
        --region "$REGION" \
        --query 'Reservations[0].Instances[0].PublicIpAddress' \
        --output text)

    echo "Instance is running at IP: $INSTANCE_IP"
    echo "Ollama will be available at: http://$INSTANCE_IP:11434"
    echo ""
    echo "To connect to the instance:"
    echo "ssh -i ~/.ssh/$KEY_NAME.pem ubuntu@$INSTANCE_IP"
}

stop_instance() {
    local instance_id=$(get_instance_id)
    if [[ -n "$instance_id" ]]; then
        echo "Stopping EC2 instance..."
        aws ec2 stop-instances --instance-ids "$instance_id" --region "$REGION"
    else
        echo "Instance not found"
    fi
}

destroy_instance() {
    local instance_id=$(get_instance_id)
    if [[ -n "$instance_id" ]]; then
        echo "Destroying EC2 instance..."
        aws ec2 terminate-instances --instance-ids "$instance_id" --region "$REGION"
    else
        echo "Instance not found"
    fi
}

get_status() {
    local instance_id=$(get_instance_id)
    if [[ -n "$instance_id" ]]; then
        local state=$(get_instance_state "$instance_id")
        echo "Instance status: $state"

        if [[ "$state" == "running" ]]; then
            INSTANCE_IP=$(aws ec2 describe-instances \
                --instance-ids "$instance_id" \
                --region "$REGION" \
                --query 'Reservations[0].Instances[0].PublicIpAddress' \
                --output text)

            echo "External IP: $INSTANCE_IP"
            echo "Ollama URL: http://$INSTANCE_IP:11434"
            echo ""
            echo "To connect to the instance:"
            echo "ssh -i ~/.ssh/$KEY_NAME.pem ubuntu@$INSTANCE_IP"

            # Try to verify Ollama status
            verify_installation "$INSTANCE_IP"
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
