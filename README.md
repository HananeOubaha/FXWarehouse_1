# Clustered Data Warehouse - FX Deals

## Description

Application Spring Boot permettant d'importer et de stocker des transactions FX (Foreign Exchange) dans une base de données PostgreSQL. Le système garantit l'idempotence (pas de doublons) et la persistance partielle (les transactions valides sont enregistrées même si d'autres échouent).

## Fonctionnalités

✅ **Importation de transactions FX** via API REST  
✅ **Validation des données** (champs requis, formats ISO 4217 pour devises)  
✅ **Idempotence** : détection et rejet des doublons  
✅ **Pas de rollback** : chaque transaction valide est enregistrée indépendamment  
✅ **Gestion d'erreurs** robuste avec logging  
✅ **Tests unitaires et d'intégration**  
✅ **Docker Compose** pour déploiement facile

## Prérequis

- **Java 17** ou supérieur
- **Maven 3.9+**
- **Docker** et **Docker Compose**
- (Optionnel) **Make** pour les commandes simplifiées

## Installation

### 1. Cloner le repository

```bash
git clone <repository-url>
cd clustered-data-warehouse
```

### 2. Compiler le projet

```bash
mvn clean package -DskipTests
```

ou avec Make:

```bash
make build
```

## Utilisation

### Démarrer l'application avec Docker Compose

```bash
docker-compose up --build
```

ou avec Make:

```bash
make run
```

L'application sera disponible sur `http://localhost:8080`  
La base de données PostgreSQL sur le port `5432`

### Démarrer uniquement la base de données

```bash
make run-db
```

Puis lancer l'application localement:

```bash
mvn spring-boot:run
```

## API Endpoints

### Importer des transactions

**POST** `/api/deals`

**Headers:**
```
Content-Type: application/json
```

**Body (exemple):**
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

**Réponse (201 Created):**
```
Imported 2 deals. Skipped 0 duplicates/errors.
```

### Tester avec curl

```bash
curl -X POST http://localhost:8080/api/deals \
  -H "Content-Type: application/json" \
  -d @sample-deals.json
```

### Tester avec PowerShell

```powershell
$json = Get-Content sample-deals.json -Raw
Invoke-WebRequest -Uri http://localhost:8080/api/deals -Method POST -Body $json -ContentType "application/json"
```

## Structure du Projet

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

## Validation des Données

Le système valide automatiquement:

- ✅ **dealUniqueId** : obligatoire, unique
- ✅ **orderingCurrency** : obligatoire, format ISO 4217 (3 lettres majuscules)
- ✅ **toCurrency** : obligatoire, format ISO 4217 (3 lettres majuscules)
- ✅ **dealTimestamp** : obligatoire, format ISO 8601
- ✅ **amount** : obligatoire, > 0

**Exemple d'erreur de validation (400 Bad Request):**

```json
{
  "orderingCurrency": "Ordering Currency must be a valid ISO 4217 code",
  "amount": "Amount is required"
}
```

## Tests

### Exécuter tous les tests

```bash
mvn test
```

ou avec Make:

```bash
make test
```

**Note:** Les tests d'intégration nécessitent Docker en cours d'exécution.

### Exécuter uniquement les tests unitaires

```bash
mvn test -Dtest=DealServiceTest
```

ou avec Make:

```bash
make test-unit
```

### Couverture des tests

- ✅ Tests unitaires du service (idempotence, validation)
- ✅ Tests d'intégration avec Testcontainers (PostgreSQL)

## Gestion des Erreurs

### Idempotence

Les transactions avec un `dealUniqueId` déjà existant sont **ignorées** (pas d'erreur). Le système log un avertissement et continue le traitement.

### Pas de Rollback

Chaque transaction est traitée dans sa propre transaction Spring (`REQUIRES_NEW`). Si une transaction échoue, les autres continuent d'être traitées et enregistrées.

## Configuration

Fichier `application.properties`:

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

## Commandes Make

| Commande | Description |
|----------|-------------|
| `make build` | Compiler le projet |
| `make run` | Démarrer avec Docker Compose |
| `make run-db` | Démarrer uniquement PostgreSQL |
| `make stop` | Arrêter les conteneurs |
| `make clean` | Nettoyer le projet et les volumes Docker |
| `make test` | Exécuter tous les tests |
| `make test-unit` | Exécuter les tests unitaires uniquement |

## Technologies Utilisées

- **Spring Boot 3.2.3** (Web, Data JPA, Validation)
- **PostgreSQL 15**
- **Lombok** (réduction du boilerplate)
- **Testcontainers** (tests d'intégration)
- **Docker & Docker Compose**
- **Maven**

## Auteur

Développé pour Bloomberg - Projet Clustered Data Warehouse

## Licence

Tous droits réservés © 2025
