
```markdown
# 💸 PayVerge

**PayVerge** is a distributed digital payment platform built with **Java, Spring Boot, React, Apache Kafka, Redis, and Docker**.

The project demonstrates how a digital payment system can be designed using a **microservices architecture**, combining synchronous communication for critical payment operations with asynchronous event-driven processing for rewards and notifications.

---

## 🚀 Overview

PayVerge provides a simplified digital wallet and payment experience where users can:

- Create an account
- Authenticate using JWT
- Manage their digital wallet
- Add funds
- Send money to other users
- View transaction history
- Receive transaction notifications
- Earn rewards from transactions

The frontend communicates with the backend through a centralized **API Gateway**, while backend services communicate synchronously through REST APIs and asynchronously through Apache Kafka.

---

# 🏗️ Architecture

```text
                         ┌──────────────────────────┐
                         │       React + Vite       │
                         │        Frontend          │
                         │         :5173            │
                         └────────────┬─────────────┘
                                      │
                                      │ HTTP
                                      ▼
                         ┌──────────────────────────┐
                         │       API Gateway        │
                         │          :8080           │
                         │                          │
                         │ JWT Authentication       │
                         │ Request Routing          │
                         │ Rate Limiting            │
                         └────────────┬─────────────┘
                                      │
             ┌────────────────────────┼────────────────────────┐
             │                        │                        │
             ▼                        ▼                        ▼
      ┌─────────────┐         ┌──────────────┐         ┌─────────────┐
      │    User     │         │   Wallet     │         │ Transaction │
      │   Service   │         │   Service    │         │   Service   │
      │    :8081    │         │    :8088     │         │    :8082    │
      └─────────────┘         └──────────────┘         └──────┬──────┘
                                                               │
                                                               │
                                                        Transaction Event
                                                               │
                                                               ▼
                                                       ┌──────────────┐
                                                       │    Kafka     │
                                                       │txn-initiated │
                                                       └──────┬───────┘
                                                              │
                                           ┌───────────────────┴───────────────────┐
                                           │                                       │
                                           ▼                                       ▼
                                   ┌─────────────┐                         ┌────────────────┐
                                   │   Reward    │                         │  Notification  │
                                   │   Service   │                         │    Service     │
                                   │    :8089    │                         │     :8084      │
                                   └─────────────┘                         └────────────────┘


                         ┌──────────────────────────┐
                         │          Redis           │
                         │          :6379           │
                         │                          │
                         │ Gateway Rate Limiting    │
                         └──────────────────────────┘
