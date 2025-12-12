package com.payroll.controllers;

import com.payroll.Main;
import com.payroll.utils.DatabaseConnector;
import com.payroll.utils.SessionManager;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.sql.*;
import javafx.scene.layout.BorderPane;

public class LoginSignupController {
    
    // Login Components
    @FXML private TextField loginUsernameField;
    @FXML private PasswordField loginPasswordField;
    @FXML private Button loginButton;
    @FXML private Label loginErrorLabel;
    @FXML private VBox loginForm;
    
    // Signup Components
    @FXML private TextField signupUsernameField;
    @FXML private TextField signupFirstNameField;
    @FXML private TextField signupLastNameField;
    @FXML private PasswordField signupPasswordField;
    @FXML private PasswordField signupConfirmPasswordField;
    @FXML private TextField signupEmailField;
    @FXML private ComboBox<String> signupRoleCombo;
    @FXML private Button signupButton;
    @FXML private Label signupErrorLabel;
    @FXML private VBox signupForm;
    
    // Navigation Buttons
    @FXML private Button switchToSignupBtn;
    @FXML private Button switchToLoginBtn;
    
    // Container
    @FXML private StackPane formContainer;
    
    // Root pane
    @FXML private BorderPane rootPane;
    
    @FXML
    private void initialize() {
        System.out.println("=== Controller Initialized ===");
        
        // Set gradient background programmatically
        setGradientBackground();
        
        // Initialize role combo box - FIXED: Only HR and EMPLOYEE roles for signup
        signupRoleCombo.getItems().addAll("HR", "EMPLOYEE");
        signupRoleCombo.setValue("EMPLOYEE");
        
        // Style components
        styleComponents();
        
        // Setup initial state - make signup form invisible
        signupForm.setVisible(false);
        signupForm.setOpacity(0);
        signupForm.setTranslateX(500);
        
        loginForm.setVisible(true);
        loginForm.setOpacity(1);
        loginForm.setTranslateX(0);
        
        // Add subtle entrance animation
        addEntranceAnimation();
        
        // Set focus
        Platform.runLater(() -> loginUsernameField.requestFocus());
        
        System.out.println("Initial setup complete. Login visible, Signup hidden.");
    }
    
    private void setGradientBackground() {
        // Apply gradient directly to root pane
        rootPane.setStyle(
            "-fx-background-color: " +
            "linear-gradient(from 0% 0% to 100% 100%, #667eea 0%, #764ba2 50%, #f093fb 100%);"
        );
    }
    
