# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A mobile-optimized checklist application with a Quarkus (Java 21) backend and React + TypeScript frontend. Uses file-based JSON storage with JWT authentication.

**Tech Stack:**
- Backend: Quarkus 3.17, JAX-RS REST endpoints, SmallRye JWT
- Frontend: React 18, TypeScript, Vite, Tailwind CSS, @dnd-kit for drag-and-drop
- Authentication: JWT with bcrypt password hashing
- Storage: JSON files organized by username (case-insensitive on Windows)

## Essential Commands

### Backend Development
```bash
# Start backend with hot reload (includes built frontend)
./gradlew quarkusDev

# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "*AuthResourceTest*"

# Build production JAR (includes frontend build)
./gradlew build

# Run production build
java -jar build/quarkus-app/quarkus-run.jar
```

### Frontend Development
```bash
cd frontend

# Install dependencies
npm install

# Start dev server with hot reload (port 5173, proxies to backend)
npm run dev

# Lint TypeScript
npm run lint

# Build for production
npm run build
```

### Testing
The project uses JUnit 5 and REST Assured for integration tests. Tests automatically clean the test data directory (`target/test-data`) before each test to ensure isolation.

## Architecture

### Backend Structure (Java)

**Package organization** (`src/main/java/net/tfassbender/checklist/`):
- `resource/` - JAX-RS REST endpoints (AuthResource, ListResource, ItemResource)
- `service/` - Business logic (AuthService, ListService, FileStorageService)
- `model/` - Domain models (User, CheckList, CheckListItem)
- `dto/` - Data Transfer Objects (all are Java records)
- `security/` - JWT generation (JwtService)

**Key architectural patterns:**

1. **DTOs are Java records** - All request/response DTOs use Java records for immutability and conciseness. Use `field()` accessors instead of `getField()`.

2. **File-based storage** - Data stored in `data/users/<username-lowercase>/`:
   - `user.json` - User credentials
   - `lists/<list-id>.json` - Individual checklist files

   The `FileStorageService` uses `sanitizeUsername()` to normalize usernames to lowercase for file system operations (Windows compatibility), but the **original case is preserved** in the User and JWT token.

3. **Case-insensitive usernames** - Usernames are stored with their original case in the User object and JWT, but file system operations use lowercase. Duplicate checking is case-insensitive.

4. **JWT Authentication**:
   - JWT tokens contain the original username (with case preserved)
   - Tokens include a "user" role and expire after 24 hours (configurable)
   - Protected endpoints use `@RolesAllowed("user")`
   - The username in SecurityContext matches the original case from registration

5. **Resource isolation** - All list operations verify ownership by checking the username from `SecurityContext.getUserPrincipal().getName()` against the stored list owner.

### Frontend Structure

**Directory organization** (`frontend/src/`):
- `pages/` - Top-level route components (Login, Dashboard, ListView)
- `components/` - Reusable UI components
- `context/` - React Context providers (AuthContext)
- `services/` - API clients (authService, listService)
- `hooks/` - Custom React hooks
- `types/` - TypeScript type definitions

**Key patterns:**
- Authentication state managed via AuthContext
- API calls use JWT token from localStorage
- Drag-and-drop reordering using @dnd-kit
- Dark/light mode with system preference detection
- Mobile-first responsive design with Tailwind CSS

### API Design

All list/item endpoints require authentication via `Authorization: Bearer <token>` header.

**REST conventions:**
- Use Java records for all DTOs (request/response objects)
- Error responses return `ErrorResponse(message, status)` record
- List operations return full `CheckList` objects with items
- GET `/api/lists` returns `CheckListSummary` records for performance

### Data Model Relationships

```
User
 └─ CheckList (many)
     └─ CheckListItem (many, ordered by orderIndex)
```

- Each user's data is isolated in their own directory
- Lists contain embedded items array (not separate files)
- Items have an `orderIndex` for client-controlled ordering

## Configuration

**Backend** (`src/main/resources/application.properties`):
- Server runs on port 4715
- JWT signing key in `smallrye.jwt.sign.key` (base64-encoded)
- Data directory configurable via `checklist.data.dir` (default: `../../../data`)
- CORS enabled for localhost:5173 (dev) and localhost:4715

**Frontend** (`frontend/vite.config.ts`):
- Dev server on port 5173
- API proxy to http://localhost:4715/api

## Common Development Scenarios

### Adding a new endpoint

1. Create/update DTO record in `dto/` package
2. Add method to appropriate service class with business logic
3. Add JAX-RS endpoint method in resource class
4. Add integration test in `src/test/java/.../resource/` following existing patterns
5. Update frontend service in `frontend/src/services/`

### Working with records

When converting classes to records or using existing records:
- Use `field()` accessor syntax, not `getField()`
- Records are immutable - create new instances for modifications
- Jackson handles serialization/deserialization automatically

### Username handling

When working with username fields:
- Store original case in User object and JWT
- Use `FileStorageService` methods for storage (handles lowercase conversion)
- Never manually lowercase usernames for JWT or display
- Duplicate checking is automatic via `FileStorageService.userExists()`

## Port Configuration

- Backend: 4715
- Frontend dev server: 5173
- Both configured for CORS to work together in development
