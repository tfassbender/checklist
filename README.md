# Checklist Application

A mobile-optimized web application for managing checklists with a Quarkus (Java) backend and React (TypeScript) frontend.

## Tech Stack

- **Backend**: Quarkus 3.17 (Java 21)
- **Frontend**: React 18 + TypeScript + Vite
- **Styling**: Tailwind CSS
- **Authentication**: JWT
- **Storage**: File-based (JSON)

## Features

- User authentication (register/login)
- Create, edit, and delete checklists
- Add, edit, check/uncheck, and reorder items
- Notes section for each list
- Reset (uncheck all items) functionality
- Dark/Light mode with system preference detection
- Mobile-optimized UI with touch-friendly interactions

## Prerequisites

- Java 21 or later
- Node.js 18 or later
- npm

## Development

### Running in Development Mode

Start the backend with hot reload:

```bash
./gradlew quarkusDev
```

The application will be available at http://localhost:4715

For frontend-only development with hot reload:

```bash
cd frontend
npm install
npm run dev
```

This starts the Vite dev server at http://localhost:5173 (proxies API calls to backend).

### Building for Production

```bash
./gradlew build
```

This will:
1. Install frontend dependencies
2. Build the React frontend
3. Copy the built files to Quarkus resources
4. Build the Quarkus application

### Running the Production Build

```bash
java -jar build/quarkus-app/quarkus-run.jar
```

## Project Structure

```
checklist/
├── src/main/java/          # Java backend code
├── src/main/resources/     # Backend resources & configs
├── frontend/               # React frontend
├── data/                   # File-based storage (gitignored)
└── build.gradle.kts        # Gradle build configuration
```

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/register` | Create account |
| POST | `/api/auth/login` | Login, returns JWT |
| GET | `/api/lists` | Get all lists (summary) |
| POST | `/api/lists` | Create new list |
| GET | `/api/lists/{id}` | Get list with items |
| PUT | `/api/lists/{id}` | Update list name/notes |
| DELETE | `/api/lists/{id}` | Delete list |
| POST | `/api/lists/{id}/reset` | Uncheck all items |
| POST | `/api/lists/{id}/items` | Add item |
| PUT | `/api/lists/{id}/items/{itemId}` | Update item |
| DELETE | `/api/lists/{id}/items/{itemId}` | Delete item |
| PUT | `/api/lists/{id}/items/reorder` | Reorder items |

## License

MIT
