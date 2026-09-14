#!/usr/bin/env sh
set -eu

MODE="${1:-same-process}"
PORT="${2:-5050}"
PROJECT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
cd "$PROJECT_DIR"

mvn -q -DskipTests compile

case "$MODE" in
  same-process)
    exec mvn -q -DskipTests exec:java \
      -Dexec.mainClass=com.vahan.assignment.app.SameProcessMain
    ;;
  multi-process)
    mvn -q -DskipTests exec:java \
      -Dexec.mainClass=com.vahan.assignment.app.PlayerProcessMain \
      -Dexec.args="responder $PORT" &
    RESPONDER_PID=$!

    cleanup() {
      kill "$RESPONDER_PID" 2>/dev/null || true
      wait "$RESPONDER_PID" 2>/dev/null || true
    }
    trap cleanup EXIT INT TERM

    sleep 1
    mvn -q -DskipTests exec:java \
      -Dexec.mainClass=com.vahan.assignment.app.PlayerProcessMain \
      -Dexec.args="initiator $PORT"

    wait "$RESPONDER_PID"
    trap - EXIT INT TERM
    ;;
  *)
    echo "Usage: $0 {same-process|multi-process} [port]" >&2
    exit 2
    ;;
esac
