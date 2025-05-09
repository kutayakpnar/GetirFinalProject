# Library Management System - Docker Guide

This guide explains how to build and run the Library Management System using Docker and Docker Compose.

## Prerequisites

- Docker
- Docker Compose

## Getting Started

### Building and Running with Docker Compose

1. Clone the repository:
   ```
   git clone <repository-url>
   cd librarymanagement
   ```

2. Build and start the containers:
   ```
   docker-compose up -d
   ```
   
   This command will:
   - Build the application container
   - Start a PostgreSQL database container
   - Link the application to the database
   - Expose the application on port 8080

3. Access the application:
   - Web Interface: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - API Documentation: http://localhost:8080/api-docs

### Stopping the Containers

To stop the running containers:
```
docker-compose down
```

To stop and remove volumes (this will delete database data):
```
docker-compose down -v
```

## Container Structure

- **library-app**: Spring Boot application container
- **library-postgres**: PostgreSQL database container

## Development Workflow

### Rebuilding the Application

After making changes to the source code, rebuild and restart the application:
```
docker-compose up -d --build app
```

### Viewing Logs

To see the application logs:
```
docker-compose logs -f app
```

To see the database logs:
```
docker-compose logs -f postgres
```

## Database Connection

- **Host**: localhost
- **Port**: 5431
- **Database**: postgres
- **Username**: postgres
- **Password**: mysecretpassword

## Troubleshooting

1. If the application container fails to start, check the logs:
   ```
   docker-compose logs app
   ```

2. If you need to connect to the containers:
   ```
   docker exec -it library-app sh
   docker exec -it library-postgres psql -U postgres
   ```

3. To check the PostgreSQL container status:
   ```
   docker exec -it library-postgres pg_isready -U postgres
   ``` 