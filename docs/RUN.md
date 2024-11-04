# Sanford

* [How to Run with Gradle](#how-to-run-with-gradle)
  * [Sample startup with Docker Compose](#sample-startup-with-docker-compose)
  * [with Storage Provider](#with-storage-provider)
  * [with OpenAI](#with-openai)
  * [with Groq Cloud](#with-groq-cloud)
  * [with Ollama](#with-ollama)
  * [with Vector database](#with-vector-database)
    * [Chroma](#chroma)
    * [PgVector](#pgvector)
    * [Redis Stack](#redis-stack)
    * [Weaviate](#weaviate)
* [How to run on Tanzu Platform for Cloud Foundry](#how-to-run-on-tanzu-platform-for-cloud-foundry)
  * [Target a foundation](#target-a-foundation)
  * [Authenticate](#authenticate)
  * [Target space](#target-space)
  * [Verify services](#verify-services)
  * [Create a MinIO instance](#create-a-minio-instance)
  * [Clone and build the app](#clone-and-build-the-app)
  * [Deploy](#deploy)
  * [Inspect and/or update the PgVector store database instance](#inspect-andor-update-the-pgvector-store-database-instance)
* [How to run on Kubernetes](#how-to-run-on-kubernetes)
  * [Build](#build)
  * [(Optional) Authenticate to a container image registry](#optional-authenticate-to-a-container-image-registry)
  * [(Optional) Push image to a container registry](#optional-push-image-to-a-container-registry)
  * [Target a cluster](#target-a-cluster)
  * [Prepare](#prepare)
  * [Apply](#apply)
  * [Setup port forwarding](#setup-port-forwarding)
  * [Teardown](#teardown)
* [How to run on Tanzu Platform for Kubernetes](#how-to-run-on-tanzu-platform-for-kubernetes)
  * [Clone this repo](#clone-this-repo)
  * [Initialize](#initialize)
    * [Configuring daemon builds](#configuring-daemon-builds)
    * [Configuring platform builds](#configuring-platform-builds)
    * [Validating build configuration](#validating-build-configuration)
  * [Pre-provision services](#pre-provision-services)
    * [Minio](#minio)
    * [Open AI](#open-ai)
    * [Weaviate Cloud](#weaviate-cloud)
  * [Specify service bindings](#specify-service-bindings)
  * [Deploy services](#deploy-services)
  * [Deploy application with service bindings](#deploy-application-with-service-bindings)
  * [Establish a domain binding](#establish-a-domain-binding)
  * [Destroy the app and services](#destroy-the-app-and-services)

Sanford has various modes of operation.

You must activate:

* a Gradle [project property](https://docs.gradle.org/current/userguide/migrating_from_maven.html#migmvn:profiles_and_properties) and
* Spring Boot [profiles](https://docs.spring.io/spring-boot/reference/features/profiles.html)

in order to package the appropriate runtime libraries and then appropriately configure runtime support a [VectorStore](https://docs.spring.io/spring-ai/reference/api/vectordbs.html#_available_implementations) and [EmbeddingModel](https://docs.spring.io/spring-ai/reference/api/embeddings.html#available-implementations).

Both modes work with a [ChatModel](https://docs.spring.io/spring-ai/reference/api/chatmodel.html#_available_implementations).  Currently model support is plumbed for Open AI (including Groq) and Ollama.

## How to Run with Gradle

### Sample startup with Docker Compose

```bash
❯ gradle clean build bootRun -Pvector-db-provider=chroma -Dspring.profiles.active=docker,openai,chroma
executing gradlew instead of gradle

> Task :compileJava
Note: /home/cphillipson/Documents/development/pivotal/cf/sanford/src/main/java/org/cftoolsuite/MinioInitializer.java uses or overrides a deprecated API.
Note: Recompile with -Xlint:deprecation for details.

> Task :bootRun

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::             (v3.4.0-M3)

08:23:05.138 [main] INFO  org.cftoolsuite.SanfordApplication - Starting SanfordApplication using Java 21.0.4 with PID 2683297 (/home/cphillipson/Documents/development/pivotal/cf/sanford/build/classes/java/main started by cphillipson in /home/cphillipson/Documents/development/pivotal/cf/sanford)
08:23:05.139 [main] INFO  org.cftoolsuite.SanfordApplication - The following 3 profiles are active: "docker", "openai", "chroma"
08:23:05.297 [main] INFO  o.s.b.d.c.l.DockerComposeLifecycleManager - Using Docker Compose file /home/cphillipson/Documents/development/pivotal/cf/sanford/docker-compose.chroma.yml
08:23:05.584 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Network sanford_net  Creating
08:23:05.658 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Network sanford_net  Created
08:23:05.659 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Network sanford_default  Creating
08:23:05.721 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Network sanford_default  Created
08:23:05.722 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Volume "sanford_chroma-data"  Creating
08:23:05.724 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Volume "sanford_chroma-data"  Created
08:23:05.724 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Volume "sanford_grafana-storage"  Creating
08:23:05.726 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Volume "sanford_grafana-storage"  Created
08:23:05.726 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Volume "sanford_minio_storage"  Creating
08:23:05.728 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Volume "sanford_minio_storage"  Created
08:23:05.728 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Container zipkin  Creating
08:23:05.728 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Container sanford-chroma-1  Creating
08:23:05.728 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Container prometheus  Creating
08:23:05.728 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Container grafana  Creating
08:23:05.728 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Container sanford-minio-1  Creating
08:23:05.823 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Container zipkin  Created
08:23:05.830 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Container sanford-chroma-1  Created
08:23:05.830 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Container sanford-minio-1  Created
08:23:05.830 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Container grafana  Created
08:23:05.832 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Container prometheus  Created
08:23:05.834 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Container sanford-minio-1  Starting
08:23:05.834 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Container prometheus  Starting
08:23:05.834 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Container grafana  Starting
08:23:05.834 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Container sanford-chroma-1  Starting
08:23:05.834 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Container zipkin  Starting
08:23:06.085 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Container prometheus  Started
08:23:06.139 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Container zipkin  Started
08:23:06.142 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Container grafana  Started
08:23:06.147 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Container sanford-minio-1  Started
08:23:06.150 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Container sanford-chroma-1  Started
08:23:06.151 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Container grafana  Waiting
08:23:06.151 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Container zipkin  Waiting
08:23:06.151 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Container sanford-minio-1  Waiting
08:23:06.151 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Container sanford-chroma-1  Waiting
08:23:06.151 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Container prometheus  Waiting
08:23:06.652 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Container sanford-chroma-1  Healthy
08:23:06.652 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Container prometheus  Healthy
08:23:06.652 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Container grafana  Healthy
08:23:06.652 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Container sanford-minio-1  Healthy
08:23:11.652 [OutputReader-stderr] INFO  o.s.b.docker.compose.core.DockerCli -  Container zipkin  Healthy
08:23:13.819 [main] INFO  o.s.b.w.e.tomcat.TomcatWebServer - Tomcat initialized with port 8080 (http)
08:23:13.827 [main] INFO  o.a.coyote.http11.Http11NioProtocol - Initializing ProtocolHandler ["http-nio-8080"]
08:23:13.829 [main] INFO  o.a.catalina.core.StandardService - Starting service [Tomcat]
08:23:13.829 [main] INFO  o.a.catalina.core.StandardEngine - Starting Servlet engine: [Apache Tomcat/10.1.30]
08:23:13.873 [main] INFO  o.a.c.c.C.[Tomcat].[localhost].[/] - Initializing Spring embedded WebApplicationContext
08:23:13.874 [main] INFO  o.s.b.w.s.c.ServletWebServerApplicationContext - Root WebApplicationContext: initialization completed in 892 ms
08:23:14.962 [main] INFO  o.s.b.a.e.web.EndpointLinksResolver - Exposing 7 endpoints beneath base path '/actuator'
08:23:15.004 [main] INFO  o.a.coyote.http11.Http11NioProtocol - Starting ProtocolHandler ["http-nio-8080"]
08:23:15.009 [main] INFO  o.s.b.w.e.tomcat.TomcatWebServer - Tomcat started on port 8080 (http) with context path '/'
08:23:15.023 [main] INFO  org.cftoolsuite.SanfordApplication - Started SanfordApplication in 10.293 seconds (process running for 10.557)
08:23:15.143 [main] INFO  org.cftoolsuite.MinioInitializer - Successfully connected to Minio server
08:23:15.143 [main] INFO  org.cftoolsuite.MinioInitializer - Checking if bucket sanford already exists
08:23:15.169 [main] INFO  org.cftoolsuite.MinioInitializer - Bucket created successfully: sanford
```

### with Storage Provider

The default storage provider is MinIO.  You may override the default provider by specifying either a command-line startup argument or environment variable.

E.g.,

```bash
-Dstorage-provider=dell-ecs
```

or

```bash
export STORAGE_PROVIDER=dell-ecs
```

This has implications for how you configure the storage provider's connection credentials.

Consult the `minio` and `dell-ecs` Spring profile stanzas inside [application.yml](../src/main/resources/application.yml) for which properties you will need to set.

### with OpenAI

Build and run a version of the utility that is compatible for use with [OpenAI](https://openai.com).  You will need to [obtain an API key](https://platform.openai.com/settings/profile?tab=api-keys).

Before launching the app:

* Create a `config` folder which would be a sibling of the `build` folder.  Create a file named `creds.yml` inside that folder.  Add your own API key into that file.

```yaml
spring:
  ai:
    openai:
      api-key: {REDACTED}
```
> Replace `{REDACTED}` above with your Groq Cloud API key

Open a terminal shell and execute

```bash
./gradlew build bootRun -Dspring.profiles.active=docker,openai -Pvector-db-provider={vector_db_provider}
```

> Replace `{vector_db_provider}` with one of [ `chroma`, `pgvector`, `redis` ]

### with Groq Cloud

Build and run a version of the utility that is compatible for use with [Groq Cloud](https://groq.com).  You will need to [obtain an API key](https://console.groq.com/keys).
Note that Groq does not currently have support for text embedding. So if you intend to run with the `groq-cloud` Spring profile activated, you will also need to provide additional credentials

Before launching the app:

* Create a `config` folder which would be a sibling of the `build` folder.  Create a file named `creds.yml` inside that folder.  Add your own API key into that file.

```yaml
spring:
  ai:
    openai:
      api-key: {REDACTED-1}
      embedding:
        api-key: {REDACTED-2}
        base_url: https://api.openai.com
```
> Replace `{REDACTED-1}` and `{REDACTED-2}` above with your Groq Cloud API and OpenAPI keys respectively.

Open a terminal shell and execute

```bash
./gradlew build bootRun -Dspring.profiles.active=docker,groq-cloud -Pvector-db-provider={vector_db_provider}
```

> Replace `{vector_db_provider}` with one of [ `chroma`, `pgvector`, `redis` ]

### with Ollama

Open a terminal shell and execute:

```bash
ollama pull mistral
ollama pull nomic-embed-text
ollama run mistral
```

Open another terminal shell and execute

```bash
./gradlew build bootRun -Dspring.profiles.active=docker,ollama -Pmodel-api-provider=ollama
```
> You'll need to manually stop to the application with `Ctrl+C`

^ If you want to override the chat model you could add `-Dspring.ai.ollama.chat.options.model={model_name}` to the above and replace `{chat_model_name}` with a supported model.  Likewise, you may override the embedding model with `-Dspring.ai.ollama.embedding.options.model={embedding_model_name}`.

### with Vector database

This setup launches either an instance of Chroma, PgVector, Redis Stack, or Weaviate for use by the VectorStore.

A key thing to note is that **you must activate a combination** of Spring profiles, like:

* `docker` - required when you are running "off platform"
* an LLM provider (i.e., `openai`, `groq-cloud` or `ollama`)
* a Vector database provider (i.e., `chroma`, `pgvector`, `redis`, or `weaviate`)

and Gradle project properties, like:

* `-Pmodel-api-provider=ollama`
* `-Pvector-db-provider=chroma` or `-Pvector-db-provider=pgvector` or `-Pvector-db-provider=redis` or `-Pvector-db-provider=weaviate`

#### Chroma

```bash
./gradlew build bootRun -Dspring.profiles.active=docker,openai,chroma -Pvector-db-provider=chroma
```
> You also have the option of building with `-Pmodel-api-provider=ollama` then replacing `openai` or `groq-cloud` in `-Dspring.profiles.active` with `ollama`.

#### PgVector

```bash
./gradlew build bootRun -Dspring.profiles.active=docker,groq-cloud,pgvector -Pvector-db-provider=pgvector
```
> You also have the option of building with `-Pmodel-api-provider=ollama` then replacing `openai` or `groq-cloud` in `-Dspring.profiles.active` with `ollama`.

#### Redis Stack

```bash
./gradlew build bootRun -Dspring.profiles.active=docker,openai,redis -Pvector-db-provider=redis
```
> You also have the option of building with `-Pmodel-api-provider=ollama` then replacing `openai` or `groq-cloud` in `-Dspring.profiles.active` with `ollama`.

#### Weaviate

```bash
./gradlew build bootRun -Dspring.profiles.active=docker,openai,weaviate -Pvector-db-provider=weaviate
```
> You also have the option of building with `-Pmodel-api-provider=ollama` then replacing `openai` or `groq-cloud` in `-Dspring.profiles.active` with `ollama`.

## How to run on Tanzu Platform for Cloud Foundry

### Target a foundation

```bash
cf api {cloud_foundry_foundation_api_endpoint}
```

> Replace `{cloud_foundry_foundation_api_endpoint}` above with an API endppint

Sample interaction

```bash
cf api api.sys.dhaka.cf-app.com
```

### Authenticate

Interactively

```bash
cf login
```

With single sign-on

```bash
cf login --sso
```

With a username and password

```bash
cf login -u {username} -p "{password}"
```

> Replace `{username}` and `{password}` above respectively with your account's username and password.

### Target space

If your user account has `OrgManager` and `SpaceManager` permissions, then you can create your own organization and space with

```bash
cf create-org {organization_name}
cf create-space -o {organization_name} {space_name}
```

> Replace `{organization_name}` and `{space_name}` above with names of your design

To target a space

```bash
cf target -o {organization_name} -s {space_name}
```

> Replace `{organization_name}` and `{space_name}` above with an existing organization and space your account has access to

Sample interaction

```bash
cf create-org zoolabs
cf create-space -o zoolabs dev
cf target -o zoolabs -s dev
```

### Verify services

Verify that the foundation has the service offerings required

```bash
cf m -e genai
cf m -e postgres
cf -m e credhub
```

Sample interaction

```bash
❯ cf m -e genai
Getting service plan information for service offering genai in org zoolabs / space dev as chris.phillipson@broadcom.com...

broker: genai-service
   plan               description                                                                                       free or paid   costs
   llama3.1           Access to the llama3.1 model. Capabilities: chat, tools. Aliases: gpt-turbo-3.5.                  free
   llava              Access to the llava model. Capabilities: chat, vision.                                            free
   nomic-embed-text   Access to the nomic-embed-text model. Capabilities: embedding. Aliases: text-ada-embedding-002.   free

❯ cf m -e postgres
Getting service plan information for service offering postgres in org zoolabs / space dev as chris.phillipson@broadcom.com...

broker: postgres-odb
   plan                       description                             free or paid   costs
   on-demand-postgres-small   A single e2-micro with 2GB of storage   free

❯ cf m -e credhub
Getting service plan information for service offering credhub in org zoolabs / space dev as chris.phillipson@broadcom.com...

broker: credhub-broker
   plan      description                                           free or paid   costs
   default   Stores configuration parameters securely in CredHub   free
```

### Create a MinIO instance

Visit [StackHero](https://www.stackhero.io/en/), create an account, a project, and launch an instance of MinIO.

> When you sign-up for a new account, you'll receive a USD 50 credit that you can use over a 1-month duration.  You can launch new Hobby instances of MinIO.  If you don't supply a payment method each time you launch an instance, it will be automatically destroyed after 24-hours.

Then, create a configuration file

```bash
mkdir -p $HOME/.minio
touch $HOME/.minio/config
```

Edit the above file and make sure it has the following environment variables exported:

```python
export MINIO_ENDPOINT_HOST=<minio-hostname>
export MINIO_ENDPOINT_PORT=<minio-port>
export MINIO_ACCESS_KEY=<minio-username>
export MINIO_SECRET_KEY=<minio-password>
```

> Replace the values above enclosed in `<>` with appropriate values for your instance hosted on StackHero


### Clone and build the app

```bash
gh repo clone cf-toolsuite/sanford
cd sanford
gradle build -Pvector-db-provider=pgvector
```

### Deploy

Take a look at the deployment script

```bash
cat deploy-on-tp4cf.sh
```

> Make any required edits to the environment variables for the services and plans.

Execute the deployment script

```bash
./deploy-on-tp4cf.sh setup
```

To teardown, execute

```bash
./deploy-on-tp4cf.sh teardown
```

### Inspect and/or update the PgVector store database instance

Create a service key for the service instance, with:

```bash
cf create-service-key sanford-db cf-psql
```

Sample interaction

```bash
❯ cf create-service-key sanford-db cf-psql
Creating service key cf-psql for service instance sanford-db as chris.phillipson@broadcom.com...
OK

❯ cf service-key sanford-db cf-psql
Getting key cf-psql for service instance sanford-db as chris.phillipson@broadcom.com...

{
  "credentials": {
    "db": "postgres",
    "hosts": [
      "q-s0.postgres-instance.dhaka-services-subnet.service-instance-967aa687-1b73-4448-8505-dca0fa2ee079.bosh"
    ],
    "jdbcUrl": "jdbc:postgresql://q-s0.postgres-instance.dhaka-services-subnet.service-instance-967aa687-1b73-4448-8505-dca0fa2ee079.bosh:5432/postgres?user=pgadmin&password=Z8ybS105mdY7i6h923H4",
...
```

Open two terminal sessions.

In the first session, execute:

```bash
❯ cf ssh -L 55432:q-s0.postgres-instance.dhaka-services-subnet.service-instance-967aa687-1b73-4448-8505-dca0fa2ee079.bosh:5432 sanford
vcap@128bacbc-b0f1-46b5-64cb-709c:~$
```

> We are creating a tunnel between the host and the service instance via the application. The host will listen on port 55432.

Switch to the second session, then execute:

```bash
❯ psql -U pgadmin -W postgres -h 127.0.0.1 -p 55432
Password:
```

Enter the password.  See that it is specified at the end fo the "jdbcUrl" JSON fragment above.

And you should see:

```bash
psql (12.9 (Ubuntu 12.9-0ubuntu0.20.04.1), server 15.6)
WARNING: psql major version 12, server major version 15.
         Some psql features might not work.
Type "help" for help.

postgres=#
```

From here you can show tables with `\dt`

```bash
postgres=# \dt
            List of relations
 Schema |     Name     | Type  |  Owner
--------+--------------+-------+---------
 public | vector_store | table | pgadmin
(1 row)
```

You can describe the table with `\d vector_store`

```bash
postgres=# \d vector_store
                     Table "public.vector_store"
  Column   |     Type     | Collation | Nullable |      Default
-----------+--------------+-----------+----------+--------------------
 id        | uuid         |           | not null | uuid_generate_v4()
 content   | text         |           |          |
 metadata  | json         |           |          |
 embedding | vector(1536) |           |          |
Indexes:
    "vector_store_pkey" PRIMARY KEY, btree (id)
    "spring_ai_vector_index" hnsw (embedding vector_cosine_ops)
```

And you can execute arbitrary SQL (e.g., `SELECT * from vector_store`).

If you need to ALTER the dimensions of the `embedding` column to adapt to the limits of an embedding model you chose, then you could, for example, execute:

```bash
-- Step 1: Drop the existing index
DROP INDEX IF EXISTS spring_ai_vector_index;

-- Step 2: Drop the existing column
ALTER TABLE public.vector_store DROP COLUMN embedding;

-- Step 3: Add the new column with the desired vector size
ALTER TABLE public.vector_store ADD COLUMN embedding vector(768);

-- Step 4: Recreate the index
CREATE INDEX spring_ai_vector_index ON public.vector_store USING hnsw (embedding vector_cosine_ops);
```

To exit, just type `exit`.


## How to run on Kubernetes

We're going to make use of the [Eclipse JKube Gradle plugin](https://eclipse.dev/jkube/docs/kubernetes-gradle-plugin/#getting-started).

To build targeting the appropriate, supporting, runtime infrastructure, you will need to choose:

* LLM provider
  * groq-cloud, openai
* Vector store
  * chroma, pgvector
* Storage provider
  * minio

### Build

To build a container image with Spring Boot, set the container image version, and assemble the required Kubernetes manifests for deployment, execute:

```bash
❯ gradle clean setVersion build bootBuildImage k8sResource -PnewVersion=$(date +"%Y.%m.%d") -Pvector-db-provider=chroma -Pjkube.environment=groq-cloud,chroma,observability,minio --stacktrace
```

This will build and tag a container image using [Paketo Buildpacks](https://paketo.io/docs/concepts/buildpacks/) and produce a collection of manifests in `build/classes/META-INF/jkube`.

**Sample output**

```bash
❯ docker images
REPOSITORY                            TAG          IMAGE ID       CREATED        SIZE
paketobuildpacks/run-jammy-tiny       latest       b4e8795ea35a   4 weeks ago    22.6MB
cftoolsuite/sanford                   2024.10.28   0ba08f8b0c60   44 years ago   360MB
cftoolsuite/sanford                   latest       0ba08f8b0c60   44 years ago   360MB
paketobuildpacks/builder-jammy-tiny   latest       7548a7329f38   44 years ago   751MB

❯ ls -la build/classes/java/main/META-INF/jkube
total 36
drwxrwxr-x 3 cphillipson cphillipson  4096 Oct 28 17:26 .
drwxrwxr-x 3 cphillipson cphillipson  4096 Oct 28 17:26 ..
drwxrwxr-x 2 cphillipson cphillipson  4096 Oct 28 17:26 kubernetes
-rw-rw-r-- 1 cphillipson cphillipson 21699 Oct 28 17:26 kubernetes.yml

❯ ls -la build/classes/java/main/META-INF/jkube/kubernetes
total 68
drwxrwxr-x 2 cphillipson cphillipson 4096 Oct 28 17:26 .
drwxrwxr-x 3 cphillipson cphillipson 4096 Oct 28 17:26 ..
-rw-rw-r-- 1 cphillipson cphillipson  871 Oct 28 17:26 chroma-service.yml
-rw-rw-r-- 1 cphillipson cphillipson 2238 Oct 28 17:26 grafana-deployment.yml
-rw-rw-r-- 1 cphillipson cphillipson  625 Oct 28 17:26 grafana-persistentvolumeclaim.yml
-rw-rw-r-- 1 cphillipson cphillipson  873 Oct 28 17:26 grafana-service.yml
-rw-rw-r-- 1 cphillipson cphillipson 2490 Oct 28 17:26 minio-deployment.yml
-rw-rw-r-- 1 cphillipson cphillipson  605 Oct 28 17:26 minio-secret.yml
-rw-rw-r-- 1 cphillipson cphillipson  905 Oct 28 17:26 minio-service.yml
-rw-rw-r-- 1 cphillipson cphillipson 1029 Oct 28 17:26 prometheus-configmap.yml
-rw-rw-r-- 1 cphillipson cphillipson 2274 Oct 28 17:26 prometheus-deployment.yml
-rw-rw-r-- 1 cphillipson cphillipson  879 Oct 28 17:26 prometheus-service.yml
-rw-rw-r-- 1 cphillipson cphillipson 2953 Oct 28 17:26 sanford-deployment.yml
-rw-rw-r-- 1 cphillipson cphillipson  906 Oct 28 17:26 sanford-service.yml
-rw-rw-r-- 1 cphillipson cphillipson  658 Oct 28 17:26 spring-ai-creds-secret.yml
-rw-rw-r-- 1 cphillipson cphillipson 2123 Oct 28 17:26 zipkin-deployment.yml
-rw-rw-r-- 1 cphillipson cphillipson  871 Oct 28 17:26 zipkin-service.yml
```

### (Optional) Authenticate to a container image registry

If you are a contributor with an account that has permissions to push updates to the container image, you will need to authenticate with the container image registry.

For [DockerHub](https://hub.docker.com/), you could execute:

```bash
docker login docker.io -u cftoolsuite -p REPLACE_ME
```

> Replace the password value `REPLACE_ME` above with a valid personal access token to DockerHub.

### (Optional) Push image to a container registry

Here's how to push an update:

```bash
gradle k8sPush
```

### Target a cluster

You will need to establish a connection context to a cluster you have access to.

The simplest thing to do... is to launch a [Kind](https://kind.sigs.k8s.io/docs/user/quick-start/#creating-a-cluster) cluster.

```bash
kind create cluster
```

**Sample interaction**

```bash
❯ kind create cluster
Creating cluster "kind" ...
 ✓ Ensuring node image (kindest/node:v1.31.0) 🖼
 ✓ Preparing nodes 📦
 ✓ Writing configuration 📜
 ✓ Starting control-plane 🕹️
 ✓ Installing CNI 🔌
 ✓ Installing StorageClass 💾
Set kubectl context to "kind-kind"
You can now use your cluster with:

kubectl cluster-info --context kind-kind

Have a question, bug, or feature request? Let us know! https://kind.sigs.k8s.io/#community


❯ kubectl get nodes
NAME                 STATUS   ROLES           AGE    VERSION
kind-control-plane   Ready    control-plane   105s   v1.31.0

❯ kubectl get pods -A
NAMESPACE            NAME                                         READY   STATUS    RESTARTS   AGE
kube-system          coredns-6f6b679f8f-5vjrg                     1/1     Running   0          48s
kube-system          coredns-6f6b679f8f-w8m42                     1/1     Running   0          48s
kube-system          etcd-kind-control-plane                      1/1     Running   0          54s
kube-system          kindnet-rvf6r                                1/1     Running   0          48s
kube-system          kube-apiserver-kind-control-plane            1/1     Running   0          54s
kube-system          kube-controller-manager-kind-control-plane   1/1     Running   0          54s
kube-system          kube-proxy-qq64n                             1/1     Running   0          48s
kube-system          kube-scheduler-kind-control-plane            1/1     Running   0          54s
local-path-storage   local-path-provisioner-57c5987fd4-k27f5      1/1     Running   0          48s
```

### Prepare

Consult DockerHub for the latest available tagged image, [here](https://hub.docker.com/r/cftoolsuite/sanford/tags).

Edit the `build/classes/java/main/META-INF/jkube/kubernetes/sanford-deployment.yml` and `build/classes/java/main/META-INF/jkube/kubernetes/sanford-service.yml` files

You should replace occurrences of `YYYY.MM.DD` (e.g., 2024.10.28) with the latest available tag, and save your changes.

Before deploying you will want to edit the contents of `build/classes/java/main/META-INF/jkube/kubernetes/spring-ai-creds-secret.yml`.

Back when you built the image and created the Kubernetes manifests, you had to supply a comma-separated `-Pjkube.environment=` set of argument values.

If that set contained `openai`, you would see the following fragment within the secret:

```yaml
stringData:
  creds.yml: |
    spring:
      ai:
        openai:
          api-key: REPLACE_WITH_OPENAI_API_KEY
```

> Your job is to replace the occurrence of `REPLACE_WITH_OPENAI_API_KEY` with valid API key value from Open AI.

If, however, that set contained `groq-cloud`, you would see the following fragment within the secret:

```yaml
stringData:
  creds.yml: |
    spring:
      ai:
        openai:
          api-key: REPLACE_WITH_GROQCLOUD_API_KEY
          embedding:
            api-key: REPLACE_WITH_OPENAI_API_KEY
            base_url: https://api.openai.com
```

> Your job is to replace the occurrences of values that start with `REPLACE_WITH` with valid API key values from Groq Cloud and Open AI respectively. The Open AI key-value is used for the embedding model as Groq Cloud does not have support for embedding models, yet.

### Apply

Finally, we can deploy the application and dependent runtime services to our Kubernetes cluster.

Do so, with:

```bash
gradle k8sApply -Pvector-db-provider=chroma -Pjkube.environment=openai,chroma,observability,minio
```

or

```bash
kubectl apply -f build/classes/java/main/META-INF/jkube/kubernetes.yml
```

### Setup port forwarding

At this point you'd probably like to interact with sanford, huh?  We need to setup port-forwarding, so execute:

```bash
kubectl port-forward service/sanford 8080:8080
```

Then visit `http://localhost:8080/actuator/info` in your favorite browser.

Consult the [ENDPOINTS.md](ENDPOINTS.md) documentation to learn about what else you can do.

When you're done, revisit the terminal where you started port-forwarding and press `Ctrl+C`.

> Yeah, this only gets you so far.  For a more production-ready footprint, there's quite a bit more work involved.  But this suffices for an inner-loop development experience.

### Teardown

```bash
gradle k8sUndeploy -Pvector-db-provider=chroma -Pjkube.environment=openai,chroma,observability,minio
```

or

```bash
kubectl delete -f build/classes/java/main/META-INF/jkube/kubernetes.yml
```

And if you launched a Kind cluster earlier, don't forget to tear it down with:

```bash
kind delete cluster
```

## How to run on Tanzu Platform for Kubernetes

Consider this a Quick Start guide to getting `sanford` deployed using the [tanzu](https://docs.vmware.com/en/VMware-Tanzu-Application-Platform/1.12/tap/install-tanzu-cli.html) CLI.

We'll be focused on a subset of commands to get the job done.  That said, you will likely need to work with a Platform Engineer within your enterprise to pre-provision an environment for your use.

This [Github gist](https://gist.github.com/pacphi/b9a7bb0f9538db1d11d1671d8a2b5566) should give you sense of how to get started with infrastructure provisioning and operational concerns, before attempting to deploy.

### Clone this repo

```bash
gh repo clone cf-toolsuite/sanford
```

### Initialize

> We're going to assume that your account is a member of an organization and has apprpriate access-level permssions to work with an existing project and space(s).

Login, set a project and space.

```bash
tanzu login
tanzu project list
tanzu project use AMER-West
tanzu space list
tanzu space use cphillipson-sbx
```

> You will set a different `project` and `space`.  The above is just illustrative of what you'll need to do to target where you'll deploy your own instance of this application.

Set the context for `kubectl`, just in case you need to inspect resources.

```bash
tanzu context current
```

**Sample interaction**

```bash
❯ tanzu context current
  Name:            sa-tanzu-platform
  Type:            tanzu
  Organization:    sa-tanzu-platform (77aee83b-308f-4c8e-b9c4-3f7a6f19ba75)
  Project:         AMER-West (3b65ba5e-52a4-4666-ad29-4eefab93127b)
  Space:           cphillipson-sbx
  Kube Config:     /home/cphillipson/.config/tanzu/kube/config
  Kube Context:    tanzu-cli-sa-tanzu-platform:AMER-West:cphillipson-sbx
```

Then

```bash
# Use the value after "Kube Config:"
# Likely this will work consistently for you
export KUBECONFIG=$HOME/.config/tanzu/kube/config
```

Now, let's jump into the root-level directory of the Git repository's project we cloned earlier, create a new branch, and freshly initialize Tanzu application configuration.

```bash
cd sanford
git checkout -b tp4k8s-experiment
tanzu app init -y
```

We'll also need to remove any large files or sensitive configuration files.

```
du -sh * -c
./prune.sh
```

Edit the file `.tanzu/config/sanford.yml`.

It should look like this after editing.  Save your work.

```yaml
apiVersion: apps.tanzu.vmware.com/v1
kind: ContainerApp
metadata:
  name: sanford
spec:
  nonSecretEnv:
    - name: JAVA_TOOL_OPTIONS
      value: "-Djava.security.egd=file:///dev/urandom -XX:+UseZGC -XX:+UseStringDeduplication"
    - name: SPRING_PROFILES_ACTIVE
      value: "default,cloud,openai,weaviate"
  build:
    nonSecretEnv:
    - name: BP_JVM_VERSION
      value: "21"
    - name: BP_GRADLE_BUILD_ARGUMENTS
      value: "-Pvector-db-provider=weaviate"
    buildpacks: {}
    path: ../..
  contact:
    team: cftoolsuite
  ports:
  - name: main
    port: 8080
  probes:
    liveness:
      httpGet:
        path: /actuator/health/liveness
        port: 8080
        scheme: HTTP
    readiness:
      httpGet:
        path: /actuator/health/readiness
        port: 8080
        scheme: HTTP
    startup:
      failureThreshold: 120
      httpGet:
        path: /actuator/health/readiness
        port: 8080
        scheme: HTTP
      initialDelaySeconds: 2
      periodSeconds: 2
```

#### Configuring daemon builds

```bash
tanzu build config \
  --build-plan-source-type ucp \
  --containerapp-registry docker.io/{contact.team}/{name} \
  --build-plan-source custom-build-plan-ingressv2  \
  --build-engine=daemon
```

Builds will be performed locally.  (Docker must be installed).  We are targeting Dockerhub as the container image registry.  If you wish to target another registry provider you would have to change the prefix value of `docker.io` above to something else.  Pay attention to `{contact.team}`.  In the `ContainerApp` resource definition above, you will have to change `cftoolsuite` to an existing repository name in your registry.

You will also have to authenticate with the registry, e.g.

```bash
export REGISTRY_USERNAME=cftoolsuite
export REGISTRY_PASSWORD=xxx
export REGISTRY_HOST=docker.io
echo $REGISTRY_PASSWORD | docker login $REGISTRY_HOST -u $REGISTRY_USERNAME --password-stdin
```

> Replace the values of `REGISTRY_` environment variables above as appropriate.

By the way, whatever container image registry provider you choose, make sure to restrict access to the repository.  If you're using DockerHub, set the visibility of your repository to private.

> If your app will work with a private registry, then your Platform Engineer will have to have had to configure the [Registry Credentials Pull Only Installer](https://www.platform.tanzu.broadcom.com/hub/application-engine/capabilities/registry-pull-only-credentials-installer.tanzu.vmware.com/details).

#### Configuring platform builds

```bash
tanzu build config \
  --build-plan-source-type ucp \
  --containerapp-registry us-west1-docker.pkg.dev/fe-cpage/west-sa-build-registry/{contact.team}/{name} \
  --build-plan-source custom-build-plan-ingressv2 \
  --build-engine=platform
```

A benefit of platform builds is that they occur on-platform.  (Therefore, Docker does not need to be installed).  We will assume that a Platform Engineer has set this up on our behalf.

> You will likely need to change `us-west1-docker.pkg.dev/fe-cpage/west-sa-build-registry` above to an appropriate prefix targeting a shared container image registry.

#### Validating build configuration

For daemon builds, e.g.

```bash
❯ tanzu build config view
Using config file: /home/cphillipson/.config/tanzu/build/config.yaml
Success: Getting config
buildengine: daemon
buildPlanSource: custom-build-plan-ingressv2
buildPlanSourceType: ucp
containerAppRegistry: docker.io/{contact.team}/{name}
experimentalFeatures: false
```

### Pre-provision services

Create a new sibling directory of `.tanzu/config` to contain service manifests.

```bash
mkdir .tanzu/service
```

Place yourself in the `.tanzu/service` directory.  We'll create `PreProvisionedService` and `Secret` manifests for a handful of the off-platform services that `sanford` will need to interact with.

```bash

cd .tanzu/service
```

#### Minio

Create a file named `minio.yml`, adjust and save the content below:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: minio-creds
type: servicebinding.io/ai
stringData:
  host: CHANGE_ME
  port: "443"
  access-key: CHANGE_ME
  secret-key: CHANGE_ME
  bucket-name: sanford
  provider: minio
  type: minio

---
apiVersion: services.tanzu.vmware.com/v1
kind: PreProvisionedService
metadata:
  name: minio
spec:
  bindingConnectors:
  - name: main
    description: MinIO credentials
    type: minio
    secretRef:
      name: minio-creds

---
apiVersion: networking.tanzu.vmware.com/v1alpha1
kind: EgressPoint
metadata:
   name: minio-egress
spec:
   targets:
   - hosts:
     - "CHANGE_ME"
     port:
      number: 443
      protocol: HTTPS
```

> You will need to replace occurrences of `CHANGE_ME` above with your own `host`, `port`, `access-key`, `secret-key`, and `bucket-name` values that will authenticate and authorize a connection to MinIO instance and bucket you are hosting.  If you're looking for an easy way to provision MinIO, visit [StackHero](https://www.stackhero.io/en/), create an account, a project, and launch an instance of MinIO.  Note the `EgressPoint`'s `hosts` value above should be the same as the `Secret`'s `host` value but without the scheme (i.e., do not include `https://`).

#### Open AI

Create a file named `openai.yml`, adjust and save the content below:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: openai-creds
type: servicebinding.io/ai
stringData:
  uri: CHANGE_ME
  api-key: CHANGE_ME
  provider: openai
  type: openai

---
apiVersion: services.tanzu.vmware.com/v1
kind: PreProvisionedService
metadata:
  name: openai
spec:
  bindingConnectors:
  - name: main
    description: Open AI credentials
    type: openai
    secretRef:
      name: openai-creds

---
apiVersion: networking.tanzu.vmware.com/v1alpha1
kind: EgressPoint
metadata:
   name: openai-egress
spec:
   targets:
   - hosts:
     - "CHANGE_ME"
     port:
      number: 443
      protocol: HTTPS
```

> You will need to replace occurrences of `CHANGE_ME` above with your own `uri` and `api-key` values that will authenticate and authorize a connection to your account on the Open AI platform.  Note the `EgressPoint`'s `hosts` value above should be the same as the `Secret`'s `uri` value but without the scheme (i.e., do not include `https://`).

#### Weaviate Cloud

Create a file named `weaviate-cloud.yml`, adjust and save the content below:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: weaviate-cloud-creds
type: servicebinding.io/ai
stringData:
  uri: CHANGE_ME
  api-key: CHANGE_ME
  provider: weaviate-cloud
  type: weaviate-cloud

---
apiVersion: services.tanzu.vmware.com/v1
kind: PreProvisionedService
metadata:
  name: weaviate-cloud
spec:
  bindingConnectors:
  - name: main
    description: Weaviate Cloud credentials
    type: weaviate-cloud
    secretRef:
      name: weaviate-cloud-creds

---
apiVersion: networking.tanzu.vmware.com/v1alpha1
kind: EgressPoint
metadata:
   name: weaviate-cloud-egress
spec:
   targets:
   - hosts:
     - "CHANGE_ME"
     port:
      number: 443
      protocol: HTTPS
```

> You will need to replace occurrences of `CHANGE_ME` above with your own `uri` and `api-key` values that will authenticate and authorize a connection to the instance of Weaviate you are hosting on [Weaviate Cloud](https://console.weaviate.io).  Note the `EgressPoint`'s `hosts` value above should be the same as the `Secret`'s `uri` value but without the scheme (i.e., do not include `https://`).

### Specify service bindings

Change directories again.  Place yourself back into the directory containing `sanford.yml`.

```bash
cd ../config
```

Create another file named `sanford-service-bindings.yml` and save the content below into it.  This file should live in same directory as the services and the application.

```yaml
apiVersion: services.tanzu.vmware.com/v1
kind: ServiceBinding
metadata:
  name: minio-service-binding
spec:
  targetRef:
    apiGroup: apps.tanzu.vmware.com
    kind: ContainerApp
    name: sanford

  serviceRef:
    apiGroup: services.tanzu.vmware.com
    kind: PreProvisionedService
    name: minio
    connectorName: main

---
apiVersion: services.tanzu.vmware.com/v1
kind: ServiceBinding
metadata:
  name: openai-service-binding
spec:
  targetRef:
    apiGroup: apps.tanzu.vmware.com
    kind: ContainerApp
    name: sanford

  serviceRef:
    apiGroup: services.tanzu.vmware.com
    kind: PreProvisionedService
    name: openai
    connectorName: main

---
apiVersion: services.tanzu.vmware.com/v1
kind: ServiceBinding
metadata:
  name: weaviate-cloud-service-binding
spec:
  targetRef:
    apiGroup: apps.tanzu.vmware.com
    kind: ContainerApp
    name: sanford

  serviceRef:
    apiGroup: services.tanzu.vmware.com
    kind: PreProvisionedService
    name: weaviate-cloud
    connectorName: main
```

### Deploy services

Let's place ourselves back into the root-level directory

```bash
cd ../..
```

then execute

```bash
tanzu deploy --only .tanzu/service -y
```

### Create and publish package to container image registry repository

```bash
tanzu build -o .tanzu/build
```

### Deploy application with service bindings

```bash
tanzu deploy --from-build .tanzu/build -y
```

Here's a few optional commands you could run afterwards to check on the state of deployment

```bash
tanzu app get sanford
tanzu app logs sanford --lines -1
```

### Establish a domain binding

```bash
tanzu domain-binding create sanford --domain sanford.sbx.tpk8s.cloudmonk.me --entrypoint main --port 443
```

> Replace the portion of the value of `--domain` before the application name above with your own sub-domain (or with one your Platform Enginer setup on your behalf).

Checkout [ENDPOINTS.md](ENDPOINTS.md) to see what you can do.

### Destroy the app and services

```bash
kubectl delete -f .tanzu/service
tanzu app delete sanford -y
```
