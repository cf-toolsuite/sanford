# Sanford

* [Endpoints](#endpoints)
  * [Upload](#upload)
  * [Get Metadata](#get-metadata)
  * [Search](#search)
  * [Summarize](#summarize)
  * [Download](#download)

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
GET /?f={fileName}
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
