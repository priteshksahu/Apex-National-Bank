package com.apexbank.ui;

import com.apexbank.exception.BankingException;
import com.apexbank.model.*;
import com.apexbank.service.BankService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

public class BankManagementApp extends JFrame {
    private final BankService bankService;
    private Account loggedInAccount;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainContainer = new JPanel(cardLayout);

    // Dashboard Labels
    private final JLabel lblWelcome = new JLabel();
    private final JLabel lblSubDetails = new JLabel();
    private final JLabel lblBalance = new JLabel();
    private final DefaultTableModel txnTableModel = new DefaultTableModel(
            new String[]{"TXN ID", "Timestamp", "Type", "Amount (₹)", "Closing Balance (₹)", "Description"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    // Admin Table Model
    private final DefaultTableModel adminTableModel = new DefaultTableModel(
            new String[]{"Account No", "Holder Name", "Email", "Phone", "Type", "Balance (₹)", "MinBal / OD Limit (₹)"}, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JLabel lblAdminSummary = new JLabel();

    public BankManagementApp() {
        this.bankService = new BankService();

        setTitle("Apex National Bank — Complete Full-Stack Banking System");
        setSize(1040, 680);
        setMinimumSize(new Dimension(920, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        mainContainer.add(buildLoginView(), "LOGIN");
        mainContainer.add(buildRegisterView(), "REGISTER");
        mainContainer.add(buildDashboardView(), "DASHBOARD");
        mainContainer.add(buildAdminView(), "ADMIN");

        setLayout(new BorderLayout());
        add(mainContainer, BorderLayout.CENTER);
        add(buildFooterBar(), BorderLayout.SOUTH);

        cardLayout.show(mainContainer, "LOGIN");
    }

    private JPanel buildFooterBar() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(15, 23, 42));
        footer.setBorder(new EmptyBorder(6, 16, 6, 16));

        JLabel dbStatus = new JLabel(bankService.getDatabaseStatus());
        dbStatus.setForeground(new Color(148, 163, 184));
        dbStatus.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JLabel demoHint = new JLabel("Demo Accounts: ACC100101, ACC100102, ACC100103 (PIN: 1234)");
        demoHint.setForeground(new Color(56, 189, 248));
        demoHint.setFont(new Font("SansSerif", Font.BOLD, 12));

        footer.add(dbStatus, BorderLayout.WEST);
        footer.add(demoHint, BorderLayout.EAST);
        return footer;
    }

    // =========================================================================
    // 1. LOGIN SCREEN
    // =========================================================================
    private JPanel buildLoginView() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(new Color(241, 245, 249));

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(32, 44, 32, 44)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        JLabel title = new JLabel("APEX NATIONAL BANK", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(new Color(15, 23, 42));
        card.add(title, gbc);

        gbc.gridy++;
        JLabel subtitle = new JLabel("Core Banking Portal — Customer & Admin Login", SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(new Color(100, 116, 139));
        card.add(subtitle, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        card.add(new JLabel("Account Number:"), gbc);

        gbc.gridx = 1;
        JTextField txtAccNum = new JTextField("ACC100101", 18);
        card.add(txtAccNum, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        card.add(new JLabel("4-Digit Security PIN:"), gbc);

        gbc.gridx = 1;
        JPasswordField txtPin = new JPasswordField("1234", 18);
        card.add(txtPin, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JButton btnLogin = createButton("Login to Customer Portal", new Color(37, 99, 235), Color.WHITE);
        card.add(btnLogin, gbc);

        gbc.gridy++;
        JButton btnRegister = createButton("Open a New Bank Account", new Color(16, 185, 129), Color.WHITE);
        card.add(btnRegister, gbc);

        gbc.gridy++;
        JButton btnAdmin = createButton("Bank Admin Dashboard & Database Inspector", new Color(71, 85, 105), Color.WHITE);
        card.add(btnAdmin, gbc);

        btnLogin.addActionListener(e -> {
            try {
                String acc = txtAccNum.getText().trim();
                String pin = new String(txtPin.getPassword()).trim();
                loggedInAccount = bankService.authenticate(acc, pin);
                refreshDashboard();
                cardLayout.show(mainContainer, "DASHBOARD");
            } catch (BankingException ex) {
                showError(ex.getMessage());
            }
        });

        btnRegister.addActionListener(e -> cardLayout.show(mainContainer, "REGISTER"));
        btnAdmin.addActionListener(e -> {
            refreshAdminView();
            cardLayout.show(mainContainer, "ADMIN");
        });

        outer.add(card);
        return outer;
    }

    // =========================================================================
    // 2. ACCOUNT REGISTRATION SCREEN
    // =========================================================================
    private JPanel buildRegisterView() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(new Color(241, 245, 249));

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(25, 40, 25, 40)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        JLabel title = new JLabel("New Customer Account Registration", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(new Color(15, 23, 42));
        card.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;
        card.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        JTextField txtName = new JTextField(20);
        card.add(txtName, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        card.add(new JLabel("Email Address:"), gbc);
        gbc.gridx = 1;
        JTextField txtEmail = new JTextField("customer@example.com", 20);
        card.add(txtEmail, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        card.add(new JLabel("10-Digit Mobile No:"), gbc);
        gbc.gridx = 1;
        JTextField txtPhone = new JTextField("9876543210", 20);
        card.add(txtPhone, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        card.add(new JLabel("Set 4-Digit PIN:"), gbc);
        gbc.gridx = 1;
        JPasswordField txtPin = new JPasswordField(20);
        card.add(txtPin, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        card.add(new JLabel("Account Type:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> cmbType = new JComboBox<>(new String[]{
                "Savings Account (Min ₹500, 4.5% Interest)",
                "Current Account (Business Overdraft)"
        });
        card.add(cmbType, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        card.add(new JLabel("Initial Deposit (₹):"), gbc);
        gbc.gridx = 1;
        JTextField txtDeposit = new JTextField("2000", 20);
        card.add(txtDeposit, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        JLabel lblOd = new JLabel("Overdraft Limit (₹):");
        lblOd.setEnabled(false);
        card.add(lblOd, gbc);
        gbc.gridx = 1;
        JTextField txtOd = new JTextField("10000", 20);
        txtOd.setEnabled(false);
        card.add(txtOd, gbc);

        cmbType.addActionListener(e -> {
            boolean isCurrent = cmbType.getSelectedIndex() == 1;
            lblOd.setEnabled(isCurrent);
            txtOd.setEnabled(isCurrent);
        });

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JButton btnSubmit = createButton("Create Account & Login", new Color(16, 185, 129), Color.WHITE);
        card.add(btnSubmit, gbc);

        gbc.gridy++;
        JButton btnBack = createButton("Back to Login Screen", new Color(100, 116, 139), Color.WHITE);
        card.add(btnBack, gbc);

        btnSubmit.addActionListener(e -> {
            try {
                String name = txtName.getText().trim();
                String email = txtEmail.getText().trim();
                String phone = txtPhone.getText().trim();
                String pin = new String(txtPin.getPassword()).trim();
                double initial = Double.parseDouble(txtDeposit.getText().trim());

                Account created;
                if (cmbType.getSelectedIndex() == 0) {
                    created = bankService.createSavingsAccount(name, email, phone, pin, initial);
                } else {
                    double od = Double.parseDouble(txtOd.getText().trim());
                    created = bankService.createCurrentAccount(name, email, phone, pin, initial, od);
                }

                JOptionPane.showMessageDialog(this,
                        "Account Opened Successfully!\n\n" +
                                "Account Number: " + created.getAccountNumber() + "\n" +
                                "Account Holder: " + created.getHolderName() + "\n" +
                                "Account Type: " + created.getAccountType(),
                        "Welcome to Apex Bank", JOptionPane.INFORMATION_MESSAGE);

                txtName.setText("");
                txtPin.setText("");
                loggedInAccount = created;
                refreshDashboard();
                cardLayout.show(mainContainer, "DASHBOARD");
            } catch (NumberFormatException ex) {
                showError("Please enter valid numeric values for deposit/overdraft.");
            } catch (BankingException ex) {
                showError(ex.getMessage());
            }
        });

        btnBack.addActionListener(e -> cardLayout.show(mainContainer, "LOGIN"));

        outer.add(card);
        return outer;
    }

    // =========================================================================
    // 3. CUSTOMER DASHBOARD VIEW
    // =========================================================================
    private JPanel buildDashboardView() {
        JPanel root = new JPanel(new BorderLayout(15, 15));
        root.setBackground(new Color(241, 245, 249));
        root.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Top Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(15, 23, 42));
        header.setBorder(new EmptyBorder(16, 22, 16, 22));

        JPanel leftBox = new JPanel(new GridLayout(2, 1, 2, 2));
        leftBox.setOpaque(false);
        lblWelcome.setFont(new Font("SansSerif", Font.BOLD, 19));
        lblWelcome.setForeground(Color.WHITE);
        lblSubDetails.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblSubDetails.setForeground(new Color(148, 163, 184));
        leftBox.add(lblWelcome);
        leftBox.add(lblSubDetails);

        lblBalance.setFont(new Font("SansSerif", Font.BOLD, 25));
        lblBalance.setForeground(new Color(74, 222, 128));

        header.add(leftBox, BorderLayout.WEST);
        header.add(lblBalance, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        // Transaction Table
        JTable table = new JTable(txnTableModel);
        table.setRowHeight(26);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder("Account Transaction Passbook (Latest First)"));
        root.add(scroll, BorderLayout.CENTER);

        // Action Sidebar
        JPanel sidebar = new JPanel(new GridLayout(8, 1, 8, 8));
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(225, 0));

        JButton btnDeposit = createButton("Deposit Money", new Color(22, 163, 74), Color.WHITE);
        JButton btnWithdraw = createButton("Withdraw Money", new Color(234, 88, 12), Color.WHITE);
        JButton btnTransfer = createButton("Transfer Funds (IMPS)", new Color(37, 99, 235), Color.WHITE);
        JButton btnInterest = createButton("Credit Annual Interest", new Color(124, 58, 237), Color.WHITE);
        JButton btnProfile = createButton("Edit Contact Profile", new Color(13, 148, 136), Color.WHITE);
        JButton btnPin = createButton("Change Security PIN", new Color(71, 85, 105), Color.WHITE);
        JButton btnExport = createButton("Export Statement (CSV)", new Color(2, 132, 199), Color.WHITE);
        JButton btnLogout = createButton("Logout", new Color(220, 38, 38), Color.WHITE);

        sidebar.add(btnDeposit);
        sidebar.add(btnWithdraw);
        sidebar.add(btnTransfer);
        sidebar.add(btnInterest);
        sidebar.add(btnProfile);
        sidebar.add(btnPin);
        sidebar.add(btnExport);
        sidebar.add(btnLogout);

        root.add(sidebar, BorderLayout.EAST);

        btnDeposit.addActionListener(e -> actionDeposit());
        btnWithdraw.addActionListener(e -> actionWithdraw());
        btnTransfer.addActionListener(e -> actionTransfer());
        btnInterest.addActionListener(e -> actionInterest());
        btnProfile.addActionListener(e -> actionEditProfile());
        btnPin.addActionListener(e -> actionChangePin());
        btnExport.addActionListener(e -> actionExportStatement());
        btnLogout.addActionListener(e -> {
            loggedInAccount = null;
            cardLayout.show(mainContainer, "LOGIN");
        });

        return root;
    }

    // =========================================================================
    // 4. BANK ADMIN DASHBOARD VIEW
    // =========================================================================
    private JPanel buildAdminView() {
        JPanel root = new JPanel(new BorderLayout(15, 15));
        root.setBackground(new Color(241, 245, 249));
        root.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(30, 41, 59));
        topBar.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("BANK ADMINISTRATION & DATABASE INSPECTOR");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        lblAdminSummary.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblAdminSummary.setForeground(new Color(56, 189, 248));

        topBar.add(title, BorderLayout.WEST);
        topBar.add(lblAdminSummary, BorderLayout.EAST);
        root.add(topBar, BorderLayout.NORTH);

        JTable adminTable = new JTable(adminTableModel);
        adminTable.setRowHeight(26);
        adminTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        JScrollPane scroll = new JScrollPane(adminTable);
        scroll.setBorder(BorderFactory.createTitledBorder("All Registered Bank Accounts (Synced with data/accounts.csv)"));
        root.add(scroll, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomBar.setOpaque(false);

        JButton btnBatchInterest = createButton("Apply Annual Interest to All Savings Accounts", new Color(124, 58, 237), Color.WHITE);
        JButton btnCloseAcc = createButton("Close / Delete Selected Account", new Color(220, 38, 38), Color.WHITE);
        JButton btnBack = createButton("Back to Login", new Color(71, 85, 105), Color.WHITE);

        bottomBar.add(btnBatchInterest);
        bottomBar.add(btnCloseAcc);
        bottomBar.add(btnBack);
        root.add(bottomBar, BorderLayout.SOUTH);

        btnBatchInterest.addActionListener(e -> {
            int count = bankService.applyAnnualInterestToAllSavings();
            refreshAdminView();
            JOptionPane.showMessageDialog(this, "Credited 4.5% annual interest to " + count + " Savings Account(s).");
        });

        btnCloseAcc.addActionListener(e -> {
            int row = adminTable.getSelectedRow();
            if (row < 0) {
                showError("Please select an account row from the table first.");
                return;
            }
            String accNum = adminTableModel.getValueAt(row, 0).toString();
            String pin = JOptionPane.showInputDialog(this, "Enter 4-digit PIN for " + accNum + " to confirm closure:");
            if (pin == null) return;
            try {
                bankService.deleteAccount(accNum, pin.trim());
                refreshAdminView();
                JOptionPane.showMessageDialog(this, "Account " + accNum + " closed and removed from database.");
            } catch (BankingException ex) {
                showError(ex.getMessage());
            }
        });

        btnBack.addActionListener(e -> cardLayout.show(mainContainer, "LOGIN"));

        return root;
    }

    // =========================================================================
    // ACTION CONTROLLERS
    // =========================================================================
    private void refreshDashboard() {
        if (loggedInAccount == null) return;
        lblWelcome.setText(loggedInAccount.getHolderName() + "  (" + loggedInAccount.getAccountNumber() + ")");
        lblSubDetails.setText("Type: " + loggedInAccount.getAccountType() +
                "   |   Email: " + loggedInAccount.getEmail() +
                "   |   Phone: " + loggedInAccount.getPhone());
        lblBalance.setText(String.format("₹%.2f", loggedInAccount.getBalance()));

        txnTableModel.setRowCount(0);
        List<Transaction> txns = loggedInAccount.getTransactions();
        for (int i = txns.size() - 1; i >= 0; i--) {
            Transaction t = txns.get(i);
            txnTableModel.addRow(new Object[]{
                    t.getId(),
                    t.getTimestamp(),
                    t.getType(),
                    String.format("%.2f", t.getAmount()),
                    String.format("%.2f", t.getPostBalance()),
                    t.getDescription()
            });
        }
    }

    private void refreshAdminView() {
        adminTableModel.setRowCount(0);
        for (Account a : bankService.getAllAccounts()) {
            adminTableModel.addRow(new Object[]{
                    a.getAccountNumber(),
                    a.getHolderName(),
                    a.getEmail(),
                    a.getPhone(),
                    a.getAccountType(),
                    String.format("%.2f", a.getBalance()),
                    String.format("%.2f", a.getOverdraftOrMinBalance())
            });
        }
        lblAdminSummary.setText(String.format("Total Accounts: %d   |   Total Deposits: ₹%.2f",
                bankService.getAllAccounts().size(), bankService.getTotalBankDeposits()));
    }

    private void actionDeposit() {
        JTextField txtAmt = new JTextField();
        JTextField txtNote = new JTextField("Self Cash Deposit");
        Object[] fields = {"Deposit Amount (₹):", txtAmt, "Remarks / Source:", txtNote};
        if (JOptionPane.showConfirmDialog(this, fields, "Deposit Money", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                double amt = Double.parseDouble(txtAmt.getText().trim());
                bankService.deposit(loggedInAccount, amt, txtNote.getText().trim());
                refreshDashboard();
                JOptionPane.showMessageDialog(this, String.format("₹%.2f deposited successfully!", amt));
            } catch (NumberFormatException ex) {
                showError("Invalid numeric amount.");
            } catch (BankingException ex) {
                showError(ex.getMessage());
            }
        }
    }

    private void actionWithdraw() {
        JTextField txtAmt = new JTextField();
        JTextField txtNote = new JTextField("ATM / Counter Withdrawal");
        Object[] fields = {"Withdrawal Amount (₹):", txtAmt, "Remarks / Purpose:", txtNote};
        if (JOptionPane.showConfirmDialog(this, fields, "Withdraw Money", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                double amt = Double.parseDouble(txtAmt.getText().trim());
                bankService.withdraw(loggedInAccount, amt, txtNote.getText().trim());
                refreshDashboard();
                JOptionPane.showMessageDialog(this, String.format("₹%.2f withdrawn successfully!", amt));
            } catch (NumberFormatException ex) {
                showError("Invalid numeric amount.");
            } catch (BankingException ex) {
                showError(ex.getMessage());
            }
        }
    }

    private void actionTransfer() {
        JTextField txtTo = new JTextField("ACC100102");
        JTextField txtAmt = new JTextField();
        JTextField txtRemarks = new JTextField("Fund Transfer");
        Object[] fields = {
                "Recipient Account Number:", txtTo,
                "Transfer Amount (₹):", txtAmt,
                "Remarks:", txtRemarks
        };
        if (JOptionPane.showConfirmDialog(this, fields, "Instant Fund Transfer (IMPS)", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                double amt = Double.parseDouble(txtAmt.getText().trim());
                bankService.transfer(loggedInAccount, txtTo.getText().trim(), amt, txtRemarks.getText().trim());
                refreshDashboard();
                JOptionPane.showMessageDialog(this, String.format("₹%.2f transferred to %s!", amt, txtTo.getText().trim().toUpperCase()));
            } catch (NumberFormatException ex) {
                showError("Invalid transfer amount.");
            } catch (BankingException ex) {
                showError(ex.getMessage());
            }
        }
    }

    private void actionInterest() {
        try {
            double interest = bankService.applyInterest(loggedInAccount);
            refreshDashboard();
            JOptionPane.showMessageDialog(this, String.format("Annual Interest of ₹%.2f credited to your account!", interest));
        } catch (BankingException ex) {
            showError(ex.getMessage());
        }
    }

    private void actionEditProfile() {
        JTextField txtName = new JTextField(loggedInAccount.getHolderName());
        JTextField txtEmail = new JTextField(loggedInAccount.getEmail());
        JTextField txtPhone = new JTextField(loggedInAccount.getPhone());
        Object[] fields = {
                "Account Holder Name:", txtName,
                "Email Address:", txtEmail,
                "10-Digit Phone Number:", txtPhone
        };
        if (JOptionPane.showConfirmDialog(this, fields, "Update Customer Profile", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                bankService.updateProfile(loggedInAccount, txtName.getText().trim(), txtEmail.getText().trim(), txtPhone.getText().trim());
                refreshDashboard();
                JOptionPane.showMessageDialog(this, "Profile updated successfully!");
            } catch (BankingException ex) {
                showError(ex.getMessage());
            }
        }
    }

    private void actionChangePin() {
        JPasswordField txtOld = new JPasswordField();
        JPasswordField txtNew = new JPasswordField();
        Object[] fields = {"Current 4-Digit PIN:", txtOld, "New 4-Digit PIN:", txtNew};
        if (JOptionPane.showConfirmDialog(this, fields, "Change Security PIN", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                bankService.changePin(loggedInAccount, new String(txtOld.getPassword()).trim(), new String(txtNew.getPassword()).trim());
                JOptionPane.showMessageDialog(this, "Security PIN updated successfully!");
            } catch (BankingException ex) {
                showError(ex.getMessage());
            }
        }
    }

    private void actionExportStatement() {
        String fileName = "data" + File.separator + "statement_" + loggedInAccount.getAccountNumber() + ".csv";
        try (PrintWriter pw = new PrintWriter(new FileWriter(fileName))) {
            pw.println("TXN_ID,TIMESTAMP,TYPE,AMOUNT,CLOSING_BALANCE,DESCRIPTION");
            for (Transaction t : loggedInAccount.getTransactions()) {
                pw.printf("%s,%s,%s,%.2f,%.2f,\"%s\"%n",
                        t.getId(), t.getTimestamp(), t.getType(), t.getAmount(), t.getPostBalance(), t.getDescription());
            }
            JOptionPane.showMessageDialog(this, "Account statement exported to:\n" + new File(fileName).getAbsolutePath());
        } catch (Exception ex) {
            showError("Failed to export statement: " + ex.getMessage());
        }
    }

    private JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setOpaque(true);             // Forces custom background color to render
        btn.setContentAreaFilled(true);  // Fills the button area with the solid color
        btn.setBorderPainted(false);     // Disables the native Windows white gradient overlay
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setBorder(new EmptyBorder(10, 16, 10, 16)); // Adds clean button padding
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Banking Alert", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new BankManagementApp().setVisible(true);
        });
    }
}
