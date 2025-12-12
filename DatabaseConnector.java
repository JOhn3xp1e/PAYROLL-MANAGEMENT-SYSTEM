package com.payroll.utils;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class DatabaseConnector {
    private static final String DB_URL = "jdbc:sqlite:payroll.db";
    private static Connection connection = null;
    
    public static void initializeDatabase() {
        try {
            getConnection();
            
            // Turn off auto-commit
            connection.setAutoCommit(false);
            
            // Create tables
            createTables();
            
            // Insert default data
            insertDefaultData();
            
            connection.commit();
            System.out.println("Database initialized successfully!");
            
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
            
            // Try to rollback
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException ex) {
                System.err.println("Rollback failed: " + ex.getMessage());
            }
        }
    }
    
    private static void createTables() throws SQLException {
    String[] createTableQueries = {
        // Users table - FIXED: Added status column
        "CREATE TABLE IF NOT EXISTS users (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "username VARCHAR(50) UNIQUE NOT NULL," +
        "password_hash VARCHAR(255) NOT NULL," +
        "role VARCHAR(20) NOT NULL," +
        "email VARCHAR(100) UNIQUE NOT NULL," +
        "first_name VARCHAR(50)," +
        "last_name VARCHAR(50)," +
        "phone VARCHAR(20)," +
        "status VARCHAR(20) DEFAULT 'PENDING_APPROVAL'," + // PENDING_APPROVAL, ACTIVE, REJECTED
        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
        "last_login TIMESTAMP)",
        
        // Employees table
        "CREATE TABLE IF NOT EXISTS employees (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "user_id INTEGER UNIQUE," +
        "employee_code VARCHAR(20) UNIQUE NOT NULL," +
        "first_name VARCHAR(50) NOT NULL," +
        "last_name VARCHAR(50) NOT NULL," +
        "email VARCHAR(100) UNIQUE NOT NULL," +
        "phone VARCHAR(20)," +
        "address TEXT," +
        "date_of_birth DATE," +
        "gender VARCHAR(10)," +
        "designation VARCHAR(50)," +
        "department VARCHAR(50)," +
        "employment_type VARCHAR(20)," +
        "hire_date DATE NOT NULL," +
        "employment_status VARCHAR(20) DEFAULT 'ACTIVE'," +
        "bank_name VARCHAR(100)," +
        "account_number VARCHAR(50)," +
        "basic_salary DECIMAL(10,2) NOT NULL," +
        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
        
        // Attendance table
        "CREATE TABLE IF NOT EXISTS attendance (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "employee_id INTEGER NOT NULL," +
        "date DATE NOT NULL," +
        "clock_in TIME," +
        "clock_out TIME," +
        "status VARCHAR(20) DEFAULT 'PRESENT'," +
        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
        
        // Leaves table
        "CREATE TABLE IF NOT EXISTS leaves (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "employee_id INTEGER NOT NULL," +
        "leave_type VARCHAR(20)," +
        "start_date DATE NOT NULL," +
        "end_date DATE NOT NULL," +
        "total_days INTEGER NOT NULL," +
        "reason TEXT," +
        "status VARCHAR(20) DEFAULT 'PENDING'," +
        "approved_by INTEGER," +
        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)",
        
        // Payroll table
        "CREATE TABLE IF NOT EXISTS payroll (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
        "employee_id INTEGER NOT NULL," +
        "pay_period_start DATE NOT NULL," +
        "pay_period_end DATE NOT NULL," +
        "payment_date DATE NOT NULL," +
        "basic_salary DECIMAL(10,2) NOT NULL," +
        "overtime_amount DECIMAL(10,2) DEFAULT 0," +
        "bonus_amount DECIMAL(10,2) DEFAULT 0," +
        "allowance_amount DECIMAL(10,2) DEFAULT 0," +
        "deduction_amount DECIMAL(10,2) DEFAULT 0," +
        "tax_amount DECIMAL(10,2) DEFAULT 0," +
        "net_salary DECIMAL(10,2) NOT NULL," +
        "status VARCHAR(20) DEFAULT 'PENDING'," +
        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
    };
    
    try (Statement stmt = connection.createStatement()) {
        for (String query : createTableQueries) {
            try {
                stmt.execute(query);
                System.out.println("Table created: " + query.substring(13, query.indexOf('(')).trim());
            } catch (SQLException e) {
                // Table might already exist
                System.out.println("Table already exists or error: " + e.getMessage());
                // Try to add missing columns
                addMissingColumns();
            }
        }
    }
}

