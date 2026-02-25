# Technical Assignment

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
- JDK 17 (recommended: Temurin 17/Oracle 17, same major version as the Docker build/runtime images)
- Maven (or use the included Maven Wrapper: `./mvnw`)

---

## Run with Docker Compose

The Docker Compose stack lives under `docker/`.

From the repository root:

```bash
docker compose -f devops/docker-compose.yml up --build
```


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

- **Swagger UI** — available at http://localhost:8080/swagger-ui when the application is running. All endpoints are documented and can be tested directly from the browser.

- **Postman Collection** — a ready-to-use collection is included at `postman/Technical Assingment.postman_collection.json`. Import it into Postman to test all endpoints.
