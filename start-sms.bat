@echo off
setlocal enabledelayedexpansion

set "SMS_DIR=%~dp0..\security-master-service-v2"
set "ENV_FILE=%SMS_DIR%\environment-v2\.env"

if not exist "%SMS_DIR%\pom.xml" (
    echo ERROR: security-master-service-v2 not found at %SMS_DIR%
    exit /b 1
)

if not exist "%ENV_FILE%" (
    echo ERROR: .env file not found at %ENV_FILE%
    exit /b 1
)

for /f "usebackq tokens=1,* delims==" %%A in ("%ENV_FILE%") do (
    set "line=%%A"
    if not "!line:~0,1!"=="#" if not "%%A"=="" (
        set "%%A=%%B"
    )
)

echo Starting Security Master Service v2 with dev profile...
cd /d "%SMS_DIR%"
call mvn spring-boot:run -Dspring-boot.run.profiles=dev
