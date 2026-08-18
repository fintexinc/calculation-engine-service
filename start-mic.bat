@echo off
setlocal enabledelayedexpansion

set "MIC_DIR=%~dp0..\security-master-service-v2"
set "ENV_FILE=%MIC_DIR%\environment-v2\.env"

if not exist "%MIC_DIR%\pom.xml" (
    echo ERROR: Market Investment Catalogue not found at %MIC_DIR%
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

echo Starting Market Investment Catalogue Service v2 with dev profile...
cd /d "%MIC_DIR%"
call mvn spring-boot:run -Dspring-boot.run.profiles=dev
