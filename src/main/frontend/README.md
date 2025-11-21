# File Manager Frontend

Modern React + TypeScript frontend for the File Manager application.

## Quick Start

### Prerequisites

- Node.js 22.12.0 or higher
- npm 10.9.2 or higher

### Installation

```bash
cd src/main/frontend
npm install
```

### Development

```bash
# Start development server with hot reload
npm run dev
```

Open http://localhost:3000 in your browser.

**Note**: The backend must be running on http://localhost:8080 for API calls to work.

### Production Build

```bash
# Type-check and build for production
npm run build
```

Output will be in `src/main/resources/static/`.

### Type Checking

```bash
# Type-check without building
npm run type-check

# Watch mode
npm run type-check -- --watch
```

## Technology Stack

- **React 19** - UI library
- **TypeScript 5.7** - Type safety
- **Vite 6** - Build tool and dev server
- **Material-UI 6** - Component library
- **Axios** - HTTP client
- **React Router 7** - Routing

## Project Structure

```
src/
├── main.tsx                    # Entry point
├── App.tsx                     # Root component
├── types/                      # TypeScript definitions
│   ├── api.types.ts            # API types
│   ├── operations.types.ts     # Operation types
│   └── index.ts                # Barrel exports
├── services/
│   └── api.ts                  # API client
└── components/
    ├── RenameOperation.tsx
    ├── OrganizeOperation.tsx
    ├── ExtractOperation.tsx
    ├── PhotoOrganizationOperation.tsx
    └── DuplicateFinderOperation.tsx
```

## Available Operations

### 1. Rename Files
Rename files based on patterns and exclusion rules.

### 2. Organize Files
Organize files by extension into categorized folders.

### 3. Extract Files
Extract files from nested subdirectories to a flat structure.

### 4. Organize Photos by Date
Organize photos/videos by EXIF date metadata.

### 5. Find Duplicates
Find and remove duplicate files based on MD5 hash.

## API Integration

The frontend communicates with the Spring Boot backend via REST API:

| Endpoint | Method | Operation |
|----------|--------|-----------|
| `/api/operations/rename` | POST | Rename files |
| `/api/operations/organize` | POST | Organize files |
| `/api/operations/extract` | POST | Extract files |
| `/api/operations/photo-organize` | POST | Organize photos |
| `/api/operations/find-duplicates` | POST | Find duplicates |

## Development Workflow

1. **Start Backend**: `./mvnw spring-boot:run` (from project root)
2. **Start Frontend**: `npm run dev` (from `src/main/frontend`)
3. **Edit Code**: Changes hot-reload automatically
4. **Type Checking**: Run `npm run type-check` to verify types

## Build Integration

The frontend is automatically built by Maven during the Spring Boot build:

```bash
# From project root
./mvnw clean package
```

This will:
1. Install Node.js and npm
2. Run `npm install`
3. Run `npm run build`
4. Package frontend into Spring Boot JAR

## TypeScript

All code is written in TypeScript for type safety. See `TYPESCRIPT_ARCHITECTURE.md` for detailed documentation.

### Type Checking

TypeScript compilation is part of the build process:

```bash
npm run build  # Runs tsc && vite build
```

### Path Aliases

Clean imports using path aliases:

```typescript
import type { RenameRequest } from '@types';
import { renameFiles } from '@services/api';
import MyComponent from '@components/MyComponent';
```

## Code Style

- **Strict TypeScript**: All strict options enabled
- **Functional Components**: Using React Hooks
- **Type Imports**: Always use `import type` for types
- **JSDoc**: Document complex types and functions

## Troubleshooting

### Port 3000 Already in Use

```bash
# Kill process on port 3000
lsof -ti:3000 | xargs kill -9

# Or use a different port
npm run dev -- --port 3001
```

### Backend API Not Working

Ensure Spring Boot is running on port 8080:

```bash
# From project root
./mvnw spring-boot:run
```

### TypeScript Errors

Run type checking to see detailed errors:

```bash
npm run type-check
```

## Documentation

- **TypeScript Architecture**: See `TYPESCRIPT_ARCHITECTURE.md`
- **API Types**: See `src/types/api.types.ts`
- **Component Patterns**: See `src/components/*.tsx`

## License

Same as parent project.
