@echo off
echo =========================================================
echo   Compiling Apex Bank Full-Stack Management System...
echo =========================================================
if not exist out mkdir out
javac -encoding UTF-8 -d out src/com/apexbank/exception/*.java src/com/apexbank/model/*.java src/com/apexbank/database/*.java src/com/apexbank/service/*.java src/com/apexbank/ui/*.java
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed! Please ensure JDK 11+ is installed.
    pause
    exit /b %errorlevel%
)
echo [SUCCESS] Launching Application...
java -cp out com.apexbank.ui.BankManagementApp
pause
