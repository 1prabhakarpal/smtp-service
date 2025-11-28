# SMTP Service / Mail Server Platform

## 🚀 Project Title & Description

**SMTP Service** is a robust, self-hosted email server platform designed to provide a private, controllable, and extensible email infrastructure. It replaces reliance on third-party email providers by offering a complete solution for handling both inbound and outbound email traffic.

The system features a custom SMTP server for standard email interoperability and a comprehensive REST API for building custom email clients, managing users, and administering the system. It is built with a microservices-ready architecture using Java Spring Boot and is fully containerized for easy deployment.

## ✨ Key Features

-   **Custom SMTP Server**: Handles inbound emails (Port 25) and submission (Port 587) with STARTTLS support.
-   **RESTful API**: Manage emails, folders, users, and settings via a modern HTTP API.
-   **Outbound Delivery**: Reliable email sending with MX lookup, DKIM signing, and retry logic.
-   **Security**: JWT-based authentication, BCrypt password hashing, and TLS enforcement.
-   **Storage**: Persistent storage of emails, attachments, and user data using PostgreSQL.
-   **Containerized**: Fully Dockerized with Docker Compose and Traefik for reverse proxying and auto-HTTPS.
-   **Monitoring**: Integrated Spring Boot Actuator for health and metrics monitoring.

## 🏗️ Architecture / System Overview

The system is composed of the following key components:

-   **SMTP Service**: The core mail server. It listens on ports 25 and 587, parses incoming MIME messages, handles authentication, and manages the outbound delivery queue.
-   **API Service**: The backend for user interaction. It provides endpoints for the web interface (or other clients) to read emails, send new messages, and manage folders.
-   **Database**: A shared PostgreSQL instance stores all data, including user credentials, email content, and queue status.
-   **Gateway**: Traefik serves as the entry point, handling SSL termination and routing HTTP traffic to the API service.

## 🛠️ Technology Stack

-   **Language**: Java 21
-   **Framework**: Spring Boot 3.2.x
-   **Build Tool**: Maven
-   **Database**: PostgreSQL 15
-   **SMTP Library**: SubEthaSMTP
-   **Email Parsing**: Jakarta Mail / Apache Mime4j
-   **Security**: Spring Security, JJWT
-   **Testing**: JUnit 5, Testcontainers, RestAssured, GreenMail
-   **Deployment**: Docker, Docker Compose

## 📂 Project File Structure

```
smtp-service/
├── api-service/       # REST API Application
│   ├── src/main/java/com/example/api/
│   │   ├── controller/ # REST Endpoints
│   │   ├── service/    # Business Logic
│   │   └── security/   # Auth & JWT
│   └── pom.xml
├── smtp-service/      # SMTP Server Application
│   ├── src/main/java/com/example/smtp/
│   │   ├── config/     # SMTP Config
│   │   ├── handler/    # Inbound Handlers
│   │   └── service/    # Delivery & DKIM
│   └── pom.xml
├── common/            # Shared Library
│   ├── src/main/java/com/example/common/
│   │   ├── entity/     # JPA Entities
│   │   └── repository/ # Data Repositories
│   └── pom.xml
├── docker-compose.yml # Orchestration
├── Dockerfile.api     # API Image Definition
├── Dockerfile.smtp    # SMTP Image Definition
└── pom.xml            # Parent POM
```

## ✅ Prerequisites

-   **Java 21 JDK**
-   **Maven 3.9+**
-   **Docker & Docker Compose**
-   **Git**

## 📥 Installation Steps

1.  **Clone the Repository**:
    ```bash
    git clone <repository-url>
    cd smtp-service
    ```

2.  **Build the Project**:
    ```bash
    mvn clean install
    ```

3.  **Generate DKIM Keys** (Required for outbound signing):
    ```bash
    mkdir -p dkim
    openssl genrsa -out dkim/private.key 2048
    openssl rsa -in dkim/private.key -pubout -out dkim/public.key
    ```

## ⚙️ Environment Configuration

Copy the example environment file:
```bash
cp .env.example .env
```

