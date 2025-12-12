package com.payroll;

import com.payroll.utils.DatabaseConnector;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("=== STARTING PAYROLL SYSTEM ===");
            
            // Initialize database
            DatabaseConnector.initializeDatabase();
            System.out.println("Database ready.");
            
            // Load FXML
            System.out.println("Loading FXML...");
            Parent root = FXMLLoader.load(getClass().getResource("/com/payroll/views/LoginSignup.fxml"));
            System.out.println("FXML loaded successfully.");
            
            // Create scene
            Scene scene = new Scene(root, 1920, 1080);
            primaryStage.setTitle("Payroll Management System v2.0");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.show();
            
            System.out.println("Application started successfully!");
            System.out.println("=================================");
            
        } catch (Exception e) {
            System.err.println("FATAL ERROR: " + e.getMessage());
            e.printStackTrace();
            
            // Show error
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Startup Error");
            alert.setHeaderText("Application failed to start");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
    
    @Override
    public void stop() {
        DatabaseConnector.closeConnection();
        System.out.println("Application stopped.");
    }
    
    public static void main(String[] args) {
        System.out.println("Launching application...");
        launch(args);
    }
}