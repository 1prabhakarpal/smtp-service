# Java Email Server

## Overview
This project is a robust, modular Java-based email server solution designed for high performance and scalability. It employs a microservices architecture within a monorepo, separating core domain logic, SMTP handling, and API management.

## Key Features
- **SMTP Server**: Built with SubEthaSMTP, supporting TLS encryption and DKIM signing utility.
- **REST API**: Spring Boot-based API for user management and email retrieval.
- **Security**:
  - **Authentication**: JWT-based auth with Role-Based Access Control (RBAC).
  - **Password Hashing**: BCrypt encryption for user passwords.
  - **Transport Security**: TLS support for SMTP connections.
- **Persistence**: Dockerized PostgreSQL database for reliable data storage.

## Architecture
The system is composed of the following modules:

- **`common`**: Shared domain entities (`User`, `Email`), repositories, and utility classes.
- **`smtp-service`**: A dedicated SMTP server listening on port 25000 (dev) or 25. Handles incoming emails and persists messages to the database.
- **`api-service`**: A Spring Boot REST API (port 8095) for:
  - User Registration & Login (`/api/auth`)
  - Email Retrieval (`/api/emails`)

## Prerequisites
Ensure the following tools are installed:
- **Java 17** or higher
- **Maven 3.8** or higher
- **Docker** & **Docker Compose**

## Getting Started

### 1. Clone the Repository
```bash
git clone <repository-url>
cd smtp-service
```

### 2. Build the Project
Build the entire monorepo to ensure shared dependencies are installed:
```bash
mvn clean install
```

### 3. Run with Docker Compose
Start the PostgreSQL database and services:
```bash
docker-compose up -d
```
*Note: This starts Postgres on port 5432, SMTP on 25000, and API on 8095.*

## API Reference

### Authentication
- **Register**: `POST /api/auth/register`
  ```json
  { "username": "user@example.com", "password": "password" }
  ```
- **Login**: `POST /api/auth/login`
  ```json
  { "username": "user@example.com", "password": "password" }
  ```
  *Returns JWT token.*

### Emails
- **Get Emails**: `GET /api/emails?recipient=user@example.com`
  *Headers*: `Authorization: Bearer <token>`

## Development

### Running Locally
1. Start Database:
   ```bash
   docker-compose up -d postgres
   ```
2. Run SMTP Service:
   ```bash
   mvn -pl smtp-service spring-boot:run
   ```
3. Run API Service:
   ```bash
   mvn -pl api-service spring-boot:run
   ```
