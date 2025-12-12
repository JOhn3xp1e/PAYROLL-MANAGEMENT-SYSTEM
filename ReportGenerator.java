package com.payroll.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReportGenerator {
    
    public static void generateAttendanceReport(ResultSet attendanceData, String filePath) throws IOException, SQLException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("ATTENDANCE REPORT\n");
            writer.write("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
            writer.write("====================================================================\n");
            writer.write(String.format("%-15s %-20s %-12s %-12s %-10s\n", 
                "Employee Code", "Employee Name", "Date", "Clock In", "Clock Out"));
            writer.write("====================================================================\n");
            
            while (attendanceData.next()) {
                writer.write(String.format("%-15s %-20s %-12s %-12s %-12s\n",
                    attendanceData.getString("employee_code"),
                    attendanceData.getString("first_name") + " " + attendanceData.getString("last_name"),
                    attendanceData.getDate("date"),
                    attendanceData.getTime("clock_in"),
                    attendanceData.getTime("clock_out")));
            }
        }
    }
    
    public static void generatePayrollReport(ResultSet payrollData, String filePath) throws IOException, SQLException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("PAYROLL REPORT\n");
            writer.write("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");
            writer.write("==================================================================================================\n");
            writer.write(String.format("%-15s %-20s %-15s %-15s %-15s\n", 
                "Employee Code", "Employee Name", "Pay Period", "Net Salary", "Status"));
            writer.write("==================================================================================================\n");
            
            double total = 0;
            while (payrollData.next()) {
                double netSalary = payrollData.getDouble("net_salary");
                total += netSalary;
                
                writer.write(String.format("%-15s %-20s %-15s $%-14.2f %-15s\n",
                    payrollData.getString("employee_code"),
                    payrollData.getString("first_name") + " " + payrollData.getString("last_name"),
                    payrollData.getDate("pay_period_start") + " to " + payrollData.getDate("pay_period_end"),
                    netSalary,
                    payrollData.getString("status")));
            }
            
            writer.write("==================================================================================================\n");
            writer.write(String.format("%-50s Total: $%.2f\n", "", total));
        }
    }
}