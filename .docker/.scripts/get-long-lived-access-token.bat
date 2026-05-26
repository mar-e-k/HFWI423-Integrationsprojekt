@echo off
setlocal enabledelayedexpansion

curl -X POST ^
  "http://localhost:8090/realms/vendix/protocol/openid-connect/token" ^
  -H "Content-Type: application/x-www-form-urlencoded" ^
  -d "grant_type=password" ^
  -d "client_id=vendix-testing" ^
  -d "client_secret=DVj0NCMBIFzt9kvvWJUABPhDcfsSbIoq" ^
  -d "username=admin" ^
  -d "password=admin"