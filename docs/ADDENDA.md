# Sanford

## Addenda

### Development profile

To activate development mode include `dev` in comma-separated list of Spring profiles, e.g.

```bash
-Dspring.profiles.active=docker,ollama,pgvector,dev
```

See [spring.config.activate.on-profile=dev in application.yml](../src/main/resources/application.yml).

### Disable tracing

To disable tracing, set an environment variable before starting the application.

```commandline
export MANAGEMENT_TRACING_ENABLED=false
```

On Cloud Foundry, you would

```commandline
cf set-env sanford MANAGEMENT_TRACING_ENABLED false
cf restage sanford
```

### Choosing models from Huggingface to run on Ollama

Models must be stored in GPT-Generated Unified Format ([GGUF](https://gguf.io/))

* Chat (4-bit precision) - [most downloads](https://huggingface.co/models?other=4-bit&sort=downloads), [recently updated](https://huggingface.co/models?other=4-bit&sort=modified&search=GGUF)
* Text Embedding (4-bit precision) - [most downloads](https://huggingface.co/models?other=text-embeddings-inference&sort=downloads&search=GGUF), [recently updated](https://huggingface.co/models?other=text-embeddings-inference&sort=modified&search=GGUF)

Prefix all models you pull with...

```commandline
ollama pull hf.co/
```

### Recommended on-platform model combo

When serving models from Cloud Foundry with the GenAI tile

#### CPU-only configuration

To be redone

* Choose compute type that has a minimum of 8-vCPU, 64Gb RAM, and 80Gb disk
  * when targeting CF environment provisioned on Google Cloud, choose [c2d-highmem-8](https://cloud.google.com/compute/docs/compute-optimized-machines#c2d-high-mem)
* Choose `qwen2.5:3b` for the chat model
* Choose `aroxima/gte-qwen2-1.5b-instruct` for the embedding model
* Choose Postgres for the vector store provider

If you're employing the [deploy-on-tp4cf.sh](../deploy-on-tp4cf.sh) script, edit the following variables to be

```bash
GENAI_CHAT_PLAN_NAME=qwen2.5:3b
GENAI_EMBEDDINGS_PLAN_NAME=aroxima/gte-qwen2-1.5b-instruct
```

and add the following to the sequence of `cf set-env` statements

```bash
export SPRING_AI_VECTORSTORE_PGVECTOR_DIMENSIONS=1536
```

### Recommended Ollama model combo

When serving models from Ollama, you're encouraged to consult then leverage one of the provisioning scripts targeting a public cloud infrastructure provider:

* [AWS](../provision-ollama-vm-on-aws.sh)
* [Azure](../provision-ollama-vm-on-azure.sh)
* [Google Cloud](../provision-ollama-vm-on-googlecloud.sh)

#### CPU-only configuration

To be redone

* Choose a compute type that has a minimum of 8-vCPU, 64Gb RAM, and 80Gb disk
  * when targeting an Ollama VM installation hosted on
    * AWS, choose [m6i.4xlarge](https://aws.amazon.com/ec2/instance-types/#general-purpose)
    * Azure, choose [Standard_D16s_v4](https://learn.microsoft.com/en-us/azure/virtual-machines/sizes/general-purpose/dsv4-series?tabs=sizebasic#sizes-in-series)
    * Google Cloud, choose [c2d-highmem-8](https://cloud.google.com/compute/docs/compute-optimized-machines#c2d-high-mem)

#### GPU assisted configuration

To be redone

* Choose a compute type that has a minimum of 16-vCPU, 64Gb RAM, and 80Gb disk

Here's what you need to know about each cloud provider's GPU configuration:

* AWS
  * [GPU instances](https://aws.amazon.com/ec2/instance-types/) have specific instance types (`p3`, `g4dn`, `p4d` families)
  * Requires NVIDIA drivers installation
  * Example configuration:

    ```bash
    GPU_INSTANCE_TYPE="g4dn.4xlarge"
    USE_GPU=true
    ```

* Azure
  * [GPU VMs](https://learn.microsoft.com/en-us/azure/virtual-machines/sizes/overview?tabs=breakdownseries%2Cgeneralsizelist%2Ccomputesizelist%2Cmemorysizelist%2Cstoragesizelist%2Cgpusizelist%2Cfpgasizelist%2Chpcsizelist#gpu-accelerated) use specific VM sizes (`NC`, `ND` series)
  * Requires NVIDIA drivers installation
  * Example configuration:

    ```bash
    GPU_VM_SIZE="Standard_NC12s_v3"
    USE_GPU=true
    ```

* Google Cloud
  * Common [GPU types](https://cloud.google.com/compute/docs/gpus): `nvidia-tesla-t4`, `nvidia-tesla-p100`, `nvidia-tesla-v100`
  * GPU-enabled zones may be limited
  * Requires special image family for GPU support
  * Example configuration:

    ```bash
    ACCELERATOR_TYPE="nvidia-tesla-t4"
    ACCELERATOR_COUNT=1
    ```

Important considerations:

* GPU instances are significantly more expensive than regular instances
* Not all regions/zones support GPU instances
* You may need to request quota increases for GPU instances
* Some GPU types require specific machine types/sizes
* Driver installation may take several minutes during instance startup

#### Getting started

Here's how to get going running locally targeting models hosted on a VM in a public cloud

```bash
# Checkout source
gh repo clone cf-toolsuite/sanford
cd sanford
# Run provisioning script to create and start a VM with Ollama hosted in [ aws|azure|googlecloud ]
./provision-ollama-vm-on-{replace_with_available_public_cloud_variant}.sh create
# Set environment variables (override defaults)
export CHAT_MODEL=qwen2.5:3b
export EMBEDDING_MODEL=aroxima/gte-qwen2-1.5b-instruct
export SPRING_AI_VECTORSTORE_PGVECTOR_DIMENSIONS=1536
export OLLAMA_BASE_URL=http://{replace_with_ip_address_of_ollama_instance}:11434
gradle clean build bootRun -Pvector-db-provider=pgvector -Pmodel-api-provider=ollama -Dspring.profiles.active=docker,ollama,pgvector,dev
time http --verify=no POST :8080/api/fetch urls:='["https://www.govtrack.us/api/v2/role?current=true&role_type=senator"]'  
time http GET 'http://localhost:8080/api/chat?q="Who are the US senators from Washington?"&f[state]="WA"&f[gender]="female"'
```

### Activate Arize Phoenix for tracing and evaluation

Activate the `arize-phoenix` Spring profile in addition to the `docker` Spring profile.

You may do that by adding it as a profile in the comma-separated list of profiles using

* a command-line runtime argument, `-Dspring.profiles.active=` 
* an environment variable, `export SPRING_PROFILES_ACTIVE=`

After launching the application and making a request, visit http://localhost:6006.

> The runtime configuration may be adapted to work without the `docker` Spring profile activated.  Consult Arize Phoenix's [self-hosting](https://docs.arize.com/phoenix/deployment) deployment documentation and the `ARIZE_PHOENIX_BASE_URL` environment variable in [application.yml](../src/main/resources/application.yml).

### Serving models on Kubernetes clusters

* Take a look at [Kserve](https://kserve.github.io/kserve/).  Then consult this quick-start [guide](KSERVE.md) to host models on your workstation or laptop.