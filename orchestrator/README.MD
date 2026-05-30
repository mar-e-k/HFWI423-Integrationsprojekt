# Orchestrator Module

The `orchestrator` is a singleton application that acts as the central hub of the Vendix ecosystem. It is responsible for routing, service discovery, and the overall orchestration of the `store` and `pos` applications.

## Core Responsibilities

- **API Gateway**: The `orchestrator` serves as the single entry point for all external traffic, running statically on `localhost:8080`. It routes incoming requests to the appropriate downstream `store` or `pos` service instance.
- **Service Discovery**: It integrates with Consul to provide a dynamic service registry. `store` and `pos` instances, which run on random ports, register themselves with the `orchestrator`, making them discoverable by other services.
- **Metadata Provider**: The `orchestrator` provides essential metadata to the `store` and `pos` applications at runtime. This allows each instance to configure its specific context, such as the store or register it represents.

## Architecture

The `orchestrator` is structured as a modular monolith with the following sub-modules:

- **`orchestrator-app`**: The main application module, responsible for bootstrapping the orchestrator service.
- **`orchestrator-bom`**: The Bill of Materials (BOM) for managing the versions of the orchestrator's dependencies.
- **`orchestrator-core`**: Contains the core business logic for orchestration, routing, and service discovery.
- **`orchestrator-ui`**: The user interface for the orchestrator, providing administrative and monitoring functionalities.
- **`orchestrator-web`**: The web layer, responsible for handling HTTP requests and exposing the orchestrator's API.

By centralizing these critical functions, the `orchestrator` simplifies the overall architecture and allows the `store` and `pos` applications to remain focused on their respective domains.