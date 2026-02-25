# Technical Assignment

Spring Boot REST API with PostgreSQL and OpenAPI/Swagger UI documentation.

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
- JDK 17 (recommended: Temurin 17/Oracle 17, same major version as the Docker build/runtime images)
- Maven (or use the included Maven Wrapper: `./mvnw`)

---

## Run with Docker Compose

The Docker Compose stack lives under `docker/`.

From the repository root:

```bash
docker compose -f docker/docker-compose.yml up --build
```

> **Note:** The PostgreSQL data is stored in `docker/db/data/` (bind mount).
> If the database container fails to start, make sure the directory exists:
> ```bash
> mkdir -p docker/db/data
> ```

---

## Security

All API endpoints are protected with **HTTP Basic Authentication**.

| Credential | Value   |
|------------|---------|
| Username   | `admin` |
| Password   | `admin` |

The following paths are **publicly accessible** and do not require authentication:
- `GET /swagger-ui/**` — Swagger UI
- `GET /v3/api-docs/**` — OpenAPI specification
- `GET /actuator/health` — Health check
- `GET /actuator/info` — Application info

The default credentials can be overridden via environment variables:
- `SECURITY_USER` — username
- `SECURITY_PASS` — password

---

## Unit Testing

The project includes unit tests covering the service, mapper, and controller layers.

To run all tests:

```bash
./mvnw test
```

---

## API Testing

There are two ways to test the API endpoints:

- **Swagger UI** — available at http://localhost:8080/swagger-ui when the application is running. All endpoints are documented and can be tested directly from the browser. Click the **Authorize** button and enter the credentials above before making requests.

- **Postman Collection** — a ready-to-use collection is included at `postman/Technical Assingment.postman_collection.json`. Import it into Postman to test all endpoints. Make sure to configure Basic Auth with the credentials above in Postman.
