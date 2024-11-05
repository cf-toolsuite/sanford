# Sanford

## How to Build

The options below represent the collection of Gradle [conditional dependencies](https://www.baeldung.com/gradle-conditional-dependencies#configuring-conditional-dependency) available in [build.gradle](../build.gradle).  These dependencies will be packaged in the resulting executable JAR.

> Note that a `developmentOnly` scoped dependency on [spring-boot-docker-compose](https://docs.spring.io/spring-boot/reference/features/dev-services.html#features.dev-services.docker-compose) is added to facilitate lifecycle management of Model API providers.


### [Chroma](https://docs.trychroma.com/guides)

Adds dependency on:

* [spring-ai-chroma-store-spring-boot-starter](https://docs.spring.io/spring-ai/reference/api/vectordbs/chroma.html)


```bash
./gradlew clean build -Pvector-db-provider=chroma
```
> You also have the option of building with `-Pmodel-api-provider=ollama` which adds a dependency on [spring-ai-ollama-spring-boot-starter](https://docs.spring.io/spring-ai/reference/api/chat/ollama-chat.html).  Work with [your choice](https://github.com/ollama/ollama?tab=readme-ov-file#model-library) of Ollama LLMs.


### [PgVector](https://github.com/pgvector/pgvector)

Adds dependency on:

* [spring-ai-pgvector-store-spring-boot-starter](https://docs.spring.io/spring-ai/reference/api/vectordbs/pgvector.html)

```bash
./gradlew build -Pvector-db-provider=pgvector
```
> You also have the option of building with `-Pmodel-api-provider=ollama`

### [Redis Stack](https://redis.io/about/about-stack/)

Adds dependency on:

* [spring-ai-redis-store-spring-boot-starter](https://docs.spring.io/spring-ai/reference/api/vectordbs/redis.html)

```bash
./gradlew build -Pvector-db-provider=redis
```
> You also have the option of building with `-Pmodel-api-provider=ollama`


### [Weaviate](https://weaviate.io/developers/weaviate)

Adds dependency on:

* [spring-ai-weaviate-store-spring-boot-starter](https://docs.spring.io/spring-ai/reference/api/vectordbs/weaviate.html)

```bash
./gradlew build -Pvector-db-provider=weaviate
```
> You also have the option of building with `-Pmodel-api-provider=ollama`
