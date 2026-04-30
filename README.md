# 💸 Payverge – Event-Driven Digital Payments Platform

> A scalable microservices-based fintech system simulating real-world payment workflows with secure transactions, wallet management, rewards, and real-time notifications.

---

## 🚀 Overview

**Payverge** is a distributed digital payment platform designed using **microservices architecture** and **event-driven communication**. It ensures **secure, reliable, and scalable transaction processing** while maintaining system decoupling.

---

## 🧠 Architecture

```text
                     ┌──────────────────────┐
                     │    Client (React)    │
                     └──────────┬───────────┘
                                │
                                ▼
                     ┌──────────────────────┐
                     │     API Gateway      │
                     │ (Routing + JWT Auth) │
                     └──────────┬───────────┘
                                │
        ┌───────────────────────┼────────────────────────┐
        │                       │                        │
        ▼                       ▼                        ▼
┌──────────────┐      ┌──────────────┐         ┌──────────────┐
│ User Service │      │Wallet Service│         │  Auth Logic  │
└──────────────┘      └──────────────┘         └──────────────┘
        │                       │
        └──────────────┬────────┘
                       ▼
             ┌────────────────────┐
             │  Transaction Svc   │
             │ (Core Processing)  │
             └─────────┬──────────┘
                       │
                       ▼
             ┌────────────────────┐
             │       Kafka        │
             │     (Event Bus)    │
             └───────┬─────┬──────┘
                     │     │
                     ▼     ▼
           ┌────────────┐ ┌────────────────┐
           │ Reward Svc │ │ Notification   │
           │            │ │   Service      │
           └────────────┘ └────────────────┘
           
```
---

## ⚙️ Key Features

- 💰 Wallet Management – Credit, debit, and balance tracking  
- 🔐 Secure Authentication – JWT-based stateless authentication  
- 🔁 Idempotent Transactions – Prevent duplicate payments  
- ⚡ Event-Driven Architecture – Kafka for async communication  
- 🎁 Reward System – Cashback/incentives on transactions  
- 🔔 Notification Service – Real-time user updates  
- 🐳 Dockerized Deployment – Environment-independent setup  

---

## 🧩 Microservices

### 🔹 User Service
- Manages user data and authentication

### 🔹 Wallet Service
- Handles balance updates (credit/debit)
- Ensures atomic and consistent operations

### 🔹 Transaction Service
- Core payment processing logic
- Maintains transaction lifecycle (PENDING → SUCCESS/FAILED)

### 🔹 Reward Service
- Consumes Kafka events
- Applies cashback logic

### 🔹 Notification Service
- Sends real-time updates based on events

### 🔹 API Gateway
- Centralized routing and security layer

---

## 🔄 Event Flow (Kafka)

1. Transaction is processed  
2. Event is published to Kafka  
3. Reward & Notification services consume independently

Transaction → Kafka → Reward + Notification

---

## 🧠 Core Concepts Implemented

- Microservices Architecture  
- Event-Driven Design  
- Saga Pattern (Distributed Transactions)  
- Idempotency Handling  
- RESTful API Design  
- JWT Authentication  
- Docker Containerization  

---

## ⚡ Tech Stack

| Category        | Technologies |
|----------------|-------------|
| Backend        | Java, Spring Boot |
| Security       | Spring Security, JWT |
| Messaging      | Apache Kafka |
| Frontend       | React |
| Database       | MySQL |
| DevOps         | Docker |
| Build Tool     | Maven |

---

## 🔐 Transaction Flow (Simplified)

1. User initiates payment
2. Transaction Service validates request
3. Wallet Service deducts balance
4. Transaction marked SUCCESS/FAILED
5. Kafka event published
6. Reward + Notification triggered asynchronously

---

## ⚠️ Challenges & Solutions

### ❌ Duplicate Transactions  
✔ Solved using Idempotency Keys  

### ❌ Distributed Consistency  
✔ Solved using Saga Pattern + Compensation  

### ❌ Tight Coupling  
✔ Solved using Kafka (Event-Driven Communication)  

---

## 🧪 Future Improvements

- 📊 Monitoring (Prometheus + Grafana)  
- 🔁 Retry & Dead Letter Queue (DLQ)  
- ☁️ Kubernetes Deployment  
- 🤖 AI-based fraud detection  

---

## 👨‍💻 Author

**Brajesh Prajapati**  
Backend Developer | Microservices | Kafka | System Design  

---

## ⭐ Final Thought

> This project demonstrates real-world backend engineering practices including distributed systems, scalability, and fault tolerance.

---

## 🚀 If you like this project

Give it a ⭐ on GitHub and feel free to contribute!