```

---

# 🧩 Microservices

| Service |   Port | Responsibility |
|---|-------:|---|
| **API Gateway** | `8080` | Central entry point, routing, JWT filtering and rate limiting |
| **User Service** | `8081` | User registration, authentication and user management |
| **Transaction Service** | `8082` | Payment orchestration and transaction lifecycle |
| **Notification Service** | `8084` | Transaction notifications |
| **Reward Service** | `8089` | Reward processing |
| **Wallet Service** | `8088` | Wallet balances, holds, credits and debits |

---

# 🌐 API Gateway

The frontend communicates with the **API Gateway on port `8080`** rather than directly accessing individual microservices.

All application APIs follow the `/api/v1/**` convention.

```text
React Frontend
      │
      ▼
API Gateway :8080
      │
      ├── /api/v1/auth/**          → User Service
      ├── /api/v1/users/**         → User Service
      ├── /api/v1/wallets/**       → Wallet Service
      ├── /api/v1/transactions/**  → Transaction Service
      ├── /api/v1/rewards/**       → Reward Service
      └── /api/v1/notify/**        → Notification Service
```

This provides a single backend entry point and keeps internal microservice locations hidden from the frontend.

---

# 💳 Payment Processing

A payment is coordinated by the **Transaction Service**.

```text
                    Transaction Request
                            │
                            ▼
                         PENDING
                            │
                            ▼
                   Place Wallet Hold
                            │
                            ▼
                   Verify Receiver
                            │
                            ▼
                  Capture Sender Hold
                            │
                            ▼
                    Credit Receiver
                            │
                   ┌────────┴────────┐
                   │                 │
                Success            Failure
                   │                 │
                   ▼                 ▼
                SUCCESS      Compensating Action
                   │
                   ▼
            Publish Kafka Event
```

The Transaction Service coordinates the payment workflow with the Wallet Service.

---

## 💰 Wallet Hold Workflow

Before finalizing a payment, funds can be placed on hold.

```text
              Available Balance
                     │
                     ▼
                 Place Hold
                     │
              ┌──────┴──────┐
              │             │
              ▼             ▼
         Capture Hold   Release Hold
              │             │
              ▼             ▼
       Finalize Funds   Restore Funds
```

This prevents funds from being finalized before the transaction has successfully completed the required operations.

---

# 🔄 Transaction Reliability

PayVerge incorporates several mechanisms to improve payment reliability.

### Idempotency

Transaction processing includes idempotency handling to prevent duplicate payment processing.

### Wallet Holds

Funds can be reserved before the payment is finalized.

### Compensating Actions

If a later operation fails after funds have already been captured, the system performs a compensating operation to restore the sender's funds.

### Transaction States

Transactions follow an explicit lifecycle:

```text
                    PENDING
                       │
              ┌────────┴────────┐
              │                 │
              ▼                 ▼
           SUCCESS            FAILED
```

This allows the system to represent the state of a payment throughout its lifecycle.

---

# ⚡ Event-Driven Architecture

After a successful transaction, the Transaction Service publishes an event to Apache Kafka.

The event is published to:

```text
txn-initiated
```

Two independent Kafka consumer groups process the event.

```text
                         txn-initiated
                              │
                 ┌────────────┴────────────┐
                 │                         │
                 ▼                         ▼
          reward-group              notification-group
                 │                         │
                 ▼                         ▼
          Reward Service            Notification Service
                 │                         │
                 ▼                         ▼
            Reward Data            Notification Data
```

Because Reward Service and Notification Service use different consumer groups, both services independently receive and process the same transaction event.

This keeps secondary processing decoupled from the core payment workflow.

---

# 🔄 Synchronous Communication

Critical payment operations use synchronous REST communication.

```text
Transaction Service
        │
        │ REST
        ▼
Wallet Service
```

The Transaction Service requires immediate responses for operations such as:

- Checking wallet availability
- Placing a wallet hold
- Capturing a hold
- Releasing a hold
- Crediting the receiver

This allows the Transaction Service to make decisions based on the current result of wallet operations.

---

# 📢 Asynchronous Communication

Secondary operations use Kafka.

```text
Transaction Service
        │
        │ Kafka Event
        ▼
      Kafka
      /   \
     ▼     ▼
 Reward  Notification
```

Rewards and notifications are therefore processed independently from the main payment workflow.

---

# 🔐 Authentication & Security

PayVerge uses **Spring Security and JWT** for authentication.

## Authentication Flow

```text
                         Login
                           │
                           ▼
                  Validate Credentials
                           │
                           ▼
                      Generate JWT
                           │
                           ▼
                   Return JWT
                           │
                           ▼
              Authorization: Bearer <JWT>
                           │
                           ▼
                  JWT Request Filter
                           │
              ┌────────────┼────────────┐
              │            │            │
              ▼            ▼            ▼
           Validate     Extract       Extract
            Token       User ID        Role
              │            │            │
              └────────────┼────────────┘
                           ▼
                 Authenticated Request
                           │
                           ▼
                  Protected API Endpoint
```

JWT tokens contain information such as:

- User ID
- Email
- Role
- Issued-at timestamp
- Expiration timestamp

Passwords are protected using **BCrypt hashing**.

---

# 🚦 Redis Rate Limiting

The API Gateway uses **Redis-backed request rate limiting** for selected APIs.

Currently rate-limited endpoints include:

```text
/api/v1/transactions/**
/api/v1/rewards/**
/api/v1/notify/**
```

Redis runs on:

```text
localhost:6379
```

The request flow is:

```text
Client
  │
  ▼
API Gateway
  │
  ▼
RequestRateLimiter
  │
  ▼
Redis
  │
  ▼
Backend Service
```

Redis maintains the state required by the Gateway's rate-limiting mechanism.

---

# 🧱 Infrastructure

PayVerge uses Docker Compose for local infrastructure.

Currently the infrastructure includes:

| Component | Port |
|---|---:|
| ZooKeeper | `2181` |
| Kafka | `9092` |
| Redis | `6379` |

The application services themselves can be started as individual Spring Boot applications during local development.

---

# 🛠️ Technology Stack

## Backend

- Java
- Spring Boot
- Spring Security
- JWT
- Spring Data JPA
- Maven
- REST APIs

## Messaging

- Apache Kafka
- Kafka Consumer Groups
- ZooKeeper

## Caching / Rate Limiting

- Redis

## Frontend

- React 19
- React Router 7
- Vite
- JavaScript
- Sass (SCSS)

## Infrastructure

- Docker
- Docker Compose

## Database

- H2 Database

> H2 is currently used for local development. A production deployment would use a persistent database configuration.

---

# 📁 Project Structure

```text
payverge/
│
├── backend/
│   ├── api-gateway/
│   ├── user-service/
│   ├── wallet-service/
│   ├── transaction-service/
│   ├── reward-service/
│   ├── notification-service/
│   │
│   ├── docker-compose.yml
│   └── pom.xml
│
├── frontend/
│   └── payverge-ui/
│
├── .gitignore
└── README.md
```

---

# 🚀 Getting Started

## Prerequisites

Make sure you have the following installed:

- Java
- Maven
- Node.js
- npm
- Docker
- Git

---

## 1. Start Infrastructure

Navigate to the backend directory:

```bash
cd backend
```

Start Kafka, ZooKeeper and Redis:

```bash
docker compose up -d
```

Verify the containers:

```bash
docker ps
```

You should see:

```text
Kafka
ZooKeeper
Redis
```

---

## 2. Build the Backend

From the backend directory:

```bash
mvn clean install
```

---

## 3. Start the Microservices

Start each Spring Boot service.

### User Service

```bash
cd backend/user-service
mvn spring-boot:run
```

### Wallet Service

```bash
cd backend/wallet-service
mvn spring-boot:run
```

### Transaction Service

```bash
cd backend/transaction-service
mvn spring-boot:run
```

### Reward Service

```bash
cd backend/reward-service
mvn spring-boot:run
```

### Notification Service

```bash
cd backend/notification-service
mvn spring-boot:run
```

### API Gateway

```bash
cd backend/api-gateway
mvn spring-boot:run
```

The API Gateway will be available at:

```text
http://localhost:8080
```

---

# 🎨 Start the Frontend

Navigate to:

```bash
cd frontend/payverge-ui
```

Install dependencies:

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

The frontend will normally be available at:

```text
http://localhost:5173
```

---

# 🔌 API Routing

All frontend API requests are sent through the API Gateway.

| API Endpoint | Destination |
|---|---|
| `/api/v1/auth/**` | User Service |
| `/api/v1/users/**` | User Service |
| `/api/v1/wallets/**` | Wallet Service |
| `/api/v1/transactions/**` | Transaction Service |
| `/api/v1/rewards/**` | Reward Service |
| `/api/v1/notify/**` | Notification Service |

The frontend therefore only needs to communicate with:

```text
http://localhost:8080
```

rather than directly accessing individual microservice ports.

---

# 🧪 Example End-to-End Payment

Suppose:

```text
User 1 → User 2
Amount → ₹400
```

The request follows this flow:

```text
                         React
                           │
                           ▼
                    API Gateway :8080
                           │
                           ▼
                 Transaction Service
                           │
                           ▼
                   Wallet Service
                           │
              ┌────────────┴────────────┐
              │                         │
        Sender Hold                Receiver
              │                         │
              └────────────┬────────────┘
                           │
                           ▼
                     Payment Success
                           │
                           ▼
                      Kafka Event
                           │
                 ┌─────────┴─────────┐
                 ▼                   ▼
              Reward             Notification
              Service              Service
```

For example, the receiver can receive a notification such as:

```text
💰 ₹400 received from user 1
```

while the payment sender can receive the corresponding reward according to the application's reward rules.

---

# 🧠 Engineering Concepts Demonstrated

PayVerge was built to explore practical backend and distributed-system concepts:

- Microservices Architecture
- API Gateway Pattern
- JWT Authentication
- Spring Security
- BCrypt Password Hashing
- REST APIs
- Synchronous Service-to-Service Communication
- Event-Driven Architecture
- Apache Kafka
- Kafka Consumer Groups
- Distributed Transaction Coordination
- Saga-Style Compensating Actions
- Idempotent Transaction Processing
- Wallet Holds
- Transaction State Management
- Redis-Backed Rate Limiting
- Dockerized Infrastructure
- Client-Side Routing
- React Application Architecture

---

# 📊 Current Project Status

### Implemented

- [x] User registration
- [x] JWT authentication
- [x] BCrypt password hashing
- [x] API Gateway
- [x] Centralized API routing
- [x] Wallet creation
- [x] Add funds
- [x] Wallet holds
- [x] Send money
- [x] Transaction lifecycle
- [x] Transaction idempotency
- [x] Compensating payment operations
- [x] Kafka transaction events
- [x] Kafka consumer groups
- [x] Reward processing
- [x] Notification processing
- [x] Redis-backed rate limiting
- [x] React frontend
- [x] Docker-based infrastructure
- [x] Unified backend and frontend repository

---

# 🔮 Future Improvements

Potential improvements include:

- Production-grade persistent database configuration
- Prometheus metrics
- Grafana dashboards
- Distributed tracing
- Centralized configuration
- Kafka retry mechanisms
- Dead Letter Topics
- Improved service resilience
- Circuit breaker implementation
- Automated unit and integration testing
- End-to-end testing
- Kubernetes deployment
- CI/CD pipeline
- Production-grade secret management
- Fraud detection
- Improved observability

---

# 👨‍💻 Author

**Brajesh Prajapati**

Java Backend Developer

**Tech Interests**

Java • Spring Boot • Microservices • Distributed Systems • Kafka • React
