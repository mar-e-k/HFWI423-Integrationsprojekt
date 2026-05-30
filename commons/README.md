# Commons Module

The `commons` module is the foundational layer of the Vendix system, designed as a modular monolith. It centralizes and standardizes various cross-cutting concerns, providing a robust framework for the other applications (`orchestrator`, `store`, and `pos`). By leveraging Spring Boot's auto-configuration capabilities, `commons` delivers pre-configured beans and functionalities, which allows the other applications to be lightweight and focused on their specific business logic.

## Sub-modules

The `commons` module is organized into the following sub-modules:

- **`api`**: This module defines the Data Transfer Objects (DTOs) that form the public API for communication between the different services. It ensures a consistent data contract across the system.
- **`bom`**: The Bill of Materials (BOM) for the `commons` module. It manages the versions of all dependencies to ensure consistency and avoid conflicts.
- **`core`**: This module contains the core domain models and business logic that are shared across all applications.
- **`spring`**: This module contains a collection of Spring-based auto-configurations and utilities that provide the core functionalities for the applications. These include configurations for data persistence with PostgreSQL, caching with Redis, security with Keycloak, and the Vaadin UI framework.

This modular structure ensures a clear separation of concerns and enhances the maintainability of the entire system.