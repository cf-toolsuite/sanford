#!/bin/bash

# Enable debug mode if needed
# set -x

# Get first enabled subscription ID
get_subscription_id() {
    az account subscription list --only-show-errors | jq -r '.[] | select(.state == "Enabled") | .subscriptionId' | head -n 1
}

# Check if service principal exists
check_sp() {
    local name="$1"
    az ad sp list --display-name "$name" --only-show-errors --query '[].appId' -o tsv
}

# Create service principal if it doesn't exist
create_sp() {
    local name="$1"
    local subscription_id=$(get_subscription_id)
    az ad sp create-for-rbac \
        --name "$name" \
        --role Contributor \
        --scopes "/subscriptions/$subscription_id" \
        --only-show-errors
}

# Main script
mkdir -p "$HOME/.ollama/azure"

DEFAULT_SP_NAME="ollama$(date +%Y%m%d%H%M%S)"
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

        # Debug output (commented out for security)
        # echo "SP_CREDS: $SP_CREDS"

        ARM_CLIENT_ID=$(echo "$SP_CREDS" | jq -r '.appId')
        ARM_CLIENT_SECRET=$(echo "$SP_CREDS" | jq -r '.password')
        ARM_TENANT_ID=$(echo "$SP_CREDS" | jq -r '.tenant')
        ARM_SUBSCRIPTION_ID=$(get_subscription_id)

        # Verify we got all values
        if [ -z "$ARM_CLIENT_ID" ] || [ -z "$ARM_CLIENT_SECRET" ] || [ -z "$ARM_TENANT_ID" ] || [ -z "$ARM_SUBSCRIPTION_ID" ]; then
            echo "Error: Failed to get all required credentials"
            exit 1
        fi

        # Save credentials
        {
            echo "export ARM_CLIENT_ID=$ARM_CLIENT_ID"
            echo "export ARM_CLIENT_SECRET='$ARM_CLIENT_SECRET'"  # Note the quotes around the secret
            echo "export ARM_TENANT_ID=$ARM_TENANT_ID"
            echo "export ARM_SUBSCRIPTION_ID=$ARM_SUBSCRIPTION_ID"
        } > "$CREDS_FILE"

        chmod 600 "$CREDS_FILE"

        # Source the file to get the variables in current session
        source "$CREDS_FILE"
    else
        echo "Service principal exists but credentials file not found"
        exit 1
    fi
fi

# Set the subscription
az account set --subscription "$ARM_SUBSCRIPTION_ID" --only-show-errors
az account show
