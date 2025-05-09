#!/bin/bash

# Docker utilities for the Library Management System

function show_help {
    echo "Library Management System Docker Utilities"
    echo ""
    echo "Usage: ./docker-utils.sh [command]"
    echo ""
    echo "Commands:"
    echo "  start           : Start all containers"
    echo "  stop            : Stop all containers"
    echo "  build           : Rebuild the application container"
    echo "  logs [service]  : Show logs for a service (app or postgres)"
    echo "  db-shell        : Open a PostgreSQL shell in the database container"
    echo "  app-shell       : Open a shell in the application container"
    echo "  status          : Show the status of all containers"
    echo "  prune           : Remove unused containers, networks, and volumes"
    echo "  help            : Show this help message"
    echo ""
}

case "$1" in
    start)
        echo "Starting containers..."
        docker-compose up -d
        echo "Containers started. Application is available at: http://localhost:8080"
        echo "Swagger UI is available at: http://localhost:8080/swagger-ui.html"
        ;;
    stop)
        echo "Stopping containers..."
        docker-compose down
        echo "Containers stopped."
        ;;
    build)
        echo "Rebuilding application container..."
        docker-compose up -d --build app
        echo "Container rebuilt."
        ;;
    logs)
        service=${2:-app}
        echo "Showing logs for $service service..."
        docker-compose logs -f $service
        ;;
    db-shell)
        echo "Opening PostgreSQL shell..."
        docker exec -it library-postgres psql -U postgres
        ;;
    app-shell)
        echo "Opening shell in application container..."
        docker exec -it library-app sh
        ;;
    status)
        echo "Container status:"
        docker-compose ps
        ;;
    prune)
        echo "Pruning unused Docker resources..."
        docker system prune -f
        echo "Pruning completed."
        ;;
    help|*)
        show_help
        ;;
esac 