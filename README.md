# Library Management System

MEHMET KUTAY AKPINAR FINAL PROJECT


## Project Overview

The Library Management System is a comprehensive solution for managing library operations, including book inventory, user management, and borrowing processes. The system is designed with security in mind, implementing JWT-based authentication and role-based access control.

### Key Features

- **Book Management**: Add, update, delete, and search books in the library inventory
- **User Management**: Register, update, and manage library users
- **Borrowing System**: Handle book borrowing, returns, and due date tracking
- **Search Functionality**: Search books by title, author, genre, or ISBN
- **Role-Based Access Control**: Different access levels for administrators and regular users
- **Secure Authentication**: JWT-based authentication system
- **Comprehensive Logging**: Track system activities with structured logging
- **API Documentation**: Swagger UI for easy API exploration and testing
- **SPRING WEB FLUX REAL TIME UPDATES**

## Technology Stack

- **Backend Framework**: Spring Boot 3.2.5
- **Security**: Spring Security with JWT
- **Database**: PostgreSQL 14
- **ORM**: Hibernate (Spring Data JPA)
- **API Documentation**: SpringDoc OpenAPI
- **Build Tool**: Maven
- **Containerization**: Docker and Docker Compose
- **Java Version**: JDK 21
- **Validation**: Jakarta Bean Validation
- **Logging**: Logback with structured logging
- **Development Tools**: Lombok for boilerplate reduction, AOP for cross-cutting concerns
- **Reactive Programming**:Spring WebFlux

## Database Schema

<img width="732" alt="Ekran Resmi 2025-05-09 18 10 47" src="https://github.com/user-attachments/assets/71325024-b097-4885-a670-36cbbfe69613" />


## Running Locally

### Prerequisites

- JDK 21 or later
- Maven 3.6+ (or use the included Maven wrapper)
- PostgreSQL 14 (or use the provided Docker setup)

### Steps to Run

1. Clone the repository
   ```bash
   git clone <repository-url>
   cd librarymanagement
   ```

2. Set up the database (Option 1 - Local PostgreSQL)
   ```bash
   # Create a PostgreSQL database with the following settings:
   # Database: postgres
   # Port: 5431
   # Username: postgres
   # Password: mysecretpassword
   ```

3. Build and run the application
   ```bash
   ./mvnw spring-boot:run
   ```

4. Access the application and API documentation
   - Main application: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - API Docs: http://localhost:8080/api-docs

## Docker Setup

### Prerequisites

- Docker
- Docker Compose

### Running with Docker

1. Build and run using Docker Compose
   ```bash
   docker-compose up -d
   ```

   This will:
   - Create a PostgreSQL container
   - Build and run the Spring Boot application
   - Configure networking between the containers

2. Access the application at http://localhost:8080

3. To stop the containers
   ```bash
   docker-compose down
   ```

4. For cleanup (including volumes)
   ```bash
   docker-compose down -v
   ```

### Docker Utilities

The project includes a `docker-utils.sh` script for common Docker operations:

```bash
# Grant execute permission
chmod +x docker-utils.sh

# Show help
./docker-utils.sh help

# Build and start containers
./docker-utils.sh up

# Stop containers
./docker-utils.sh down

# View logs
./docker-utils.sh logs
```

## API Endpoints

The API is documented using Swagger UI, which can be accessed at http://localhost:8080/swagger-ui.html when the application is running.

### Core Endpoints
<img width="1348" alt="Ekran Resmi 2025-05-09 18 15 42" src="https://github.com/user-attachments/assets/b14353fa-d2e3-4185-9ba8-87d166273977" />
<img width="1358" alt="Ekran Resmi 2025-05-09 18 15 34" src="https://github.com/user-attachments/assets/577e86fd-4879-4428-9389-7ead7fbf0ed5" />
<img width="1381" alt="Ekran Resmi 2025-05-09 18 15 25" src="https://github.com/user-attachments/assets/dc934bd6-ff86-4f9c-a597-6625a9aea62b" />
<img width="1269" alt="Ekran Resmi 2025-05-09 18 15 54" src="https://github.com/user-attachments/assets/a67122b4-11b0-440b-82ce-1edf68605d95" />



## Configuration

The application uses Spring Boot's property-based configuration:

### application.properties
- Database connection settings
- JPA/Hibernate configuration
- JWT security settings
- Borrowing rules (max books, default period)
- Logging configuration
- Swagger/OpenAPI settings

## Logging

The application implements a structured logging system using Logback:

- Log files are stored in the `logs` directory
- Log levels can be configured in `application.properties`
- Different log levels for different packages
- Detailed request and security logging

## Security

The application implements JWT-based authentication with the following features:

- Token-based authentication
- Password encryption
- Role-based access control (ADMIN and USER roles)
- Secure API endpoints based on roles
- JWT expiration configuration

## Postman
   https://.postman.co/workspace/My-Workspace~369b2625-e9cc-409b-8d7a-08fdbb568a29/collection/28747315-bdbcd869-a55d-442a-906f-6f92fc066307?action=share&creator=28747315
   
## Additional Information

- The application uses aspect-oriented programming (AOP) for cross-cutting concerns such as logging
- Exception handling is centralized with custom exception types
- Data transfer objects (DTOs) are used to separate API models from internal entities
- The system automatically manages book availability status when borrowed or returned
- SPRING WEB FLUX REAL TIME UPDATES


<img width="1470" alt="Ekran Resmi 2025-05-09 17 31 40" src="https://github.com/user-attachments/assets/48f81ac2-c004-46ac-b865-88f32bd31be7" />
<img width="1470" alt="Ekran Resmi 2025-05-09 17 31 13" src="https://github.com/user-attachments/assets/d5bb6bc1-a548-4d83-9700-d929f8b305b7" />
<img width="1470" alt="Ekran Resmi 2025-05-09 17 31 02" src="https://github.com/user-attachments/assets/b18d4555-87ef-43bd-9d5d-11da4c778089" />
<img width="1470" alt="Ekran Resmi 2025-05-09 17 30 33" src="https://github.com/user-attachments/assets/2e2d85f2-12e8-4f54-8eaa-7896d673b89d" />
<img width="1470" alt="Ekran Resmi 2025-05-09 17 30 12" src="https://github.com/user-attachments/assets/48ef169d-df5e-4d6e-8a7e-ede2dc068181" />
<img width="1470" alt="Ekran Resmi 2025-05-09 17 29 58" src="https://github.com/user-attachments/assets/09c0e7e1-50ad-43b2-a0d0-0850d708e872" />
<img width="1465" alt="Ekran Resmi 2025-05-09 17 29 37" src="https://github.com/user-attachments/assets/e99561a5-1f67-49bf-ae96-cc0867e739e4" />

## SPRING WEBFLUX DEMO
<img width="1470" alt="Ekran Resmi 2025-05-10 21 33 24" src="https://github.com/user-attachments/assets/d356aeb0-5ff1-4b05-a697-657b56d39f21" />

<img width="1470" alt="Ekran Resmi 2025-05-10 21 35 27" src="https://github.com/user-attachments/assets/5d7fac7b-4f80-4fa4-8e6e-ea13c4a874ac" />


