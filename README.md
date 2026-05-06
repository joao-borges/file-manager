# File Manager

A modern, full-stack file management application built with Spring Boot and React. This application provides a comprehensive suite of file operations with a clean, responsive UI and enterprise-grade features including security, caching, async processing, and real-time progress updates.

## 🚀 Features

### File Operations
- **Rename Files** - Batch rename files with pattern matching and exclusion rules
- **Organize Files** - Automatically organize files by extension into categorized folders
- **Extract Files** - Recursively extract files from nested subdirectories
- **Photo Organization** - Organize photos/videos by date using EXIF metadata
- **Duplicate Finder** - Find and remove duplicate files based on MD5 hash comparison

### Enterprise Features
- **Async Operations** - Non-blocking file operations with CompletableFuture
- **WebSocket Progress** - Real-time progress updates during long-running operations
- **Caching** - Caffeine-based in-memory caching for improved performance
- **Rate Limiting** - Per-IP rate limiting to prevent API abuse (10 req/sec default)
- **Input Validation** - Jakarta Validation with comprehensive error handling
- **Security** - Path traversal prevention, CORS configuration, and path validation
- **Global Exception Handling** - Consistent error responses across all endpoints
- **Comprehensive Testing** - Unit and integration tests for all layers

## 🏗️ Architecture

### Backend (Spring Boot 3.5.8)
```
┌─────────────────────────────────────────────┐
│           Controllers Layer                  │
│  ┌──────────────────────────────────────┐   │
│  │ FileOperationsController             │   │
│  │ FileSystemController                 │   │
│  └──────────────────────────────────────┘   │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│           Service Layer                      │
│  ┌──────────────────────────────────────┐   │
│  │ FileOperationsService (sync/async)   │   │
│  │ ProgressService (WebSocket)          │   │
│  │ PathSecurityService (validation)     │   │
│  └──────────────────────────────────────┘   │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│         Operations Layer                     │
│  ┌──────────────────────────────────────┐   │
│  │ Renomeador, Organizador, Extrator    │   │
│  │ PhotoOrganizator, DuplicateFinder    │   │
│  └──────────────────────────────────────┘   │
└─────────────────────────────────────────────┘
```

### Frontend (React 18 + TypeScript + Vite)
```
┌─────────────────────────────────────────────┐
│             App Component                    │
│  ┌──────────────────────────────────────┐   │
│  │ Navigation Drawer                    │   │
│  │ Operation Selection                  │   │
│  │ ErrorBoundary Wrapper                │   │
│  └──────────────────────────────────────┘   │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│        Operation Components                  │
│  ┌──────────────────────────────────────┐   │
│  │ RenameOperation                      │   │
│  │ OrganizeOperation                    │   │
│  │ ExtractOperation                     │   │
│  │ PhotoOrganizationOperation           │   │
│  │ DuplicateFinderOperation             │   │
│  └──────────────────────────────────────┘   │
└─────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────┐
│         Custom Hooks & Services              │
│  ┌──────────────────────────────────────┐   │
│  │ useOperation (state management)      │   │
│  │ useProgress (WebSocket)              │   │
│  │ api.ts (HTTP client)                 │   │
│  └──────────────────────────────────────┘   │
└─────────────────────────────────────────────┘
```

## 🛠️ Tech Stack

### Backend
- **Java 21** - Modern Java with latest features
- **Spring Boot 3.5.8** - Application framework
- **Spring Web** - REST API
- **Spring WebSocket** - Real-time communication
- **Spring Cache** - Caching abstraction
- **Spring Validation** - Input validation
- **Caffeine** - High-performance caching
- **Guava** - Rate limiting utilities
- **Lombok** - Boilerplate reduction
- **Metadata Extractor** - EXIF data reading
- **Apache Commons** - Utilities (IO, Text)

### Frontend
- **React 18** - UI library
- **TypeScript** - Type safety
- **Vite 6** - Build tool
- **Material-UI (MUI)** - Component library
- **SockJS** - WebSocket fallback
- **STOMP** - WebSocket protocol

### Testing
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework
- **Spring Test** - Integration testing
- **MockMvc** - Controller testing

## 📦 Installation & Setup

### Prerequisites
- Java 21 or higher
- Maven 3.6+ (or use included `./mvnw`)
- Node.js 22+ and npm 10+ (auto-installed by frontend-maven-plugin)

### Build & Run

**Build the entire project:**
```bash
./mvnw clean package
```

**Run the application:**
```bash
./mvnw spring-boot:run
```

**Access the application:**
- Frontend UI: http://localhost:8080
- API Endpoints: http://localhost:8080/api/*
- WebSocket: ws://localhost:8080/ws

**Run a single operation from the command line (oneshot mode):**
```bash
java -jar target/file-manager-*.jar \
  --oneshot='{"operation":"organize","params":{"sourceDirectory":"/in","destinationDirectory":"/out"}}'

# Or load the JSON from a file:
java -jar target/file-manager-*.jar --oneshot-file=/path/to/payload.json
```
The embedded web server is suppressed in oneshot mode; the JVM exits after the
operation. See [`docs/oneshot-cli.md`](docs/oneshot-cli.md) for the full JSON schema.

**Run tests:**
```bash
./mvnw test
```

## 🔧 Configuration

### Application Configuration (`src/main/resources/application.yml`)

