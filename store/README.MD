# Store Module

The `store` application represents a single physical store within the Vendix system. It is a dynamically scalable application, meaning multiple instances can be run, each representing a unique store.

## Core Functionalities

- **Contextualization**: At startup, the application uses the `StoreContext` interface to set its specific store context (`StoreDTO`). This context is fetched from the `orchestrator` and determines the data and operations for that particular store instance.
- **Article Management**: It provides information about articles, including pricing and availability, to the `pos` (Point of Sale) application.
- **Receipt Processing**: It receives and processes receipts from the `pos` application.

## Architecture

The `store` application is designed to be lightweight, relying heavily on the `commons` module for cross-cutting concerns. Its structure is as follows:

- **`store-app`**: The main application module that bootstraps the store service.
- **`store-bom`**: The Bill of Materials (BOM) for managing the versions of the store's dependencies.
- **`store-core`**: Contains the core business logic specific to a store's operations.
- **`store-ui`**: The user interface for managing a store, used by store managers.
- **`store-web`**: The web layer, exposing the store's API for communication with the `pos` application and other services.

## Interaction with Other Services

- **Orchestrator**: The `store` application registers with the `orchestrator` to become discoverable and to fetch its initial runtime context.
- **POS/Register**: It communicates directly with the `pos` application, providing data and processing requests.
- **Shared Services**: It utilizes the shared services defined in the main `docker-compose.yaml`, such as PostgreSQL for data persistence, Redis for caching, and RabbitMQ for asynchronous communication.