    private void addEntranceAnimation() {
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(600), loginForm);
        scaleIn.setFromX(0.8);
        scaleIn.setFromY(0.8);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(600), loginForm);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        ParallelTransition entrance = new ParallelTransition(scaleIn, fadeIn);
        entrance.setInterpolator(Interpolator.EASE_OUT);
        entrance.play();
    }
    
    private void styleComponents() {
        // Modern glassmorphism field style
        String fieldStyle = 
            "-fx-background-color: rgba(255, 255, 255, 0.95);" +
            "-fx-border-color: linear-gradient(to right, #667eea 0%, #764ba2 100%);" +
            "-fx-border-width: 0 0 2 0;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 12 16;" +
            "-fx-font-size: 13px;" +
            "-fx-font-family: 'Segoe UI', Arial, sans-serif;" +
            "-fx-pref-width: 300;" +
            "-fx-effect: dropshadow(gaussian, rgba(102, 126, 234, 0.15), 8, 0, 0, 2);";
        
        String fieldFocusStyle = 
            "-fx-background-color: rgba(255, 255, 255, 1.0);" +
            "-fx-border-color: #667eea;" +
            "-fx-border-width: 0 0 2 0;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 12 16;" +
            "-fx-font-size: 13px;" +
            "-fx-font-family: 'Segoe UI', Arial, sans-serif;" +
            "-fx-pref-width: 300;" +
            "-fx-effect: dropshadow(gaussian, rgba(102, 126, 234, 0.3), 12, 0, 0, 3);";
        
        // Style all text fields with focus effects
        styleFieldWithFocus(loginUsernameField, fieldStyle, fieldFocusStyle);
        styleFieldWithFocus(loginPasswordField, fieldStyle, fieldFocusStyle);
        styleFieldWithFocus(signupUsernameField, fieldStyle, fieldFocusStyle);
        styleFieldWithFocus(signupPasswordField, fieldStyle, fieldFocusStyle);
        styleFieldWithFocus(signupConfirmPasswordField, fieldStyle, fieldFocusStyle);
        styleFieldWithFocus(signupEmailField, fieldStyle, fieldFocusStyle);
        
        // Style combo box
        signupRoleCombo.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.95);" +
            "-fx-border-color: linear-gradient(to right, #667eea 0%, #764ba2 100%);" +
            "-fx-border-width: 0 0 2 0;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 10 16;" +
            "-fx-font-size: 13px;" +
            "-fx-font-family: 'Segoe UI', Arial, sans-serif;" +
            "-fx-pref-width: 300;" +
            "-fx-effect: dropshadow(gaussian, rgba(102, 126, 234, 0.15), 8, 0, 0, 2);"
        );
        
        // Modern gradient buttons
        String buttonStyle = 
            "-fx-background-color: linear-gradient(to right, #667eea 0%, #764ba2 100%);" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 14px;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 12 30;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(102, 126, 234, 0.15), 8, 0, 0, 2);";
        
        loginButton.setStyle(buttonStyle);
        signupButton.setStyle(buttonStyle);
        
        // Switch buttons
        switchToSignupBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #667eea;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 12px;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 5;" +
            "-fx-border-radius: 5;"
        );
        
        switchToLoginBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #667eea;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 12px;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 5;" +
            "-fx-border-radius: 5;"
        );
        
        // Error labels
        String errorStyle = 
            "-fx-text-fill: #e74c3c;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 5 0 0 0;" +
            "-fx-background-color: rgba(231, 76, 60, 0.1);" +
            "-fx-background-radius: 5;" +
            "-fx-padding: 8;" +
            "-fx-effect: dropshadow(gaussian, rgba(231, 76, 60, 0.2), 5, 0, 0, 1);";
        
        loginErrorLabel.setStyle(errorStyle);
        signupErrorLabel.setStyle(errorStyle);
        
        // Form containers with glassmorphism
        String formStyle = 
            "-fx-background-color: rgba(255, 255, 255, 0.9);" +
            "-fx-background-radius: 20;" +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 20, 0.15, 0, 10);" +
            "-fx-padding: 30;" +
            "-fx-alignment: center;" +
            "-fx-pref-width: 360;" +
            "-fx-border-radius: 20;" +
            "-fx-border-color: rgba(255, 255, 255, 0.3);" +
            "-fx-border-width: 1;";
        
        loginForm.setStyle(formStyle);
        signupForm.setStyle(formStyle);
    }
    
    private void styleFieldWithFocus(TextInputControl field, String normalStyle, String focusStyle) {
        field.setStyle(normalStyle);
        
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle(focusStyle);
            } else {
                field.setStyle(normalStyle);
            }
        });
    }
    
    @FXML
    private void switchToSignup() {
        TranslateTransition loginOut = new TranslateTransition(Duration.millis(300), loginForm);
        loginOut.setFromX(0);
        loginOut.setToX(-500);
        
        FadeTransition loginFade = new FadeTransition(Duration.millis(300), loginForm);
        loginFade.setFromValue(1);
        loginFade.setToValue(0);
        
        ParallelTransition loginExit = new ParallelTransition(loginOut, loginFade);
        loginExit.setOnFinished(e -> {
            loginForm.setVisible(false);
            signupForm.setVisible(true);
            
            TranslateTransition signupIn = new TranslateTransition(Duration.millis(300), signupForm);
            signupIn.setFromX(500);
            signupIn.setToX(0);
            
            FadeTransition signupFade = new FadeTransition(Duration.millis(300), signupForm);
            signupFade.setFromValue(0);
            signupFade.setToValue(1);
            
            ParallelTransition signupEnter = new ParallelTransition(signupIn, signupFade);
            signupEnter.play();
            
            // Clear signup fields
            signupUsernameField.clear();
            signupFirstNameField.clear();
            signupLastNameField.clear();
            signupEmailField.clear();
            signupPasswordField.clear();
            signupConfirmPasswordField.clear();
            signupRoleCombo.setValue("EMPLOYEE");
        });
        
        loginExit.play();
    }
    
    @FXML
    private void switchToLogin() {
        TranslateTransition signupOut = new TranslateTransition(Duration.millis(300), signupForm);
        signupOut.setFromX(0);
        signupOut.setToX(500);
        
        FadeTransition signupFade = new FadeTransition(Duration.millis(300), signupForm);
        signupFade.setFromValue(1);
        signupFade.setToValue(0);
        
        ParallelTransition signupExit = new ParallelTransition(signupOut, signupFade);
        signupExit.setOnFinished(e -> {
            signupForm.setVisible(false);
            loginForm.setVisible(true);
            
            TranslateTransition loginIn = new TranslateTransition(Duration.millis(300), loginForm);
            loginIn.setFromX(-500);
            loginIn.setToX(0);
            
            FadeTransition loginFade = new FadeTransition(Duration.millis(300), loginForm);
            loginFade.setFromValue(0);
            loginFade.setToValue(1);
            
            ParallelTransition loginEnter = new ParallelTransition(loginIn, loginFade);
            loginEnter.play();
            
            // Clear login fields
            loginUsernameField.clear();
            loginPasswordField.clear();
        });
        
        signupExit.play();
    }
    
    @FXML
    private void handleLogin() {
        String username = loginUsernameField.getText().trim();
        String password = loginPasswordField.getText().trim();
        
        if (username.isEmpty() || password.isEmpty()) {
            showLoginError("Enter username and password");
            shakeAnimation(loginForm);
            return;
        }
        
        loginErrorLabel.setVisible(false);
        loginButton.setDisable(true);
        loginButton.setText("Signing in...");
        
        new Thread(() -> {
            try {
                Thread.sleep(500); // Simulate processing
                
                boolean authenticated = authenticateUser(username, password);
                
                Platform.runLater(() -> {
                    loginButton.setDisable(false);
                    loginButton.setText("Sign In");
                    
                    if (authenticated) {
                        try {
                            navigateToDashboard();
                        } catch (Exception e) {
                            e.printStackTrace();
                            showLoginError("Navigation failed");
                        }
                    } else {
                        showLoginError("Invalid credentials");
                        shakeAnimation(loginForm);
                    }
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loginButton.setDisable(false);
                    loginButton.setText("Sign In");
                    showLoginError("Login failed: " + e.getMessage());
                    shakeAnimation(loginForm);
                });
            }
        }).start();
    }
    
   @FXML
