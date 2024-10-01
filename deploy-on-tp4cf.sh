#!/usr/bin/env bash

APP_NAME="sanford"
APP_VERSION="0.0.1-SNAPSHOT"

COMMAND=$1

GENAI_CHAT_SERVICE_NAME="sanford-llm"
GENAI_CHAT_PLAN_NAME="llama3.1" # plan must have chat capabilty

GENAI_EMBEDDINGS_SERVICE_NAME="sanford-embedding"
GENAI_EMBEDDINGS_PLAN_NAME="nomic-embed-text"
 # plan must have Embeddings capabilty
PGVECTOR_SERVICE_NAME="sanford-db"
PGVECTOR_PLAN_NAME="on-demand-postgres-small"
PGVECTOR_EXTERNAL_PORT=1025

STORAGE_PROVIDER_SERVICE_NAME="sanford-filestore"
STORAGE_PROVIDER_PLAN_NAME="default"

# Easiest thing to do for demo purposes in the absence of having the MinIO tile installed is to spin up an instance of MinIO on StackHero (https://www.stackhero.io/en/)
if  [[ -f ${HOME}/.minio/config ]]; then
    echo "MinIO configuration file found."

# Source the $HOME/.minio/config file
# This file should contain at a minimum the following key-value environment variable pairs:
# export MINIO_ENDPOINT_HOST=<minio-hostname>
# export MINIO_ENDPOINT_PORT=<minio-port>
# export MINIO_ACCESS_KEY=<minio-username>
# export MINIO_SECRET_KEY=<minio-password>
# export MINIO_BUCKET_NAME=<minio-bucket>

    source $HOME/.minio/config
fi

if  [[ -f ${HOME}/.dell/ecs/config ]]; then
    echo "Dell ECS configuration file found."

# Source the $HOME/.dell/ecs/config file
# This file should contain at a minimum the following key-value environment variable pairs:
# export ECS_ENDPOINT_HOST=<dell-ecs-hostname>
# export ECS_ENDPOINT_PORT=<dell-ecs-port>
# export ECS_ACCESS_KEY=<dell-ecs-username>
# export ECS_SECRET_KEY=<dell-ecs-password>
# export ECS_BUCKET_NAME=<dell-ecs-bucket>

    source $HOME/.dell/ecs/config
fi


case $COMMAND in

setup)

    echo && printf "\e[37mℹ️  Creating services ...\e[m\n" && echo

    cf create-service postgres $PGVECTOR_PLAN_NAME $PGVECTOR_SERVICE_NAME -c "{\"svc_gw_enable\": true, \"router_group\": \"default-tcp\", \"external_port\": $PGVECTOR_EXTERNAL_PORT}" -w
	printf "Waiting for service $PGVECTOR_SERVICE_NAME to create."
	while [ `cf services | grep 'in progress' | wc -l | sed 's/ //g'` != 0 ]; do
  		printf "."
  		sleep 5
	done
	echo "$PGVECTOR_SERVICE_NAME creation completed."

    if [[ -n "$ECS_ENDPOINT_HOST" ]]; then
        echo && printf "\e[37mℹ️  Creating $STORAGE_PROVIDER_SERVICE_NAME Dell ECS service configuration...\e[m\n" && echo
        cf create-service credhub $STORAGE_PROVIDER_PLAN_NAME $STORAGE_PROVIDER_SERVICE_NAME -c "{\"ECS_ENDPOINT_HOST\":\"$ECS_ENDPOINT_HOST\",\"ECS_ENDPOINT_PORT\":\"$ECS_ENDPOINT_PORT\",\"ECS_ACCESS_KEY\":\"$ECS_ACCESS_KEY\",\"ECS_SECRET_KEY\":\"$ECS_SECRET_KEY\",\"ECS_BUCKET_NAME\":\"$ECS_BUCKET_NAME\"}"
    fi

    if [[ -n "$MINIO_ENDPOINT_HOST" ]]; then
        echo && printf "\e[37mℹ️  Creating $MINIO_SERVICE_NAME MinIO service configuration...\e[m\n" && echo
        cf create-service credhub $STORAGE_PROVIDER_PLAN_NAME $STORAGE_PROVIDER_SERVICE_NAME -c "{\"MINIO_ENDPOINT_HOST\":\"$MINIO_ENDPOINT_HOST\",\"MINIO_ENDPOINT_PORT\":\"$MINIO_ENDPOINT_PORT\",\"MINIO_ACCESS_KEY\":\"$MINIO_ACCESS_KEY\",\"MINIO_SECRET_KEY\":\"$MINIO_SECRET_KEY\"}"
    fi

    echo && printf "\e[37mℹ️  Creating $GENAI_CHAT_SERVICE_NAME and $GENAI_EMBEDDINGS_SERVICE_NAME GenAI services ...\e[m\n" && echo
    cf create-service genai $GENAI_CHAT_PLAN_NAME $GENAI_CHAT_SERVICE_NAME
    cf create-service genai $GENAI_EMBEDDINGS_PLAN_NAME $GENAI_EMBEDDINGS_SERVICE_NAME

    echo && printf "\e[37mℹ️  Deploying $APP_NAME application ...\e[m\n" && echo
    cf push $APP_NAME -k 1GB -m 1GB -p build/libs/$APP_NAME-$APP_VERSION.jar --no-start --random-route

    echo && printf "\e[37mℹ️  Binding services ...\e[m\n" && echo
    cf bind-service $APP_NAME $PGVECTOR_SERVICE_NAME
    cf bind-service $APP_NAME $GENAI_CHAT_SERVICE_NAME
    cf bind-service $APP_NAME $GENAI_EMBEDDINGS_SERVICE_NAME
    cf bind-service $APP_NAME $STORAGE_PROVIDER_SERVICE_NAME

    echo && printf "\e[37mℹ️  Setting environment variables for use by $APP_NAME application ...\e[m\n" && echo
    cf set-env $APP_NAME JAVA_OPTS "-Djava.security.egd=file:///dev/urandom -XX:+UseG1GC -XX:+UseStringDeduplication"
    cf set-env $APP_NAME SPRING_PROFILES_ACTIVE "default,cloud,openai,pgvector"
    cf set-env $APP_NAME JBP_CONFIG_OPEN_JDK_JRE "{ jre: { version: 21.+ } }"
    cf set-env $APP_NAME JBP_CONFIG_SPRING_AUTO_RECONFIGURATION "{ enabled: false }"

    echo && printf "\e[37mℹ️  Starting $APP_NAME application ...\e[m\n" && echo
    cf start $APP_NAME

    ;;

teardown)
    cf unbind-service $STORAGE_PROVIDER_SERVICE_NAME -f
    cf unbind-service $PGVECTOR_SERVICE_NAME -f
    cf unbind-service $GENAI_CHAT_SERVICE_NAME -f
    cf unbind-service $GENAI_EMBEDDINGS_SERVICE_NAME -f

    cf delete-service $STORAGE_PROVIDER_SERVICE_NAME -f
    cf delete-service $PGVECTOR_SERVICE_NAME -f
    cf delete-service $GENAI_CHAT_SERVICE_NAME -f
    cf delete-service $GENAI_EMBEDDINGS_SERVICE_NAME -f

    cf delete $APP_NAME -f -r

    ;;

*)
    echo && printf "\e[31m⏹  Usage: setup/teardown \e[m\n" && echo
    ;;
esac
