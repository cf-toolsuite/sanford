#!/usr/bin/env bash

# Check if service principal exists
check_sp() {
    local name=$1
    az ad sp list --display-name "$name" --query '[].appId' -o tsv
}

# Create service principal if it doesn't exist
create_sp() {
    local name=$1
    az ad sp create-for-rbac --name "$name" --skip-assignment
}

# Main script
mkdir -p $HOME/.ollama/azure

DEFAULT_SP_NAME=$(echo "ollama$(date +%Y%m%d%H%M%S)")
SP_NAME="${1:-$DEFAULT_SP_NAME}"
CREDS_FILE="$HOME/.ollama/azure/credentials"

# Check if credentials file exists
if [ -f "$CREDS_FILE" ]; then
    source "$CREDS_FILE"
else
    CLIENT_ID=$(check_sp "$SP_NAME")

    if [ -z "$CLIENT_ID" ]; then
        echo "Creating new service principal: $SP_NAME"
        SP_CREDS=$(create_sp "$SP_NAME")

        ARM_CLIENT_ID=$(echo $SP_CREDS | jq -r '.appId')
        ARM_CLIENT_SECRET=$(echo $SP_CREDS | jq -r '.password')
        ARM_TENANT_ID=$(echo $SP_CREDS | jq -r '.tenant')

        # Save credentials
        echo "export ARM_CLIENT_ID=$ARM_CLIENT_ID" > "$CREDS_FILE"
        echo "export ARM_CLIENT_SECRET=$ARM_CLIENT_SECRET" >> "$CREDS_FILE"
        echo "export ARM_TENANT_ID=$ARM_TENANT_ID" >> "$CREDS_FILE"

        chmod 600 "$CREDS_FILE"
    else
        echo "Service principal exists but credentials file not found"
        exit 1
    fi
fi

# Login using service principal
az login --service-principal -u "$ARM_CLIENT_ID" -p "$ARM_CLIENT_SECRET" --tenant "$ARM_TENANT_ID"