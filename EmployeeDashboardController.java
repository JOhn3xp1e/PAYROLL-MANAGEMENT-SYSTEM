package com.payroll.controllers;

import com.payroll.utils.DatabaseConnector;
import com.payroll.utils.SessionManager;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class EmployeeDashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label employeeNameLabel;
    @FXML private Label employeeCodeLabel;
    @FXML private Label departmentLabel;
    @FXML private Label currentTimeLabel;
    @FXML private StackPane contentPane;
    @FXML private Button btnDashboard;
    @FXML private Button btnProfile;
    @FXML private Button btnSalary;
    @FXML private Button btnAttendance;
    @FXML private Button btnLeaves;
    @FXML private Button btnLogout;
    
    @FXML private Button btnClockIn;
    @FXML private Button btnClockOut;
    @FXML private Label clockStatusLabel;
    
    // Profile Components
    @FXML private TextField profileFirstName;
    @FXML private TextField profileLastName;
    @FXML private TextField profileEmail;
    @FXML private TextField profilePhone;
    @FXML private TextArea profileAddress;
    @FXML private DatePicker profileDOB;
    @FXML private ComboBox<String> profileGender;
    @FXML private TextField profileDesignation;
    @FXML private TextField profileDepartment;
    @FXML private TextField profileBankName;
    @FXML private TextField profileAccountNumber;
    @FXML private Button btnUpdateProfile;
    
    // Attendance Components
    @FXML private TableView<AttendanceRecord> attendanceTable;
    @FXML private TableColumn<AttendanceRecord, String> colDate;
    @FXML private TableColumn<AttendanceRecord, String> colClockIn;
    @FXML private TableColumn<AttendanceRecord, String> colClockOut;
    @FXML private TableColumn<AttendanceRecord, String> colAttendanceStatus;
    @FXML private DatePicker attendanceDateFilter;
    @FXML private Button btnRefreshAttendance;
    
    // Leaves Components
    @FXML private TableView<LeaveRecord> leavesTable;
    @FXML private TableColumn<LeaveRecord, String> colLeaveType;
    @FXML private TableColumn<LeaveRecord, String> colStartDate;
    @FXML private TableColumn<LeaveRecord, String> colEndDate;
    @FXML private TableColumn<LeaveRecord, Integer> colTotalDays;
    @FXML private TableColumn<LeaveRecord, String> colLeaveStatus;
    @FXML private ComboBox<String> leaveTypeCombo;
    @FXML private DatePicker leaveStartDate;
    @FXML private DatePicker leaveEndDate;
    @FXML private TextArea leaveReason;
    @FXML private Button btnApplyLeave;
    @FXML private Button btnCancelLeave;
    
    // Salary Components
    @FXML private TableView<SalaryRecord> salaryTable;
    @FXML private TableColumn<SalaryRecord, String> colPayPeriod;
    @FXML private TableColumn<SalaryRecord, Double> colBasic;
    @FXML private TableColumn<SalaryRecord, Double> colOvertime;
    @FXML private TableColumn<SalaryRecord, Double> colBonus;
    @FXML private TableColumn<SalaryRecord, Double> colAllowance;
    @FXML private TableColumn<SalaryRecord, Double> colDeduction;
    @FXML private TableColumn<SalaryRecord, Double> colTax;
    @FXML private TableColumn<SalaryRecord, Double> colNetSalary;
    @FXML private Label lblTotalSalary;
    @FXML private Label lblAvgSalary;
    @FXML private Button btnDownloadPayslip;
    
    private Integer employeeId;
    private Integer userId;
    private boolean isClockedIn = false;
    private LocalDateTime clockInTime;
    private javafx.animation.Timeline clockTimeline;

    // In EmployeeDashboardController.java, in the initialize method:
