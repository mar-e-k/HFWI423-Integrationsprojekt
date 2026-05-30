# POS (Point of Sale) Module

The `pos` application, also referred to as the register, represents a single cash register within a store. Similar to the `store` application, it is dynamically scalable, and each instance is configured at runtime to represent a unique register.

## Core Functionalities

- **Sales Transactions**: The primary function of the `pos` application is to handle sales transactions. This includes scanning articles, calculating totals, and processing payments.
- **Receipt Generation**: After a transaction is complete, the `pos` application generates a receipt and sends it to the corresponding `store` application for processing.
- **Article Information**: It communicates with the `store` application to retrieve article information, such as price and description.

## Architecture

The `pos` application follows the same lightweight, modular monolith pattern as the other services.

- **`pos-app`**: The main application module that bootstraps the POS service.
- **`pos-bom`**: The Bill of Materials (BOM) for managing the versions of the `pos` module's dependencies.
- **`pos-ui`**: The user interface for the cash register, used by cashiers to process sales.

## Interaction with Other Services

- **Orchestrator**: The `pos` application registers with the `orchestrator` to become discoverable and to fetch its initial runtime context (`RegisterDTO`) via the `RegisterContext` interface.
- **Store**: It communicates directly with its parent `store` application to get article information and to post completed receipts.
- **Shared Services**: It relies on the shared infrastructure, including PostgreSQL, Redis, and RabbitMQ, for its data and messaging needs.