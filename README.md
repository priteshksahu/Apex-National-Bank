# Apex National Bank — Full-Stack Java Bank Management System

A complete, modular **Full-Stack Java Desktop Banking Application** featuring a **Java Swing GUI Frontend**, a **Layered OOP Service Backend**, and a **Dual-Mode Database Layer** (JDBC SQL + Embedded Local File & CSV Database).

---

## Folder Structure

```text
BankManagementSystem/
├── src/
│   └── com/apexbank/
│       ├── exception/
│       │   └── BankingException.java       # Custom checked exception handling
│       ├── model/
│       │   ├── Account.java                # Abstract Account entity
│       │   ├── SavingsAccount.java         # Savings rules (Min balance + 4.5% Interest)
│       │   ├── CurrentAccount.java         # Current rules (Overdraft limit protection)
│       │   └── Transaction.java            # Immutable Transaction audit record
│       ├── database/
│       │   └── DatabaseRepository.java     # JDBC SQL + Embedded Binary & CSV persistence
│       ├── service/
│       │   └── BankService.java            # Core banking business logic & authentication
│       └── ui/
│           └── BankManagementApp.java      # Full Java Swing GUI (Customer + Admin portals)
├── database/
│   ├── schema.sql                          # MySQL / PostgreSQL / SQLite DDL & seed script
│   └── db.properties                       # JDBC connection configuration
├── data/
│   ├── accounts.csv                        # Live synced human-readable Accounts table
│   └── transactions.csv                    # Live synced human-readable Transactions table
├── run.bat                                 # One-click build & launch script for Windows
├── run.sh                                  # One-click build & launch script for Linux / macOS
└── README.md                               # Project documentation
```

---

## How to Run (Zero Configuration Needed)

### Option 1: One-Click Run Script
* **Windows:** Double-click `run.bat` (or run `run.bat` in Command Prompt).
* **macOS / Linux:**
  ```bash
  chmod +x run.sh
  ./run.sh
  ```

### Option 2: Manual Terminal Compilation
```bash
mkdir out
javac -encoding UTF-8 -d out src/com/apexbank/exception/*.java src/com/apexbank/model/*.java src/com/apexbank/database/*.java src/com/apexbank/service/*.java src/com/apexbank/ui/*.java
java -cp out com.apexbank.ui.BankManagementApp
```

---

## Pre-Seeded Demo Accounts

| Account Number | Holder Name | Account Type | PIN | Initial Balance | Special Rules |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `ACC100101` | Aarav Sharma | Savings | `1234` | ₹12,500.00 | Min Balance: ₹500, 4.5% Interest |
| `ACC100102` | Apex Tech Solutions Pvt Ltd | Current | `1234` | ₹45,000.00 | Overdraft Limit: ₹20,000.00 |
| `ACC100103` | Priya Verma | Savings | `1234` | ₹8,200.00 | Min Balance: ₹500, 4.5% Interest |

---

## Database Options

1. **Embedded Local Database (Active by Default):**
   Every account registration, deposit, withdrawal, transfer, and interest credit is automatically saved to `data/bank_database.ser` and exported into `data/accounts.csv` and `data/transactions.csv`.
2. **External SQL Database via JDBC (MySQL / PostgreSQL / SQLite):**
   Run `database/schema.sql` in your SQL server, then open `database/db.properties` and set `db.enabled=true` along with your `db.url`, `db.user`, and `db.password`.