```yaml
# File Manager Configuration
filemanager:
  allowed-paths: ${user.home},${java.io.tmpdir},/data,/uploads
  max-file-size: 100MB

# CORS Configuration
cors:
  allowed-origins: http://localhost:3000,http://localhost:8080

# Server Configuration
server:
  port: 8080
```

### Cache Configuration
- **Directory Listings**: 5 min TTL, 100 entries max
- **Path Validations**: 30 min TTL, 500 entries max
- **Operation Results**: 10 min TTL, 50 entries max

### Rate Limiting
- Default: 10 requests per second per IP
- Applies to all `/api/**` endpoints
- Returns `429 Too Many Requests` when exceeded

### Async Operations
- Core pool: 5 threads
- Max pool: 10 threads
- Queue capacity: 100 pending tasks
- Graceful shutdown: 30 seconds

## 🔐 Security Features

1. **Path Traversal Prevention**
   - Validates all file paths
   - Blocks `..` directory traversal
   - Checks invalid characters
   - Enforces allowed base paths

2. **Input Validation**
   - Jakarta Bean Validation
   - `@NotBlank` and `@Pattern` on DTOs
   - Global exception handler for validation errors

3. **CORS Configuration**
   - Environment-specific allowed origins
   - No wildcard (`*`) in production
   - Credential support with proper validation

4. **Rate Limiting**
   - Per-IP request limiting
   - Token bucket algorithm
   - Proxy-aware (X-Forwarded-For)

## 📡 API Endpoints

### File Operations
- `POST /api/operations/rename` - Rename files
- `POST /api/operations/organize` - Organize by extension
- `POST /api/operations/extract` - Extract from subdirectories
- `POST /api/operations/photo-organize` - Organize photos by date
- `POST /api/operations/find-duplicates` - Find duplicate files

### File System
- `GET /api/filesystem/home` - Get user home directory
- `GET /api/filesystem/roots` - Get system roots (drives)
- `GET /api/filesystem/list?path={path}` - List directory contents

### WebSocket
- `CONNECT /ws` - WebSocket endpoint (SockJS enabled)
- `SUBSCRIBE /topic/progress/{operationId}` - Progress updates

## 🧪 Testing

Test coverage includes:
- **Service Layer** - Business logic unit tests
- **Controller Layer** - HTTP integration tests
- **Exception Handling** - Error response tests
- **WebSocket** - Progress notification tests

Run all tests:
```bash
./mvnw test
```

Run specific test:
```bash
./mvnw test -Dtest=FileOperationsServiceTest
```

## 📊 Performance Optimizations

1. **Caching** - Reduces file system I/O for repeated requests
2. **Async Operations** - Non-blocking execution prevents thread exhaustion
3. **Rate Limiting** - Protects against API abuse and server overload
4. **Connection Pooling** - Efficient resource management
5. **Lazy Loading** - On-demand resource initialization

## 🔄 Development Workflow

### Backend Development
```bash
./mvnw spring-boot:run
# Edit Java files - Spring Boot DevTools auto-restart
```

### Frontend Development
```bash
cd src/main/frontend
npm run dev
# Hot module replacement at http://localhost:5173
```

### Full Build
```bash
./mvnw clean package
# Builds both backend and frontend
# Frontend assets copied to src/main/resources/static
```

## 📝 Project Structure

```
file-manager/
├── src/main/java/br/com/joaoborges/filemanager/
│   ├── config/              # Configuration classes
│   │   ├── AsyncConfig.java
│   │   ├── CacheConfig.java
│   │   ├── WebSecurityConfig.java
│   │   ├── WebSocketConfig.java
│   │   └── WebMvcConfig.java
│   ├── controller/          # REST controllers
│   │   ├── FileOperationsController.java
│   │   └── FileSystemController.java
│   ├── dto/                 # Data Transfer Objects
│   │   ├── RenameRequest.java
│   │   ├── OrganizeRequest.java
│   │   └── ...
│   ├── exception/           # Exception handling
│   │   ├── GlobalExceptionHandler.java
│   │   └── FileManagerException.java
│   ├── operations/          # File operations
│   │   ├── renaming/
│   │   ├── organization/
│   │   ├── extraction/
│   │   ├── photoOrganization/
│   │   └── duplicateFinder/
│   ├── security/            # Security services
│   │   └── PathSecurityService.java
│   └── service/             # Business services
│       ├── FileOperationsService.java
│       └── ProgressService.java
├── src/main/frontend/       # React application
│   ├── src/
│   │   ├── components/      # React components
│   │   ├── hooks/           # Custom hooks
│   │   ├── services/        # API services
│   │   └── types/           # TypeScript types
│   ├── package.json
│   └── vite.config.ts
├── src/test/java/           # Test files
├── pom.xml                  # Maven configuration
└── README.md               # This file
```

## 🐛 Troubleshooting

**Port already in use:**
```bash
# Change port in application.properties
server.port=8081
```

**Frontend build fails:**
```bash
cd src/main/frontend
rm -rf node_modules package-lock.json
npm install
```

**WebSocket connection fails:**
- Check CORS configuration
- Ensure WebSocket endpoint is `/ws`
- Verify SockJS is enabled

## 📄 License

Copyright (c) João Borges. All rights reserved.

## 🤝 Contributing

This is a private project. For questions or issues, contact the maintainer.

---

**Built with ❤️ using Spring Boot & React**

🤖 Enhanced with [Claude Code](https://claude.com/claude-code)
