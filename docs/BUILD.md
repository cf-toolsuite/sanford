# Sanford

## How to Build

The options below represent the collection of Gradle [conditional dependencies](https://www.baeldung.com/gradle-conditional-dependencies#configuring-conditional-dependency) available in [build.gradle](../build.gradle).  These dependencies will be packaged in the resulting executable JAR.

> Note that a `developmentOnly` scoped dependency on [spring-boot-docker-compose](https://docs.spring.io/spring-boot/reference/features/dev-services.html#features.dev-services.docker-compose) is added to facilitate lifecycle management of Model API providers.

### Vector database providers

#### [Chroma](https://docs.trychroma.com/guides)

Adds dependency on:

* [spring-ai-chroma-store-spring-boot-starter](https://docs.spring.io/spring-ai/reference/api/vectordbs/chroma.html)


```bash
./gradlew clean build -Pvector-db-provider=chroma
```

#### [Gemfire](https://gemfire.dev/)

Adds dependency on:

* [spring-ai-gemfire-store-spring-boot-starter](https://docs.spring.io/spring-ai/reference/api/vectordbs/gemfire.html)


```bash
./gradlew clean build -Pvector-db-provider=gemfire
```

#### [PgVector](https://github.com/pgvector/pgvector)

Adds dependency on:

* [spring-ai-pgvector-store-spring-boot-starter](https://docs.spring.io/spring-ai/reference/api/vectordbs/pgvector.html)

```bash
./gradlew build -Pvector-db-provider=pgvector
```

#### [Redis Stack](https://redis.io/about/about-stack/)

Adds dependency on:

* [spring-ai-redis-store-spring-boot-starter](https://docs.spring.io/spring-ai/reference/api/vectordbs/redis.html)

```bash
./gradlew build -Pvector-db-provider=redis
```

#### [Weaviate](https://weaviate.io/developers/weaviate)

Adds dependency on:

* [spring-ai-weaviate-store-spring-boot-starter](https://docs.spring.io/spring-ai/reference/api/vectordbs/weaviate.html)

```bash
./gradlew build -Pvector-db-provider=weaviate
```

### Model API providers

You have the ability to swap model API providers.  The default provider is OpenAI.  But you can override the default by setting the `model-api-provider` Gradle property.

You have the option of setting this property's value to:

* `bedrock` - provides access to Amazon Bedrock models
  * Adds a dependency on [spring-ai-bedrock-ai-spring-boot-starter](https://docs.spring.io/spring-ai/reference/api/bedrock-chat.html).  Work with [your choice](https://us-east-1.console.aws.amazon.com/bedrock/home?region=us-east-1#/model-catalog) of models from the Bedrock model catalog.
* `gemini` - provides access to Google Cloud's [Vertex AI](https://cloud.google.com/vertex-ai/generative-ai/docs/multimodal/call-vertex-using-openai-library)
  * Adds dependencies on [spring-ai-vertex-ai-gemini-spring-boot-starter](https://docs.spring.io/spring-ai/reference/api/chat/vertexai-gemini-chat.html) and [spring-ai-vertex-ai-embedding-spring-boot-starter](https://docs.spring.io/spring-ai/reference/api/embeddings/vertexai-embeddings-text.html).
* `ollama` - provides access to Ollama models
  * Adds a dependency on [spring-ai-ollama-spring-boot-starter](https://docs.spring.io/spring-ai/reference/api/chat/ollama-chat.html).  Work with [your choice](https://ollama.com/search) of Ollama LLMs.

E.g., with

```commandline
-Pmodel-api-provider=ollama
```
