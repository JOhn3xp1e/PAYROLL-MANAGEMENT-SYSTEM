package com.payroll.utils;

import java.time.LocalDateTime;

public class SessionManager {
    private static Integer currentUserId;
    private static String currentUsername;
    private static String currentUserRole;
    private static String currentUserEmail;
    private static Integer currentEmployeeId;
    private static LocalDateTime loginTime;
    
    public static void setCurrentUserId(Integer userId) {
        currentUserId = userId;
    }
    
    public static Integer getCurrentUserId() {
        return currentUserId;
    }
    
    public static void setCurrentUsername(String username) {
        currentUsername = username;
    }
    
    public static String getCurrentUsername() {
        return currentUsername;
    }
    
    public static void setCurrentUserRole(String role) {
    if (role != null) {
        currentUserRole = role.trim().toUpperCase();
        System.out.println("SessionManager: Role set to '" + currentUserRole + "'");
    } else {
        currentUserRole = null;
        System.out.println("SessionManager: Role set to null");
    }
}
    
    public static String getCurrentUserRole() {
        return currentUserRole;
    }
    
    public static void setCurrentUserEmail(String email) {
        currentUserEmail = email;
    }
    
    public static String getCurrentUserEmail() {
        return currentUserEmail;
    }
    
    public static void setCurrentEmployeeId(Integer employeeId) {
        currentEmployeeId = employeeId;
    }
    
    public static Integer getCurrentEmployeeId() {
        return currentEmployeeId;
    }
    
    public static void setLoginTime(LocalDateTime time) {
        loginTime = time;
    }
    
    public static LocalDateTime getLoginTime() {
        return loginTime;
    }
    
    public static void clearSession() {
        currentUserId = null;
        currentUsername = null;
        currentUserRole = null;
        currentUserEmail = null;
        currentEmployeeId = null;
        loginTime = null;
    }
    
    public static boolean isLoggedIn() {
        return currentUserId != null;
    }
    
    public static boolean isAdmin() {
        return "ADMIN".equals(currentUserRole);
    }
    
    public static boolean isHR() {
        return "HR".equals(currentUserRole);
    }
    
    public static boolean isEmployee() {
        return "EMPLOYEE".equals(currentUserRole);
    }
}