# Quick Start Guide - File Manager Web App

## Overview

This is a full-stack web application with:
- **Backend**: Spring Boot 3.5.8 (Java 21)
- **Frontend**: React 19 + TypeScript 5.7 + Vite 6

## Prerequisites

- **Java 21** or higher
- **Maven** (included via wrapper)
- **Node.js** 22.12+ and **npm** 10.9+ (auto-installed by Maven)

## Running the Application

### Option 1: Production Mode (Recommended)

Build and run everything together:

```bash
# Build backend + frontend in one command
./mvnw clean package

# Run the application
./mvnw spring-boot:run

# Or run the JAR directly
java -jar target/file-manager-4.0-SNAPSHOT.jar
```

Open http://localhost:8080 in your browser.

### Option 2: Development Mode (Fast Iteration)

Run backend and frontend separately for hot reload:

```bash
# Terminal 1: Start Backend
./mvnw spring-boot:run

# Terminal 2: Start Frontend (in new terminal)
cd src/main/frontend
npm install
npm run dev
```

Open http://localhost:3000 in your browser.

**Benefits**:
- Frontend changes reload instantly (< 100ms)
- TypeScript errors shown immediately
- Full source maps for debugging

### Option 3: Swing Mode (Legacy Desktop App)

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--swing"
```

## Available Operations

Once running, you can use these file operations:

1. **Rename Files** - Rename files based on patterns
2. **Organize Files** - Sort files by extension into folders
3. **Extract Files** - Flatten nested directory structures
4. **Organize Photos** - Sort photos by EXIF date
5. **Find Duplicates** - Detect and remove duplicate files

## Project Structure

```
file-manager/
├── src/main/
│   ├── java/                           # Spring Boot backend
│   │   └── br/com/joaoborges/filemanager/
│   │       ├── controller/             # REST API endpoints
│   │       ├── operations/             # File operations
│   │       └── ...
│   ├── frontend/                       # React + TypeScript frontend
│   │   ├── src/
│   │   │   ├── main.tsx               # Entry point
│   │   │   ├── App.tsx                # Main component
│   │   │   ├── components/            # React components
│   │   │   ├── services/              # API client
│   │   │   └── types/                 # TypeScript types
│   │   ├── package.json
│   │   ├── tsconfig.json
│   │   └── vite.config.ts
│   └── resources/
│       ├── application.properties
│       └── static/                    # Built frontend (generated)
├── pom.xml                            # Maven configuration
└── QUICKSTART.md                      # This file
```

## Development Workflow

### Making Frontend Changes

1. Start dev mode (see Option 2 above)
2. Edit files in `src/main/frontend/src/`
3. Changes appear instantly in browser
4. TypeScript errors shown in console

### Making Backend Changes

1. Edit Java files
2. Spring Boot DevTools auto-restarts (if configured)
3. Or manually restart with `Ctrl+C` and `./mvnw spring-boot:run`

### Type Checking

```bash
cd src/main/frontend
npm run type-check              # Check once
npm run type-check -- --watch   # Watch mode
```

### Building for Production

```bash
# Build everything
./mvnw clean package

# Output: target/file-manager-4.0-SNAPSHOT.jar
```

## Troubleshooting

### Frontend Build Fails

```bash
cd src/main/frontend
rm -rf node_modules package-lock.json
npm install
npm run build
```

### Backend Build Fails (Lombok)

The pom.xml already includes Lombok annotation processor configuration. If issues persist:

```bash
./mvnw clean install -DskipTests
```

### Port Already in Use

**Backend (8080)**:
```bash
# Find and kill process
lsof -ti:8080 | xargs kill -9
```

**Frontend Dev Server (3000)**:
```bash
# Use different port
npm run dev -- --port 3001
```

### TypeScript Errors

```bash
cd src/main/frontend
npm run type-check
```

Check the console output for specific error locations.

## Documentation

- **`TYPESCRIPT_ARCHITECTURE.md`** - Complete TypeScript architecture guide (450+ lines)
- **`REACT_MIGRATION.md`** - Migration documentation and status
- **`TYPESCRIPT_MIGRATION_SUMMARY.md`** - Executive summary
- **`src/main/frontend/README.md`** - Frontend-specific docs
- **`CLAUDE.md`** - Backend architecture and patterns

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/operations/rename` | POST | Rename files |
| `/api/operations/organize` | POST | Organize by extension |
| `/api/operations/extract` | POST | Extract from subdirectories |
| `/api/operations/photo-organize` | POST | Organize photos by date |
| `/api/operations/find-duplicates` | POST | Find/remove duplicates |

## Technology Stack

### Backend
- Spring Boot 3.5.8
- Java 21
- Lombok
- Apache Commons
- metadata-extractor (EXIF)

### Frontend
- React 19.0.0
- TypeScript 5.7.2
- Vite 6.0.7
- Material-UI 6.3.2
- Axios 1.7.9

## Performance

- **Build time**: ~30 seconds (full build)
- **Frontend build**: ~4 seconds
- **Hot reload**: < 100ms
- **Bundle size**: 164 KB (gzipped)

## Need Help?

1. Check the error message carefully
2. Review the appropriate documentation file
3. Try cleaning and rebuilding: `./mvnw clean install`
4. Check that all ports are available (8080, 3000)

---

**Last Updated**: November 21, 2024
**Version**: 4.0.0
