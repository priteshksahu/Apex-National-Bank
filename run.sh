#!/bin/bash
echo "========================================================="
echo "  Compiling Apex Bank Full-Stack Management System..."
echo "========================================================="
mkdir -p out
javac -encoding UTF-8 -d out src/com/apexbank/exception/*.java src/com/apexbank/model/*.java src/com/apexbank/database/*.java src/com/apexbank/service/*.java src/com/apexbank/ui/*.java
if [ $? -ne 0 ]; then
    echo "[ERROR] Compilation failed! Please ensure JDK 11+ is installed."
    exit 1
fi
echo "[SUCCESS] Launching Application..."
java -cp out com.apexbank.ui.BankManagementApp
