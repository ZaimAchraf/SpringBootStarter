# Spring Boot Starter

<img width="1847" height="864" alt="image" src="https://github.com/user-attachments/assets/8c4eda11-e978-437d-af26-9857063515c8" />


A production-oriented **Spring Boot multi-module starter** with:
- JWT authentication
- refresh token flow (HTTP-only cookie)
- password reset by email
- Flyway database migrations
- local file storage service
- Dockerized local environment
- test suite (API, security, services, storage)

This `main` branch is a lightweight landing page.  
The full source code lives on the **`master`** branch.

## Architecture

The starter is structured as a Maven multi-module project:

- `app-api`: application entrypoint, controllers, global exception handling
- `app-security`: JWT, security config, auth services, refresh/reset flows
- `app-user`: user domain (entity, repository, service)
- `app-mail`: mail sender and templates (password reset email)
- `app-storage`: local upload/delete/resolve service
- `app-shared`: shared DTOs, enums, exceptions, cross-cutting helpers

Design goal: keep business domain out of the starter and provide reusable infrastructure blocks.

## Key Features

- **Auth APIs**
  - login / register / refresh / logout
  - password reset request + confirm
- **User APIs**
  - current user (`/me`)
  - admin user management endpoints
- **Security**
  - stateless JWT auth
  - role-based access (`ROLE_ADMIN`, `ROLE_CLIENT`)
  - configurable CORS
- **Migrations**
  - Flyway enabled
  - schema versioned via `V1`, `V2`, `V3`, ...
- **Logging**
  - console + rolling file logs (`app_stdout.log`, zipped rotation)
- **Storage**
  - local file storage abstraction (`store`, `delete`, `absolutePathFor`)

## Docker Setup

The project provides `docker-compose` for local development with:
- PostgreSQL
- pgAdmin
- Maildev
- Spring Boot backend

Typical flow:

```bash
docker compose up -d --build
```

Maildev UI:
- http://localhost:1080

Backend:
- http://localhost:8180

## Run Without Docker

You can run the backend directly on your machine (Java + Maven), without Docker.

Prerequisites:
- Java 21
- Maven 3.9.x
- PostgreSQL running locally
- Optional SMTP server for email/reset flow

Typical flow:

```bash
DB_HOST=127.0.0.1 DB_PORT=5433 MAIL_HOST=127.0.0.1 MAIL_PORT=1025 source ./scripts/set-local-env.sh
mvn -pl app-api -am clean install -DskipTests
mvn -pl app-api -am spring-boot:run
```

Notes:
- On Windows PowerShell, use:
  - `. .\scripts\set-local-env.ps1 -DbHost 127.0.0.1 -DbPort 5433 -MailHost 127.0.0.1 -MailPort 1025`
- If your machine has `localhost` routing issues, use `127.0.0.1` for API/frontend URLs.

## API Documentation (Swagger/OpenAPI)

Swagger/OpenAPI is enabled in the implementation branch (`master`) and can be accessed at:

- Swagger UI: `http://localhost:8180/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8180/v3/api-docs`

Notes:
- Swagger endpoints are public for local/dev usage.
- Business endpoint security remains unchanged.
- For secured endpoints, first authenticate and then use the bearer token in Swagger UI.

## Secrets & Environment

Secrets are managed through environment variables (`.env`), not hardcoded.

- Copy `.env.example` to `.env`
- Fill values (DB password, JWT secret, etc.)
- Keep `.env` out of git

## Testing Strategy

The starter includes:

- **Controller tests** (MockMvc) for auth and user endpoints
- **Security access tests** for route authorization policies
- **Service unit tests** for:
  - `AuthService`
  - `PasswordResetService`
  - `UserService`
- **Storage unit tests** for `LocalUploadService`

Run tests from repository root:

```bash
mvn test
```

If Maven is not installed locally, you can run tests with Docker:

```bash
docker run --rm -v "${PWD}:/workspace" -w /workspace maven:3.9.9-eclipse-temurin-21 mvn test
```

## CI and Smoke Tests

The implementation branch (`master`) now includes:

- GitHub Actions workflow: `.github/workflows/ci.yml`
- unit/slice test job (`mvn -B -ntp test`)
- Docker smoke test job:
  - `docker compose up -d --build`
  - `scripts/smoke-tests.sh`
  - backend logs exported on failure
  - cleanup with `docker compose down -v`

Smoke checks cover:
- API readiness
- register
- login + JWT extraction
- `GET /api/users/me` with bearer token
- admin access protection on `GET /api/users`
- password reset request endpoint

## Branches

- `main`: GitHub landing branch (README only)
- `master`: full implementation branch

## Roadmap Ideas

- stricter request validation (`@Valid`, bean validation annotations)
- standardized error payload contract
- optional integration tests with Testcontainers
