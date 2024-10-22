# Sanford

* [Endpoints](#endpoints)
  * [Upload](#upload)
  * [Chat](#chat)
  * [Get Metadata](#get-metadata)
  * [Search](#search)
  * [Summarize](#summarize)
  * [Download](#download)
  * [Delete](#delete)

## Endpoints

All endpoints below are to be prefixed with `/api/files`.

### Upload

Upload a file to an S3-compliant object store's bucket

```python
POST /upload
```

Sample interaction

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

### Chat

Converse with an AI chatbot who is aware of all uploaded content.  Ask a question, get a response.

```python
GET /chat
```

**Sample interaction**

```bash
❯ http :8080/api/files/chat q=="Tell me something about the character Hermia from A Midsummer Night's Dream"
HTTP/1.1 200
Connection: keep-alive
Content-Length: 795
Content-Type: text/plain;charset=UTF-8
Date: Tue, 22 Oct 2024 12:24:58 GMT
Keep-Alive: timeout=60

Hermia is a key character in "A Midsummer Night's Dream," portrayed as the daughter of Egeus. She is in love with Lysander, but her father wishes her to marry Demetrius, which creates conflict in the story. Hermia is depicted as strong-willed and defiant; she boldly asserts her feelings and desires, expressing her wish that her father could see things from her perspective (Act I).

She passionately defends her love for Lysander and is determined to be with him despite the obstacles posed by her father and societal expectations. Hermia's character embodies themes of love, rebellion, and the struggle for autonomy within the constraints of Athenian law. Her determination to follow her heart leads her to plan an escape with Lysander, showcasing her bravery and commitment to love (Act I).
```


### Get Metadata

Retrieve metadata for all files in an S3-compliant object store's bucket

```python
GET
```

Sample interaction

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
GET /?fileName={fileName}
```

> Replace `{fileName}` above with the name of a file already stored in an S3-compliant object store's bucket

Sample interaction

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
GET /search/?q={your_query}
```

> Replace `{your_query}` above with your query constraint

Sample interaction

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
GET /summarize/{fileName}
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
GET /download/{fileName}
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
DELETE /{fileName}
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
