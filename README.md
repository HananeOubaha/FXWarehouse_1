# Clustered Data Warehouse - FX Deals

## Description

Spring Boot application for importing and storing FX (Foreign Exchange) transactions in a PostgreSQL database. The system ensures **idempotence** (no duplicates) and **partial persistence** (valid transactions are saved even if others fail).

## Features

✅ **Import FX transactions** via REST API
✅ **Data validation** (required fields, ISO 4217 currency codes)
✅ **Idempotence**: duplicate detection and rejection
✅ **No rollback**: each valid transaction is saved independently
✅ **Robust error handling** with logging
✅ **Unit and integration tests**
✅ **Docker Compose** for easy deployment

## Prerequisites

* **Java 17** or higher
* **Maven 3.9+**
* **Docker** and **Docker Compose**
* (Optional) **Make** for simplified commands

## Installation

### 1. Clone the repository

```bash
git clone <repository-url>
cd clustered-data-warehouse
```

### 2. Build the project

```bash
mvn clean package -DskipTests
```

or with Make:

```bash
make build
```

## Usage

### Start the application with Docker Compose

```bash
docker-compose up --build
```

or with Make:

```bash
make run
```

The application will be available at `http://localhost:8080`
The PostgreSQL database will be on port `5432`

### Start only the database

```bash
make run-db
```

Then run the application locally:

```bash
mvn spring-boot:run
```

## API Endpoints

### Import Transactions

**POST** `/api/deals`

**Headers:**

```
Content-Type: application/json
```

**Body (example):**

```json
[
  {
    "dealUniqueId": "FX001",
    "orderingCurrency": "USD",
    "toCurrency": "EUR",
    "dealTimestamp": "2025-11-26T10:30:00",
    "amount": 1000.50
  },
  {
    "dealUniqueId": "FX002",
    "orderingCurrency": "GBP",
    "toCurrency": "USD",
    "dealTimestamp": "2025-11-26T11:15:00",
    "amount": 2500.75
  }
]
```

**Response (201 Created):**

```
Imported 2 deals. Skipped 0 duplicates/errors.
```

### Test with curl

```bash
curl -X POST http://localhost:8080/api/deals \
  -H "Content-Type: application/json" \
  -d @sample-deals.json
```

### Test with PowerShell

```powershell
$json = Get-Content sample-deals.json -Raw
Invoke-WebRequest -Uri http://localhost:8080/api/deals -Method POST -Body $json -ContentType "application/json"
```

## Project Structure

```
clustered-data-warehouse/
├── src/
│   ├── main/
│   │   ├── java/com/bloomberg/fx/
│   │   │   ├── ClusteredDataWarehouseApplication.java
│   │   │   ├── controller/
│   │   │   │   └── DealController.java
│   │   │   ├── service/
│   │   │   │   └── DealService.java
│   │   │   ├── repository/
│   │   │   │   └── DealRepository.java
│   │   │   ├── model/
│   │   │   │   └── Deal.java
│   │   │   └── exception/
│   │   │       └── GlobalExceptionHandler.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/bloomberg/fx/
│           ├── service/
│           │   └── DealServiceTest.java
│           └── controller/
│               └── DealControllerIntegrationTest.java
├── docker-compose.yml
├── Dockerfile
├── Makefile
├── pom.xml
├── sample-deals.json
└── README.md
```

## Data Validation

The system automatically validates:

* ✅ **dealUniqueId**: required, unique
* ✅ **orderingCurrency**: required, ISO 4217 format (3 uppercase letters)
* ✅ **toCurrency**: required, ISO 4217 format (3 uppercase letters)
* ✅ **dealTimestamp**: required, ISO 8601 format
* ✅ **amount**: required, > 0

**Example of validation error (400 Bad Request):**

```json
{
  "orderingCurrency": "Ordering Currency must be a valid ISO 4217 code",
  "amount": "Amount is required"
}
```

## Tests

### Run all tests

```bash
mvn test
```

or with Make:

```bash
make test
```

**Note:** Integration tests require Docker to be running.

### Run only unit tests

```bash
mvn test -Dtest=DealServiceTest
```

or with Make:

```bash
make test-unit
```

### Test Coverage

* ✅ Unit tests for service (idempotence, validation)
* ✅ Integration tests with Testcontainers (PostgreSQL)

## Error Handling

### Idempotence

Transactions with an already existing `dealUniqueId` are **ignored** (no error). The system logs a warning and continues processing.

### No Rollback

Each transaction is processed in its own Spring transaction (`REQUIRES_NEW`). If a transaction fails, others continue to be processed and saved.

## Configuration

`application.properties` file:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/fx_deals
spring.datasource.username=bloomberg
spring.datasource.password=password

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Logging
logging.level.com.bloomberg.fx=DEBUG
```

## Make Commands

| Command          | Description                      |
| ---------------- | -------------------------------- |
| `make build`     | Build the project                |
| `make run`       | Start with Docker Compose        |
| `make run-db`    | Start only PostgreSQL            |
| `make stop`      | Stop containers                  |
| `make clean`     | Clean project and Docker volumes |
| `make test`      | Run all tests                    |
| `make test-unit` | Run only unit tests              |

## Technologies Used

* **Spring Boot 3.2.3** (Web, Data JPA, Validation)
* **PostgreSQL 15**
* **Lombok** (reduce boilerplate)
* **Testcontainers** (integration tests)
* **Docker & Docker Compose**
* **Maven**

## Author

Hanane Oubaha – Clustered Data Warehouse Project

## License

All rights reserved © 2025
