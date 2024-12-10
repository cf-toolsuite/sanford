# Sanford

## Benchmarks

### Groq

Test 1

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

Test 2

```commandline
export CHAT_MODEL=llama-3.3-70b-versatile
export EMBEDDING_MODEL=text-embedding-3-large
gradle clean build bootRun -Pvector-db-provider=chroma -Dspring.profiles.active=docker,groq-cloud,arize-phoenix,chroma,dev

# Ingest US senators via /api/fetch

❯ time http --verify=no POST :8080/api/fetch urls:='["https://www.govtrack.us/api/v2/role?current=true&role_type=senator"]'
HTTP/1.1 200 
Connection: keep-alive
Content-Type: application/json
Date: Mon, 09 Dec 2024 18:09:09 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked

{
    "failureCount": 0,
    "results": [
        {
            "error": null,
            "savedPath": "/tmp/fetch/2024.12.09.10.09.03/www.govtrack.us-api-v2-role.json",
            "success": true,
            "url": "https://www.govtrack.us/api/v2/role?current=true&role_type=senator"
        }
    ],
    "successCount": 1,
    "totalUrls": 1
}


http --verify=no POST :8080/api/fetch   0.24s user 0.04s system 4% cpu 6.313 total

# Search for US senators in a particular state via /api/chat

❯ time http GET 'http://localhost:8080/api/chat?q="Who are the US senators from Washington?"&f[state]="WA"&f[gender]="female"'
HTTP/1.1 200 
Connection: keep-alive
Content-Length: 68
Content-Type: text/plain;charset=UTF-8
Date: Mon, 09 Dec 2024 18:09:20 GMT
Keep-Alive: timeout=60

The US senators from Washington are Patty Murray and Maria Cantwell.


http GET   0.22s user 0.03s system 15% cpu 1.567 total

# Results [ Ingest (~6.3s), Chat (~1.6s) ]
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
export EMBEDDING_MODEL=text-embedding-ada-002
export SPRING_AI_VECTORSTORE_PGVECTOR_DIMENSIONS=1536
gradle clean build bootRun -Pvector-db-provider=pgvector -Dspring.profiles.active=docker,alting,arize-phoenix,pgvector,dev

# Ingest US senators via /api/fetch

❯ time http --verify=no POST :8080/api/fetch urls:='["https://www.govtrack.us/api/v2/role?current=true&role_type=senator"]'
HTTP/1.1 200 
Connection: keep-alive
Content-Type: application/json
Date: Wed, 04 Dec 2024 04:14:49 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked

{
    "failureCount": 0,
    "results": [
        {
            "error": null,
            "savedPath": "/tmp/fetch/2024.12.03.20.14.44/www.govtrack.us-api-v2-role.json",
            "success": true,
            "url": "https://www.govtrack.us/api/v2/role?current=true&role_type=senator"
        }
    ],
    "successCount": 1,
    "totalUrls": 1
}


http --verify=no POST :8080/api/fetch   0.26s user 0.04s system 5% cpu 5.808 total

# Search for US senators in a particular state via /api/multichat

❯ time http --verify=no :8080/api/multichat q=="Who are the senators from Washington state?"
HTTP/1.1 200 
Connection: keep-alive
Content-Type: application/json
Date: Wed, 04 Dec 2024 04:17:12 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked

[
    {
        "content": "I don't have sufficient information to provide the names of the senators from Washington state. The context only mentions Senator Patty Murray from Washington without mentioning the other senator.",
        "errorMessage": null,
        "generationTokens": 33,
        "modelName": "neversleep/llama-3.1-lumimaid-70b",
        "promptTokens": 3309,
        "responseTime": "9s826ms",
        "success": true,
        "totalTokens": 3342
    },
    {
        "content": "\n The senators from Washington state are Patty Murray and Maria Cantwell, both Democrats.",
        "errorMessage": null,
        "generationTokens": 21,
        "modelName": "gryphe/mythomax-l2-13b",
        "promptTokens": 2879,
        "responseTime": "2s960ms",
        "success": true,
        "totalTokens": 2900
    },
    {
        "content": " The senators from Washington state are Sen. Patty Murray [D-WA] and Sen. Maria Cantwell [D-WA]. The context includes Sen. Patty Murray, whose information matches the given description. Also, based on the list of senators, I can infer that Sen. Maria Cantwell is the other senator from Washington state. Please note that this information is derived solely from the provided context and not from any external knowledge.",
        "errorMessage": null,
        "generationTokens": 91,
        "modelName": "mistralai/mixtral-8x7b-instruct",
        "promptTokens": 4670,
        "responseTime": "4s750ms",
        "success": true,
        "totalTokens": 4761
    },
    {
        "content": null,
        "errorMessage": "Error while extracting response for type [org.springframework.ai.openai.api.OpenAiApi$ChatCompletion] and content type [application/json]",
        "generationTokens": null,
        "modelName": "qwen/qwen-2.5-7b-instruct",
        "promptTokens": null,
        "responseTime": null,
        "success": false,
        "totalTokens": null
    },
    {
        "content": "The current U.S. Senators from Washington state are:\n\n1. **Patty Murray (D)** - Senior Senator, serving since 1993[1][4][5].\n2. **Maria Cantwell (D)** - Junior Senator, serving since 2001[1][4][5].\n\nThese sources provide the names and tenure of the current senators representing Washington state in the U.S. Senate.",
        "errorMessage": null,
        "generationTokens": 82,
        "modelName": "perplexity/llama-3.1-sonar-huge-128k-online",
        "promptTokens": 3293,
        "responseTime": "8s904ms",
        "success": true,
        "totalTokens": 3375
    },
    {
        "content": "Based on the provided information, the current senators from Washington state are:\n\n1. Patty Murray (D-WA), Senior Senator, Class 3, Term ending 2029-01-03\n   - Phone: 202-224-2621\n   - Website: https://www.murray.senate.gov\n   - Twitter: @PattyMurray\n   - Leadership Title: President Pro Tempore of the Senate\n\n2. Maria Cantwell (D-WA), Junior Senator, Class 2, Term ending 2027-01-03\n   - Phone: 202-224-3441\n   - Website: https://www.cantwell.senate.gov\n   - Twitter: @SenatorCantwell\n\nThe information is sourced from the Congress.gov database and is current as of the 118th Congress.",
        "errorMessage": null,
        "generationTokens": 0,
        "modelName": "anthracite-org/magnum-v4-72b",
        "promptTokens": 0,
        "responseTime": "35s458ms",
        "success": true,
        "totalTokens": 0
    },
    {
        "content": "The senators from Washington state are:\n\n1. **Patty Murray** (Senior Senator, Democrat)\n   - Start Date: January 3, 2023\n   - End Date: January 3, 2029\n   - Website: [www.murray.senate.gov](https://www.murray.senate.gov)\n\n2. **Maria Cantwell** (not mentioned in the provided context, but she is also a senator from Washington state). \n\nPlease note that Maria Cantwell's details are not included in the provided information.",
        "errorMessage": null,
        "generationTokens": 111,
        "modelName": "openai/gpt-4o-mini",
        "promptTokens": 3369,
        "responseTime": "4s613ms",
        "success": true,
        "totalTokens": 3480
    },
    {
        "content": null,
        "errorMessage": "Error while extracting response for type [org.springframework.ai.openai.api.OpenAiApi$ChatCompletion] and content type [application/json]",
        "generationTokens": null,
        "modelName": "anthropic/claude-3.5-sonnet-20240620:beta",
        "promptTokens": null,
        "responseTime": null,
        "success": false,
        "totalTokens": null
    },
    {
        "content": null,
        "errorMessage": "Error while extracting response for type [org.springframework.ai.openai.api.OpenAiApi$ChatCompletion] and content type [application/json]",
        "generationTokens": null,
        "modelName": "google/gemini-pro-1.5",
        "promptTokens": null,
        "responseTime": null,
        "success": false,
        "totalTokens": null
    },
    {
        "content": "Based on the provided context, the Senior Senator for Washington state is Patty Murray. She is a Democrat and her current term ends on January 3, 2029. The context states:\n\n\"{caucus=null, congress_numbers=[118, 119, 120], current=true, description=Senior Senator for Washington, district=null, enddate=2029-01-03, extra={address=154 Russell Senate Office Building Washington DC 20510, contact_form=https://www.murray.senate.gov/write-to-patty/, office=154 Russell Senate Office Building, rss_url=http://www.murray.senate.gov/public/?a=rss.feed}, leadership_title=President Pro Tempore of the Senate, party=Democrat, person={bioguideid=M001111, birthday=1950-10-11, cspanid=25277, fediverse_webfinger=null, firstname=Patty, gender=female, gender_label=Female, lastname=Murray, link=https://www.govtrack.us/congress/members/patty_murray/300076, middlename=, name=Sen. Patty Murray [D-WA], namemod=, nickname=, osid=N00007876, pvsid=null, sortname=Murray, Patty (Sen.) [D-WA], twitterid=PattyMurray, youtubeid=SenatorPattyMurray}, phone=202-224-2621, role_type=senator, role_type_label=Senator, senator_class=class3, senator_class_label=Class 3, senator_rank=senior, senator_rank_label=Senior, startdate=2023-01-03, state=WA, title=Sen., title_long=Senator, website=https://www.murray.senate.gov}\"\n\nThe Junior Senator for Washington state is not mentioned in the given context. Therefore, I do not have sufficient information to identify the Junior Senator for Washington state.",
        "errorMessage": null,
        "generationTokens": 0,
        "modelName": "nousresearch/hermes-3-llama-3.1-405b",
        "promptTokens": 0,
        "responseTime": "21s178ms",
        "success": true,
        "totalTokens": 0
    },
    {
        "content": " Based on the provided context, the senators from Washington state are Patty Murray (Democrat) and Maria Cantwell (Democrat).",
        "errorMessage": null,
        "generationTokens": 31,
        "modelName": "pygmalionai/mythalion-13b",
        "promptTokens": 4689,
        "responseTime": "5s690ms",
        "success": true,
        "totalTokens": 4720
    },
    {
        "content": "The senior senator for Washington state is **Sen. Patty Murray [D-WA]** (Context: {caucus=null, congress_numbers=[118, 119, 120], current=true, description=Senior Senator for Washington, district=null, enddate=2029-01-03, leadership_title=President Pro Tempore of the Senate, party=Democrat, person={bioguideid=M001111, birthday=1950-10-11, cspanid=25277, fediverse_webfinger=null, firstname=Patty, gender=female, gender_label=Female, lastname=Murray, link=https://www.govtrack.us/congress/members/patty_murray/300076, middlename=, name=Sen. Patty Murray [D-WA], namemod=, nickname=, osid=N00007876, pvsid=null, sortname=Murray, Patty (Sen.) [D-WA], twitterid=PattyMurray, youtubeid=SenatorPattyMurray}, phone=202-224-2621, role_type=senator, role_type_label=Senator, senator_class=class3, senator_class_label=Class 3, senator_rank=senior, senator_rank_label=Senior, startdate=2023-01-03, state=WA, title=Sen., title_long=Senator, website=https://www.murray.senate.gov}). However, the context does not provide information on who the junior senator from Washington state is.",
        "errorMessage": null,
        "generationTokens": 353,
        "modelName": "x-ai/grok-beta",
        "promptTokens": 3864,
        "responseTime": "8s294ms",
        "success": true,
        "totalTokens": 4217
    }
]


http --verify=no :8080/api/multichat   0.25s user 0.05s system 0% cpu 1:55.64 total

# Results [ Ingest (~6s), MultiChat (~2m) returning 8 accurate responses, 1 partial response, and 3 errors due to deserialization issues ]
```

