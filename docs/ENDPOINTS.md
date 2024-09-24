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

### Get Metadata

Retrieve metadata for all files in an S3-compliant object store's bucket

```python
GET
```

Retrieve the metadata for a single file

```python
GET /?f={fileName}
```

> Replace `{fileName}` above with the name of a file already stored in an S3-compliant object store's bucket

### Search

Similarity search for file metadata matching your query constraints.  (A maximum of up to 10 matching results will be returned).

```python
GET /search/?q={your_query}
```

> Replace `{your_query}` above with your query constraint

### Summarize

Summarize the file content

```python
GET /summarize/{fileName}
```

> Replace `{fileName}` above with the name of a file already stored in an S3-compliant object store's bucket

### Download

Download the file content from an S3-compliant object store's bucket

```python
GET /download/{fileName}
```

> Replace `{fileName}` above with the name of a file already stored in an S3-compliant object store's bucket