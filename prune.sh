#!/bin/bash

export BACKUP_LOC=/tmp/sanford/$(date +"%Y.%m.%d.%N" | cut -b1-14)
mkdir -p $BACKUP_LOC

# Candidates to be pruned
candidates=(".history" ".github" ".gradle" ".trunk" "config" "deploy-on-tp4cf.sh" "docs" "docker-compose.chroma.yml" "docker-compose.minio.yml" "docker-compose.observability.yml" "docker-compose.pgvector.yml" "docker-compose.redis.yml" "docker-compose.weaviate.yml" "prometheus.yml" "bin" "build" "data" "samples" "README.md")

for item in "${candidates[@]}"; do
    if [ -e "$item" ] || [ -d "$item" ] || compgen -G "$item" > /dev/null; then
        cp -Rf "$item" $BACKUP_LOC
        rm -Rf "$item"
    fi
done
