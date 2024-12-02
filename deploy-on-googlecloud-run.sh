#!/usr/bin/env bash

# Default path to environment configuration
ENV_FILE="${ENV_FILE:-.env}"

# Load environment variables from file
load_env_vars() {
    # Check if .env file exists
    if [[ ! -f "$ENV_FILE" ]]; then
        echo "Error: Environment configuration file $ENV_FILE not found"
        exit 1
    fi

    # Source the environment file
    # Use set -a to export all variables
    set -a
    source "$ENV_FILE"
    set +a

    # Validate critical environment variables
    local required_vars=(
        "APP_NAME"
        "APP_VERSION"
        "PROJECT_ID"
        "REGION"
    )

    for var in "${required_vars[@]}"; do
        if [[ -z "${!var}" ]]; then
            echo "Error: ${var} environment variable is not set in $ENV_FILE"
            exit 1
        fi
    done
}

# Pre-requisites check
check_prerequisites() {
    # Check if gcloud CLI is installed and authenticated
    if ! command -v gcloud &> /dev/null; then
        echo "Error: gcloud CLI is not installed"
        exit 1
    fi
}

# Source Code Deployment (Buildpack approach)
deploy_from_source() {
    echo "Deploying from source code using Cloud Run buildpacks"

    # Prepare build arguments array
    BUILD_ARGS=()

    # Check and add Gradle properties as build arguments
    if [[ -n "${VECTOR_DB_PROVIDER:-}" ]]; then
        BUILD_ARGS+=("--build-arg" "vector-db-provider=$VECTOR_DB_PROVIDER")
    fi

    if [[ -n "${MODEL_API_PROVIDER:-}" ]]; then
        BUILD_ARGS+=("--build-arg" "model-api-provider=$MODEL_API_PROVIDER")
    fi

    # Deploy with dynamically generated build arguments
    gcloud run deploy "$APP_NAME" \
        --source . \
        --region "$REGION" \
        --project "$PROJECT_ID" \
        --builder google-buildpacks" \
        --env-vars-file="$ENV_FILE" \
        "${BUILD_ARGS[@]}" \
        --set-env-vars ^@^
            SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-default,cloud,openai,pgvector}"@
            JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:--XX:+UseZGC -XX:+UseStringDeduplication}"@
        --allow-unauthenticated
}


# Pre-built JAR Deployment
deploy_from_jar() {
    echo "Deploying pre-built JAR to Cloud Run"

    # Build container image
    gcloud builds submit \
        --project "$PROJECT_ID" \
        --tag "gcr.io/$PROJECT_ID/$APP_NAME:$APP_VERSION" \
        --file Dockerfile

    # Deploy to Cloud Run
    gcloud run deploy "$APP_NAME" \
        --image "gcr.io/$PROJECT_ID/$APP_NAME:$APP_VERSION" \
        --region "$REGION" \
        --project "$PROJECT_ID" \
        --env-vars-file="$ENV_FILE" \
        --set-env-vars ^@^
            SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-default,cloud,openai,pgvector}"@
            JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:--XX:+UseZGC -XX:+UseStringDeduplication}"@
        --allow-unauthenticated
}

# Cloud Services Setup
setup_cloud_services() {
    # Example of creating and configuring services
    # PostgreSQL (Cloud SQL)
    gcloud sql instances create "$APP_NAME-db" \
        --project "$PROJECT_ID" \
        --database-version=POSTGRES_15

    # Create a Cloud Storage bucket for file storage
    gsutil mb -p "$PROJECT_ID" "gs://$APP_NAME-filestore"

    # Create a Secret Manager secret for sensitive configurations
    echo "Placeholder sensitive configuration" | \
    gcloud secrets create "$APP_NAME-config" \
        --project "$PROJECT_ID" \
        --data-file=-
}

# Main Execution
main() {
    # Load environment variables
    load_env_vars

    # Use environment variable for deployment method, default to source
    DEPLOYMENT_METHOD="${DEPLOYMENT_METHOD:-src}"

    check_prerequisites

    case "$DEPLOYMENT_METHOD" in
        src)
            deploy_from_source
            ;;
        jar)
            deploy_from_jar
            ;;
        services)
            setup_cloud_services
            ;;
        *)
            echo "Usage: set DEPLOYMENT_METHOD to [ src, jar, or services ]"
            exit 1
            ;;
    esac
}

main