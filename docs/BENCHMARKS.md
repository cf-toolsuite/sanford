# Sanford

WIP - to be validated

## Benchmarks

### Groq

```commandline
export CHAT_MODEL=mixtral-8x7b-32768
export EMBEDDING_MODEL=text-embedding-ada-002
export SPRING_AI_VECTORSTORE_PGVECTOR_DIMENSIONS=1536
gradle clean build bootRun -Pvector-db-provider=pgvector -Dspring.profiles.active=docker,groq-cloud,arize-phoenix,pgvector,dev

# Ingest US senators via /api/fetch

❯ time http --verify=no POST :8080/api/fetch urls:='["https://www.govtrack.us/api/v2/role?current=true&role_type=senator"]'
HTTP/1.1 200 
Connection: keep-alive
Content-Type: application/json
Date: Tue, 03 Dec 2024 03:49:51 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked

{
    "failureCount": 0,
    "results": [
        {
            "error": null,
            "savedPath": "/tmp/fetch/2024.12.02.19.49.47/www.govtrack.us-api-v2-role.json",
            "success": true,
            "url": "https://www.govtrack.us/api/v2/role?current=true&role_type=senator"
        }
    ],
    "successCount": 1,
    "totalUrls": 1
}


http --verify=no POST :8080/api/fetch   0.27s user 0.03s system 6% cpu 4.493 total

# Search for US senators in a particular state via /api/chat

❯ time http --verify=no :8080/api/chat q=="Tell me who the senators are from Washington state"
HTTP/1.1 200 
Connection: keep-alive
Content-Length: 356
Content-Type: text/plain;charset=UTF-8
Date: Tue, 03 Dec 2024 03:50:30 GMT
Keep-Alive: timeout=60

The senators from Washington state are:

1. Senior Senator: Patty Murray (D-WA)
   - Start date: 2023-01-03
   - Website: https://www.murray.senate.gov

2. Junior Senator: Maria Cantwell (D-WA)
   - Start date: 2019-01-03
   - Website: https://www.cantwell.senate.gov

These details can be found in the provided context within the LONG_TERM_MEMORY section.


http --verify=no :8080/api/chat   0.25s user 0.04s system 10% cpu 2.770 total

# Results [ Ingest (~4.5s), Chat (~2.8s) ]
```

### OpenAI

```commandline
export CHAT_MODEL=gpt-4o
export EMBEDDING_MODEL=text-embedding-ada-002
export SPRING_AI_VECTORSTORE_PGVECTOR_DIMENSIONS=1536
gradle clean build bootRun -Pvector-db-provider=pgvector -Dspring.profiles.active=docker,openai,arize-phoenix,pgvector,dev

# Ingest US senators via /api/fetch

❯ time http --verify=no POST :8080/api/fetch urls:='["https://www.govtrack.us/api/v2/role?current=true&role_type=senator"]'                                                                                                                        0|1 ✘  18:19:42   
HTTP/1.1 200
Connection: keep-alive
Content-Type: application/json
Date: Tue, 03 Dec 2024 02:23:12 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked

{
    "failureCount": 0,
    "results": [
        {
            "error": null,
            "savedPath": "/tmp/fetch/2024.12.02.18.23.06/www.govtrack.us-api-v2-role.json",
            "success": true,
            "url": "https://www.govtrack.us/api/v2/role?current=true&role_type=senator"
        }
    ],
    "successCount": 1,
    "totalUrls": 1
}


http --verify=no POST :8080/api/fetch   0.26s user 0.05s system 4% cpu 6.288 total

# Search for US senators in a particular state via /api/chat

❯ time http --verify=no :8080/api/chat q=="Tell me who the senators are from Washington state"
HTTP/1.1 200
Connection: keep-alive
Content-Length: 148
Content-Type: text/plain;charset=UTF-8
Date: Tue, 03 Dec 2024 02:24:16 GMT
Keep-Alive: timeout=60

The senators from Washington state are Patty Murray and Maria Cantwell. Patty Murray is the Senior Senator and Maria Cantwell is the Junior Senator.


http --verify=no :8080/api/chat   0.27s user 0.05s system 7% cpu 4.110 total

# Results [ Ingest (~6s), Chat (~4s) ]
```

### Alting

```commandline
❯ time http --verify=no POST :8080/api/fetch urls:='["https://www.govtrack.us/api/v2/role?current=true&role_type=senator"]'
HTTP/1.1 200 
Connection: keep-alive
Content-Type: application/json
Date: Tue, 03 Dec 2024 21:31:52 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked

{
    "failureCount": 0,
    "results": [
        {
            "error": null,
            "savedPath": "/tmp/fetch/2024.12.03.13.31.47/www.govtrack.us-api-v2-role.json",
            "success": true,
            "url": "https://www.govtrack.us/api/v2/role?current=true&role_type=senator"
        }
    ],
    "successCount": 1,
    "totalUrls": 1
}


http --verify=no POST :8080/api/fetch   0.26s user 0.04s system 5% cpu 5.808 total


```

### Ollama on Google Cloud, CPU-only

Tested with [n2-highmem-16](https://cloud.google.com/compute/docs/general-purpose-machines#n2-high-mem).
Performance profile: [Cascade Lake](https://www.intel.com/content/www/us/en/products/platforms/details/cascade-lake.html).

```commandline
export CHAT_MODEL=gemma2:27b
export EMBEDDING_MODEL=aroxima/gte-qwen2-1.5b-instruct
export OLLAMA_BASE_URL=http://34.169.103.239:11434
export SPRING_AI_VECTORSTORE_PGVECTOR_DIMENSIONS=1536 
gradle clean build bootRun -Pmodel-api-provider=ollama -Pvector-db-provider=pgvector -Dspring.profiles.active=docker,ollama,arize-phoenix,pgvector,dev

# Ingest US senators via /api/fetch

❯ time http --verify=no POST :8080/api/fetch urls:='["https://www.govtrack.us/api/v2/role?current=true&role_type=senator"]'
HTTP/1.1 200 
Connection: keep-alive
Content-Type: application/json
Date: Tue, 03 Dec 2024 01:38:00 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked

{
    "failureCount": 0,
    "results": [
        {
            "error": null,
            "savedPath": "/tmp/fetch/2024.12.02.17.32.48/www.govtrack.us-api-v2-role.json",
            "success": true,
            "url": "https://www.govtrack.us/api/v2/role?current=true&role_type=senator"
        }
    ],
    "successCount": 1,
    "totalUrls": 1
}


http --verify=no POST :8080/api/fetch   0.23s user 0.03s system 0% cpu 5:12.76 total

# Search for US senators in a particular state via /api/chat

❯ time http --verify=no :8080/api/chat q=="Tell me who the senators are from Washington state"
HTTP/1.1 200 
Connection: keep-alive
Content-Length: 75
Content-Type: text/plain;charset=UTF-8
Date: Tue, 03 Dec 2024 01:43:50 GMT
Keep-Alive: timeout=60

The senators from Washington state are Patty Murray and Maria Cantwell. 


http --verify=no :8080/api/chat   0.24s user 0.04s system 0% cpu 3:47.98 total

# Results [ Ingest (~5m), Chat (~3m50s) ]
# Embedding model consumption peaked at 2.65Gb of RAM
# Chat model consumption peaked at 64.6Gb of RAM
```
