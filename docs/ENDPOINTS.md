# Sanford

* [Endpoints](#endpoints)
  * [Upload](#upload)
  * [Crawl](#crawl)
  * [Fetch](#fetch)
  * [Chat](#chat)
  * [Multichat](#multichat)
  * [Get Metadata](#get-metadata)
  * [Search](#search)
  * [Summarize](#summarize)
  * [Download](#download)
  * [Delete](#delete)

## Endpoints

### Upload

Upload a file to an S3-compliant object store's bucket; in addition the contents of the file will be vectorized and persisted into a [Vector Database](https://docs.spring.io/spring-ai/reference/api/vectordbs.html) provider.

```python
POST /api/files/upload
```
**Sample interaction**

```bash
❯ http -f POST :8080/api/files/upload fileName@./samples/United_States_Constitution.pdf
HTTP/1.1 200
Connection: keep-alive
Content-Type: application/json
Date: Wed, 25 Sep 2024 18:14:29 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked

{
    "contentType": "application/pdf",
    "fileExtension": "pdf",
    "fileName": "United_States_Constitution.pdf",
    "objectId": "da7c93d2-5681-4f9b-919a-bb1997bb7552"
}
```

#### Troubleshooting

If you see the following in your application log output

```bash
08:47:53.952 [tomcat-handler-2] DEBUG o.s.web.servlet.DispatcherServlet - Completed 413 PAYLOAD_TOO_LARGE
08:47:53.955 [tomcat-handler-2] DEBUG o.s.web.servlet.DispatcherServlet - "ERROR" dispatch for POST "/error", parameters={multipart}
08:47:53.955 [tomcat-handler-2] WARN  o.s.w.s.m.s.DefaultHandlerExceptionResolver - Resolved [org.springframework.web.multipart.MaxUploadSizeExceededException: Maximum upload size exceeded
```

You will need to adjust startup arguments, e.g., you could add the following to `gradle bootRun`

```bash
-Dspring.servlet.multipart.max-file-size=250MB
```

### Crawl

Facilitate web-crawling (i.e., fetch HTML pages and any hyperlinks to HTML pages within them).  Users may issue a [CrawlRequest](../src/main/java/org/cftoolsuite/domain/crawl/CrawlRequest.java) and each document found will be (a) uploaded to S3-compliant object store and (b) contents will be vectorized and persisted into a Vector Database provider.

```python
POST /api/crawl
```

**Sample interaction**

```bash
❯ http POST :8080/api/crawl rootDomain="https://docs.vmware.com/en/VMware-Tanzu-Platform/SaaS/" seeds:='["https://docs.vmware.com/en/VMware-Tanzu-Platform/SaaS/create-manage-apps-tanzu-platform-k8s/"]' maxDepthOfCrawling=5
HTTP/1.1 202
Connection: keep-alive
Content-Type: application/json
Date: Thu, 24 Oct 2024 13:38:24 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked

{
    "id": "1",
    "result": "Accepted",
    "storageFolder": "/tmp/crawler4j/1"
}
```

### Fetch

Facilitate fetching content from one or more URLs.  Users may issue a [FetchRequest](../src/main/java/org/cftoolsuite/domain/fetch/FetchRequest.java) and each document found will be (a) uploaded to S3-compliant object store and (b) contents will be vectorized and persisted into a Vector Database provider.

```python
POST /api/fetch
```

**Sample interaction**

```bash
❯ http POST :8080/api/fetch urls:='["https://www.govtrack.us/api/v2/role?current=true&role_type=senator"]'
HTTP/1.1 200
Connection: keep-alive
Content-Type: application/json
Date: Mon, 18 Nov 2024 15:10:10 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked

{
    "failureCount": 0,
    "results": [
        {
            "error": null,
            "savedPath": "/tmp/fetch/2024.11.18.07.10.03/www.govtrack.us-api-v2-role.json",
            "success": true,
            "url": "https://www.govtrack.us/api/v2/role?current=true&role_type=senator"
        }
    ],
    "successCount": 1,
    "totalUrls": 1
}
```

### Chat

Converse with an AI chatbot who is aware of all uploaded content.  Ask a question, get a response.

```python
POST /api/chat
```

**Sample interaction**

```bash
❯ http POST http://localhost:8080/api/chat \
  Content-Type:application/json \
  question="Tell me something about the character Hermia from A Midsummer Night's Dream"
HTTP/1.1 200
Connection: keep-alive
Content-Length: 795
Content-Type: text/plain;charset=UTF-8
Date: Tue, 22 Oct 2024 12:24:58 GMT
Keep-Alive: timeout=60

Hermia is a key character in "A Midsummer Night's Dream," portrayed as the daughter of Egeus. She is in love with Lysander, but her father wishes her to marry Demetrius, which creates conflict in the story. Hermia is depicted as strong-willed and defiant; she boldly asserts her feelings and desires, expressing her wish that her father could see things from her perspective (Act I).

She passionately defends her love for Lysander and is determined to be with him despite the obstacles posed by her father and societal expectations. Hermia's character embodies themes of love, rebellion, and the struggle for autonomy within the constraints of Athenian law. Her determination to follow her heart leads her to plan an escape with Lysander, showcasing her bravery and commitment to love (Act I).
```

You may also optionally specify filter metadata in your request, e.g.

```bash
❯ http POST http://localhost:8080/api/chat \
  Content-Type:application/json \
  question="Who are the senators from Minnesota" \
  filter:='[
    {"key": "gender", "value": "male"},
    {"key": "state", "value": "MN"}
  ]'
```

### Multichat

Converse with an AI chatbot who is aware of all uploaded content.  Ask a question, get multiple responses from various pre-configured models.
(This endpoint is only available when the `alting` Spring profile is activated).  Consult `spring.ai.alting.chat.options.models` in [application.yml](../src/main/resources/application.yml) for models participating in each request.

```python
POST /api/multichat
```
**Sample interaction**

As with chat, multichat allows you to optionally supply filter metadata...

```bash
❯ http POST http://localhost:8080/api/multichat \
  Content-Type:application/json \
  question="Who are the US senators from Washington?" \
  filter:='[
    {"key": "gender", "value": "female"},
    {"key": "state", "value": "WA"}
  ]'
HTTP/1.1 200 
Connection: keep-alive
Content-Type: application/json
Date: Thu, 05 Dec 2024 17:44:04 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked

[
    {
        "content": "The US senators from Washington are Patty Murray and Maria Cantwell.",
        "errorMessage": null,
        "generationTokens": 14,
        "modelName": "neversleep/llama-3.1-lumimaid-70b",
        "promptTokens": 3230,
        "responseTime": "8s19ms",
        "success": true,
        "totalTokens": 3244
    },
    {
        "content": " The two US senators from Washington are Patty Murray and Maria Cantwell.",
        "errorMessage": null,
        "generationTokens": 16,
        "modelName": "gryphe/mythomax-l2-13b",
        "promptTokens": 2989,
        "responseTime": "4s47ms",
        "success": true,
        "totalTokens": 3005
    },
    {
        "content": " The US senators from Washington are Sen. Patty Murray and Sen. Maria Cantwell.",
        "errorMessage": null,
        "generationTokens": 19,
        "modelName": "mistralai/mixtral-8x7b-instruct",
        "promptTokens": 4592,
        "responseTime": "2s833ms",
        "success": true,
        "totalTokens": 4611
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
        "content": "The US senators from Washington are:\n- Senior Senator: Patty Murray (Democrat)\n- Junior Senator: Not mentioned in the provided context, but according to external knowledge, it is Maria Cantwell. However, following the rules: I don't know who the junior senator is based on the given context.",
        "errorMessage": null,
        "generationTokens": 61,
        "modelName": "perplexity/llama-3.1-sonar-huge-128k-online",
        "promptTokens": 3219,
        "responseTime": "11s634ms",
        "success": true,
        "totalTokens": 3280
    },
    {
        "content": "The U.S. senators from Washington are:\n\nPatty Murray (D-WA) - Senior Senator \nCory Booker (D-NJ) - Junior Senator\n\nHowever, there seems to be an error in the provided information. Cory Booker is not a senator from Washington, he is the senior senator from New Jersey. Patty Murray is indeed the senior senator from Washington. The junior senator from Washington is not mentioned in the given context.",
        "errorMessage": null,
        "generationTokens": 88,
        "modelName": "anthracite-org/magnum-v4-72b",
        "promptTokens": 3739,
        "responseTime": "11s623ms",
        "success": true,
        "totalTokens": 3827
    },
    {
        "content": "The US senators from Washington are Sen. Patty Murray [D-WA] and Sen. Maria Cantwell [D-WA].",
        "errorMessage": null,
        "generationTokens": 26,
        "modelName": "openai/gpt-4o-mini",
        "promptTokens": 3293,
        "responseTime": "3s9ms",
        "success": true,
        "totalTokens": 3319
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
        "content": "The US senators from Washington are:\n1. Senior Senator Patty Murray (Democrat)\n2. Junior Senator Maria Cantwell (Democrat)",
        "errorMessage": null,
        "generationTokens": 0,
        "modelName": "nousresearch/hermes-3-llama-3.1-405b",
        "promptTokens": 0,
        "responseTime": "3s575ms",
        "success": true,
        "totalTokens": 0
    },
    {
        "content": "Patty Murray (D) and Marco Rubio (R) are the US senators from Washington.",
        "errorMessage": null,
        "generationTokens": 22,
        "modelName": "pygmalionai/mythalion-13b",
        "promptTokens": 4628,
        "responseTime": "4s900ms",
        "success": true,
        "totalTokens": 4650
    },
    {
        "content": "The US senators from Washington are:\n- **Patty Murray** [D-WA], who is the Senior Senator and holds the leadership title of President Pro Tempore of the Senate.\n- **The Junior Senator's information is not provided in the given context.**",
        "errorMessage": null,
        "generationTokens": 53,
        "modelName": "x-ai/grok-beta",
        "promptTokens": 3787,
        "responseTime": "4s7ms",
        "success": true,
        "totalTokens": 3840
    }
]
```

### Get Metadata

Retrieve metadata for all files in an S3-compliant object store's bucket

```python
GET /api/files
```

**Sample interaction**

```bash
❯ http :8080/api/files
HTTP/1.1 200
Connection: keep-alive
Content-Type: application/json
Date: Wed, 25 Sep 2024 19:26:36 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked

[
    {
        "contentType": "application/pdf",
        "fileExtension": "pdf",
        "fileName": "United_States_Constitution.pdf",
        "objectId": "504333ba-32a2-4f99-a9c0-1b820ef0ddf8"
    }
]
```

Retrieve the metadata for a single file

```python
GET /api/files?filename={unique_filename}
```

> Replace `{unique_filename}` above with the name of a file already stored in an S3-compliant object store's bucket

**Sample interaction**

```bash
❯ http :8080/api/files fileName==United_States_Constitution.pdf
HTTP/1.1 200
Connection: keep-alive
Content-Type: application/json
Date: Wed, 25 Sep 2024 19:28:04 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked

[
    {
        "contentType": "application/pdf",
        "fileExtension": "pdf",
        "fileName": "United_States_Constitution.pdf",
        "objectId": "504333ba-32a2-4f99-a9c0-1b820ef0ddf8"
    }
]
```

### Search

Similarity search for file metadata matching your query constraints.  (A maximum of up to 10 matching results will be returned).

```python
GET /api/files/search?q="{your_inquiry}"
```

> Replace `{your_inquiry}` above with any topic, concern, or criteria you desire to perform a similarity search 

**Sample interaction**

```bash
❯ http :8080/api/files/search q=="Where can I find information about the 24th amendment of the US Constitution"
HTTP/1.1 200
Connection: keep-alive
Content-Type: application/json
Date: Wed, 25 Sep 2024 19:36:07 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked

[
    {
        "contentType": "application/pdf",
        "fileExtension": "pdf",
        "fileName": "United_States_Constitution.pdf",
        "objectId": "a6d1b0a1-9cab-41ff-971e-f5de64774698"
    }
]
```

### Summarize

Summarize the file content

```python
GET /api/files/summarize/{fileName}
```

> Replace `{fileName}` above with the name of a file already stored in an S3-compliant object store's bucket

Sample interaction

```bash
❯ http :8080/api/files/summarize/Midsummer_Night%27s_Dream_Entire_Play.html
HTTP/1.1 200
Connection: keep-alive
Content-Disposition: inline;filename=f.txt
Content-Length: 1169
Content-Type: text/plain;charset=UTF-8
Date: Thu, 26 Sep 2024 01:54:00 GMT
Keep-Alive: timeout=60

**Summary of "A Midsummer Night's Dream"**

- **Setting:** The play begins in Athens with Duke Theseus preparing for his wedding to Hippolyta.
- **Main Conflict:** Egeus seeks the Duke’s support to enforce his decision that his daughter Hermia must marry Demetrius, despite her love for Lysander.
- **Themes of Love:** The complexities of love and its irrational nature are explored as Hermia and Lysander plan to elope, while Helena, in love with Demetrius, seeks his affection.
- **Interwoven Stories:** A group of Athenian tradesmen prepare a play for the Duke's wedding, adding a comedic subplot.
- **Magical Elements:** The fairy realm, led by Oberon and Titania, introduces enchantments that complicate the lovers' relationships, leading to humorous misunderstandings.
- **Resolution:** The chaos resolves with the intervention of magic, resulting in the correct pairings of lovers and concluding with multiple weddings.
- **Finale:** The play ends with a reflection on dreams and imagination, with Puck suggesting that the events may have been merely a dream.

This summary captures the central plot, themes, and characters without delving into minor details.
```

### Download

Download the file content from an S3-compliant object store's bucket

```python
GET /api/files/download/{fileName}
```

> Replace `{fileName}` above with the name of a file already stored in an S3-compliant object store's bucket

Sample interaction

```bash
❯ http --download :8080/api/files/download/United_States_Constitution.pdf
HTTP/1.1 200
Connection: keep-alive
Content-Disposition: attachment; filename="United_States_Constitution.pdf"
Content-Type: application/json
Date: Wed, 25 Sep 2024 19:55:18 GMT
Keep-Alive: timeout=60
Transfer-Encoding: chunked

Downloading to
United_States_Constitution.pdf
Done. 627.4 kB in 00:0.08617 (7.3 MB/s)

❯ ls -la *.pdf
-rw-rw-r-- 1 cphillipson cphillipson 627430 Sep 25 12:55 United_States_Constitution.pdf
```

### Delete

Delete a file in an S3-compliant object store's bucket. As well, prune file metadata from Vector store.

```python
DELETE /api/files/{fileName}
```

> Replace `{fileName}` above with the name of a file already stored in an S3-compliant object store's bucket

Sample interaction

```bash
❯ http DELETE :8080/api/files/United_States_Constitution.pdf
HTTP/1.1 204
Connection: keep-alive
Date: Tue, 01 Oct 2024 17:32:00 GMT
Keep-Alive: timeout=60
```