**Key Variables in `.env`**:

| Variable | Description | Default |
| :--- | :--- | :--- |
| `DB_USER` | Database username | `mailuser` |
| `DB_PASSWORD` | Database password | `securepassword` |
| `DB_NAME` | Database name | `maildb` |
| `JWT_SECRET` | Secret for JWT signing | (Generate a strong key) |
| `DOMAIN` | Your domain name | `devprabhakar.in` |
| `API_DOMAIN` | API subdomain | `api.devprabhakar.in` |
| `ACME_EMAIL` | Email for Let's Encrypt | `admin@example.com` |

## 🚀 Running the Application

### Using Docker Compose (Recommended)

Start the entire stack:
```bash
docker-compose up -d --build
```

-   **API**: `http://localhost:8080` (or configured domain)
-   **SMTP**: Ports `25` and `587`
-   **Traefik Dashboard**: `http://localhost:8081`

### Local Development

1.  Start PostgreSQL locally.
2.  Update `application.properties` in `api-service` and `smtp-service` with local DB credentials.
3.  Run API:
    ```bash
    cd api-service && mvn spring-boot:run
    ```
4.  Run SMTP:
    ```bash
    cd smtp-service && mvn spring-boot:run
    ```

## 📖 API Documentation

### Authentication
-   **Register**: `POST /api/auth/signup`
    ```json
    { "username": "user", "email": "user@example.com", "password": "password" }
    ```
-   **Login**: `POST /api/auth/signin`
    ```json
    { "username": "user", "password": "password" }
    ```
    *Response*: `{ "token": "eyJhbG..." }`

### Emails (Requires `Authorization: Bearer <token>`)
-   **List Emails**: `GET /api/emails?page=0&size=20`
-   **Send Email**: `POST /api/emails`
    ```json
    { "to": "friend@example.com", "subject": "Hi", "body": "Hello!" }
    ```
-   **Get Email**: `GET /api/emails/{id}`

### Folders
-   **Create Folder**: `POST /api/folders`
    ```json
    { "name": "Work" }
    ```
-   **Move Email**: `POST /api/emails/{id}/move?folderId={folderId}`

## 🗄️ Database Schema

-   **User**: Stores user credentials and profile.
-   **Email**: Stores metadata and content of messages.
-   **Folder**: User-defined folders for organizing emails.
-   **Attachment**: Metadata for email attachments.
-   **OutboundQueue**: Emails waiting for delivery.
-   **Settings**: User preferences (theme, signature).

## 🧪 Testing Instructions

Run the full test suite (Unit + Integration + E2E):
```bash
mvn test
```

Run specific E2E tests:
```bash
mvn test -Dtest=FullSystemE2ETest -pl api-service
```

## 🚀 Build & Deployment / CI-CD

The project uses **GitHub Actions** for CI/CD. The pipeline (`.github/workflows/e2e.yml`):
1.  Builds the code with Maven.
2.  Runs Unit and Integration tests.
3.  Spins up the Docker stack.
4.  Runs End-to-End tests using Testcontainers and GreenMail.

To deploy manually:
1.  Provision a Linux VM (e.g., AWS EC2).
2.  Clone repo and configure `.env`.
3.  Run `docker-compose up -d`.
4.  Configure DNS (A records for API, MX records for SMTP).

## ❓ Common Issues & Troubleshooting

-   **SMTP Port 25 Blocked**: Many cloud providers block port 25. Request an unblock or use a relay service.
-   **Emails to Spam**: Ensure DKIM, SPF, and DMARC records are correctly configured in your DNS.
-   **Database Connection**: Check `DB_HOST` and credentials in `.env`.

## 🤝 Contribution Guidelines

1.  Fork the repository.
2.  Create a feature branch (`git checkout -b feature/amazing-feature`).
3.  Commit your changes (`git commit -m 'Add amazing feature'`).
4.  Push to the branch (`git push origin feature/amazing-feature`).
5.  Open a Pull Request.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Contact / Maintainers

-   **Maintainer**: Prabhakar Pal
-   **Email**: admin@devprabhakar.in
