#!/usr/bin/env bash
set -euo pipefail
REQUESTS="${1:-100}"
PARALLEL="${2:-100}"

echo "Sending ${REQUESTS} requests for the SAME key with ${PARALLEL} concurrent clients..."
seq "${REQUESTS}" | xargs -P "${PARALLEL}" -I{} curl -s -o /dev/null http://localhost:8081/api/products/123

echo
echo "Stats:"
curl -s http://localhost:8081/api/stats
echo
