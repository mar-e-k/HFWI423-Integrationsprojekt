#!/usr/bin/env bash

set -euo pipefail

CONTAINER_NAME="vendix-k6"
SCRIPTS_DIR="/etc/k6/scripts"
SHUTDOWN_TIMEOUT=30

log() {
    local LEVEL="$1"
    local MESSAGE="$2"
    printf '[%s] [%s] %s\n' "$(date '+%Y-%m-%d %H:%M:%S')" "$LEVEL" "$MESSAGE"
}

show_help() {
    echo "Vendix k6 Test Management CLI"
    echo "=============================================="
    echo "Usage: $0 [command] [arguments]"
    echo ""
    echo "Commands:"
    echo "  run <scenario-name>   Execute a scenario script"
    echo "  stop                  Gracefully stop active k6 execution"
    echo "  status                Check running k6 status"
    echo ""
    echo "Examples:"
    echo "  $0 run load-test"
    echo "  $0 run stress-test"
    echo "  $0 stop"
}

get_k6_pid() {
    docker exec "$CONTAINER_NAME" pgrep -x k6 2>/dev/null | head -n 1 || true
}

case "${1:-}" in

    run)
        if [ -z "${2:-}" ]; then
            log "ERROR" "Missing scenario name"
            echo "Usage: $0 run <scenario-name>"
            exit 1
        fi

        SCENARIO_NAME="$2"

        if [[ ! "$SCENARIO_NAME" == *.js ]]; then
            SCENARIO_NAME="${SCENARIO_NAME}.js"
        fi

        SCENARIO_PATH="$SCRIPTS_DIR/scenarios/$SCENARIO_NAME"

        log "INFO" "Initializing execution for scenario [$SCENARIO_NAME]"

        if ! docker exec "$CONTAINER_NAME" test -f "$SCENARIO_PATH"; then
            log "ERROR" "Scenario file not found: $SCENARIO_PATH"
            exit 1
        fi

        log "INFO" "Launching k6 execution"
        log "INFO" "Container=$CONTAINER_NAME Scenario=$SCENARIO_NAME"

        echo "------------------------------------------------------------"

        docker exec -it "$CONTAINER_NAME" \
            k6 run \
            -o experimental-prometheus-rw \
            "$SCENARIO_PATH"
        ;;

    stop)
        log "INFO" "Checking for active k6 processes"

        PID=$(get_k6_pid)

        if [ -z "$PID" ]; then
            log "WARN" "No active k6 process found in container [$CONTAINER_NAME]"
            exit 0
        fi

        log "INFO" "Sending SIGINT to k6 PID=$PID"

        docker exec "$CONTAINER_NAME" kill -2 "$PID" || true

        log "INFO" "Waiting for k6 graceful shutdown (timeout=${SHUTDOWN_TIMEOUT}s)"

        for i in $(seq 1 "$SHUTDOWN_TIMEOUT"); do
            if [ -z "$(get_k6_pid)" ]; then
                log "INFO" "k6 terminated cleanly after ${i}s"
                exit 0
            fi
            sleep 1
        done

        log "WARN" "k6 did not terminate within timeout (${SHUTDOWN_TIMEOUT}s)"
        log "WARN" "You may need to inspect the container manually"
        ;;

    status)
        PID=$(get_k6_pid)

        if [ -n "$PID" ]; then
            log "INFO" "k6 status=RUNNING container=$CONTAINER_NAME pid=$PID"
        else
            log "INFO" "k6 status=IDLE container=$CONTAINER_NAME"
        fi
        ;;

    *)
        show_help
        exit 0
        ;;
esac