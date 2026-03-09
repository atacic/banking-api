# Banking API

A robust, secure, and scalable Banking API built with Spring Boot. This application provides essential banking functionalities, including user management, account operations, transactions, and inter-account transfers.

## 🚀 Features

- **User Management**:
  - Secure registration and authentication (JWT-based).
  - Role-based access control.
- **Account Operations**:
  - Create, view, update, and delete bank accounts.
  - Paginated and sortable account listings.
- **Transactions**:
  - Secure deposits and withdrawals.
  - Full transaction history with pagination.
- **Transfers**:
  - seamless money transfers between internal accounts.
- **Infrastructure & Performance**:
  - **Caching**: Redis integration for optimized performance.
  - **Messaging**: RabbitMQ for asynchronous notifications (e.g., email simulation).
  - **Rate Limiting**: Protects endpoints from abuse using a custom rate-limiting service.
  - **Database Migrations**: Liquibase for version-controlled schema management.
- **API Documentation**: Interactive Swagger/OpenAPI documentation.
- **Reliability**: Comprehensive test suite including Unit and Integration tests with Testcontainers.

## 🛠 Tech Stack

- **Backend**: Java 21, Spring Boot 3.5.7
- **Database**: PostgreSQL
- **Security**: Spring Security, JJWT
- **Caching**: Redis
- **Messaging**: RabbitMQ
- **Migration**: Liquibase
- **Mapping**: MapStruct
- **Documentation**: Springdoc-OpenAPI
- **Testing**: JUnit 5, Mockito, Testcontainers, Awaitility
- **Containerization**: Docker, Docker Compose

## 📋 Prerequisites

- **Java**: JDK 21+
- **Maven**: 3.9+
- **Docker**: For running infrastructure services.

## 🏁 Getting Started

### 1. Clone the repository
```bash
git clone <repository-url>
cd banking-api
```

### 2. Start Infrastructure Services
Use Docker Compose to spin up PostgreSQL, Redis, RabbitMQ, and pgAdmin:
```bash
docker compose up -d
```

### 3. Configure Environment Variables
Ensure the following environment variable is set (or provide it at runtime):
- `APPLICATION_SECRET`: Secret key for JWT signing.

### 4. Build and Run
```bash
mvn clean install
mvn spring-boot:run
```

The application will be available at `http://localhost:8080`.

## 📖 API Documentation

Once the application is running, you can access the interactive Swagger UI:
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI Specs**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### Key Endpoints:
- `POST /api/v1/user/register`: Register a new user.
- `POST /api/v1/user/login`: Login and receive a JWT.
- `GET /api/v1/account`: List accounts (requires authentication).
- `POST /api/v1/transaction/deposit`: Deposit funds.
- `POST /api/v1/transfer`: Transfer funds between accounts.

## 🧪 Testing

### Run Unit Tests
```bash
mvn test
```

### Run Integration Tests
Integration tests use **Testcontainers** to spin up real instances of PostgreSQL and RabbitMQ.
```bash
mvn verify
```

## 🏗 Infrastructure Details (Docker)

- **PostgreSQL**: `localhost:5432` (DB: `banking_api_db`)
- **pgAdmin**: `localhost:8888`
- **Redis**: `localhost:6379`
- **RabbitMQ**: `localhost:5672` (Management UI: `localhost:15672`)