private void handleSignup() {
    String username = signupUsernameField.getText().trim();
    String firstName = signupFirstNameField.getText().trim();
    String lastName = signupLastNameField.getText().trim();
    String password = signupPasswordField.getText().trim();
    String confirmPassword = signupConfirmPasswordField.getText().trim();
    String email = signupEmailField.getText().trim();
    String role = signupRoleCombo.getValue();
    
    // Debug: Print the selected role
    System.out.println("=== SIGNUP DEBUG ===");
    System.out.println("Username: " + username);
    System.out.println("Email: " + email);
    System.out.println("Selected Role: " + role);
    System.out.println("Role Combo Value: " + signupRoleCombo.getValue());
    System.out.println("====================");
    
    // Validation
    if (username.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || password.isEmpty() || 
        confirmPassword.isEmpty() || email.isEmpty()) {
        showSignupError("All fields required");
        shakeAnimation(signupForm);
        return;
    }
    
    if (!password.equals(confirmPassword)) {
        showSignupError("Passwords don't match");
        shakeAnimation(signupForm);
        return;
    }
    
    if (password.length() < 6) {
        showSignupError("Password must be 6+ characters");
        shakeAnimation(signupForm);
        return;
    }
    
    if (!email.contains("@") || !email.contains(".")) {
        showSignupError("Invalid email format");
        shakeAnimation(signupForm);
        return;
    }
    
    signupErrorLabel.setVisible(false);
    signupButton.setDisable(true);
    signupButton.setText("Creating...");
    
    new Thread(() -> {
        try {
            Thread.sleep(500); // Simulate processing
            
            boolean created = createUser(username, password, email, role, firstName, lastName);
            
            Platform.runLater(() -> {
                signupButton.setDisable(false);
                signupButton.setText("Create Account");
                
                if (created) {
                    // Show success alert and switch to login
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Registration Successful");
                    alert.setHeaderText("Your account is now pending approval.");
                    alert.setContentText("An administrator will review your registration. You will be able to log in once your account is approved.");
                    alert.showAndWait();
                    
                    // Switch back to the login form
                    switchToLogin();
                } else {
                    showSignupError("Username/email already exists");
                    shakeAnimation(signupForm);
                }
            });
            
        } catch (Exception e) {
            Platform.runLater(() -> {
                signupButton.setDisable(false);
                signupButton.setText("Create Account");
                showSignupError("Signup failed: " + e.getMessage());
                shakeAnimation(signupForm);
            });
        }
    }).start();
}
    
    private void shakeAnimation(VBox form) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), form);
        tt.setFromX(0);
        tt.setByX(10);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.play();
    }
    
    // In handleLogin method, add this check:

