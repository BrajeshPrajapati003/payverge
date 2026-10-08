```markdown
# 💸 Payverge

**Payverge** is a distributed digital payment platform built with **Java, Spring Boot, React, Apache Kafka, and Docker**.

The project simulates a digital payment system using a **microservices architecture**, combining synchronous transaction processing with asynchronous event-driven processing for rewards and notifications.

---

## 🏗️ Architecture

```text
                         ┌──────────────────────┐
                         │     React + Vite     │
                         │      Frontend        │
                         └──────────┬───────────┘
                                    │
                              HTTP :8080
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │     API Gateway      │
                         └──────────┬───────────┘
                                    │
                 ┌──────────────────┼──────────────────┐
                 │                  │                  │
                 ▼                  ▼                  ▼
          ┌─────────────┐   ┌──────────────┐   ┌─────────────┐
          │ User Service│   │ Transaction  │   │   Wallet    │
          │             │   │   Service    │   │   Service   │
          └─────────────┘   └──────┬───────┘   └─────────────┘
                                   │
                                   │ Transaction Event
                                   ▼
                            ┌─────────────┐
                            │    Kafka    │
                            │txn-initiated│
                            └──────┬──────┘
                                   │
                     ┌─────────────┴─────────────┐
                     ▼                           ▼
              ┌─────────────┐             ┌───────────────┐
              │   Reward    │             │ Notification  │
              │   Service   │             │    Service    │
              └─────────────┘             └───────────────┘
```

---

## ✨ Features

- Microservices-based payment architecture
- API Gateway as the central backend entry point
- JWT-based authentication with Spring Security
- BCrypt password hashing
- User management
- Digital wallet management
- Transaction processing
- Wallet hold, capture, and release workflow
- Idempotent transaction handling
- Compensating actions for failed distributed operations
- Kafka-based event-driven processing
- Automatic reward processing
- Transaction notification processing
- Dockerized Kafka and ZooKeeper infrastructure
- React-based frontend with client-side routing

---

## 💳 Transaction Processing

A payment follows a controlled transaction lifecycle:

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
                      SUCCESS       Compensating Action
                         │
                         ▼
                  Publish Kafka Event
```

The **Transaction Service** coordinates the payment workflow with the **Wallet Service**.

If a later operation fails after funds have already been captured, the system performs a **compensating action** to restore the sender's funds.

---

## ⚡ Event-Driven Architecture

After a successful transaction, the Transaction Service publishes a transaction event to the Kafka topic:

```text
txn-initiated
```

The event is independently consumed by the Reward and Notification services.

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

Separate Kafka consumer groups allow both services to independently process the same transaction event.

This keeps reward and notification processing decoupled from the core transaction workflow.

---

## 🔐 Authentication & Security

Payverge uses **Spring Security and JWT** for authentication.

### Authentication Flow

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
        Client stores token
                │
                ▼
 Authorization: Bearer <JWT>
                │
                ▼
        JWT Request Filter
                │
        ┌───────┼────────┐
        │       │        │
     Validate  Extract  Extract
     Token     User     Role
        │       │        │
        └───────┼────────┘
                ▼
       SecurityContextHolder
                │
                ▼
        Protected Endpoints
```

JWT tokens contain information such as:

- User ID
- Email
- Role
- Issued-at timestamp
- Expiration timestamp

Passwords are protected using **BCrypt**.

---

## 🧩 Microservices

| Service | Responsibility |
|---|---|
| **API Gateway** | Central entry point and request routing |
| **User Service** | User registration, authentication, and user management |
| **Wallet Service** | Wallet balances and wallet operations |
| **Transaction Service** | Payment orchestration and transaction lifecycle |
| **Reward Service** | Processes transaction events and creates rewards |
| **Notification Service** | Processes transaction events and stores notifications |

---

## 🔄 Communication Patterns

Payverge uses both synchronous and asynchronous communication.

### Synchronous Communication

The Transaction Service communicates with the Wallet Service during payment processing.

```text
Transaction Service
        │
        │ REST
        ▼
  Wallet Service
```

This is used for operations where the transaction workflow needs an immediate result.

### Asynchronous Communication

Once a transaction is successfully processed:

```text
Transaction Service
        │
        │ Kafka Event
        ▼
      Kafka
     /     \
    ▼       ▼
Reward   Notification
```

This allows secondary operations to happen independently of the main payment workflow.

---

## 🛡️ Transaction Reliability

The payment workflow incorporates several mechanisms for reliable transaction processing:

### Idempotency

Transaction processing includes idempotency handling to prevent duplicate payment processing.

### Wallet Holds

Funds can first be placed on hold before the transaction is finalized.

```text
Available Balance
        │
        ▼
    Place Hold
        │
        ├───────────────┐
        ▼               ▼
   Capture Hold     Release Hold
        │               │
        ▼               ▼
   Final Balance   Restore Availability
```

### Compensating Actions

If a transaction fails after an operation has already been completed, the system performs a compensating operation to restore consistency.

This provides a **Saga-style approach using compensating actions** rather than relying on a distributed database transaction.

---

## 🛠️ Technology Stack

### Backend

- Java
- Spring Boot
- Spring Security
- JWT
- Spring Data JPA
- Maven

### Messaging

- Apache Kafka
- ZooKeeper

### Frontend

- React 19
- React Router 7
- Vite
- JavaScript
- Sass (SCSS)

### Infrastructure

- Docker
- Docker Compose

### Database

- H2 Database

---

## 📁 Project Structure

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

## 🚀 Getting Started

### Prerequisites

Make sure the following are installed:

- Java
- Maven
- Node.js and npm
- Docker
- Git

---

### 1. Start Kafka Infrastructure

Navigate to the backend directory:

```bash
cd backend
```

Start Kafka and ZooKeeper:

```bash
docker compose up -d
```

This starts:

| Component | Port |
|---|---:|
| ZooKeeper | `2181` |
| Kafka | `9092` |

---

### 2. Build the Backend

From the backend directory:

```bash
mvn clean install
```

Start the individual Spring Boot services.

The API Gateway is available at:

```text
http://localhost:8080
```

---

### 3. Start the Frontend

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

The React application communicates with the backend through the API Gateway.

---

## 🌐 API Gateway Routing

The frontend communicates with the backend through the API Gateway rather than directly accessing individual backend services.

Examples include:

```text
/auth/**
/api/users/**
/api/v1/wallets/**
/api/transactions/**
/api/rewards/**
/api/notify/**
```

This provides a single entry point for the frontend while keeping individual microservices behind the gateway.

---

## 🧠 Engineering Concepts Demonstrated

Payverge was built to explore practical distributed-system and backend engineering concepts:

- Microservices Architecture
- API Gateway Pattern
- REST APIs
- Spring Security
- JWT Authentication
- Password Hashing
- Synchronous Service-to-Service Communication
- Event-Driven Architecture
- Apache Kafka
- Kafka Consumer Groups
- Distributed Transaction Coordination
- Saga-Style Compensating Actions
- Idempotent Transaction Processing
- Wallet Consistency
- Transaction State Management
- Dockerized Infrastructure
- Client-Side Routing

---

## 🔮 Future Improvements

Potential improvements include:

- Prometheus and Grafana monitoring
- Kafka retry mechanisms and Dead Letter Topics
- Centralized configuration
- Improved service resilience and circuit breaking
- Production-grade database configuration
- Automated integration and end-to-end testing
- Kubernetes deployment
- Fraud detection capabilities
- Improved observability and distributed tracing

---

## 👨‍💻 Author

**Brajesh Prajapati**

Java Backend Developer  
Spring Boot • Microservices • Distributed Systems • React