@Override
public void initialize(URL url, ResourceBundle rb) {
    System.out.println("=== EMPLOYEE DASHBOARD INITIALIZATION ===");
    System.out.println("Session User ID: " + SessionManager.getCurrentUserId());
    System.out.println("Session Username: " + SessionManager.getCurrentUsername());
    System.out.println("Session Role: '" + SessionManager.getCurrentUserRole() + "'");
    System.out.println("Session Email: " + SessionManager.getCurrentUserEmail());
    
    userId = SessionManager.getCurrentUserId();
    loadEmployeeData();
    initializeClock();
    styleEmployeeDashboard();
    loadDashboardContent();
    initializeTables();
    setupEventHandlers();
}
    
    private void loadEmployeeData() {
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT e.* FROM employees e JOIN users u ON e.user_id = u.id WHERE u.id = ?")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                employeeId = rs.getInt("id");
                employeeNameLabel.setText(rs.getString("first_name") + " " + rs.getString("last_name"));
                employeeCodeLabel.setText("ID: " + rs.getString("employee_code"));
                departmentLabel.setText(rs.getString("department"));
                
                // Set profile data
                profileFirstName.setText(rs.getString("first_name"));
                profileLastName.setText(rs.getString("last_name"));
                profileEmail.setText(rs.getString("email"));
                profilePhone.setText(rs.getString("phone"));
                profileAddress.setText(rs.getString("address"));
                
                if (rs.getDate("date_of_birth") != null) {
                    profileDOB.setValue(rs.getDate("date_of_birth").toLocalDate());
                }
                
                // Set gender if exists
                if (rs.getString("gender") != null) {
                    profileGender.setValue(rs.getString("gender"));
                }
                
                profileDesignation.setText(rs.getString("designation"));
                profileDepartment.setText(rs.getString("department"));
                profileBankName.setText(rs.getString("bank_name"));
                profileAccountNumber.setText(rs.getString("account_number"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        welcomeLabel.setText("Welcome, " + SessionManager.getCurrentUsername() + "!");
        
        // Initialize gender combo
        profileGender.getItems().addAll("Male", "Female", "Other");
        if (profileGender.getValue() == null) {
            profileGender.setValue("Male");
        }
        
        // Initialize leave type combo
        leaveTypeCombo.getItems().addAll("Sick Leave", "Annual Leave", "Maternity Leave", 
                                         "Paternity Leave", "Study Leave", "Unpaid Leave");
        leaveTypeCombo.setValue("Annual Leave");
        
        // Set dates for leave application
        leaveStartDate.setValue(LocalDate.now());
        leaveEndDate.setValue(LocalDate.now().plusDays(1));
        
        // Check current clock status
        checkClockStatus();
    }
    
    private void initializeClock() {
        clockTimeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(
                Duration.seconds(1),
                e -> updateCurrentTime()
            )
        );
        clockTimeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        clockTimeline.play();
    }
    
    private void updateCurrentTime() {
        currentTimeLabel.setText(LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("EEE, MMM dd, yyyy • HH:mm:ss")
        ));
    }
    
    private void checkClockStatus() {
        LocalDate today = LocalDate.now();
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM attendance WHERE employee_id = ? AND date = ?")) {
            stmt.setInt(1, employeeId);
            stmt.setDate(2, Date.valueOf(today));
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                if (rs.getTime("clock_in") != null) {
                    isClockedIn = true;
                    clockInTime = rs.getTimestamp("clock_in").toLocalDateTime();
                    btnClockIn.setDisable(true);
                    btnClockOut.setDisable(false);
                    clockStatusLabel.setText("Clocked in at: " + rs.getTime("clock_in").toString());
                }
                if (rs.getTime("clock_out") != null) {
                    btnClockOut.setDisable(true);
                    clockStatusLabel.setText("Clocked out at: " + rs.getTime("clock_out").toString());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void styleEmployeeDashboard() {
        String buttonStyle = 
            "-fx-background-color: linear-gradient(to right, #667eea, #764ba2);" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 14px;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 12 30;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(102, 126, 234, 0.3), 8, 0, 0, 2);";
        
        btnClockIn.setStyle(buttonStyle);
        btnClockOut.setStyle(buttonStyle);
        btnUpdateProfile.setStyle(buttonStyle);
        btnApplyLeave.setStyle(buttonStyle);
        btnRefreshAttendance.setStyle(buttonStyle);
        btnDownloadPayslip.setStyle(buttonStyle);
        btnCancelLeave.setStyle(buttonStyle);
        
        // Navigation buttons
        Button[] navButtons = {btnDashboard, btnProfile, btnSalary, btnAttendance, btnLeaves};
        for (Button btn : navButtons) {
            btn.setStyle(
                "-fx-background-color: white;" +
                "-fx-text-fill: #4a5568;" +
                "-fx-font-size: 14px;" +
                "-fx-padding: 12 20;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;" +
                "-fx-border-color: #e2e8f0;" +
                "-fx-border-width: 1;"
            );
            
            btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: #f7fafc;" +
                "-fx-text-fill: #2d3748;" +
                "-fx-font-size: 14px;" +
                "-fx-padding: 12 20;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;" +
                "-fx-border-color: #cbd5e0;" +
                "-fx-border-width: 1;" +
                "-fx-font-weight: bold;"
            ));
            
            btn.setOnMouseExited(e -> {
                if (!btn.getStyle().contains("-fx-background-color: #4299e1")) {
                    btn.setStyle(
                        "-fx-background-color: white;" +
                        "-fx-text-fill: #4a5568;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 12 20;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-border-width: 1;"
                    );
                }
            });
        }
        
        btnLogout.setStyle(
            "-fx-background-color: #fc8181;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 12 30;" +
            "-fx-cursor: hand;"
        );
    }
    
    private void setActiveButton(Button activeButton) {
        Button[] buttons = {btnDashboard, btnProfile, btnSalary, btnAttendance, btnLeaves};
        
        for (Button btn : buttons) {
            btn.setStyle(
                "-fx-background-color: white;" +
                "-fx-text-fill: #4a5568;" +
                "-fx-font-size: 14px;" +
                "-fx-padding: 12 20;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;" +
                "-fx-border-color: #e2e8f0;" +
                "-fx-border-width: 1;"
            );
        }
        
        activeButton.setStyle(
            "-fx-background-color: #4299e1;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 12 20;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;" +
            "-fx-font-weight: bold;" +
            "-fx-effect: dropshadow(gaussian, rgba(66, 153, 225, 0.3), 6, 0, 0, 2);"
        );
    }
    
    private void initializeTables() {
        // Attendance table
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colClockIn.setCellValueFactory(new PropertyValueFactory<>("clockIn"));
        colClockOut.setCellValueFactory(new PropertyValueFactory<>("clockOut"));
        colAttendanceStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Leaves table
        colLeaveType.setCellValueFactory(new PropertyValueFactory<>("leaveType"));
        colStartDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colEndDate.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        colTotalDays.setCellValueFactory(new PropertyValueFactory<>("totalDays"));
        colLeaveStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Salary table
        colPayPeriod.setCellValueFactory(new PropertyValueFactory<>("payPeriod"));
        colBasic.setCellValueFactory(new PropertyValueFactory<>("basic"));
        colOvertime.setCellValueFactory(new PropertyValueFactory<>("overtime"));
        colBonus.setCellValueFactory(new PropertyValueFactory<>("bonus"));
        colAllowance.setCellValueFactory(new PropertyValueFactory<>("allowance"));
        colDeduction.setCellValueFactory(new PropertyValueFactory<>("deduction"));
        colTax.setCellValueFactory(new PropertyValueFactory<>("tax"));
        colNetSalary.setCellValueFactory(new PropertyValueFactory<>("netSalary"));
    }
    
    private void setupEventHandlers() {
        // Clock in/out
        btnClockIn.setOnAction(e -> handleClockIn());
        btnClockOut.setOnAction(e -> handleClockOut());
        
        // Profile update
        btnUpdateProfile.setOnAction(e -> updateProfile());
        
        // Leave management
        btnApplyLeave.setOnAction(e -> applyForLeave());
        btnCancelLeave.setOnAction(e -> cancelSelectedLeave());
        btnRefreshAttendance.setOnAction(e -> loadAttendanceData());
        
        // Payslip download
        btnDownloadPayslip.setOnAction(e -> downloadPayslip());
    }
    
    @FXML
    private void loadDashboardContent() {
        setActiveButton(btnDashboard);
        contentPane.getChildren().clear();
        
        VBox dashboard = new VBox(20);
        dashboard.setStyle("-fx-padding: 30; -fx-background-color: #f8fafc;");
        
        // Quick Stats
        HBox statsRow = new HBox(20);
        statsRow.setStyle("-fx-padding: 20 0;");
        
        VBox todayAttendance = createStatCard("⏰ Today's Status", getTodayAttendanceStatus(), "#4299e1");
        VBox leaveBalance = createStatCard("📅 Leave Balance", getLeaveBalance(), "#38a169");
        VBox nextPayday = createStatCard("💰 Next Payday", getNextPayday(), "#d69e2e");
        VBox recentActivity = createStatCard("📊 Recent Activity", getRecentActivity(), "#ed8936");
        
        statsRow.getChildren().addAll(todayAttendance, leaveBalance, nextPayday, recentActivity);
        
        // Clock in/out section
        VBox clockSection = new VBox(15);
        clockSection.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 4);");
        
        Label clockTitle = new Label("Time Tracking");
        clockTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        
        HBox clockButtons = new HBox(20);
        clockButtons.setAlignment(Pos.CENTER);
        clockButtons.getChildren().addAll(btnClockIn, btnClockOut);
        
        clockStatusLabel.setStyle("-fx-text-fill: #718096; -fx-font-size: 14px;");
        
        clockSection.getChildren().addAll(clockTitle, clockButtons, clockStatusLabel);
        
        // Upcoming Events
        VBox eventsSection = new VBox(15);
        eventsSection.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20; " +
                             "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 4);");
        
        Label eventsTitle = new Label("Upcoming Events");
        eventsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        
        ListView<String> eventsList = new ListView<>();
        eventsList.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        eventsList.setPrefHeight(150);
        
        // Load upcoming events
        loadUpcomingEvents(eventsList);
        
        eventsSection.getChildren().addAll(eventsTitle, eventsList);
        
        HBox bottomRow = new HBox(20);
        bottomRow.getChildren().addAll(clockSection, eventsSection);
        
        dashboard.getChildren().addAll(statsRow, bottomRow);
        contentPane.getChildren().add(dashboard);
        
        FadeTransition ft = new FadeTransition(Duration.millis(300), dashboard);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }
    
    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20; " +
                     "-fx-min-width: 200; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 4);");
        
        Label iconLabel = new Label(title.substring(0, 2));
        iconLabel.setStyle("-fx-font-size: 24px;");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        
        Label descLabel = new Label(title.substring(2).trim());
        descLabel.setStyle("-fx-text-fill: #718096; -fx-font-size: 11px;");
        
        card.getChildren().addAll(iconLabel, valueLabel, descLabel);
        return card;
    }
    
    private String getTodayAttendanceStatus() {
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT status FROM attendance WHERE employee_id = ? AND date = ?")) {
            stmt.setInt(1, employeeId);
            stmt.setDate(2, Date.valueOf(LocalDate.now()));
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("status");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Not Recorded";
    }
    
    private String getLeaveBalance() {
        // Simplified - in real app, calculate from leave records
        return "15 days";
    }
    
    private String getNextPayday() {
        LocalDate nextPayday = LocalDate.now().withDayOfMonth(25);
        if (nextPayday.isBefore(LocalDate.now())) {
            nextPayday = nextPayday.plusMonths(1);
        }
        return nextPayday.format(DateTimeFormatter.ofPattern("MMM dd"));
    }
    
    private String getRecentActivity() {
        return "Last login: Today";
    }
    
    private void loadUpcomingEvents(ListView<String> listView) {
        ObservableList<String> events = FXCollections.observableArrayList();
        
        // Add upcoming leaves
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM leaves WHERE employee_id = ? AND start_date >= ? ORDER BY start_date LIMIT 5")) {
            stmt.setInt(1, employeeId);
            stmt.setDate(2, Date.valueOf(LocalDate.now()));
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                events.add("📅 " + rs.getString("leave_type") + " - " + 
                          rs.getDate("start_date").toLocalDate().format(DateTimeFormatter.ofPattern("MMM dd")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        listView.setItems(events);
    }
    
    @FXML
    private void loadProfile() {
        setActiveButton(btnProfile);
        contentPane.getChildren().clear();
        
        VBox profileContent = new VBox(20);
        profileContent.setStyle("-fx-padding: 30; -fx-background-color: #f8fafc;");
        
        Label title = new Label("👤 My Profile");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        
        // Profile form
        VBox form = new VBox(15);
        form.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 30; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 4);");
        
        GridPane formGrid = new GridPane();
        formGrid.setHgap(20);
        formGrid.setVgap(15);
        formGrid.setPadding(new Insets(10));
        
        // Row 1
        Label lblFirstName = new Label("First Name:");
        lblFirstName.setStyle("-fx-font-weight: bold; -fx-text-fill: #4a5568;");
        formGrid.add(lblFirstName, 0, 0);
        formGrid.add(profileFirstName, 1, 0);
        
        Label lblLastName = new Label("Last Name:");
        lblLastName.setStyle("-fx-font-weight: bold; -fx-text-fill: #4a5568;");
        formGrid.add(lblLastName, 2, 0);
        formGrid.add(profileLastName, 3, 0);
        
        // Row 2
        Label lblEmail = new Label("Email:");
        lblEmail.setStyle("-fx-font-weight: bold; -fx-text-fill: #4a5568;");
        formGrid.add(lblEmail, 0, 1);
        formGrid.add(profileEmail, 1, 1);
        
        Label lblPhone = new Label("Phone:");
        lblPhone.setStyle("-fx-font-weight: bold; -fx-text-fill: #4a5568;");
        formGrid.add(lblPhone, 2, 1);
        formGrid.add(profilePhone, 3, 1);
        
        // Row 3
        Label lblDOB = new Label("Date of Birth:");
        lblDOB.setStyle("-fx-font-weight: bold; -fx-text-fill: #4a5568;");
        formGrid.add(lblDOB, 0, 2);
        formGrid.add(profileDOB, 1, 2);
        
        Label lblGender = new Label("Gender:");
        lblGender.setStyle("-fx-font-weight: bold; -fx-text-fill: #4a5568;");
        formGrid.add(lblGender, 2, 2);
        formGrid.add(profileGender, 3, 2);
        
        // Row 4
        Label lblDesignation = new Label("Designation:");
        lblDesignation.setStyle("-fx-font-weight: bold; -fx-text-fill: #4a5568;");
        formGrid.add(lblDesignation, 0, 3);
        formGrid.add(profileDesignation, 1, 3);
        
        Label lblDepartment = new Label("Department:");
        lblDepartment.setStyle("-fx-font-weight: bold; -fx-text-fill: #4a5568;");
        formGrid.add(lblDepartment, 2, 3);
        formGrid.add(profileDepartment, 3, 3);
        
        // Row 5
        Label lblAddress = new Label("Address:");
        lblAddress.setStyle("-fx-font-weight: bold; -fx-text-fill: #4a5568;");
        formGrid.add(lblAddress, 0, 4);
        formGrid.add(profileAddress, 1, 4, 3, 1);
        
        // Row 6
        Label lblBankName = new Label("Bank Name:");
        lblBankName.setStyle("-fx-font-weight: bold; -fx-text-fill: #4a5568;");
        formGrid.add(lblBankName, 0, 5);
        formGrid.add(profileBankName, 1, 5);
        
        Label lblAccountNumber = new Label("Account Number:");
        lblAccountNumber.setStyle("-fx-font-weight: bold; -fx-text-fill: #4a5568;");
        formGrid.add(lblAccountNumber, 2, 5);
        formGrid.add(profileAccountNumber, 3, 5);
        
        // Set column constraints
        for (int i = 0; i < 4; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPrefWidth(150);
            formGrid.getColumnConstraints().add(col);
        }
        
        // Update button
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(btnUpdateProfile);
        
        form.getChildren().addAll(formGrid, buttonBox);
        profileContent.getChildren().addAll(title, form);
        contentPane.getChildren().add(profileContent);
    }
    
    @FXML
    private void loadSalary() {
        setActiveButton(btnSalary);
        contentPane.getChildren().clear();
        
        VBox salaryContent = new VBox(20);
        salaryContent.setStyle("-fx-padding: 30; -fx-background-color: #f8fafc;");
        
        Label title = new Label("💰 Salary & Payslips");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        
        // Summary stats
        HBox summaryBox = new HBox(20);
        summaryBox.setStyle("-fx-padding: 20; -fx-background-color: white; -fx-background-radius: 10; " +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 4);");
        
        VBox totalSalary = new VBox(5);
        totalSalary.setAlignment(Pos.CENTER);
        totalSalary.setStyle("-fx-padding: 20; -fx-min-width: 200;");
        Label totalLabel = new Label("Total Earnings");
        totalLabel.setStyle("-fx-text-fill: #718096; -fx-font-size: 12px;");
        Label totalValue = new Label(lblTotalSalary.getText());
        totalValue.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #38a169;");
        totalSalary.getChildren().addAll(totalLabel, totalValue);
        
        VBox avgSalary = new VBox(5);
        avgSalary.setAlignment(Pos.CENTER);
        avgSalary.setStyle("-fx-padding: 20; -fx-min-width: 200;");
        Label avgLabel = new Label("Average Monthly");
        avgLabel.setStyle("-fx-text-fill: #718096; -fx-font-size: 12px;");
        Label avgValue = new Label(lblAvgSalary.getText());
        avgValue.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #4299e1;");
        avgSalary.getChildren().addAll(avgLabel, avgValue);
        
        summaryBox.getChildren().addAll(totalSalary, avgSalary);
        
        // Table
        VBox tableContainer = new VBox(10);
        tableContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20; " +
                              "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 4);");
        
        Label tableTitle = new Label("Salary History");
        tableTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        
        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(btnDownloadPayslip);
        
        tableContainer.getChildren().addAll(tableTitle, salaryTable, buttonBox);
        
        salaryContent.getChildren().addAll(title, summaryBox, tableContainer);
        contentPane.getChildren().add(salaryContent);
        
        loadSalaryData();
    }
    
    @FXML
    private void loadAttendance() {
        setActiveButton(btnAttendance);
        contentPane.getChildren().clear();
        
        VBox attendanceContent = new VBox(20);
        attendanceContent.setStyle("-fx-padding: 30; -fx-background-color: #f8fafc;");
        
        Label title = new Label("⏰ Attendance Records");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        
        // Filter controls
        HBox filterBox = new HBox(15);
        filterBox.setStyle("-fx-padding: 20; -fx-background-color: white; -fx-background-radius: 10; " +
                         "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 4);");
        
        Label filterLabel = new Label("Filter by Date:");
        filterLabel.setStyle("-fx-text-fill: #4a5568; -fx-font-weight: bold;");
        
        attendanceDateFilter.setValue(LocalDate.now());
        
        filterBox.getChildren().addAll(filterLabel, attendanceDateFilter, btnRefreshAttendance);
        
        // Table
        VBox tableContainer = new VBox(10);
        tableContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20; " +
                              "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 4);");
        
        Label tableTitle = new Label("Your Attendance History");
        tableTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        
        tableContainer.getChildren().addAll(tableTitle, attendanceTable);
        
        attendanceContent.getChildren().addAll(title, filterBox, tableContainer);
        contentPane.getChildren().add(attendanceContent);
        
        loadAttendanceData();
    }
    
    @FXML
    private void loadLeaves() {
        setActiveButton(btnLeaves);
        contentPane.getChildren().clear();
        
        VBox leavesContent = new VBox(20);
        leavesContent.setStyle("-fx-padding: 30; -fx-background-color: #f8fafc;");
        
        Label title = new Label("📅 Leave Management");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        
        // Apply leave form
        VBox applyForm = new VBox(15);
        applyForm.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20; " +
                         "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 4);");
        
        Label formTitle = new Label("Apply for Leave");
        formTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        
        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(15);
        formGrid.setPadding(new Insets(10));
        
        // Form fields
        formGrid.add(new Label("Leave Type:"), 0, 0);
        formGrid.add(leaveTypeCombo, 1, 0);
        
        formGrid.add(new Label("Start Date:"), 0, 1);
        formGrid.add(leaveStartDate, 1, 1);
        
        formGrid.add(new Label("End Date:"), 0, 2);
        formGrid.add(leaveEndDate, 1, 2);
        
        formGrid.add(new Label("Reason:"), 0, 3);
        formGrid.add(leaveReason, 1, 3);
        
        leaveStartDate.setValue(LocalDate.now());
        leaveEndDate.setValue(LocalDate.now().plusDays(1));
        
        HBox buttonBox = new HBox(15);
        buttonBox.getChildren().addAll(btnApplyLeave, btnCancelLeave);
        
        applyForm.getChildren().addAll(formTitle, formGrid, buttonBox);
        
        // Leave history table
        VBox tableContainer = new VBox(10);
        tableContainer.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20; " +
                              "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 4);");
        
        Label tableTitle = new Label("Your Leave History");
        tableTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");
        
        tableContainer.getChildren().addAll(tableTitle, leavesTable);
        
        leavesContent.getChildren().addAll(title, applyForm, tableContainer);
        contentPane.getChildren().add(leavesContent);
        
        loadLeavesData();
    }
    
    private void handleClockIn() {
        LocalTime currentTime = LocalTime.now();
        
        try (Connection conn = DatabaseConnector.getConnection()) {
            // Check if already clocked in today
            PreparedStatement checkStmt = conn.prepareStatement(
                "SELECT * FROM attendance WHERE employee_id = ? AND date = ?");
            checkStmt.setInt(1, employeeId);
            checkStmt.setDate(2, Date.valueOf(LocalDate.now()));
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                // Update existing record
                PreparedStatement updateStmt = conn.prepareStatement(
                    "UPDATE attendance SET clock_in = ?, status = 'PRESENT' WHERE id = ?");
                updateStmt.setTime(1, Time.valueOf(currentTime));
                updateStmt.setInt(2, rs.getInt("id"));
                updateStmt.executeUpdate();
            } else {
                // Insert new record
                PreparedStatement insertStmt = conn.prepareStatement(
                    "INSERT INTO attendance (employee_id, date, clock_in, status) VALUES (?, ?, ?, 'PRESENT')");
                insertStmt.setInt(1, employeeId);
                insertStmt.setDate(2, Date.valueOf(LocalDate.now()));
                insertStmt.setTime(3, Time.valueOf(currentTime));
                insertStmt.executeUpdate();
            }
            
            isClockedIn = true;
            clockInTime = LocalDateTime.now();
            btnClockIn.setDisable(true);
            btnClockOut.setDisable(false);
            clockStatusLabel.setText("Clocked in at: " + currentTime.toString());
            
            showAlert("Success", "Clock in recorded successfully!", Alert.AlertType.INFORMATION);
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to clock in: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void handleClockOut() {
        if (!isClockedIn) {
            showAlert("Error", "You haven't clocked in yet!", Alert.AlertType.WARNING);
            return;
        }
        
        LocalTime currentTime = LocalTime.now();
        
        try (Connection conn = DatabaseConnector.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE attendance SET clock_out = ? WHERE employee_id = ? AND date = ?");
            stmt.setTime(1, Time.valueOf(currentTime));
            stmt.setInt(2, employeeId);
            stmt.setDate(3, Date.valueOf(LocalDate.now()));
            
            int rows = stmt.executeUpdate();
            
            if (rows > 0) {
                isClockedIn = false;
                btnClockOut.setDisable(true);
                clockStatusLabel.setText("Clocked out at: " + currentTime.toString());
                
                // Calculate hours worked
                long minutesWorked = java.time.Duration.between(clockInTime, LocalDateTime.now()).toMinutes();
                double hoursWorked = minutesWorked / 60.0;
                
                showAlert("Success", 
                    String.format("Clock out recorded! Hours worked today: %.2f hours", hoursWorked),
                    Alert.AlertType.INFORMATION);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to clock out: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void updateProfile() {
        try (Connection conn = DatabaseConnector.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "UPDATE employees SET first_name = ?, last_name = ?, email = ?, phone = ?, " +
                "address = ?, date_of_birth = ?, gender = ?, bank_name = ?, account_number = ? WHERE id = ?");
            
            stmt.setString(1, profileFirstName.getText());
            stmt.setString(2, profileLastName.getText());
            stmt.setString(3, profileEmail.getText());
            stmt.setString(4, profilePhone.getText());
            stmt.setString(5, profileAddress.getText());
            stmt.setDate(6, profileDOB.getValue() != null ? 
                        Date.valueOf(profileDOB.getValue()) : null);
            stmt.setString(7, profileGender.getValue());
            stmt.setString(8, profileBankName.getText());
            stmt.setString(9, profileAccountNumber.getText());
            stmt.setInt(10, employeeId);
            
            int rows = stmt.executeUpdate();
            
            if (rows > 0) {
                showAlert("Success", "Profile updated successfully!", Alert.AlertType.INFORMATION);
                employeeNameLabel.setText(profileFirstName.getText() + " " + profileLastName.getText());
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to update profile: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void loadAttendanceData() {
        ObservableList<AttendanceRecord> data = FXCollections.observableArrayList();
        LocalDate filterDate = attendanceDateFilter.getValue();
        
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM attendance WHERE employee_id = ? " +
                 (filterDate != null ? "AND date = ? " : "") +
                 "ORDER BY date DESC LIMIT 50")) {
            
            stmt.setInt(1, employeeId);
            if (filterDate != null) {
                stmt.setDate(2, Date.valueOf(filterDate));
            }
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                data.add(new AttendanceRecord(
                    rs.getDate("date").toString(),
                    rs.getTime("clock_in") != null ? rs.getTime("clock_in").toString() : "N/A",
                    rs.getTime("clock_out") != null ? rs.getTime("clock_out").toString() : "N/A",
                    rs.getString("status")
                ));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        attendanceTable.setItems(data);
    }
    
    private void loadLeavesData() {
        ObservableList<LeaveRecord> data = FXCollections.observableArrayList();
        
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM leaves WHERE employee_id = ? ORDER BY start_date DESC")) {
            
            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                data.add(new LeaveRecord(
                    rs.getString("leave_type"),
                    rs.getDate("start_date").toString(),
                    rs.getDate("end_date").toString(),
                    rs.getInt("total_days"),
                    rs.getString("status")
                ));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        leavesTable.setItems(data);
    }
    
    private void loadSalaryData() {
        ObservableList<SalaryRecord> data = FXCollections.observableArrayList();
        double totalSalary = 0;
        int count = 0;
        
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM payroll WHERE employee_id = ? ORDER BY payment_date DESC")) {
            
            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                double basic = rs.getDouble("basic_salary");
                double overtime = rs.getDouble("overtime_amount");
                double bonus = rs.getDouble("bonus_amount");
                double allowance = rs.getDouble("allowance_amount");
                double deduction = rs.getDouble("deduction_amount");
                double tax = rs.getDouble("tax_amount");
                double net = rs.getDouble("net_salary");
                
                data.add(new SalaryRecord(
                    rs.getDate("pay_period_start") + " to " + rs.getDate("pay_period_end"),
                    basic, overtime, bonus, allowance, deduction, tax, net
                ));
                
                totalSalary += net;
                count++;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        salaryTable.setItems(data);
        lblTotalSalary.setText(String.format("$%.2f", totalSalary));
        lblAvgSalary.setText(count > 0 ? String.format("$%.2f", totalSalary / count) : "$0.00");
    }
    
    private void applyForLeave() {
        if (leaveStartDate.getValue() == null || leaveEndDate.getValue() == null) {
            showAlert("Error", "Please select start and end dates", Alert.AlertType.WARNING);
            return;
        }
        
        if (leaveStartDate.getValue().isAfter(leaveEndDate.getValue())) {
            showAlert("Error", "Start date must be before end date", Alert.AlertType.WARNING);
            return;
        }
        
        if (leaveReason.getText().trim().isEmpty()) {
            showAlert("Error", "Please provide a reason for leave", Alert.AlertType.WARNING);
            return;
        }
        
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(
            leaveStartDate.getValue(), leaveEndDate.getValue()) + 1;
        
        if (daysBetween <= 0) {
            showAlert("Error", "Invalid date range", Alert.AlertType.WARNING);
            return;
        }
        
        try (Connection conn = DatabaseConnector.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO leaves (employee_id, leave_type, start_date, end_date, " +
                "total_days, reason, status) VALUES (?, ?, ?, ?, ?, ?, 'PENDING')");
            
            stmt.setInt(1, employeeId);
            stmt.setString(2, leaveTypeCombo.getValue());
            stmt.setDate(3, Date.valueOf(leaveStartDate.getValue()));
            stmt.setDate(4, Date.valueOf(leaveEndDate.getValue()));
            stmt.setInt(5, (int) daysBetween);
            stmt.setString(6, leaveReason.getText());
            
            int rows = stmt.executeUpdate();
            
            if (rows > 0) {
                showAlert("Success", "Leave application submitted successfully!", Alert.AlertType.INFORMATION);
                leaveReason.clear();
                loadLeavesData();
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to apply for leave: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void cancelSelectedLeave() {
        LeaveRecord selected = leavesTable.getSelectionModel().getSelectedItem();
        
        if (selected == null) {
            showAlert("Warning", "Please select a leave record to cancel", Alert.AlertType.WARNING);
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Cancellation");
        confirm.setHeaderText("Cancel Leave Application");
        confirm.setContentText("Are you sure you want to cancel this leave application?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (Connection conn = DatabaseConnector.getConnection()) {
                    PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE leaves SET status = 'CANCELLED' WHERE employee_id = ? AND start_date = ?");
                    
                    stmt.setInt(1, employeeId);
                    stmt.setDate(2, Date.valueOf(LocalDate.parse(selected.getStartDate())));
                    
                    int rows = stmt.executeUpdate();
                    
                    if (rows > 0) {
                        showAlert("Success", "Leave application cancelled!", Alert.AlertType.INFORMATION);
                        loadLeavesData();
                    }
                    
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Error", "Failed to cancel leave: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }
    
    private void downloadPayslip() {
        SalaryRecord selected = salaryTable.getSelectionModel().getSelectedItem();
        
        if (selected == null) {
            showAlert("Warning", "Please select a salary record to download", Alert.AlertType.WARNING);
            return;
        }
        
        // In a real application, generate PDF payslip here
        showAlert("Info", "Payslip download feature would generate a PDF file with salary details.", 
                 Alert.AlertType.INFORMATION);
    }
    
    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Logout");
        alert.setHeaderText("Logout from Employee Portal");
        alert.setContentText("Are you sure you want to logout?");
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                SessionManager.clearSession();
                clockTimeline.stop();
                
                try {
                    Stage stage = (Stage) btnLogout.getScene().getWindow();
                    Parent root = FXMLLoader.load(getClass().getResource("/com/payroll/views/LoginSignup.fxml"));
                    Scene scene = new Scene(root);
                    stage.setScene(scene);
                    stage.setTitle("Payroll System - Login");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Data Model Classes
    public static class AttendanceRecord {
        private final String date;
        private final String clockIn;
        private final String clockOut;
        private final String status;
        
        public AttendanceRecord(String date, String clockIn, String clockOut, String status) {
            this.date = date;
            this.clockIn = clockIn;
            this.clockOut = clockOut;
            this.status = status;
        }
        
        public String getDate() { return date; }
        public String getClockIn() { return clockIn; }
        public String getClockOut() { return clockOut; }
        public String getStatus() { return status; }
    }
    
    public static class LeaveRecord {
        private final String leaveType;
        private final String startDate;
        private final String endDate;
        private final int totalDays;
        private final String status;
        
        public LeaveRecord(String leaveType, String startDate, String endDate, int totalDays, String status) {
            this.leaveType = leaveType;
            this.startDate = startDate;
            this.endDate = endDate;
            this.totalDays = totalDays;
            this.status = status;
        }
        
        public String getLeaveType() { return leaveType; }
        public String getStartDate() { return startDate; }
        public String getEndDate() { return endDate; }
        public int getTotalDays() { return totalDays; }
        public String getStatus() { return status; }
    }
    
    public static class SalaryRecord {
        private final String payPeriod;
        private final double basic;
        private final double overtime;
        private final double bonus;
        private final double allowance;
        private final double deduction;
        private final double tax;
        private final double netSalary;
        
        public SalaryRecord(String payPeriod, double basic, double overtime, double bonus, 
                           double allowance, double deduction, double tax, double netSalary) {
            this.payPeriod = payPeriod;
            this.basic = basic;
            this.overtime = overtime;
            this.bonus = bonus;
            this.allowance = allowance;
            this.deduction = deduction;
            this.tax = tax;
            this.netSalary = netSalary;
        }
        
        public String getPayPeriod() { return payPeriod; }
        public double getBasic() { return basic; }
        public double getOvertime() { return overtime; }
        public double getBonus() { return bonus; }
        public double getAllowance() { return allowance; }
        public double getDeduction() { return deduction; }
        public double getTax() { return tax; }
        public double getNetSalary() { return netSalary; }
    }
}