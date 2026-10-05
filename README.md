# Coworking Reservations API

REST API for managing coworking spaces and reservations, developed as a technical assessment using Spring Boot and Java.

The application supports authentication and authorization with JWT, coworking space management, reservation management with concurrency control, asynchronous notifications, payment validation with resilience mechanisms, and occupancy reporting with caching.

## Features

- JWT-based authentication and authorization
- User registration with `USER` role
- Administrative access with `ADMIN` role
- Coworking space CRUD
- Reservation creation, consultation and cancellation
- Ownership-based reservation access control
- Reservation overlap prevention
- Transactional concurrency control using pessimistic locking
- Simulated external payment validation
- Circuit breaker, timeout and fallback with Resilience4j
- Asynchronous reservation confirmation notifications
- Occupancy reports by date range
- Caffeine-based report caching
- Spring Boot Actuator
- OpenAPI / Swagger documentation
- Unit and integration tests
- PostgreSQL support
- Docker Compose environment

## Tech Stack

- Java 17
- Spring Boot 3.5.x
- Spring Data JPA
- Spring Security
- JWT
- PostgreSQL
- H2
- Flyway
- Resilience4j
- Caffeine
- Spring Boot Actuator
- Springdoc OpenAPI
- Maven
- Docker / Docker Compose

## Architecture

The application follows a layered architecture organized by domain:

```text
com.coworking.reservations
├── auth
├── user
├── space
├── reservation
├── payment
├── report
├── common
└── config
```

Controllers are responsible for HTTP concerns, services contain business logic, repositories handle persistence, and DTOs are used as the API boundary.

## Requirements

For local development and test execution:

- Java 17+
- Maven 3.9+

For the containerized environment:

- Docker
- Docker Compose

## Running locally

The default Spring profile is `dev`.

The development profile uses an in-memory H2 database, so PostgreSQL is not required for running the application or executing the automated test suite.

```powershell
mvn spring-boot:run
```

The API will be available at:

```text
http://localhost:8080
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

### Development credentials

The development profile provides sample users:

| Role | Email | Password |
|---|---|---|
| ADMIN | `admin@localhost` | `Admin123!` |
| USER | `user1@localhost` | `User123!` |
| USER | `user2@localhost` | `User123!` |

These credentials are intended exclusively for local development and demonstration.

## Running with Docker Compose

The Docker Compose environment is the recommended way to run the application with PostgreSQL.

```powershell
docker compose up --build
```

This starts:

1. PostgreSQL
2. Database schema migrations through Flyway
3. Initial administrative data
4. Spring Boot using the `prod` profile

The application will be available at:

```text
http://localhost:8080
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

No manual PostgreSQL setup or SQL execution is required.

### Resetting the database

To stop the environment while preserving PostgreSQL data:

```powershell
docker compose down
```

To completely reset the PostgreSQL database and recreate it from scratch:

```powershell
docker compose down -v
docker compose up --build
```

## Authentication

The API uses stateless JWT authentication.

Public endpoints include:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /actuator/health`
- Swagger/OpenAPI endpoints

Protected endpoints require:

```text
Authorization: Bearer <JWT>
```

Users can access their own reservations, while administrators can manage and inspect reservations globally.

## API Documentation

Interactive API documentation is available through Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI specification:

```text
http://localhost:8080/v3/api-docs
```

## Postman

A Postman collection is provided in:

```text
requests/coworking-reservations.postman_collection.json
```

The collection includes requests for:

- Authentication
- User registration
- Space management
- Reservation management
- Occupancy reports
- Actuator endpoints

Authentication requests automatically store JWT tokens as collection variables so subsequent requests can be executed without manually copying tokens.

## Testing

The project contains unit tests and Spring Boot integration tests.

Run the complete test suite with:

```powershell
mvn clean verify
```

The integration tests use H2 so the test suite remains self-contained and does not require Docker or PostgreSQL.

The integration tests exercise real application components including:

- Spring Security
- JWT authentication
- Controllers
- Services
- Spring Data JPA
- Persistence
- Payment simulation
- Reservation concurrency rules

## Resilience

Payment validation represents an external dependency and is protected with Resilience4j.

The payment flow uses:

- Circuit Breaker
- Time Limiter
- Fallback

If the simulated payment provider fails or becomes unavailable, the reservation remains in `PENDING_PAYMENT` instead of blocking indefinitely or being incorrectly confirmed.

The circuit breaker state is exposed through Actuator.

## Concurrency and Transactions

Reservation creation uses a transactional workflow that locks the relevant coworking space while checking for overlapping reservations and creating the pending reservation.

The external payment call is intentionally performed outside that transaction so a slow external dependency does not hold a database lock unnecessarily.

Reservation confirmation then uses a separate transaction and locks the reservation row, preventing races between payment confirmation and cancellation.

## Asynchronous Notifications

Reservation confirmation publishes a domain event.

The notification handler uses:

- `@TransactionalEventListener(AFTER_COMMIT)`
- `@Async`

This ensures the simulated notification is only processed after the reservation transaction commits and does not block the HTTP request.

## GoF Pattern

The application explicitly uses the Observer pattern for reservation confirmation notifications.

```text
Reservation
     |
     | ReservationConfirmedEvent
     v
Event infrastructure
     |
     v
NotificationService
```

This decouples reservation business logic from notification delivery.

## Caching

Occupancy reports are cached using Caffeine.

The cache is invalidated when changes can affect occupancy, including:

- Reservation confirmation
- Reservation cancellation
- Space creation
- Space update
- Space deletion

## Configuration

The application uses Spring profiles and externalized configuration.

### Development

```text
application-dev.yml
```

Uses H2 and automatically creates the database schema.

### Production / Docker

```text
application-prod.yml
```

Uses PostgreSQL and validates the schema managed by Flyway.

Database credentials and JWT secrets are provided through environment variables.

## Design Decisions and Trade-offs

### H2 for automated integration tests

H2 keeps the test suite self-contained and fast. PostgreSQL is used in the Dockerized runtime environment.

### PostgreSQL schema management with Flyway

Flyway provides deterministic and versioned database schema creation while Hibernate remains configured with `ddl-auto=validate`.

### Payment outside the reservation transaction

The payment provider is treated as an external and potentially slow dependency. Holding database locks while waiting for it would unnecessarily increase contention.

The resulting trade-off is that a payment failure leaves the reservation in `PENDING_PAYMENT`, allowing the process to be retried or completed later.

### Cache invalidation

The occupancy cache uses broad invalidation because a reservation can affect multiple possible report ranges. This favors correctness and simplicity over highly granular cache management.

## Out of Scope / Future Improvements

The following are intentionally outside the scope of this technical assessment:

- Real payment provider integration
- Payment idempotency keys
- Payment refunds and compensation workflows
- Persistent notification delivery
- Email provider integration
- Distributed locking
- Refresh tokens
- Advanced reservation search and pagination
- Database migration history beyond the initial schema
- Production-grade secret management
- Full observability stack with external Prometheus/Grafana deployment

## License

MIT