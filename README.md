# Vendix POS System

Vendix is a modern, cloud-native Point of Sale (POS) system designed with a modular monolith architecture. It provides a scalable and maintainable platform for managing retail operations, including stores, registers, sales, and inventory.

## Architecture Overview

The system is composed of several independent applications that work together, orchestrated by a central service. This design allows for flexibility and scalability.

- **`commons`**: The foundational module of the system. It contains all the shared code, including API contracts, core domain models, and Spring Boot auto-configurations for common concerns like database access, security, and caching. For more details, see the [commons README](./commons/README.md).

- **`orchestrator`**: A singleton application that acts as the central nervous system. It serves as an API gateway, provides service discovery via Consul, and supplies metadata to the other applications. All other services register with the orchestrator to become part of the system. For more details, see the [orchestrator README](./orchestrator/README.MD).

- **`store`**: Represents a single physical store. Multiple instances of the `store` application can be run, each configured at runtime to represent a unique store. It manages its articles and processes receipts from its registers. For more details, see the [store README](./store/README.MD).

- **`pos`**: Represents a single Point of Sale terminal (or register) within a store. Like the `store` application, it is configured at runtime. It handles sales transactions and communicates with its parent `store` application. For more details, see the [pos README](./pos/README.MD).

## Technology Stack

The Vendix system is built on a modern technology stack, containerized with Docker for consistent development and deployment environments.

- **Backend**: Java & Spring Boot
- **UI**: Vaadin
- **Database**: PostgreSQL
- **Caching**: Redis
- **Messaging**: RabbitMQ
- **Security**: Keycloak
- **Service Discovery**: Consul
- **Monitoring**: Grafana, Prometheus, and Loki
- **Load Testing**: k6

## Getting Started

The entire Vendix system is designed to be run with Docker. The `.docker` directory contains the `docker-compose.yaml` files and all the necessary configurations to run the applications.

For detailed instructions on how to set up the development, monitoring, and testing environments, please refer to the [README in the .docker directory](./.docker/README.md).