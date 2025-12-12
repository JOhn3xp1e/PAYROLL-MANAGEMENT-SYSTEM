package com.payroll.controllers;

import com.payroll.utils.DatabaseConnector;
import com.payroll.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {
    
    @FXML private Label welcomeLabel;
    @FXML private Label userRoleLabel;
    @FXML private Label dateLabel;
    @FXML private FlowPane statsContainer;
    @FXML private Button btnDashboard;
    @FXML private Button btnEmployees;
    @FXML private Button btnPayroll;
    @FXML private Button btnAttendance;
    @FXML private Button btnLeaves;
    @FXML private Button btnReports;
    @FXML private Button btnSettings;
    @FXML private Button btnLogout;
    @FXML private StackPane contentPane;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set current date
        dateLabel.setText(LocalDate.now().toString());
        
        // Set user info
        welcomeLabel.setText("Welcome, " + SessionManager.getCurrentUsername());
        userRoleLabel.setText(SessionManager.getCurrentUserRole());
        
        // Style all components
        styleDashboard();
        
        // Load dashboard by default
        loadDashboardContent();
        loadStatistics();
    }
    
    private void styleDashboard() {
        // Style header labels
        welcomeLabel.setStyle(
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #2c3e50;"
        );
        
        userRoleLabel.setStyle(
            "-fx-font-size: 12px;" +
            "-fx-text-fill: #7f8c8d;" +
            "-fx-padding: 2 10;" +
            "-fx-background-color: #ecf0f1;" +
            "-fx-background-radius: 10;" +
            "-fx-border-radius: 10;"
        );
        
        dateLabel.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-text-fill: #7f8c8d;"
        );
        
        // Style navigation buttons
        Button[] menuButtons = {btnDashboard, btnEmployees, btnPayroll, 
                               btnAttendance, btnLeaves, btnReports, btnSettings};
        
        for (Button btn : menuButtons) {
            btn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #2c3e50;" +
                "-fx-font-size: 14px;" +
                "-fx-padding: 12 20;" +
                "-fx-alignment: center-left;" +
                "-fx-cursor: hand;" +
                "-fx-border-width: 0;" +
                "-fx-border-color: transparent;"
            );
            
            btn.setOnMouseEntered(e -> {
                if (!btn.getStyle().contains("-fx-background-color: #3498db;")) {
                    btn.setStyle(
                        "-fx-background-color: #ecf0f1;" +
                        "-fx-text-fill: #2c3e50;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 12 20;" +
                        "-fx-alignment: center-left;" +
                        "-fx-cursor: hand;" +
                        "-fx-border-width: 0;" +
                        "-fx-border-color: transparent;"
                    );
                }
            });
            
            btn.setOnMouseExited(e -> {
                if (!btn.getStyle().contains("-fx-background-color: #3498db;")) {
                    btn.setStyle(
                        "-fx-background-color: transparent;" +
                        "-fx-text-fill: #2c3e50;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 12 20;" +
                        "-fx-alignment: center-left;" +
                        "-fx-cursor: hand;" +
                        "-fx-border-width: 0;" +
                        "-fx-border-color: transparent;"
                    );
                }
            });
        }
        
        // Set dashboard as active
        btnDashboard.setStyle(
            "-fx-background-color: #3498db;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 12 20;" +
            "-fx-alignment: center-left;" +
            "-fx-cursor: hand;" +
            "-fx-border-width: 0;" +
            "-fx-border-color: transparent;"
        );
        
        // Style logout button
        btnLogout.setStyle(
            "-fx-background-color: #e74c3c;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 5;" +
            "-fx-padding: 10 20;" +
            "-fx-cursor: hand;"
        );
        
        btnLogout.setOnMouseEntered(e -> 
            btnLogout.setStyle(
                "-fx-background-color: #c0392b;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 5;" +
                "-fx-padding: 10 20;" +
                "-fx-cursor: hand;"
            )
        );
        
        btnLogout.setOnMouseExited(e -> 
            btnLogout.setStyle(
                "-fx-background-color: #e74c3c;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 5;" +
                "-fx-padding: 10 20;" +
                "-fx-cursor: hand;"
            )
        );
        
        // Style content area
        contentPane.setStyle("-fx-background-color: #f5f5f5;");
        statsContainer.setStyle("-fx-padding: 20; -fx-hgap: 15; -fx-vgap: 15;");
    }
    
    private void setActiveButton(Button activeButton) {
        Button[] menuButtons = {btnDashboard, btnEmployees, btnPayroll, 
                               btnAttendance, btnLeaves, btnReports, btnSettings};
        
        for (Button btn : menuButtons) {
            btn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #2c3e50;" +
                "-fx-font-size: 14px;" +
                "-fx-padding: 12 20;" +
                "-fx-alignment: center-left;" +
                "-fx-cursor: hand;" +
                "-fx-border-width: 0;" +
                "-fx-border-color: transparent;"
            );
        }
        
        activeButton.setStyle(
            "-fx-background-color: #3498db;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 12 20;" +
            "-fx-alignment: center-left;" +
            "-fx-cursor: hand;" +
            "-fx-border-width: 0;" +
            "-fx-border-color: transparent;"
        );
    }
    
    private void loadDashboardContent() {
        contentPane.getChildren().clear();
        
        GridPane dashboardGrid = new GridPane();
        dashboardGrid.setPadding(new Insets(20));
        dashboardGrid.setHgap(20);
        dashboardGrid.setVgap(20);
        
        // Card 1: Quick Actions
        VBox quickActionsCard = createCard("Quick Actions", "#3498db");
        VBox quickActionsContent = new VBox(10);
        quickActionsContent.setPadding(new Insets(15));
        
        Button btnAddEmployee = createQuickButton("Add Employee", "#3498db");
        Button btnProcessPayroll = createQuickButton("Process Payroll", "#2ecc71");
        Button btnMarkAttendance = createQuickButton("Mark Attendance", "#9b59b6");
        Button btnGenerateReports = createQuickButton("Generate Reports", "#f39c12");
        
        btnAddEmployee.setOnAction(e -> loadEmployeesContent());
        btnProcessPayroll.setOnAction(e -> loadPayrollContent());
        btnMarkAttendance.setOnAction(e -> loadAttendanceContent());
        btnGenerateReports.setOnAction(e -> loadReportsContent());
        
        quickActionsContent.getChildren().addAll(btnAddEmployee, btnProcessPayroll, btnMarkAttendance, btnGenerateReports);
        quickActionsCard.getChildren().add(quickActionsContent);
        dashboardGrid.add(quickActionsCard, 0, 0, 1, 2);
        
        // Card 2: Recent Activities
        VBox activitiesCard = createCard("Recent Activities", "#2ecc71");
        ListView<String> activitiesList = new ListView<>();
        activitiesList.setStyle("-fx-background-color: white; -fx-border-color: rgba(255,255,255,0.3);");
        activitiesList.getItems().addAll(
            "John Doe marked present - Today",
            "Payroll processed for December",
            "New employee Sarah added",
            "Leave request from Mike approved"
        );
        activitiesList.setPrefHeight(200);
        activitiesCard.getChildren().add(activitiesList);
        dashboardGrid.add(activitiesCard, 1, 0);
        
        // Card 3: Statistics Summary
        VBox statsCard = createCard("Statistics Summary", "#e74c3c");
        VBox statsContent = new VBox(15);
        statsContent.setPadding(new Insets(15));
        
        Label totalEmp = new Label("Total Employees: " + getEmployeeCount());
        totalEmp.setStyle("-fx-text-fill: white; -fx-font-size: 14;");
        
        Label activeEmp = new Label("Active Employees: " + getActiveEmployeeCount());
        activeEmp.setStyle("-fx-text-fill: white; -fx-font-size: 14;");
        
        Label payroll = new Label("Monthly Payroll: " + getMonthlyPayroll());
        payroll.setStyle("-fx-text-fill: white; -fx-font-size: 14;");
        
        statsContent.getChildren().addAll(totalEmp, activeEmp, payroll);
        statsCard.getChildren().add(statsContent);
        dashboardGrid.add(statsCard, 1, 1);
        
        // Card 4: Payroll Summary
        VBox payrollSummaryCard = createCard("Payroll Summary", "#f39c12");
        Label payrollLabel = new Label("December 2023");
        payrollLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        payrollLabel.setTextFill(Color.WHITE);
        
        Label amountLabel = new Label("$125,430.00");
        amountLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        amountLabel.setTextFill(Color.WHITE);
        
        VBox payrollContent = new VBox(10, payrollLabel, amountLabel);
        payrollContent.setPadding(new Insets(20));
        payrollSummaryCard.getChildren().add(payrollContent);
        dashboardGrid.add(payrollSummaryCard, 2, 0, 1, 2);
        
        contentPane.getChildren().add(dashboardGrid);
    }
    
    private VBox createCard(String title, String color) {
        VBox card = new VBox();
        card.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-background-radius: 10;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);"
        );
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setPadding(new Insets(15, 15, 10, 15));
        
        card.getChildren().add(titleLabel);
        return card;
    }
    
    private Button createQuickButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle(
            "-fx-background-color: white;" +
            "-fx-text-fill: " + color + ";" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 5;" +
            "-fx-padding: 10;" +
            "-fx-cursor: hand;" +
            "-fx-min-width: 150;"
        );
        
        button.setOnMouseEntered(e -> button.setStyle(
            "-fx-background-color: #f8f9fa;" +
            "-fx-text-fill: " + color + ";" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 5;" +
            "-fx-padding: 10;" +
            "-fx-cursor: hand;" +
            "-fx-min-width: 150;"
        ));
        
        button.setOnMouseExited(e -> button.setStyle(
            "-fx-background-color: white;" +
            "-fx-text-fill: " + color + ";" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 5;" +
            "-fx-padding: 10;" +
            "-fx-cursor: hand;" +
            "-fx-min-width: 150;"
        ));
        
        return button;
    }
    
    private void loadStatistics() {
        statsContainer.getChildren().clear();
        
        // Create statistic cards
        VBox[] statCards = {
            createStatCard("Total Employees", getEmployeeCount(), "#3498db"),
            createStatCard("Active Employees", getActiveEmployeeCount(), "#2ecc71"),
            createStatCard("Pending Leaves", getPendingLeavesCount(), "#e74c3c"),
            createStatCard("Monthly Payroll", getMonthlyPayroll(), "#f39c12"),
            createStatCard("Today Attendance", getTodayAttendance(), "#9b59b6"),
            createStatCard("Departments", getDepartmentCount(), "#1abc9c")
        };
        
        for (VBox card : statCards) {
            statsContainer.getChildren().add(card);
        }
    }
    
    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox();
        card.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 20;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 0);"
        );
        card.setPrefSize(180, 120);
        card.setAlignment(Pos.CENTER);
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        valueLabel.setTextFill(Color.WHITE);
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", 12));
        titleLabel.setTextFill(Color.WHITE);
        
        VBox content = new VBox(5, valueLabel, titleLabel);
        content.setAlignment(Pos.CENTER);
        
        card.getChildren().add(content);
        
        return card;
    }
    
    // Database methods for statistics
    private String getEmployeeCount() {
        try {
            ResultSet rs = DatabaseConnector.executeQuery("SELECT COUNT(*) FROM employees");
            return rs.next() ? rs.getString(1) : "0";
        } catch (SQLException e) {
            return "0";
        }
    }
    
    private String getActiveEmployeeCount() {
        try {
            ResultSet rs = DatabaseConnector.executeQuery("SELECT COUNT(*) FROM employees WHERE employment_status = 'ACTIVE'");
            return rs.next() ? rs.getString(1) : "0";
        } catch (SQLException e) {
            return "0";
        }
    }
    
    private String getPendingLeavesCount() {
        try {
            ResultSet rs = DatabaseConnector.executeQuery("SELECT COUNT(*) FROM leaves WHERE status = 'PENDING'");
            return rs.next() ? rs.getString(1) : "0";
        } catch (SQLException e) {
            return "0";
        }
    }
    
    private String getMonthlyPayroll() {
        try {
            ResultSet rs = DatabaseConnector.executeQuery(
                "SELECT COALESCE(SUM(net_salary), 0) FROM payroll WHERE strftime('%Y-%m', payment_date) = strftime('%Y-%m', 'now')");
            double amount = rs.next() ? rs.getDouble(1) : 0.0;
            return String.format("$%.2f", amount);
        } catch (SQLException e) {
            return "$0.00";
        }
    }
    
    private String getTodayAttendance() {
        try {
            ResultSet rs = DatabaseConnector.executeQuery(
                "SELECT COUNT(*) FROM attendance WHERE date = DATE('now') AND status = 'PRESENT'");
            return rs.next() ? rs.getString(1) : "0";
        } catch (SQLException e) {
            return "0";
        }
    }
    
    private String getDepartmentCount() {
        try {
            ResultSet rs = DatabaseConnector.executeQuery("SELECT COUNT(DISTINCT department) FROM employees");
            return rs.next() ? rs.getString(1) : "0";
        } catch (SQLException e) {
            return "0";
        }
    }
    
    // Navigation methods
    @FXML
    private void handleDashboard() {
        setActiveButton(btnDashboard);
        loadDashboardContent();
        loadStatistics();
    }
    
    @FXML
    private void handleEmployees() {
        setActiveButton(btnEmployees);
        loadEmployeesContent();
    }
    
    @FXML
    private void handlePayroll() {
        setActiveButton(btnPayroll);
        loadPayrollContent();
    }
    
    @FXML
    private void handleAttendance() {
        setActiveButton(btnAttendance);
        loadAttendanceContent();
    }
    
    @FXML
    private void handleLeaves() {
        setActiveButton(btnLeaves);
        loadLeavesContent();
    }
    
    @FXML
    private void handleReports() {
        setActiveButton(btnReports);
        loadReportsContent();
    }
    
    @FXML
    private void handleSettings() {
        setActiveButton(btnSettings);
        loadSettingsContent();
    }
    
    @FXML
    private void handleLogout() {
        SessionManager.clearSession();
        
        try {
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            stage.close();
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/payroll/views/Login.fxml"));
            Parent root = loader.load();
            
            Stage loginStage = new Stage();
            loginStage.setTitle("Payroll Management System - Login");
            loginStage.setScene(new Scene(root, 800, 500));
            loginStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to logout");
        }
    }
    
    private void loadEmployeesContent() {
        contentPane.getChildren().clear();
        
        VBox container = new VBox(20);
        container.setPadding(new Insets(30));
        container.setAlignment(Pos.CENTER);
        
        Label title = new Label("👥 Employee Management");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Simple form for adding employees
        VBox form = new VBox(10);
        form.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 10;");
        
        TextField nameField = new TextField();
        nameField.setPromptText("Employee Name");
        nameField.setStyle("-fx-padding: 10; -fx-font-size: 14;");
        
        TextField emailField = new TextField();
        emailField.setPromptText("Email Address");
        emailField.setStyle("-fx-padding: 10; -fx-font-size: 14;");
        
        Button addButton = new Button("Add Employee");
        addButton.setStyle(
            "-fx-background-color: #3498db;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10 20;" +
            "-fx-cursor: hand;"
        );
        
        form.getChildren().addAll(nameField, emailField, addButton);
        container.getChildren().addAll(title, form);
        contentPane.getChildren().add(container);
    }
    
    private void loadPayrollContent() {
        contentPane.getChildren().clear();
        
        VBox container = new VBox(20);
        container.setPadding(new Insets(30));
        container.setAlignment(Pos.CENTER);
        
        Label title = new Label("💰 Payroll Management");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Simple payroll form
        VBox form = new VBox(10);
        form.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 10;");
        
        ComboBox<String> employeeCombo = new ComboBox<>();
        employeeCombo.setPromptText("Select Employee");
        employeeCombo.setStyle("-fx-padding: 10; -fx-font-size: 14;");
        
        TextField amountField = new TextField();
        amountField.setPromptText("Amount");
        amountField.setStyle("-fx-padding: 10; -fx-font-size: 14;");
        
        Button processButton = new Button("Process Payroll");
        processButton.setStyle(
            "-fx-background-color: #2ecc71;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10 20;" +
            "-fx-cursor: hand;"
        );
        
        form.getChildren().addAll(employeeCombo, amountField, processButton);
        container.getChildren().addAll(title, form);
        contentPane.getChildren().add(container);
    }
    
    private void loadAttendanceContent() {
        contentPane.getChildren().clear();
        Label label = new Label("⏰ Attendance Management");
        label.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        contentPane.getChildren().add(label);
    }
    
    private void loadLeavesContent() {
        contentPane.getChildren().clear();
        Label label = new Label("📅 Leave Management");
        label.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        contentPane.getChildren().add(label);
    }
    
    private void loadReportsContent() {
        contentPane.getChildren().clear();
        Label label = new Label("📊 Reports");
        label.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        contentPane.getChildren().add(label);
    }
    
    private void loadSettingsContent() {
        contentPane.getChildren().clear();
        Label label = new Label("⚙️ Settings");
        label.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        contentPane.getChildren().add(label);
    }

    
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}