private boolean authenticateUser(String username, String password) {
    try {
        // First, check if status column exists
        boolean hasStatusColumn = checkIfColumnExists("users", "status");
        
        String query;
        if (hasStatusColumn) {
            query = "SELECT id, username, password_hash, role, email, status FROM users WHERE username = ?";
        } else {
            query = "SELECT id, username, password_hash, role, email FROM users WHERE username = ?";
        }
        
        Connection conn = DatabaseConnector.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, username);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            // Check status if column exists
            if (hasStatusColumn) {
                String status = rs.getString("status");
                
                // Check if user is pending approval
                if ("PENDING_APPROVAL".equals(status)) {
                    Platform.runLater(() -> {
                        showLoginError("Your account is pending approval. Please wait for admin approval.");
                    });
                    return false;
                }
                
                // Check if user is rejected
                if ("REJECTED".equals(status)) {
                    Platform.runLater(() -> {
                        showLoginError("Your account registration was rejected.");
                    });
                    return false;
                }
            }
            
            String storedHash = rs.getString("password_hash");
            if (DatabaseConnector.verifyPassword(password, storedHash)) {
                int userId = rs.getInt("id");
                String userRole = rs.getString("role");
                String userEmail = rs.getString("email");
                
                System.out.println("=== AUTHENTICATION DEBUG ===");
                System.out.println("User ID: " + userId);
                System.out.println("Username: " + username);
                System.out.println("Role from DB: '" + userRole + "'");
                System.out.println("Email: " + userEmail);
                System.out.println("=============================");
                
                // Set session with proper values
                SessionManager.setCurrentUserId(userId);
                SessionManager.setCurrentUsername(username);
                SessionManager.setCurrentUserRole(userRole);
                SessionManager.setCurrentUserEmail(userEmail);
                
                // Update last login
                updateLastLogin(userId);
                
                // Close resources
                rs.close();
                stmt.close();
                return true;
            }
        }
        
        // Close resources
        rs.close();
        stmt.close();
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}

