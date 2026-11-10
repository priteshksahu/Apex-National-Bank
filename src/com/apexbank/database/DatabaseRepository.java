package com.apexbank.database;

import com.apexbank.model.*;
import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * Dual-Mode Persistence Layer:
 * 1. Primary Mode: Connects to SQL Database (MySQL / PostgreSQL / SQLite) via JDBC if configured in db.properties.
 * 2. Embedded Auto-Fallback Mode: Automatically persists to binary database file (data/bank_database.ser)
 *    and CSV human-readable tables (data/accounts.csv, data/transactions.csv) so the project works
 *    out-of-the-box without requiring external database server installation.
 */
public class DatabaseRepository {
    private static final String DATA_DIR = "data";
    private static final String BINARY_DB_FILE = DATA_DIR + File.separator + "bank_database.ser";
    private static final String ACCOUNTS_CSV = DATA_DIR + File.separator + "accounts.csv";
    private static final String TRANSACTIONS_CSV = DATA_DIR + File.separator + "transactions.csv";
    private static final String DB_CONFIG_FILE = "database" + File.separator + "db.properties";

    private boolean jdbcEnabled = false;
    private String jdbcUrl;
    private String jdbcUser;
    private String jdbcPassword;

    public DatabaseRepository() {
        new File(DATA_DIR).mkdirs();
        loadJdbcConfig();
        if (jdbcEnabled) {
            initializeSqlTables();
        }
    }

