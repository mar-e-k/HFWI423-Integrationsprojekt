# Vendix Docker Environment

This directory contains the Docker-based development, monitoring, and testing environments for the Vendix platform.

## Directory Structure

- **`/app`**: Core application services, including Keycloak identity management and PostgreSQL databases.
- **`/local`**: Dockerfiles for building local service images (`pos`, `store`, `orchestrator`).
- **`/monitor`**: Monitoring stack, featuring Grafana for visualization, Loki for logging, and Prometheus for metrics.
- **`/testing`**: k6 load testing environment, with scripts for various performance test scenarios.
- **`/.scripts`**: Helper scripts for managing the Docker environment.

## Scripts

### `docker-compose-up.sh`
Starts the complete Vendix platform, including the application, monitoring, and testing services.

**Usage:**
```bash
./.scripts/docker-compose-up.sh
```

### `docker-compose-down.sh`
Stops all running Vendix services and removes the containers.

**Usage:**
```bash
./.scripts/docker-compose-down.sh
```

### `sync-keycloak.sh`
Exports the Keycloak `vendix` realm configuration to `.docker/app/keycloak/vendix-realm.json`. This is useful for persisting realm changes.

**Usage:**
```bash
./.scripts/sync-keycloak.sh
```

### `get-long-lived-access-token.sh`
Retrieves a long-lived access token from Keycloak for testing purposes.

**Usage:**
```bash
./.scripts/get-long-lived-access-token.sh
```

### `manage-k6-test.sh`
A command-line interface for running and managing k6 load tests.

**Usage:**
```bash
# Run a specific test scenario (e.g., load-test)
./.scripts/manage-k6-test.sh run load-test

# Stop a running test
./.scripts/manage-k6-test.sh stop

# Check the status of the k6 container
./.scripts/manage-k6-test.sh status
```
