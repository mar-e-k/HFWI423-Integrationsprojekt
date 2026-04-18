#!/bin/bash
# Kontingent-Simulation als einfacher HTTP-Aufruf (Voraussetzung fuer den Lasttest)
#
# Verwendung:
#   ./simulate-kontingent.sh [count] [host] [port]
#
# Beispiele:
#   ./simulate-kontingent.sh              # 5000 Nachrichten, localhost:8081
#   ./simulate-kontingent.sh 10000        # 10000 Nachrichten
#   ./simulate-kontingent.sh 5000 myhost 8082

COUNT=${1:-5000}
HOST=${2:-localhost}
PORT=${3:-8081}

URL="http://${HOST}:${PORT}/api/load/contingents/simulate?count=${COUNT}"

echo "Starte Kontingent-Simulation..."
echo "  URL:   ${URL}"
echo "  Count: ${COUNT}"
echo ""

RESPONSE=$(curl -s -w "\nHTTP_STATUS:%{http_code}" \
  -X POST "${URL}" \
  --max-time 120 \
  -H "Content-Type: application/json")

HTTP_STATUS=$(echo "${RESPONSE}" | grep "HTTP_STATUS:" | cut -d: -f2)
BODY=$(echo "${RESPONSE}" | grep -v "HTTP_STATUS:")

echo "Status: ${HTTP_STATUS}"
echo "Antwort: ${BODY}"

if [ "${HTTP_STATUS}" != "200" ]; then
  echo ""
  echo "FEHLER: Simulation fehlgeschlagen (HTTP ${HTTP_STATUS})"
  exit 1
fi

echo ""
echo "Kontingent-Simulation abgeschlossen."
