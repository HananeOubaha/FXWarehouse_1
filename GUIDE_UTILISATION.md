# Guide d'Utilisation - Clustered Data Warehouse

## 📋 Table des Matières
1. [Pré-requis](#pré-requis)
2. [Commandes de Base](#commandes-de-base)
3. [Endpoints API](#endpoints-api)
4. [Tests Postman](#tests-postman)
5. [Vérification de la Couverture](#vérification-de-la-couverture)

---

## 🔧 Pré-requis

- **Docker** et **Docker Compose** installés
- **Postman** (ou tout autre client REST)
- (Optionnel) **Java 17** et **Maven** pour exécution locale

---

## 🏗️ Architecture

Le projet utilise une **architecture en couches** avec DTOs et Mappers :

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

**Avantages de cette architecture :**
- 🔒 **Sécurité** : L'entity interne n'est pas exposée directement
- 🔄 **Flexibilité** : Peut modifier la structure de la BD sans impacter l'API
- ✅ **Validation

## 🚀 Commandes de Base

### 1. Démarrer l'Application (avec Docker)

```bash
# Démarrer l'application et la base de données
docker-compose up --build

# Démarrer en arrière-plan (mode détaché)
docker-compose up --build -d

# Voir les logs de l'application
docker logs fx_app -f

# Voir les logs de la base de données
docker logs fx_db -f
```

### 2. Arrêter l'Application

```bash
# Arrêter les conteneurs
docker-compose down

# Arrêter et supprimer les volumes (données effacées)
docker-compose down -v
```

### 3. Tests Unitaires

```bash
# ✅ RECOMMANDÉ : Exécuter UNIQUEMENT les tests unitaires (évite les problèmes Testcontainers)
docker run --rm -v c:\Users\safiy\Desktop\NEWProjet:/app -w /app maven:3.9-eclipse-temurin-17-alpine mvn test -Dtest=DealServiceTest

# Avec Maven local (si Java 17 installé)
mvn test -Dtest=DealServiceTest

# Avec Makefile
make test-unit

# ⚠️ Tous les tests (unitaires + intégration) - Les tests d'intégration peuvent échouer
docker run --rm -v c:\Users\safiy\Desktop\NEWProjet:/app -w /app maven:3.9-eclipse-temurin-17-alpine mvn test
```

> [!NOTE]
> **Pourquoi séparer les tests ?**
> Les tests d'intégration utilisent Testcontainers qui nécessite un accès Docker. Dans un conteneur Docker, cela crée un problème "Docker-in-Docker". Les tests unitaires (qui testent la logique métier principale) fonctionnent parfaitement sans Docker.

### 4. Générer le Rapport de Couverture JaCoCo

```bash
# Avec Docker
docker run --rm -v c:\Users\safiy\Desktop\NEWProjet:/app -w /app maven:3.9-eclipse-temurin-17-alpine mvn test jacoco:report

# Avec Maven local
mvn test jacoco:report

# Ouvrir le rapport (dans un navigateur)
start target/site/jacoco/index.html
```

### 5. Build du Projet

```bash
# Avec Docker
docker run --rm -v c:\Users\safiy\Desktop\NEWProjet:/app -w /app maven:3.9-eclipse-temurin-17-alpine mvn clean package

# Avec Maven local
mvn clean package

# Avec Makefile
make build
```

### 6. Nettoyer le Projet

```bash
# Avec Maven
mvn clean

# Avec Makefile
make clean
```

---

## 🌐 Endpoints API

### Configuration
- **Base URL** : `http://localhost:8080`
- **Content-Type** : `application/json`

### Endpoint Principal

#### **POST /api/deals** - Importer des Transactions

**Description** : Importe une liste de transactions FX. Les transactions dupliquées sont automatiquement ignorées.

**URL** : `http://localhost:8080/api/deals`

**Méthode** : `POST`

**Headers** :
```
Content-Type: application/json
```

**Body (JSON Array)** :
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

**Réponse - Succès (201 Created)** :
```
Imported 1 deals. Skipped 0 duplicates/errors.
```

**Réponse - Validation Error (400 Bad Request)** :
```json
{
  "orderingCurrency": "Ordering Currency is required",
  "amount": "Amount must be greater than 0"
}
```

---

## 🧪 Tests Postman

### Collection Postman Complète

#### **Test 1 : Import de Transactions Valides**

**Request** :
```
POST http://localhost:8080/api/deals
Content-Type: application/json
```

**Body** :
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

**Résultat Attendu** :
- Status: `201 Created`
- Body: `Imported 3 deals. Skipped 0 duplicates/errors.`

---

#### **Test 2 : Détection de Doublons**

**Request** (envoyer 2 fois la même requête) :
```
POST http://localhost:8080/api/deals
Content-Type: application/json
```

**Body** :
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

**Première fois** :
- Status: `201 Created`
- Body: `Imported 1 deals. Skipped 0 duplicates/errors.`

**Deuxième fois** :
- Status: `201 Created`
- Body: `Imported 0 deals. Skipped 1 duplicates/errors.`

---

#### **Test 3 : Validation - Champ Manquant**

**Request** :
```
POST http://localhost:8080/api/deals
Content-Type: application/json
```

**Body** :
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

**Résultat Attendu** :
- Status: `400 Bad Request`
- Body:
```json
{
  "orderingCurrency": "Ordering Currency is required"
}
```

---

#### **Test 4 : Validation - Code Devise Invalide**

**Request** :
```
POST http://localhost:8080/api/deals
Content-Type: application/json
```

**Body** :
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

**Résultat Attendu** :
- Status: `400 Bad Request`
- Body:
```json
{
  "orderingCurrency": "Ordering Currency must be a valid ISO 4217 code"
}
```

---

#### **Test 5 : Validation - Montant Négatif**

**Request** :
```
POST http://localhost:8080/api/deals
Content-Type: application/json
```

**Body** :
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

**Résultat Attendu** :
- Status: `400 Bad Request`
- Body:
```json
{
  "amount": "Amount must be greater than 0"
}
```

---

#### **Test 6 : Validation - Montant Zero**

**Request** :
```
POST http://localhost:8080/api/deals
Content-Type: application/json
```

**Body** :
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

**Résultat Attendu** :
- Status: `400 Bad Request`
- Body:
```json
{
  "amount": "Amount must be greater than 0"
}
```

---

#### **Test 7 : Import Mixte (Valides + Doublons)**

**Pré-requis** : Avoir déjà importé FX001

**Request** :
```
POST http://localhost:8080/api/deals
Content-Type: application/json
```

**Body** :
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

**Résultat Attendu** :
- Status: `201 Created`
- Body: `Imported 2 deals. Skipped 1 duplicates/errors.`

---

## 📊 Vérification de la Couverture

### Générer et Consulter le Rapport JaCoCo

```bash
# 1. Exécuter les tests avec couverture
docker run --rm -v c:\Users\safiy\Desktop\NEWProjet:/app -w /app maven:3.9-eclipse-temurin-17-alpine mvn test jacoco:report

# 2. Ouvrir le rapport dans le navigateur
start target/site/jacoco/index.html
```

### Interprétation du Rapport

- **Vert** : Code couvert par les tests
- **Jaune** : Code partiellement couvert
- **Rouge** : Code non couvert

**Objectif de couverture** : 80% (configuré dans `pom.xml`)

---

## 🐛 Troubleshooting

### L'application ne démarre pas

```bash
# Vérifier les logs
docker logs fx_app

# Redémarrer proprement
docker-compose down -v
docker-compose up --build
```

### Erreur de connexion à la base de données

```bash
# Vérifier que la base de données est démarrée
docker ps | grep fx_db

# Redémarrer uniquement la base
docker-compose restart db
```

### Les tests échouent

```bash
# Nettoyer et relancer
mvn clean test

# Voir les logs détaillés
mvn test -X
```

---

## 📝 Utilisation avec d'autres IDE

### IntelliJ IDEA
1. Importer le projet Maven
2. Configurer Java 17
3. Run : Click droit sur `ClusteredDataWarehouseApplication.java` → Run
4. Tests : Click droit sur `src/test` → Run Tests

### Eclipse
1. Import → Maven → Existing Maven Projects
2. Configurer Java 17
3. Run As → Spring Boot App
4. Run As → JUnit Test

### VS Code
1. Installer les extensions : Java Extension Pack, Spring Boot Extension Pack
2. Ouvrir le dossier du projet
3. F5 pour déboguer ou Run sans déboguer

---

## 🔗 Liens Utiles

- **Rapport JaCoCo** : `target/site/jacoco/index.html`
- **Logs Application** : `docker logs fx_app`
- **Base de données** : `localhost:5432` (user: postgres, password: root, database: fx_deals)
- **API** : `http://localhost:8080/api/deals`

---

## ✅ Checklist de Test Complet

- [ ] Démarrer l'application avec `docker-compose up --build`
- [ ] Vérifier que l'application est accessible sur `http://localhost:8080`
- [ ] Test 1 : Import de transactions valides (3 transactions)
- [ ] Test 2 : Détection de doublons (renvoyer FX001)
- [ ] Test 3 : Validation - Champ manquant
- [ ] Test 4 : Validation - Code devise invalide
- [ ] Test 5 : Validation - Montant négatif
- [ ] Test 6 : Validation - Montant zéro
- [ ] Test 7 : Import mixte (valides + doublons)
- [ ] Exécuter les tests unitaires : `mvn test`
- [ ] Générer le rapport de couverture : `mvn test jacoco:report`
- [ ] Vérifier que la couverture du service est à 100%
