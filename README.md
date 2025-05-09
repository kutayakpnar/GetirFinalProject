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

#### Authentication
- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Authenticate and get JWT token

#### Books
- `GET /api/books` - List all books
- `GET /api/books/{id}` - Get book by ID
- `POST /api/books` - Create a new book
- `PUT /api/books/{id}` - Update book information
- `DELETE /api/books/{id}` - Delete a book
- `GET /api/books/search` - Search books by criteria

#### Users
- `GET /api/users` - List all users (admin only)
- `GET /api/users/{id}` - Get user by ID
- `PUT /api/users/{id}` - Update user information
- `DELETE /api/users/{id}` - Delete a user

#### Borrowings
- `GET /api/borrowings` - List all borrowings
- `GET /api/borrowings/{id}` - Get borrowing details
- `POST /api/borrowings` - Create a new borrowing
- `PUT /api/borrowings/{id}/return` - Return a book
- `GET /api/borrowings/user/{userId}` - Get user borrowing history

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

## Additional Information

- The application uses aspect-oriented programming (AOP) for cross-cutting concerns such as logging
- Exception handling is centralized with custom exception types
- Data transfer objects (DTOs) are used to separate API models from internal entities
- The system automatically manages book availability status when borrowed or returned

<img width="1470" alt="Ekran Resmi 2025-05-09 17 31 40" src="https://github.com/user-attachments/assets/48f81ac2-c004-46ac-b865-88f32bd31be7" />
<img width="1470" alt="Ekran Resmi 2025-05-09 17 31 13" src="https://github.com/user-attachments/assets/d5bb6bc1-a548-4d83-9700-d929f8b305b7" />
<img width="1470" alt="Ekran Resmi 2025-05-09 17 31 02" src="https://github.com/user-attachments/assets/b18d4555-87ef-43bd-9d5d-11da4c778089" />
<img width="1470" alt="Ekran Resmi 2025-05-09 17 30 33" src="https://github.com/user-attachments/assets/2e2d85f2-12e8-4f54-8eaa-7896d673b89d" />
<img width="1470" alt="Ekran Resmi 2025-05-09 17 30 12" src="https://github.com/user-attachments/assets/48ef169d-df5e-4d6e-8a7e-ede2dc068181" />
<img width="1470" alt="Ekran Resmi 2025-05-09 17 29 58" src="https://github.com/user-attachments/assets/09c0e7e1-50ad-43b2-a0d0-0850d708e872" />
<img width="1465" alt="Ekran Resmi 2025-05-09 17 29 37" src="https://github.com/user-attachments/assets/e99561a5-1f67-49bf-ae96-cc0867e739e4" />