// Helper method to check if a column exists
private boolean checkIfColumnExists(String tableName, String columnName) {
    try (Connection conn = DatabaseConnector.getConnection();
         Statement stmt = conn.createStatement()) {
        ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + tableName + ")");
        while (rs.next()) {
            if (columnName.equals(rs.getString("name"))) {
                return true;
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return false;
}

private void updateLastLogin(int userId) {
    try (Connection conn = DatabaseConnector.getConnection()) {
        String query = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, userId);
        stmt.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
    
    private boolean createUser(String username, String password, String email, String role, String firstName, String lastName) {
    try {
        return DatabaseConnector.createUser(username, password, email, role, firstName, lastName);
    } catch (SQLException e) {
        e.printStackTrace();
        
        // If database is locked, wait and retry
        if (e.getMessage().contains("locked")) {
            try {
                Thread.sleep(100); // Wait 100ms
                return DatabaseConnector.createUser(username, password, email, role, firstName, lastName);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }
}
    
   private void navigateToDashboard() throws Exception {
    Stage stage = (Stage) loginButton.getScene().getWindow();
    stage.close();
    
    try {
        String role = SessionManager.getCurrentUserRole();
        role = role != null ? role.trim().toUpperCase() : "";
        
        String fxmlPath;
        String title;
        
        if ("ADMIN".equals(role)) {
            fxmlPath = "/com/payroll/views/AdminDashboard.fxml";
            title = "Admin Dashboard - Payroll System";
        } else {
            // For employee/HR, use a different FXML file
            fxmlPath = "/com/payroll/views/EmployeeDashboard.fxml";  // Create this new file
            title = role.equals("HR") ? "HR Dashboard - Payroll System" : "Employee Dashboard - Payroll System";
        }
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        
        Stage dashboardStage = new Stage();
        dashboardStage.setTitle(title);
        dashboardStage.setScene(new Scene(root, 1366, 768));
        dashboardStage.show();
        
    } catch (Exception ex) {
        ex.printStackTrace();
        // Fallback to simple dashboard
        showSimpleDashboard();
    }
}

private void showSimpleDashboard() {
    try {
        // Create a simple dashboard as fallback
        Stage stage = new Stage();
        stage.setTitle("Employee Dashboard");
        
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f8fafc;");
        
        Label welcomeLabel = new Label("Welcome, " + SessionManager.getCurrentUsername());
        welcomeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        Label roleLabel = new Label("Role: " + SessionManager.getCurrentUserRole());
        roleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");
        
        Button logoutBtn = new Button("Logout");
        logoutBtn.setOnAction(e -> {
            SessionManager.clearSession();
            stage.close();
            try {
                new Main().start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        
        root.getChildren().addAll(welcomeLabel, roleLabel, logoutBtn);
        
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
    } catch (Exception e) {
        e.printStackTrace();
    }
}

// Add this helper method to get role from database if session doesn't have it
private String getUserRoleFromDatabase(int userId) {
    try (Connection conn = DatabaseConnector.getConnection()) {
        String query = "SELECT role FROM users WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, userId);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            String role = rs.getString("role");
            // Update session with correct role
            SessionManager.setCurrentUserRole(role);
            return role;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
}
    
    private void showLoginError(String message) {
        loginErrorLabel.setText("⚠️ " + message);
        loginErrorLabel.setVisible(true);
        
        // Fade in error with pulse
        FadeTransition ft = new FadeTransition(Duration.millis(200), loginErrorLabel);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }
    
    private void showSignupError(String message) {
        signupErrorLabel.setText("⚠️ " + message);
        signupErrorLabel.setVisible(true);
        
        // Fade in error with pulse
        FadeTransition ft = new FadeTransition(Duration.millis(200), signupErrorLabel);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }
    
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Style the alert dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(
            "-fx-background-color: white;" +
            "-fx-font-family: 'Segoe UI', Arial, sans-serif;" +
            "-fx-font-size: 14px;"
        );
        
        alert.showAndWait();
    }
}