private static void addMissingColumns() throws SQLException {
    try (Statement stmt = connection.createStatement()) {
        // Check if status column exists in users table
        try {
            stmt.executeQuery("SELECT status FROM users LIMIT 1");
        } catch (SQLException e) {
            // Column doesn't exist, add it
            if (e.getMessage().contains("no such column: status")) {
                System.out.println("Adding missing column: status to users table");
                stmt.execute("ALTER TABLE users ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE'");
                
                // Update existing users
                stmt.execute("UPDATE users SET status = 'ACTIVE' WHERE status IS NULL");
            }
        }
    }
    }
    
    private static void insertDefaultData() throws SQLException {
        // Check if admin exists
        String checkAdmin = "SELECT COUNT(*) FROM users WHERE username = 'admin'";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(checkAdmin)) {
            
            if (rs.next() && rs.getInt(1) == 0) {
                // Insert admin
                String insertAdmin = "INSERT INTO users (username, password_hash, role, email, first_name, last_name, status) " +
                                   "VALUES ('admin', '" + hashPassword("admin123") + "', 'ADMIN', 'admin@payroll.com', 'Admin', 'User', 'ACTIVE')";
                stmt.executeUpdate(insertAdmin);
                System.out.println("Default admin user created!");
            }
        }
    }
    
    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(DB_URL);
                // Enable foreign keys
                connection.createStatement().execute("PRAGMA foreign_keys = ON");
                System.out.println("Database connected successfully!");
            } catch (ClassNotFoundException e) {
                throw new SQLException("SQLite JDBC driver not found", e);
            }
        }
        return connection;
    }
    
    public static synchronized void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connection = null;
        }
    }
    
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Password hashing failed: " + e.getMessage());
            return password;
        }
    }
    
    public static boolean verifyPassword(String inputPassword, String storedHash) {
        return hashPassword(inputPassword).equals(storedHash);
    }
    
    public static ResultSet executeQuery(String query) throws SQLException {
        Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(query);
    }
    
    public static int executeUpdate(String query) throws SQLException {
        Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        int result = stmt.executeUpdate(query);
        
        // Only commit if we're not in auto-commit mode
        if (!conn.getAutoCommit()) {
            conn.commit();
        }
        return result;
    }
    
    // New method for user creation with proper connection management
    public static boolean createUser(String username, String password, String email, String role, String firstName, String lastName) throws SQLException {
    String checkQuery = "SELECT COUNT(*) FROM users WHERE username = ? OR email = ?";
    String insertQuery = "INSERT INTO users (username, password_hash, role, email, first_name, last_name, status) VALUES (?, ?, ?, ?, ?, ?, 'PENDING_APPROVAL')";
    
    Connection conn = getConnection();
    
    try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
        checkStmt.setString(1, username);
        checkStmt.setString(2, email);
        ResultSet rs = checkStmt.executeQuery();
        
        if (rs.next() && rs.getInt(1) > 0) {
            System.out.println("User already exists: " + username + " or email: " + email);
            return false; // User already exists
        }
        
        try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
            insertStmt.setString(1, username);
            insertStmt.setString(2, hashPassword(password));
            
            // Ensure role is uppercase
            String normalizedRole = role != null ? role.toUpperCase() : "EMPLOYEE";
            insertStmt.setString(3, normalizedRole);
            
            insertStmt.setString(4, email);
            insertStmt.setString(5, firstName);
            insertStmt.setString(6, lastName);
            
            System.out.println("=== CREATING USER ===");
            System.out.println("Username: " + username);
            System.out.println("Email: " + email);
            System.out.println("Role: " + normalizedRole);
            System.out.println("First Name: " + firstName);
            System.out.println("Last Name: " + lastName);
            System.out.println("=====================");
            
            int rows = insertStmt.executeUpdate();
            
            if (!conn.getAutoCommit()) {
                conn.commit();
            }
            System.out.println("User created successfully: " + (rows > 0));
            return rows > 0;
        }
    }
}
    
    // Add employee method with proper transaction management
    public static boolean addEmployee(String firstName, String lastName, String email, String phone,
                                     String address, Date dob, String gender, String designation,
                                     String department, String employmentType, Date hireDate,
                                     String bankName, String accountNumber, double basicSalary) throws SQLException {
        String employeeCode = "EMP" + System.currentTimeMillis();
        String query = "INSERT INTO employees (first_name, last_name, email, phone, address, " +
                       "date_of_birth, gender, designation, department, employment_type, hire_date, " +
                       "bank_name, account_number, basic_salary, employee_code) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = getConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, email);
            stmt.setString(4, phone);
            stmt.setString(5, address);
            stmt.setDate(6, dob);
            stmt.setString(7, gender);
            stmt.setString(8, designation);
            stmt.setString(9, department);
            stmt.setString(10, employmentType);
            stmt.setDate(11, hireDate);
            stmt.setString(12, bankName);
            stmt.setString(13, accountNumber);
            stmt.setDouble(14, basicSalary);
            stmt.setString(15, employeeCode);
            
            int rows = stmt.executeUpdate();
            
            if (!conn.getAutoCommit()) {
                conn.commit();
            }
            return rows > 0;
        }
    }
    
    // Method to get all employees
    public static ResultSet getAllEmployees() throws SQLException {
        String query = "SELECT * FROM employees ORDER BY id DESC";
        Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(query);
    }
    
    // Method to get all payroll records
    public static ResultSet getAllPayroll() throws SQLException {
        String query = "SELECT p.*, e.first_name, e.last_name FROM payroll p " +
                      "JOIN employees e ON p.employee_id = e.id ORDER BY p.payment_date DESC";
        Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(query);
    }
    
    // Method to get all attendance records
    public static ResultSet getAllAttendance() throws SQLException {
        String query = "SELECT a.*, e.first_name, e.last_name FROM attendance a " +
                      "JOIN employees e ON a.employee_id = e.id ORDER BY a.date DESC LIMIT 50";
        Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(query);
    }
    
    // Method to get all leaves
    public static ResultSet getAllLeaves() throws SQLException {
        String query = "SELECT l.*, e.first_name, e.last_name FROM leaves l " +
                      "JOIN employees e ON l.employee_id = e.id ORDER BY l.start_date DESC";
        Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(query);
    }

   
    // Add this method to DatabaseConnector.java