### Amazon Bedrock

Anthropic3 + Cohere
```commandline
export BEDROCK_CONVERSE_CHAT_ENABLED=false
export BEDROCK_ANTHROPIC3_CHAT_ENABLED=true
export CHAT_MODEL=anthropic.claude-3-sonnet-20240229-v1:0
export BEDROCK_COHERE_EMBEDDING_ENABLED=true
export EMBEDDING_MODEL=cohere.embed-english-v3
gradle build bootRun -Dspring.profiles.active=docker,bedrock,chroma,dev -Pmodel-api-provider=bedrock -Pvector-db-provider=chroma

# Ingest US senators via /api/fetch

❯ time http --verify=no POST :8080/api/fetch urls:='["https://www.govtrack.us/api/v2/role?current=true&role_type=senator"]'
HTTP/1.1 200 
Connection: keep-alive
Content-Type: application/json
Date: Tue, 10 Dec 2024 04:49:55 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked

{
    "failureCount": 0,
    "results": [
        {
            "error": null,
            "savedPath": "/tmp/fetch/2024.12.09.20.49.52/www.govtrack.us-api-v2-role.json",
            "success": true,
            "url": "https://www.govtrack.us/api/v2/role?current=true&role_type=senator"
        }
    ],
    "successCount": 1,
    "totalUrls": 1
}


http --verify=no POST :8080/api/fetch   0.23s user 0.05s system 7% cpu 3.651 total


# Search for US senators in a particular state via /api/chat

❯ time http GET 'http://localhost:8080/api/chat?q="Who are the US senators from Washington?"&f[state]="WA"&f[gender]="female"'
HTTP/1.1 200 
Connection: keep-alive
Content-Length: 162
Content-Type: text/plain;charset=UTF-8
Date: Tue, 10 Dec 2024 04:50:11 GMT
Keep-Alive: timeout=60

The US senators from Washington are Maria Cantwell (Democrat, Junior Senator) and Patty Murray (Democrat, Senior Senator and President Pro Tempore of the Senate).


http GET   0.24s user 0.04s system 12% cpu 2.294 total

# Results [ Ingest (~3.7s), Chat (~2.3s) ]
```

