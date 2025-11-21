# TypeScript Architecture Documentation

## Overview

This document provides comprehensive documentation for the TypeScript-based frontend of the File Manager application. The frontend is built with **React 19**, **TypeScript 5.7**, **Vite 6**, and **Material-UI 6**.

## Table of Contents

1. [Technology Stack](#technology-stack)
2. [Project Structure](#project-structure)
3. [TypeScript Configuration](#typescript-configuration)
4. [Type System Architecture](#type-system-architecture)
5. [Component Architecture](#component-architecture)
6. [API Service Layer](#api-service-layer)
7. [Build System](#build-system)
8. [Development Workflow](#development-workflow)
9. [Type Safety Patterns](#type-safety-patterns)
10. [Best Practices](#best-practices)

---

## Technology Stack

### Core Dependencies

| Package | Version | Purpose |
|---------|---------|---------|
| **react** | ^19.0.0 | UI library with latest features |
| **react-dom** | ^19.0.0 | DOM rendering for React |
| **typescript** | ^5.7.2 | Static type checking |
| **vite** | ^6.0.7 | Build tool and dev server |
| **@mui/material** | ^6.3.2 | Material Design components |
| **axios** | ^1.7.9 | HTTP client for API calls |
| **react-router-dom** | ^7.1.1 | Client-side routing |

### TypeScript Type Definitions

```json
{
  "@types/react": "^19.0.6",
  "@types/react-dom": "^19.0.2",
  "@types/node": "^22.10.2"
}
```

All type definitions are at their latest versions to ensure compatibility with the latest features and best practices.

---

## Project Structure

```
src/main/frontend/
├── index.html                      # HTML entry point
├── package.json                    # Dependencies and scripts
├── tsconfig.json                   # TypeScript configuration
├── vite.config.ts                  # Vite build configuration
├── TYPESCRIPT_ARCHITECTURE.md      # This file
└── src/
    ├── main.tsx                    # Application entry point
    ├── App.tsx                     # Root component
    ├── types/                      # TypeScript type definitions
    │   ├── index.ts                # Central type exports
    │   ├── api.types.ts            # API request/response types
    │   └── operations.types.ts     # Operation-specific types
    ├── services/                   # API and business logic
    │   └── api.ts                  # API client with full typing
    └── components/                 # React components
        ├── RenameOperation.tsx
        ├── OrganizeOperation.tsx
        ├── ExtractOperation.tsx
        ├── PhotoOrganizationOperation.tsx
        └── DuplicateFinderOperation.tsx
```

### Directory Responsibilities

- **`types/`**: Contains all TypeScript type definitions and interfaces
- **`services/`**: Business logic, API communication, and data transformation
- **`components/`**: React UI components (presentation and container components)

---

## TypeScript Configuration

### tsconfig.json

The TypeScript configuration is optimized for modern React development:

```json
{
  "compilerOptions": {
    // Modern JavaScript/TypeScript target
    "target": "ES2022",
    "lib": ["ES2023", "DOM", "DOM.Iterable"],
    "jsx": "react-jsx",

    // Module resolution for Vite
    "module": "ESNext",
    "moduleResolution": "bundler",
    "resolveJsonModule": true,
    "allowImportingTsExtensions": true,

    // Strict type checking (recommended)
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,
    "noUncheckedIndexedAccess": true,
    "noImplicitReturns": true,

    // Path aliases for clean imports
    "baseUrl": ".",
    "paths": {
      "@/*": ["./src/*"],
      "@components/*": ["./src/components/*"],
      "@services/*": ["./src/services/*"],
      "@types/*": ["./src/types/*"]
    }
  }
}
```

### Key Configuration Highlights

1. **Strict Mode**: Enabled for maximum type safety
2. **Path Aliases**: Clean imports using `@` prefix
3. **Module Resolution**: Optimized for Vite bundler
4. **No Emit**: TypeScript only for type checking; Vite handles transpilation

---

## Type System Architecture

### Type Definition Philosophy

The type system is organized into three layers:

1. **API Layer** (`api.types.ts`): Request/response contracts with backend
2. **Domain Layer** (`operations.types.ts`): Business logic and UI state
3. **Component Layer**: Component-specific prop types

### API Types (`api.types.ts`)

All API communication is fully typed:

```typescript
// Request types
export interface RenameRequest {
  sourceDirectory: string;
  includeSubDirectories: boolean;
}

// Response types
export interface RenameResponse extends ApiResponse {
  filesRenamed: number;
}

// Error types
export interface ApiError {
  success: false;
  message: string;
  error?: string;
}
```

### Operation Types (`operations.types.ts`)

UI-specific types for operations:

```typescript
export type OperationId = 'rename' | 'organize' | 'extract' | 'photo' | 'duplicate';

export interface Operation {
  id: OperationId;
  label: string;
  icon: ReactNode;
  description?: string;
}

export type OperationStatus = 'idle' | 'loading' | 'success' | 'error';
```

### Type Imports

All types can be imported from the central barrel export:

```typescript
import type { RenameRequest, OperationId, ApiResponse } from '@types';
```

---

## Component Architecture

### Component Pattern

All components follow a consistent pattern:

```typescript
import { useState, type FC } from 'react';
import type { SomeResponse, ApiError } from '@types';

const MyComponent: FC = () => {
  // 1. State management with explicit types
  const [data, setData] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(false);
  const [result, setResult] = useState<SomeResponse | null>(null);
  const [error, setError] = useState<string | null>(null);

  // 2. Event handlers with typed parameters
  const handleSubmit = async (): Promise<void> => {
    // Implementation
  };

  // 3. JSX with type-safe props
  return (
    <div>{/* Component JSX */}</div>
  );
};

export default MyComponent;
```

### Type Safety Benefits

1. **Compile-time validation**: Errors caught before runtime
2. **IntelliSense support**: Full autocomplete in IDEs
3. **Refactoring safety**: Breaking changes detected immediately
4. **Documentation**: Types serve as inline documentation

---

## API Service Layer

### Service Architecture

The `api.ts` service provides a fully typed API client:

```typescript
import axios, { type AxiosInstance } from 'axios';
import type { RenameRequest, RenameResponse } from '../types';

const api: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 60000,
});

export const renameFiles = async (
  params: RenameRequest
): Promise<RenameResponse> => {
  const response = await api.post<RenameResponse>('/operations/rename', params);
  return response.data;
};
```

### Error Handling

The API service includes a response interceptor for consistent error handling:

```typescript
api.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiError>) => {
    const apiError: ApiError = {
      success: false,
      message: error.response?.data?.message || 'An error occurred',
      error: error.response?.data?.error,
    };
    return Promise.reject(apiError);
  }
);
```

### Type-Safe API Calls

All API methods are fully typed:

```typescript
// TypeScript knows the exact shape of params and return value
const result: RenameResponse = await renameFiles({
  sourceDirectory: '/path',
  includeSubDirectories: true,
});

// Autocomplete works for all properties
console.log(result.filesRenamed);
```

---

## Build System

### Vite Configuration

Vite is configured for optimal TypeScript development:

```typescript
// vite.config.ts
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': resolve(__dirname, './src'),
      '@components': resolve(__dirname, './src/components'),
      '@services': resolve(__dirname, './src/services'),
      '@types': resolve(__dirname, './src/types'),
    },
  },
  build: {
    outDir: '../resources/static',
    sourcemap: true,
    target: 'es2022',
  },
});
```

### Build Process

The build process is integrated with Maven:

1. **Maven triggers frontend build** via `frontend-maven-plugin`
2. **npm install** downloads dependencies
3. **TypeScript compilation** (`tsc`) type-checks all files
4. **Vite build** bundles and optimizes for production
5. **Output** is placed in `src/main/resources/static/`
6. **Spring Boot** serves the built React app

### Build Commands

```bash
# Type checking only (no output)
npm run type-check

# Build for production (type-check + bundle)
npm run build

# Development with hot reload
npm run dev
```

---

## Development Workflow

### Setting Up Development Environment

```bash
# Navigate to frontend directory
cd src/main/frontend

# Install dependencies
npm install

# Start development server
npm run dev
```

### Development Server

- **Frontend**: http://localhost:3000 (Vite dev server)
- **Backend**: http://localhost:8080 (Spring Boot)
- **API Proxy**: Vite proxies `/api/*` to backend

### Hot Module Replacement (HMR)

Vite provides instant updates without page refresh:

- Edit a `.tsx` file
- Save the file
- See changes immediately in the browser
- TypeScript errors appear in the browser console

### Type Checking Workflow

```bash
# Run type checker in watch mode
npm run type-check -- --watch

# TypeScript will continuously check for type errors
# Errors appear in the terminal
```

---

## Type Safety Patterns

### 1. Strict Null Checks

Always handle nullable values explicitly:

```typescript
// ✅ Good
const [result, setResult] = useState<RenameResponse | null>(null);

if (result) {
  console.log(result.filesRenamed); // TypeScript knows result is not null
}

// ❌ Bad (will cause type errors)
const [result, setResult] = useState<RenameResponse>();
console.log(result.filesRenamed); // Error: result might be undefined
```

### 2. Discriminated Unions

Use discriminated unions for state management:

```typescript
type OperationStatus = 'idle' | 'loading' | 'success' | 'error';

// TypeScript can narrow the type based on status
if (status === 'success') {
  // TypeScript knows we have data here
}
```

### 3. Type Guards

Create type guards for runtime type checking:

```typescript
function isApiError(error: unknown): error is ApiError {
  return (
    typeof error === 'object' &&
    error !== null &&
    'success' in error &&
    'message' in error
  );
}

// Usage
try {
  await someApiCall();
} catch (err) {
  if (isApiError(err)) {
    console.log(err.message); // TypeScript knows it's ApiError
  }
}
```

### 4. Generic Components

Use generics for reusable components:

```typescript
interface DataDisplayProps<T> {
  data: T;
  render: (item: T) => JSX.Element;
}

function DataDisplay<T>({ data, render }: DataDisplayProps<T>) {
  return <div>{render(data)}</div>;
}
```

---

## Best Practices

### 1. Import Types with `type` Keyword

Always use the `type` keyword when importing types:

```typescript
// ✅ Good
import type { RenameRequest } from '@types';
import { renameFiles } from '@services/api';

// ❌ Bad
import { RenameRequest } from '@types';
```

This helps bundlers eliminate type-only imports.

### 2. Use Explicit Return Types

Always specify return types for functions:

```typescript
// ✅ Good
const handleSubmit = async (): Promise<void> => {
  // Implementation
};

// ❌ Bad (implicit return type)
const handleSubmit = async () => {
  // Implementation
};
```

### 3. Avoid `any` Type

Never use `any` unless absolutely necessary:

```typescript
// ✅ Good
const [data, setData] = useState<string | null>(null);

// ❌ Bad
const [data, setData] = useState<any>(null);
```

### 4. Use Const Assertions

Use `as const` for literal types:

```typescript
// ✅ Good
const OPERATIONS = [
  { id: 'rename', label: 'Rename' },
  { id: 'organize', label: 'Organize' },
] as const;

type OperationId = typeof OPERATIONS[number]['id']; // 'rename' | 'organize'

// ❌ Bad (loses type narrowing)
const OPERATIONS = [
  { id: 'rename', label: 'Rename' },
  { id: 'organize', label: 'Organize' },
];
```

### 5. Document Complex Types

Add JSDoc comments to complex types:

```typescript
/**
 * Represents the result of a file rename operation
 * @property filesRenamed - Number of files successfully renamed
 * @property errors - Array of error messages, if any
 */
export interface RenameResponse extends ApiResponse {
  filesRenamed: number;
  errors?: string[];
}
```

---

## TypeScript "Engine" Internals

### How TypeScript Works in This Project

1. **Development Mode**:
   - Vite uses **esbuild** to transpile TypeScript on-the-fly
   - Type checking happens in parallel (via `tsc --noEmit`)
   - Extremely fast hot module replacement

2. **Build Mode**:
   - TypeScript compiler (`tsc`) validates all types
   - Vite/esbuild transpiles TypeScript to JavaScript
   - Rollup bundles everything into optimized chunks
   - Source maps generated for debugging

3. **Type Checking**:
   - Runs independently from bundling
   - Catches errors at compile time
   - Can be run in watch mode during development

### Compilation Pipeline

```
┌─────────────────┐
│  TypeScript     │
│  Source (.tsx)  │
└────────┬────────┘
         │
         ├──────────────────┐
         │                  │
         ▼                  ▼
┌─────────────────┐  ┌─────────────────┐
│  tsc (type-     │  │  esbuild        │
│  checking only) │  │  (transpile)    │
└────────┬────────┘  └────────┬────────┘
         │                     │
         ▼                     ▼
┌─────────────────┐  ┌─────────────────┐
│  Type Errors    │  │  JavaScript     │
│  (if any)       │  │  (.js)          │
└─────────────────┘  └────────┬────────┘
                              │
                              ▼
                     ┌─────────────────┐
                     │  Rollup         │
                     │  (bundle)       │
                     └────────┬────────┘
                              │
                              ▼
                     ┌─────────────────┐
                     │  Optimized      │
                     │  Bundle         │
                     └─────────────────┘
```

---

## Conclusion

This TypeScript architecture provides:

✅ **Type Safety**: Catch errors at compile time, not runtime
✅ **Developer Experience**: IntelliSense, autocomplete, inline docs
✅ **Maintainability**: Refactoring with confidence
✅ **Performance**: Fast development with Vite, optimized production builds
✅ **Modern Stack**: Latest versions of React, TypeScript, and tooling

For questions or improvements, consult the React, TypeScript, and Vite documentation.
