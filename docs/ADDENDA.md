# Sanford

## Addenda

### Development profile

To activate development mode include `dev` in comma-separated list of Spring profiles, e.g.

```bash
-Dspring.profiles.active=docker,ollama,pgvector,dev
```

See [spring.config.activate.on-profile=dev in application.yml](../src/main/resources/application.yml).

### Recommended on-platform model combo

When serving models from Cloud Foundry with the GenAI tile

#### CPU-only configuration

* Choose compute type that has a minimum of 32-vCPU, 208Gb RAM, and 80Gb disk
  * when targeting CF environment provisioned on Google Cloud, choose [n1-highmem-32](https://cloud.google.com/compute/docs/general-purpose-machines#n1_machine_types)
* Choose `gemma2` for the chat model
* Choose `aroxima/gte-qwen2-1.5b-instruct` for the embedding model
* Choose Postgres for the vector store provider

If you're employing the [deploy-on-tp4cf.sh](../deploy-on-tp4cf.sh) script, edit the following variables to be

```bash
GENAI_CHAT_PLAN_NAME=gemma2
GENAI_EMBEDDINGS_PLAN_NAME=aroxima/gte-qwen2-1.5b-instruct
```

and add the following to the sequence of `cf set-env` statements

```bash
export SPRING_AI_VECTORSTORE_PGVECTOR_DIMENSIONS=1536
```

### Recommended Ollama model combo

When serving models from Ollama

#### CPU-only configuration

* Choose compute type that has a minimum of 32-vCPU, 128Gb RAM, and 80Gb disk
  * when targeting an Ollama VM installation hosted on Google Cloud, choose [n2-highmem-32](https://cloud.google.com/compute/docs/general-purpose-machines#n2_machine_types)

Here's how to get going running locally targeting models hosted on a VM in Google Cloud

```bash
# Checkout source
gh repo clone cf-toolsuite/sanford
cd sanford
# Run provisioning script to create and start a VM with Ollama
./provision-ollama-vm-on-googlecloud.sh create
# Set environment variables (override defaults)
export CHAT_MODEL=gemma2
export EMBEDDING_MODEL=aroxima/gte-qwen2-1.5b-instruct
export SPRING_AI_VECTORSTORE_PGVECTOR_DIMENSIONS=1536
export OLLAMA_BASE_URL=http://{replace_with_ip_address_of_ollama_instance}:11434
gradle clean build bootRun -Pvector-db-provider=pgvector -Pmodel-api-provider=ollama -Dspring.profiles.active=docker,ollama,pgvector,dev
time http --verify=no POST :8080/api/fetch urls:='["https://www.govtrack.us/api/v2/role?current=true&role_type=senator"]'  
time http --verify=no :8080/api/files/chat q=="Tell me who the senators are from Washington state" 
```