### Google Cloud Vertex AI

```commandline
export PROJECT=pa-dpatel
export REGION=us-west-2
export CHAT_MODEL=gemini-1.5-flash-002
export EMBEDDING_MODEL=text-embedding-005
gradle build bootRun -Dspring.profiles.active=docker,gemini,chroma,dev -Pmodel-api-provider=gemini -Pvector-db-provider=chroma

# Ingest US senators via /api/fetch

❯ time http --verify=no POST :8080/api/fetch urls:='["https://www.govtrack.us/api/v2/role?current=true&role_type=senator"]'
HTTP/1.1 200 
Connection: keep-alive
Content-Type: application/json
Date: Tue, 10 Dec 2024 06:19:12 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked

{
    "failureCount": 0,
    "results": [
        {
            "error": null,
            "savedPath": "/tmp/fetch/2024.12.09.22.19.07/www.govtrack.us-api-v2-role.json",
            "success": true,
            "url": "https://www.govtrack.us/api/v2/role?current=true&role_type=senator"
        }
    ],
    "successCount": 1,
    "totalUrls": 1
}


http --verify=no POST :8080/api/fetch   0.26s user 0.04s system 5% cpu 5.240 total


# Search for US senators in a particular state via /api/chat

❯ time http GET 'http://localhost:8080/api/chat?q="Who are the US senators from Washington?"&f[state]="WA"&f[gender]="female"'
HTTP/1.1 200 
Connection: keep-alive
Content-Length: 69
Content-Type: text/plain;charset=UTF-8
Date: Tue, 10 Dec 2024 06:19:23 GMT
Keep-Alive: timeout=60

The US Senators from Washington are Patty Murray and Maria Cantwell.


http GET   0.25s user 0.03s system 7% cpu 3.791 total

# Results [ Ingest (~5.2s), Chat (~3.8s) ]
```

