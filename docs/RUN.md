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
* [How to run on Cloud Foundry](#how-to-run-on-cloud-foundry)
  * [Target a foundation](#target-a-foundation)
  * [Authenticate](#authenticate)
  * [Target space](#target-space)
  * [Verify services](#verify-services)
  * [Create a MinIO instance](#create-a-minio-instance)
  * [Clone and build the app](#clone-and-build-the-app)
  * [Deploy](#deploy)
  * [Inspect and/or update the PgVector store database instance](#inspect-andor-update-the-pgvector-store-database-instance)
* [How to run on Kubernetes](#how-to-run-on-kubernetes)

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

This setup launches either an instance of Chroma, PgVector, or Redis Stack for use by the VectorStore.

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

A key thing to note is that **you must activate a combination** of Spring profiles, like:

* `docker` - required when you are running "off platform"
* an LLM provider (i.e., `openai`, `groq-cloud` or `ollama`)
* a Vector database provider (i.e., `chroma`, `pgvector`, or `redis`)

and Gradle project properties, like:

* `-Pmodel-api-provider=ollama`
* `-Pvector-db-provider=chroma` or `-Pvector-db-provider=pgvector` or `-Pvector-db-provider=redis`

## How to run on Cloud Foundry

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

TBD
