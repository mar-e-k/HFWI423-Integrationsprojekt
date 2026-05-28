#!/usr/bin/env bash

# Exit immediately if a command fails
set -e

CONTAINER_NAME="vendix-k6"
SCRIPTS_DIR="/etc/k6/scripts"

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

case "$1" in
    run)
        if [ -z "$2" ]; then
            log "ERROR" "Missing scenario name"
            echo "Usage: $0 run <scenario-name>"
            exit 1
        fi

        SCENARIO_NAME="$2"

        # Automatically append .js extension if omitted
        if [[ ! "$SCENARIO_NAME" == *.js ]]; then
            SCENARIO_NAME="${SCENARIO_NAME}.js"
        fi

        log "INFO" "Initializing execution for scenario [$SCENARIO_NAME]"

        # Verify scenario exists inside container
        if ! docker exec "$CONTAINER_NAME" test -f "$SCRIPTS_DIR/scenarios/$SCENARIO_NAME"; then
            log "ERROR" "Scenario file not found: $SCRIPTS_DIR/scenarios/$SCENARIO_NAME"
            exit 1
        fi

        log "INFO" "Launching k6 execution"
        log "INFO" "Container=$CONTAINER_NAME Scenario=$SCENARIO_NAME"

        echo "------------------------------------------------------------"

        docker exec -it "$CONTAINER_NAME" \
            k6 run \
            -o experimental-prometheus-rw \
            "$SCRIPTS_DIR/scenarios/$SCENARIO_NAME"
        ;;

    stop)
        log "INFO" "Checking for active k6 processes"

        if ! docker exec "$CONTAINER_NAME" pgrep -x "k6" > /dev/null; then
            log "WARN" "No active k6 process found in container [$CONTAINER_NAME]"
            exit 0
        fi

        # SIGINT allows teardown() and metric flushing
        docker exec "$CONTAINER_NAME" pkill -2 -x "k6"

        log "INFO" "SIGINT signal dispatched to active k6 process"
        log "INFO" "Awaiting graceful shutdown and metrics flush"
        ;;

    status)
        if docker exec "$CONTAINER_NAME" pgrep -x "k6" > /dev/null; then
            log "INFO" "k6 status=RUNNING container=$CONTAINER_NAME"
        else
            log "INFO" "k6 status=IDLE container=$CONTAINER_NAME"
        fi
        ;;

    *)
        show_help
        exit 0
        ;;
esac