public static int getPendingApprovalsCount() throws SQLException {
    try {
        // First check if status column exists
        Connection conn = getConnection();
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("PRAGMA table_info(users)");
            boolean hasStatusColumn = false;
            while (rs.next()) {
                if ("status".equals(rs.getString("name"))) {
                    hasStatusColumn = true;
                    break;
                }
            }
            
            if (hasStatusColumn) {
                String query = "SELECT COUNT(*) FROM users WHERE status = 'PENDING_APPROVAL'";
                try (Statement countStmt = conn.createStatement();
                     ResultSet countRs = countStmt.executeQuery(query)) {
                    return countRs.next() ? countRs.getInt(1) : 0;
                }
            } else {
                return 0; // No status column means no pending approvals
            }
        }
    } catch (SQLException e) {
        System.err.println("Error checking pending approvals: " + e.getMessage());
        return 0;
    }
}

// Add this method to get pending users
public static ResultSet getPendingUsers() throws SQLException {
    try {
        Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        
        // Check if status column exists
        ResultSet rs = stmt.executeQuery("PRAGMA table_info(users)");
        boolean hasStatusColumn = false;
        while (rs.next()) {
            if ("status".equals(rs.getString("name"))) {
                hasStatusColumn = true;
                break;
            }
        }
        
        if (hasStatusColumn) {
            String query = "SELECT * FROM users WHERE status = 'PENDING_APPROVAL' ORDER BY created_at DESC";
            return conn.createStatement().executeQuery(query);
        } else {
            // Return empty result set
            return conn.createStatement().executeQuery("SELECT * FROM users WHERE 1=0");
        }
    } catch (SQLException e) {
        System.err.println("Error getting pending users: " + e.getMessage());
        throw e;
    }
}

    
    
    

    public static boolean updateUserStatus(int userId, String status) {
        String query = "UPDATE users SET status = ? WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, status);
            pstmt.setInt(2, userId);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (!conn.getAutoCommit()) {
                conn.commit();
            }
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating user status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}