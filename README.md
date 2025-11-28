# Java Email Server

## Overview
This project is a robust, modular Java-based email server solution designed for high performance and scalability. It employs a microservices architecture within a monorepo, separating core domain logic, SMTP handling, and API management.

## Architecture
The system is composed of the following modules:

- **`common`**: Shared domain entities, repositories, and utility classes used across services.
- **`smtp-service`**: A dedicated SMTP server implementation using SubEthaSMTP, responsible for receiving and processing emails.
- **`api-service`**: A Spring Boot REST API for managing users, mailboxes, and retrieving email data.

## Prerequisites
Ensure the following tools are installed in your development environment:
- **Java 17** or higher
- **Maven 3.8** or higher
- **Docker** & **Docker Compose** (for database and deployment)

## Getting Started

### 1. Clone the Repository
```bash
git clone <repository-url>
cd smtp-service
```

### 2. Build the Project
**Important**: This project uses a local shared library (`common`). You must build the project from the root directory to ensure dependencies are correctly installed in your local Maven repository.

```bash
mvn clean install
```

## Modules

### Common Module
Contains the core business logic and database entities (JPA).
- **Path**: `./common`
- **Key Dependencies**: Spring Data JPA, Lombok

### SMTP Service
Handles incoming SMTP traffic.
- **Path**: `./smtp-service`
- **Port**: 25 (default) or 25000 (dev)
- **Key Dependencies**: SubEthaSMTP, Spring Boot

### API Service
Exposes REST endpoints for the frontend or external clients.
- **Path**: `./api-service`
- **Port**: 8080
- **Key Dependencies**: Spring Web, Spring Data JPA

## Running the Application

### Using Docker Compose
The easiest way to run the full stack (PostgreSQL + Services) is via Docker Compose.

```bash
docker-compose up -d
```

### Local Development
1. Start the PostgreSQL database:
   ```bash
   docker-compose up -d postgres
   ```
2. Run the services individually via your IDE or Maven:
   ```bash
   # Run SMTP Service
   mvn -pl smtp-service spring-boot:run

   # Run API Service
   mvn -pl api-service spring-boot:run
   ```
