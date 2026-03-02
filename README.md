# Personal Finance Budget Tracker - Backend

A Spring Boot 3 REST API for managing personal finances including transactions, budgets, categories, financial goals, and ML-based spending forecasting.

## Tech Stack

- Java 17
- Spring Boot 3.2.3
- Spring Security 6 with JWT Authentication
- Spring Data JPA / Hibernate
- MySQL 8 (Local Development)
- H2 Database (Testing)
- Apache Commons Math 3.6.1 (ML Forecasting)
- Maven
- Swagger / OpenAPI (springdoc-openapi)

## Project Structure

```
src/main/java/com/personalfinance/tracker/
    config/          Security, CORS, JWT configuration
    controller/      REST API controllers
    dto/             Data Transfer Objects with validation
    exception/       Global exception handling
    model/           JPA entity classes and enums
    repository/      Spring Data JPA repositories
    service/         Business logic layer
```

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- MySQL 8 running on localhost:3306

## Database Setup

Create a MySQL database (auto-created by Spring JPA if configured):

```sql
CREATE DATABASE IF NOT EXISTS personal_finance_tracker;
```

Default local configuration:
- Host: localhost:3306
- Username: root
- Password: root
- Database: personal_finance_tracker

## Running Locally

```bash
mvn clean install
mvn spring-boot:run
```

The API will start on `http://localhost:8080`.

## API Documentation

Swagger UI is available at:
- `http://localhost:8080/swagger-ui.html`

OpenAPI docs:
- `http://localhost:8080/v3/api-docs`

## API Endpoints

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/auth/register | Register a new user |
| POST | /api/auth/login | Login and receive JWT token |

### Transactions

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/transactions | List all transactions |
| POST | /api/transactions | Create a transaction |
| GET | /api/transactions/{id} | Get transaction by ID |
| PUT | /api/transactions/{id} | Update a transaction |
| DELETE | /api/transactions/{id} | Delete a transaction |

### Budgets

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/budgets | List all budgets |
| POST | /api/budgets | Create a budget |
| GET | /api/budgets/{id} | Get budget by ID |
| PUT | /api/budgets/{id} | Update a budget |
| DELETE | /api/budgets/{id} | Delete a budget |

### Categories

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/categories | List all categories |
| POST | /api/categories | Create a category |
| GET | /api/categories/{id} | Get category by ID |
| PUT | /api/categories/{id} | Update a category |
| DELETE | /api/categories/{id} | Delete a category |

### Financial Goals

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/goals | List all goals |
| POST | /api/goals | Create a financial goal |
| GET | /api/goals/{id} | Get goal by ID |
| PUT | /api/goals/{id} | Update a goal |
| DELETE | /api/goals/{id} | Delete a goal |

### Dashboard

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/dashboard/summary | Get dashboard summary stats |

### Forecasting

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/forecast/spending | Get ML-based spending forecast |

## Spending Forecast

The forecasting feature uses Apache Commons Math `SimpleRegression` (linear regression) to predict future spending based on historical transaction data. It returns:

- Predicted monthly spending for the next 3 months
- Trend direction (INCREASING, DECREASING, or STABLE)
- Confidence score (R-squared value)

## Authentication

All endpoints except `/api/auth/**` require a valid JWT token in the Authorization header:

```
Authorization: Bearer <jwt-token>
```

Tokens expire after 24 hours (configurable via `app.jwt.expiration`).

## Input Validation

All request bodies are validated using Jakarta Bean Validation:

- Required fields: `@NotNull`, `@NotBlank`
- Monetary amounts: `@DecimalMin("0.01")`, `@DecimalMax("999999999.99")`
- String lengths: `@Size(max=500)` on descriptions
- Dates: `@PastOrPresent` on transaction dates
- Enums: Valid enum values enforced

Invalid requests return structured error responses with field-level messages.

## Static Code Analysis

The project includes Maven plugins for static analysis:

| Tool | Purpose | Command |
|------|---------|---------|
| SpotBugs | Bug detection in bytecode | `mvn spotbugs:check` |
| PMD | Code style and complexity | `mvn pmd:check` |
| OWASP Dependency-Check | Known CVEs in dependencies | `mvn dependency-check:check` |
| JaCoCo | Code coverage (min 60%) | `mvn jacoco:report` |

All checks run automatically during `mvn verify`.

## Testing

```bash
mvn test
```

Tests use H2 in-memory database configured in `src/test/resources/application.properties`.

## Building

```bash
mvn clean package -DskipTests
```

The JAR file is generated at `target/personal-finance-tracker-0.0.1-SNAPSHOT.jar`.

## Deployment

The backend is deployed to an AWS EC2 instance via GitHub Actions CI/CD pipeline. The application runs as a systemd service on the EC2 instance.

## Configuration

Key configuration properties in `application.properties`:

| Property | Description | Default |
|----------|-------------|---------|
| spring.datasource.url | Database JDBC URL | jdbc:mysql://localhost:3306/personal_finance_tracker |
| spring.datasource.username | Database username | root |
| spring.datasource.password | Database password | root |
| server.port | Application port | 8080 |
| app.jwt.secret | JWT signing secret | (configured per environment) |
| app.jwt.expiration | JWT expiration in ms | 86400000 (24 hours) |
