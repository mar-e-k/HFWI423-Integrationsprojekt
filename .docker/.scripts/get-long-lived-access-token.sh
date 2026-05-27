#!/usr/bin/env bash

curl -X POST \
  "http://localhost:8090/realms/vendix/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=vendix-testing" \
  -d "client_secret=BP16NWavYehafU43EGGv66xORTXSn7bc" \
  -d "username=admin.one" \
  -d "password=admin"