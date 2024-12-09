#!/usr/bin/env bash

# Install Ollama
curl -fsSL https://ollama.com/install.sh | sh

# Create directory for Ollama service override
mkdir -p /etc/systemd/system/ollama.service.d

# Configure Ollama to listen on all interfaces
cat << 'END' > /etc/systemd/system/ollama.service.d/override.conf
[Service]
Environment="OLLAMA_HOST=0.0.0.0"
END

# Install necessary packages
apt-get update && apt-get install -y net-tools

%{ if use_gpu }
# Install NVIDIA driver and CUDA toolkit for GPU instances
apt-get install -y nvidia-driver-525 nvidia-cuda-toolkit
%{ endif }

# Reload systemd configs
systemctl daemon-reload

# Enable and start Ollama service
systemctl enable ollama
systemctl start ollama

# Log the status and configuration for debugging
echo "Ollama installation completed at $(date)" >> /var/log/ollama-install.log
echo "Checking Ollama service status:" >> /var/log/ollama-install.log
systemctl status ollama >> /var/log/ollama-install.log
echo "Checking network listeners:" >> /var/log/ollama-install.log
netstat -tulpn | grep 11434 >> /var/log/ollama-install.log