    private void loadJdbcConfig() {
        File cfg = new File(DB_CONFIG_FILE);
        if (!cfg.exists()) return;
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(cfg)) {
            props.load(fis);
            this.jdbcEnabled = Boolean.parseBoolean(props.getProperty("db.enabled", "false"));
            this.jdbcUrl = props.getProperty("db.url", "jdbc:mysql://localhost:3306/apex_bank_db").trim();
            this.jdbcUser = props.getProperty("db.user", "root").trim();
            this.jdbcPassword = props.getProperty("db.password", "").trim();
        } catch (IOException e) {
            System.err.println("Could not read db.properties, using embedded file database.");
        }
    }

    private Connection getConnection() throws SQLException {
        if (jdbcUser.isEmpty()) {
            return DriverManager.getConnection(jdbcUrl);
        }
        return DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);
    }

    private void initializeSqlTables() {
        String createAccountsSql = "CREATE TABLE IF NOT EXISTS accounts (" +
                "account_number VARCHAR(20) PRIMARY KEY," +
                "holder_name VARCHAR(100) NOT NULL," +
                "email VARCHAR(100)," +
                "phone VARCHAR(30)," +
                "pin VARCHAR(10) NOT NULL," +
                "account_type VARCHAR(20) NOT NULL," +
                "balance DOUBLE NOT NULL," +
                "overdraft_limit DOUBLE DEFAULT 0.0" +
                ")";

        String createTransactionsSql = "CREATE TABLE IF NOT EXISTS transactions (" +
                "txn_id VARCHAR(30) PRIMARY KEY," +
                "account_number VARCHAR(20) NOT NULL," +
                "timestamp VARCHAR(30) NOT NULL," +
                "txn_type VARCHAR(20) NOT NULL," +
                "amount DOUBLE NOT NULL," +
                "post_balance DOUBLE NOT NULL," +
                "description VARCHAR(255)," +
                "FOREIGN KEY (account_number) REFERENCES accounts(account_number) ON DELETE CASCADE" +
                ")";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(createAccountsSql);
            stmt.execute(createTransactionsSql);
            System.out.println("[DB] Connected to SQL Database via JDBC successfully.");
        } catch (SQLException e) {
            System.err.println("[DB] JDBC connection failed (" + e.getMessage() + "). Falling back to Embedded File Database.");
            this.jdbcEnabled = false;
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Account> loadAllAccounts() {
        if (jdbcEnabled) {
            return loadFromSqlDatabase();
        }
        File file = new File(BINARY_DB_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                return (Map<String, Account>) ois.readObject();
            } catch (Exception e) {
                System.err.println("[DB] Starting fresh embedded database.");
            }
        }
        return new LinkedHashMap<>();
    }

    public void saveAll(Map<String, Account> accounts) {
        // Always save to Embedded File + CSV for instant portability and inspection
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(BINARY_DB_FILE))) {
            oos.writeObject(accounts);
        } catch (IOException e) {
            System.err.println("[DB] Error saving binary database: " + e.getMessage());
        }
        exportToCsvTables(accounts);

        if (jdbcEnabled) {
            syncToSqlDatabase(accounts);
        }
    }

    private void exportToCsvTables(Map<String, Account> accounts) {
        try (PrintWriter accWriter = new PrintWriter(new FileWriter(ACCOUNTS_CSV));
             PrintWriter txnWriter = new PrintWriter(new FileWriter(TRANSACTIONS_CSV))) {

            accWriter.println("ACCOUNT_NUMBER,HOLDER_NAME,EMAIL,PHONE,ACCOUNT_TYPE,BALANCE,MIN_OR_OVERDRAFT");
            txnWriter.println("TXN_ID,ACCOUNT_NUMBER,TIMESTAMP,TYPE,AMOUNT,POST_BALANCE,DESCRIPTION");

            for (Account a : accounts.values()) {
                accWriter.printf("%s,\"%s\",\"%s\",\"%s\",%s,%.2f,%.2f%n",
                        a.getAccountNumber(),
                        a.getHolderName().replace("\"", "'"),
                        a.getEmail().replace("\"", "'"),
                        a.getPhone().replace("\"", "'"),
                        a.getAccountType(),
                        a.getBalance(),
                        a.getOverdraftOrMinBalance());

                for (Transaction t : a.getTransactions()) {
                    txnWriter.printf("%s,%s,%s,%s,%.2f,%.2f,\"%s\"%n",
                            t.getId(),
                            a.getAccountNumber(),
                            t.getTimestamp(),
                            t.getType(),
                            t.getAmount(),
                            t.getPostBalance(),
                            t.getDescription().replace("\"", "'"));
                }
            }
        } catch (IOException e) {
            System.err.println("[DB] CSV export warning: " + e.getMessage());
        }
    }

    private Map<String, Account> loadFromSqlDatabase() {
        Map<String, Account> map = new LinkedHashMap<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM accounts")) {

            while (rs.next()) {
                String accNum = rs.getString("account_number");
                String name = rs.getString("holder_name");
                String email = rs.getString("email");
                String phone = rs.getString("phone");
                String pin = rs.getString("pin");
                String type = rs.getString("account_type");
                double balance = rs.getDouble("balance");
                double od = rs.getDouble("overdraft_limit");

                Account acc;
                if ("SAVINGS".equalsIgnoreCase(type)) {
                    acc = new SavingsAccount(accNum, name, email, phone, pin, Math.max(balance, SavingsAccount.MIN_BALANCE));
                } else {
                    acc = new CurrentAccount(accNum, name, email, phone, pin, Math.max(balance, 0), od);
                }
                acc.getTransactions().clear();
                map.put(accNum, acc);
            }

            try (ResultSet trs = stmt.executeQuery("SELECT * FROM transactions ORDER BY timestamp ASC")) {
                while (trs.next()) {
                    String accNum = trs.getString("account_number");
                    Account acc = map.get(accNum);
                    if (acc != null) {
                        acc.loadTransaction(new Transaction(
                                trs.getString("txn_id"),
                                accNum,
                                trs.getString("timestamp"),
                                trs.getString("txn_type"),
                                trs.getDouble("amount"),
                                trs.getDouble("post_balance"),
                                trs.getString("description")
                        ));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[DB] Failed loading SQL rows: " + e.getMessage());
        }
        return map;
    }

    private void syncToSqlDatabase(Map<String, Account> accounts) {
        String upsertAcc = "REPLACE INTO accounts (account_number, holder_name, email, phone, pin, account_type, balance, overdraft_limit) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String upsertTxn = "REPLACE INTO transactions (txn_id, account_number, timestamp, txn_type, amount, post_balance, description) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement psAcc = conn.prepareStatement(upsertAcc);
             PreparedStatement psTxn = conn.prepareStatement(upsertTxn)) {

            for (Account a : accounts.values()) {
                psAcc.setString(1, a.getAccountNumber());
                psAcc.setString(2, a.getHolderName());
                psAcc.setString(3, a.getEmail());
                psAcc.setString(4, a.getPhone());
                psAcc.setString(5, a.getPin());
                psAcc.setString(6, a.getAccountType());
                psAcc.setDouble(7, a.getBalance());
                psAcc.setDouble(8, a instanceof CurrentAccount ? ((CurrentAccount) a).getOverdraftLimit() : 0.0);
                psAcc.addBatch();

                for (Transaction t : a.getTransactions()) {
                    psTxn.setString(1, t.getId());
                    psTxn.setString(2, a.getAccountNumber());
                    psTxn.setString(3, t.getTimestamp());
                    psTxn.setString(4, t.getType());
                    psTxn.setDouble(5, t.getAmount());
                    psTxn.setDouble(6, t.getPostBalance());
                    psTxn.setString(7, t.getDescription());
                    psTxn.addBatch();
                }
            }
            psAcc.executeBatch();
            psTxn.executeBatch();
        } catch (SQLException e) {
            System.err.println("[DB] SQL sync warning: " + e.getMessage());
        }
    }

    public String getStorageStatus() {
        return jdbcEnabled ? "Connected: JDBC SQL Database + CSV Backup" : "Connected: Embedded Local Database (data/bank_database.ser & CSV)";
    }
}
