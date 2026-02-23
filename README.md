# techAssignment

Spring Boot REST API with PostgreSQL and OpenAPI/Swagger UI documentation.
An optional UI is included (served by the backend) to interact with the endpoints.

## Tech Stack
- Java 17
- Spring Boot + Maven
- PostgreSQL
- OpenAPI/Swagger UI (springdoc)
- Docker + Docker Compose

---

## Prerequisites

### Recommended (run via Docker)
- Docker Engine / Docker Desktop
- Docker Compose v2 (`docker compose`)

### Local development
- JDK 17 (recommended: Temurin 17, same major version as the Docker build/runtime images)
- Maven (or use the included Maven Wrapper: `./mvnw`)

---

## Run with Docker Compose

The Docker Compose stack lives under `devops/`.

From the repository root:

```bash
docker compose -f devops/docker-compose.yml up --build