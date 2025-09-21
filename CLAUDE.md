# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Architecture Overview

Sanford is a Spring Boot application that leverages AI/LLM capabilities, vector stores, and file storage to provide document ingestion, search, and chat functionality. The architecture consists of:

- **Spring Boot Application**: Main application entry point with configurable AI providers and vector stores
- **AI/LLM Integration**: Supports multiple providers (OpenAI, Groq, Bedrock, Ollama, Gemini) via Spring AI
- **Vector Store Support**: Pluggable vector databases (Chroma, PgVector, Redis, Weaviate, Gemfire)
- **File Storage**: MinIO for document storage and retrieval
- **Document Processing**: Handles multiple file formats (PDF, Word, HTML, CSV, etc.)
- **Web Crawling**: Custom web crawler for content extraction
- **REST API**: Controllers for chat, document upload, and multi-chat functionality

## Key Components

### Controllers
- `ChatController`: Basic chat functionality with streaming support
- `MultiChatController`: Multi-model chat capabilities
- `DocumentController`: Document upload, search, and summarization
- `ConverseController`: Amazon Bedrock conversation API
- `WebCrawlController`: Web crawling functionality
- `FetchController`: URL content fetching

### Services
- `ChatService`: Core chat functionality
- `MultiChatService`: Multi-model chat orchestration
- `DocumentIngestionService`: Document processing and vector store ingestion
- `DocumentSearchService`: Vector similarity search
- `DocumentSummarizationService`: Document summarization
- `FileService`: File storage abstraction with MinIO implementation

### Configuration
- Extensive Spring profiles for different AI providers and vector stores
- Conditional dependencies based on Gradle properties
- Docker Compose integration for development

## Common Commands

### Build Commands
```bash
# Basic build
./gradlew clean build

# Build with specific vector store
./gradlew clean build -Pvector-db-provider=chroma
./gradlew clean build -Pvector-db-provider=pgvector
./gradlew clean build -Pvector-db-provider=redis
./gradlew clean build -Pvector-db-provider=weaviate

# Build with specific AI provider
./gradlew clean build -Pmodel-api-provider=ollama
./gradlew clean build -Pmodel-api-provider=bedrock
./gradlew clean build -Pmodel-api-provider=gemini
```

### Run Commands
```bash
# Basic run with OpenAI and Chroma
./gradlew bootRun -Dspring.profiles.active=docker,openai,chroma -Pvector-db-provider=chroma

# Run with Groq Cloud and PgVector
./gradlew bootRun -Dspring.profiles.active=docker,groq-cloud,pgvector -Pvector-db-provider=pgvector

# Run with Ollama (requires Ollama to be running)
./gradlew bootRun -Dspring.profiles.active=docker,ollama -Pmodel-api-provider=ollama
```

### Test Commands
```bash
# Run all tests
./gradlew test

# Run tests with JUnit Platform
./gradlew test --tests "CustomWebCrawlerTest"
```

### Container Commands
```bash
# Build container image
./gradlew bootBuildImage

# Build with specific version
./gradlew setVersion bootBuildImage -PnewVersion=2024.01.01
```

### Kubernetes Commands
```bash
# Generate Kubernetes manifests
./gradlew k8sResource -Pvector-db-provider=chroma -Pjkube.environment=openai,chroma,observability,minio

# Deploy to Kubernetes
./gradlew k8sApply -Pvector-db-provider=chroma -Pjkube.environment=openai,chroma,observability,minio

# Undeploy from Kubernetes
./gradlew k8sUndeploy -Pvector-db-provider=chroma -Pjkube.environment=openai,chroma,observability,minio
```

## Configuration Requirements

### AI Provider Setup
- **OpenAI**: Requires API key in `config/creds.yml`
- **Groq Cloud**: Requires Groq API key + OpenAI key for embeddings
- **Bedrock**: Requires AWS credentials and region
- **Ollama**: Requires local Ollama installation
- **Gemini**: Requires Google Cloud project setup

### Vector Store Configuration
- **Chroma**: Runs via Docker Compose
- **PgVector**: Requires PostgreSQL with pgvector extension
- **Redis**: Requires Redis Stack
- **Weaviate**: Supports both local and cloud instances

### Spring Profiles
Must activate both:
- Storage profile: `minio` (default)
- AI provider profile: `openai`, `groq-cloud`, `ollama`, `bedrock`, `gemini`
- Vector store profile: `chroma`, `pgvector`, `redis`, `weaviate`
- Environment profile: `docker` (for local development)

## Development Notes

### Gradle Properties
The build system uses conditional dependencies controlled by:
- `-Pmodel-api-provider=`: Controls which AI provider dependencies are included
- `-Pvector-db-provider=`: Controls which vector store dependencies are included
- `-Pjkube.environment=`: Controls Kubernetes manifest generation

### Docker Compose Integration
The application uses Spring Boot Docker Compose support for development:
- Automatically manages vector store containers
- Configures observability stack (Grafana, Prometheus, Zipkin)
- Manages MinIO storage container

### File Processing
Supports multiple file formats with automatic content type detection:
- Documents: PDF, Word (DOC/DOCX), PowerPoint (PPT/PPTX)
- Text: Markdown, HTML, plain text, CSV, TSV
- Structured: JSON, XML

### API Endpoints
Key endpoints available:
- `/api/chat` - Basic chat functionality
- `/api/stream/chat` - Streaming chat responses
- `/api/multichat` - Multi-model chat
- `/api/documents` - Document upload/management
- `/api/crawl` - Web crawling
- `/swagger-ui.html` - API documentation