### Ollama on workstation

Specs:

* OS - Ubuntu 24.04.1 LTS
* Hardware Model - System76 [Meerkat](https://system76.com/desktops/meerkat/)
* Processor - Intel Core i7-10710U x 12
* Memory - 64.0 GiB
* Disk Capacity - 2.5 TB

CPU-only!

Test 1

```commandline
export CHAT_MODEL=qwen2.5:3b
export EMBEDDING_MODEL=aroxima/gte-qwen2-1.5b-instruct
export SPRING_AI_VECTORSTORE_PGVECTOR_DIMENSIONS=1536 
gradle clean build bootRun -Pmodel-api-provider=ollama -Pvector-db-provider=pgvector -Dspring.profiles.active=docker,ollama,arize-phoenix,pgvector,dev

# Ingest US senators via /api/fetch

❯ time http --verify=no POST :8080/api/fetch urls:='["https://www.govtrack.us/api/v2/role?current=true&role_type=senator"]'
HTTP/1.1 200 
Connection: keep-alive
Content-Type: application/json
Date: Thu, 05 Dec 2024 19:32:25 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked

{
    "failureCount": 0,
    "results": [
        {
            "error": null,
            "savedPath": "/tmp/fetch/2024.12.05.11.09.32/www.govtrack.us-api-v2-role.json",
            "success": true,
            "url": "https://www.govtrack.us/api/v2/role?current=true&role_type=senator"
        }
    ],
    "successCount": 1,
    "totalUrls": 1
}


http --verify=no POST :8080/api/fetch   0.27s user 0.03s system 0% cpu 22:53.42 total

# Search for US senators in a particular state via /api/chat

❯ time http GET 'http://localhost:8080/api/chat?q="Who are the US senators from Washington?"&f[state]="WA"&f[gender]="female"'
HTTP/1.1 200 
Connection: keep-alive
Content-Length: 45
Content-Type: text/plain;charset=UTF-8
Date: Thu, 05 Dec 2024 19:37:50 GMT
Keep-Alive: timeout=60

Patty Murray is a US senator from Washington.


http GET   0.29s user 0.05s system 0% cpu 3:53.99 total


# Results [ Ingest (~23m), Chat (~4m), partially correct response ]
# Embedding model consumption peaked at 2.65Gb of RAM
# Chat model consumption peaked at 22.7Gb of RAM
```

Test 2

```commandline
export CHAT_MODEL=wizardlm2
export EMBEDDING_MODEL=all-minilm:33m
export SPRING_AI_VECTORSTORE_PGVECTOR_DIMENSIONS=384
gradle clean build bootRun -Pvector-db-provider=pgvector -Pmodel-api-provider=ollama -Dspring.profiles.active=docker,ollama,pgvector,dev

❯ time http --verify=no POST :8080/api/fetch urls:='["https://www.govtrack.us/api/v2/role?current=true&role_type=senator"]'
HTTP/1.1 200 
Connection: keep-alive
Content-Type: application/json
Date: Fri, 06 Dec 2024 01:47:38 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked

{
    "failureCount": 0,
    "results": [
        {
            "error": null,
            "savedPath": "/tmp/fetch/2024.12.05.17.47.27/www.govtrack.us-api-v2-role.json",
            "success": true,
            "url": "https://www.govtrack.us/api/v2/role?current=true&role_type=senator"
        }
    ],
    "successCount": 1,
    "totalUrls": 1
}


http --verify=no POST :8080/api/fetch   0.24s user 0.03s system 2% cpu 11.328 total
❯ time http GET 'http://localhost:8080/api/chat?q="Who are the US senators from Washington?"&f[state]="WA"&f[gender]="female"'
HTTP/1.1 200 
Connection: keep-alive
Content-Length: 134
Content-Type: text/plain;charset=UTF-8
Date: Fri, 06 Dec 2024 01:56:09 GMT
Keep-Alive: timeout=60

The U.S. Senators from Washington (state) as of the context information provided are:

1. Maria Cantwell (D-WA)
2. Patty Murray (D-WA)


http GET   0.24s user 0.03s system 0% cpu 8:15.35 total
```

### Tanzu Application Service 6.0 with GenAI 10 tile

Operating environment: [VMware vCenter Server 8.0 Update 3](https://docs.vmware.com/en/VMware-vSphere/8.0/rn/vsphere-vcenter-server-803-release-notes/index.html)

Sanford was deployed using [deploy-on-tp4cf.sh](../scripts/deploy-on-tp4cf.sh) script.

Infrastructure configuration for model hosting via the [GenAI tile](https://techdocs.broadcom.com/us/en/vmware-tanzu/platform-services/genai-on-tanzu-platform-for-cloud-foundry/10-0/ai-cf/how-to-guides-how-to-guides.html)

* 14 vCPU ([Intel Xeon E5v4](https://www.intel.com/content/www/us/en/products/sku/91770/intel-xeon-processor-e52690-v4-35m-cache-2-60-ghz/specifications.html))
* 32Gb RAM

CPU-only!

```commandline
# Before deploying sanford the aforementioned script was edited to have:
# GENAI_CHAT_PLAN_NAME="phi3.5"
# GENAI_EMBEDDINGS_PLAN_NAME="all-minilm:33m"
# These models were sourced then provisioned to run on Tanzu Application Service 6 via the GenAI 10 tile

# Ingest US senators via /api/fetch

➜  ~ time http --verify=no POST https://sanford-accountable-baboon-ls.apps.tas-cdc.kuhn-labs.com/api/fetch urls:='["https://www.govtrack.us/api/v2/role?current=true&role_type=senator"]'

HTTP/1.1 200 OK
Content-Type: application/json
Date: Fri, 06 Dec 2024 18:18:21 GMT
Transfer-Encoding: chunked
X-Vcap-Request-Id: fce95946-1577-4753-6d2f-1bc115986294

{
    "failureCount": 0,
    "results": [
        {
            "error": null,
            "savedPath": "/home/vcap/tmp/fetch/2024.12.06.18.18.12/www.govtrack.us-api-v2-role.json",
            "success": true,
            "url": "https://www.govtrack.us/api/v2/role?current=true&role_type=senator"
        }
    ],
    "successCount": 1,
    "totalUrls": 1
}


http --verify=no POST    0.33s user 0.16s system 4% cpu 10.468 total

# Search for US senators in a particular state via /api/chat

➜  ~ time http GET 'https://sanford-accountable-baboon-ls.apps.tas-cdc.kuhn-labs.com/api/chat?q="Who are the US senators from Washington?"&f[state]="WA"&f[gender]="female"'

HTTP/1.1 200 OK
Content-Length: 54
Content-Type: text/plain;charset=UTF-8
Date: Fri, 06 Dec 2024 18:20:03 GMT
X-Vcap-Request-Id: b5d0ce0b-4e95-49db-6dee-cbf3b41ac36e

John McCain (Republican) and Maria Cantwell (Democrat)


http GET   0.33s user 0.13s system 0% cpu 1:20.22 total

# Results [ Ingest (~10.5s), Chat (~1m20s), partially correct response ]
```

And with an alternate model combination...

```commandline
# Before deploying sanford the aforementioned script was edited to have:
# GENAI_CHAT_PLAN_NAME="wizardlm2"
# GENAI_EMBEDDINGS_PLAN_NAME="all-minilm:33m"
# These models were sourced then provisioned to run on Tanzu Application Service 6 via the GenAI 10 tile

➜  ~ time http --verify=no POST https://sanford-accountable-baboon-ls.apps.tas-cdc.kuhn-labs.com/api/fetch urls:='["https://www.govtrack.us/api/v2/role?current=true&role_type=senator"]'

HTTP/1.1 200 OK
Content-Type: application/json
Date: Fri, 06 Dec 2024 20:58:51 GMT
Transfer-Encoding: chunked
X-Vcap-Request-Id: 9ec10539-e410-4044-58a9-80e1029b5bc4

{
    "failureCount": 0,
    "results": [
        {
            "error": null,
            "savedPath": "/home/vcap/tmp/fetch/2024.12.06.20.58.39/www.govtrack.us-api-v2-role.json",
            "success": true,
            "url": "https://www.govtrack.us/api/v2/role?current=true&role_type=senator"
        }
    ],
    "successCount": 1,
    "totalUrls": 1
}


http --verify=no POST    0.33s user 0.17s system 3% cpu 12.727 total
➜  ~ time http GET 'https://sanford-accountable-baboon-ls.apps.tas-cdc.kuhn-labs.com/api/chat?q="Who are the US senators from Washington?"&f[state]="WA"&f[gender]="female"'

HTTP/1.1 200 OK
Content-Length: 185
Content-Type: text/plain;charset=UTF-8
Date: Fri, 06 Dec 2024 21:01:45 GMT
X-Vcap-Request-Id: a12f4738-5f92-4697-6c0b-21363a9272d8

 The US senators from Washington are Patty Murray and Maria Cantwell. They are both Democrats representing Washington in the United States Senate as of my knowledge cutoff date in 2023.


http GET   0.31s user 0.12s system 0% cpu 2:35.66 total
➜  ~ time http GET 'https://sanford-accountable-baboon-ls.apps.tas-cdc.kuhn-labs.com/api/chat?q="Who are the US senators from Washington?"&f[state]="WA"&f[gender]="female"'

HTTP/1.1 200 OK
Content-Length: 227
Content-Type: text/plain;charset=UTF-8
Date: Fri, 06 Dec 2024 21:06:00 GMT
X-Vcap-Request-Id: 37daee3a-f369-4b82-4e8a-584a5a9e6255

 The US senators from Washington (state) are Patty Murray and Maria Cantwell. They have been representing Washington in the U.S. Senate since 1993 and 2001, respectively. Their terms are not set to expire until January 3, 2027.


http GET   0.33s user 0.13s system 0% cpu 2:44.90 total

# Results [ Ingest (~12m7s), Chat (~2m45s), correct response ]
# Embedding model consumption peaked at 517m of RAM
# Chat model consumption peaked at 5.7Gb of RAM
```



