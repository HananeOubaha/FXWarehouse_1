# User Guide - Clustered Data Warehouse

## 📋 Table of Contents

1. [Prerequisites](#prerequisites)
2. [Basic Commands](#basic-commands)
3. [API Endpoints](#api-endpoints)
4. [Postman Tests](#postman-tests)
5. [Coverage Verification](#coverage-verification)

---

## 🔧 Prerequisites

* **Docker** and **Docker Compose** installed
* **Postman** (or any REST client)
* (Optional) **Java 17** and **Maven** for local execution

---

## 🏗️ Architecture

The project uses a **layered architecture** with DTOs and Mappers:

```
┌─────────────────┐
│  API Layer      │  ← DealRequest/DealResponse (DTOs)
└────────┬────────┘
         │ DealMapper
┌────────▼────────┐
│ Service Layer   │  ← Deal (Entity)
└────────┬────────┘
         │
┌────────▼────────┐
│ Repository      │  ← PostgreSQL
└─────────────────┘
```

**Advantages of this architecture:**

* 🔒 **Security**: Internal entities are not exposed directly
* 🔄 **Flexibility**: Database structure can be changed without impacting the API
* ✅ **Validation**

---

## 🚀 Basic Commands

### 1. Start the Application (with Docker)

```bash
# Start the application and database
docker-compose up --build

# Start in detached mode
docker-compose up --build -d

# View application logs
docker logs fx_app -f

# View database logs
docker logs fx_db -f
```

### 2. Stop the Application

```bash
# Stop containers
docker-compose down

# Stop and remove volumes (data will be deleted)
docker-compose down -v
```

### 3. Unit Tests

```bash
# ✅ RECOMMENDED: Run ONLY unit tests (avoids Testcontainers issues)
docker run --rm -v c:\Users\safiy\Desktop\NEWProjet:/app -w /app maven:3.9-eclipse-temurin-17-alpine mvn test -Dtest=DealServiceTest

# Using local Maven (if Java 17 installed)
mvn test -Dtest=DealServiceTest

# Using Makefile
make test-unit

# ⚠️ Run all tests (unit + integration) - Integration tests may fail
docker run --rm -v c:\Users\safiy\Desktop\NEWProjet:/app -w /app maven:3.9-eclipse-temurin-17-alpine mvn test
```

> [!NOTE]
> **Why separate tests?**
> Integration tests use Testcontainers, which requires Docker access. Running inside a Docker container may cause "Docker-in-Docker" issues. Unit tests (testing core business logic) work perfectly without Docker.

### 4. Generate JaCoCo Coverage Report

```bash
# With Docker
docker run --rm -v c:\Users\safiy\Desktop\NEWProjet:/app -w /app maven:3.9-eclipse-temurin-17-alpine mvn test jacoco:report

# With local Maven
mvn test jacoco:report

# Open the report (in a browser)
start target/site/jacoco/index.html
```

### 5. Build the Project

```bash
# With Docker
docker run --rm -v c:\Users\safiy\Desktop\NEWProjet:/app -w /app maven:3.9-eclipse-temurin-17-alpine mvn clean package

# With local Maven
mvn clean package

# With Makefile
make build
```

### 6. Clean the Project

```bash
# With Maven
mvn clean

# With Makefile
make clean
```

---

## 🌐 API Endpoints

### Configuration

* **Base URL**: `http://localhost:8080`
* **Content-Type**: `application/json`

### Main Endpoint

#### **POST /api/deals** - Import Transactions

**Description**: Imports a list of FX transactions. Duplicate transactions are automatically ignored.

**URL**: `http://localhost:8080/api/deals`

**Method**: `POST`

**Headers**:

```
Content-Type: application/json
```

**Body (JSON Array)**:

```json
[
  {
    "dealUniqueId": "FX001",
    "orderingCurrency": "USD",
    "toCurrency": "EUR",
    "dealTimestamp": "2025-11-27T10:30:00",
    "amount": 1000.50
  }
]
```

**Success Response (201 Created)**:

```
Imported 1 deals. Skipped 0 duplicates/errors.
```

**Validation Error Response (400 Bad Request)**:

```json
{
  "orderingCurrency": "Ordering Currency is required",
  "amount": "Amount must be greater than 0"
}
```

---

## 🧪 Postman Tests

### Complete Postman Collection

#### **Test 1: Import Valid Transactions**

**Request**:

```
POST http://localhost:8080/api/deals
Content-Type: application/json
```

**Body**:

```json
[
  {
    "dealUniqueId": "FX001",
    "orderingCurrency": "USD",
    "toCurrency": "EUR",
    "dealTimestamp": "2025-11-27T10:30:00",
    "amount": 1000.50
  },
  {
    "dealUniqueId": "FX002",
    "orderingCurrency": "GBP",
    "toCurrency": "USD",
    "dealTimestamp": "2025-11-27T11:15:00",
    "amount": 2500.75
  },
  {
    "dealUniqueId": "FX003",
    "orderingCurrency": "EUR",
    "toCurrency": "JPY",
    "dealTimestamp": "2025-11-27T12:00:00",
    "amount": 5000.00
  }
]
```

**Expected Result**:

* Status: `201 Created`
* Body: `Imported 3 deals. Skipped 0 duplicates/errors.`

---

#### **Test 2: Duplicate Detection**

**Request** (send the same request twice):

```
POST http://localhost:8080/api/deals
Content-Type: application/json
```

**Body**:

```json
[
  {
    "dealUniqueId": "FX001",
    "orderingCurrency": "USD",
    "toCurrency": "EUR",
    "dealTimestamp": "2025-11-27T10:30:00",
    "amount": 1000.50
  }
]
```

**First Time**:

* Status: `201 Created`
* Body: `Imported 1 deals. Skipped 0 duplicates/errors.`

**Second Time**:

* Status: `201 Created`
* Body: `Imported 0 deals. Skipped 1 duplicates/errors.`

---

#### **Test 3: Validation - Missing Field**

**Request**:

```
POST http://localhost:8080/api/deals
Content-Type: application/json
```

**Body**:

```json
[
  {
    "dealUniqueId": "FX004",
    "toCurrency": "EUR",
    "dealTimestamp": "2025-11-27T10:30:00",
    "amount": 1000.50
  }
]
```

**Expected Result**:

* Status: `400 Bad Request`
* Body:

```json
{
  "orderingCurrency": "Ordering Currency is required"
}
```

---

#### **Test 4: Validation - Invalid Currency Code**

**Request**:

```
POST http://localhost:8080/api/deals
Content-Type: application/json
```

**Body**:

```json
[
  {
    "dealUniqueId": "FX005",
    "orderingCurrency": "US",
    "toCurrency": "EUR",
    "dealTimestamp": "2025-11-27T10:30:00",
    "amount": 1000.50
  }
]
```

**Expected Result**:

* Status: `400 Bad Request`
* Body:

```json
{
  "orderingCurrency": "Ordering Currency must be a valid ISO 4217 code"
}
```

---

#### **Test 5: Validation - Negative Amount**

**Request**:

```
POST http://localhost:8080/api/deals
Content-Type: application/json
```

**Body**:

```json
[
  {
    "dealUniqueId": "FX006",
    "orderingCurrency": "USD",
    "toCurrency": "EUR",
    "dealTimestamp": "2025-11-27T10:30:00",
    "amount": -100
  }
]
```

**Expected Result**:

* Status: `400 Bad Request`
* Body:

```json
{
  "amount": "Amount must be greater than 0"
}
```

---

#### **Test 6: Validation - Zero Amount**

**Request**:

```
POST http://localhost:8080/api/deals
Content-Type: application/json
```

**Body**:

```json
[
  {
    "dealUniqueId": "FX007",
    "orderingCurrency": "USD",
    "toCurrency": "EUR",
    "dealTimestamp": "2025-11-27T10:30:00",
    "amount": 0
  }
]
```

**Expected Result**:

* Status: `400 Bad Request`
* Body:

```json
{
  "amount": "Amount must be greater than 0"
}
```

---

#### **Test 7: Mixed Import (Valid + Duplicates)**

**Prerequisite**: FX001 already imported

**Request**:

```
POST http://localhost:8080/api/deals
Content-Type: application/json
```

**Body**:

```json
[
  {
    "dealUniqueId": "FX001",
    "orderingCurrency": "USD",
    "toCurrency": "EUR",
    "dealTimestamp": "2025-11-27T10:30:00",
    "amount": 1000.50
  },
  {
    "dealUniqueId": "FX008",
    "orderingCurrency": "CAD",
    "toCurrency": "USD",
    "dealTimestamp": "2025-11-27T14:00:00",
    "amount": 3000.00
  },
  {
    "dealUniqueId": "FX009",
    "orderingCurrency": "AUD",
    "toCurrency": "GBP",
    "dealTimestamp": "2025-11-27T15:00:00",
    "amount": 4500.25
  }
]
```

**Expected Result**:

* Status: `201 Created`
* Body: `Imported 2 deals. Skipped 1 duplicates/errors.`

---

## 📊 Coverage Verification

### Generate and View JaCoCo Report

```bash
# 1. Run tests with coverage
docker run --rm -v c:\Users\safiy\Desktop\NEWProjet:/app -w /app maven:3.9-eclipse-temurin-17-alpine mvn test jacoco:report

# 2. Open report in browser
start target/site/jacoco/index.html
```

### Report Interpretation

* **Green**: Code covered by tests
* **Yellow**: Partially covered code
* **Red**: Code not covered

**Coverage Goal**: 80% (configured in `pom.xml`)

---

## 🐛 Troubleshooting

### Application Won’t Start

```bash
# Check logs
docker logs fx_app

# Restart cleanly
docker-compose down -v
docker-compose up --build
```

### Database Connection Error

```bash
# Check if database is running
docker ps | grep fx_db

# Restart only the database
docker-compose restart db
```

### Tests Fail

```bash
# Clean and rerun
mvn clean test

# View detailed logs
mvn test -X
```

---

## 📝 Using Other IDEs

### IntelliJ IDEA

1. Import Maven project
2. Configure Java 17
3. Run: Right-click `ClusteredDataWarehouseApplication.java` → Run
4. Tests: Right-click `src/test` → Run Tests

### Eclipse

1. Import → Maven → Existing Maven Projects
2. Configure Java 17
3. Run As → Spring Boot App
4. Run As → JUnit Test

### VS Code

1. Install extensions: Java Extension Pack, Spring Boot Extension Pack
2. Open project folder
3. F5 to debug or Run without debugging

---

## 🔗 Useful Links

* **JaCoCo Report**: `target/site/jacoco/index.html`
* **Application Logs**: `docker logs fx_app`
* **Database**: `localhost:5432` (user: postgres, password: root, database: fx_deals)
* **API**: `http://localhost:8080/api/deals`

---

## ✅ Complete Test Checklist

* [ ] Start the application with `docker-compose up --build`
* [ ] Check application is accessible at `http://localhost:8080`
* [ ] Test 1: Import valid transactions (3 transactions)
* [ ] Test 2: Duplicate detection (resend FX001)
* [ ] Test 3: Validation - Missing field
* [ ] Test 4: Validation - Invalid currency code
* [ ] Test 5: Validation - Negative amount
* [ ] Test 6: Validation - Zero amount
* [ ] Test 7: Mixed import (valid + duplicates)
* [ ] Run unit tests: `mvn test`
* [ ] Generate coverage report: `mvn test jacoco:report`
* [ ] Verify service coverage is 100%
