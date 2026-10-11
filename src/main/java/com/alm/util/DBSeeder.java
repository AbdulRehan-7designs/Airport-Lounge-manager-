package com.alm.util;

import com.alm.dao.UserDAO;
import com.alm.model.User;
import java.sql.*;

public class DBSeeder {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("Connection successful.");

            // Check if user exists
            UserDAO dao = new UserDAO();
            if (dao.findByUsername("admin") == null) {
                System.out.println("Seeding admin user...");
                // password is "admin123" hashed
                String hashed = PasswordUtil.hashPassword("admin123");
                User admin = new User(0, "admin", "admin@loungeos.com", hashed, "ADMIN", true);
                if (dao.createUser(admin)) {
                    System.out.println("Admin user created successfully.");
                } else {
                    System.err.println("Failed to create admin user.");
                }
            } else {
                System.out.println("Admin user already exists.");
                // Ensure password is "admin123"
                String hashed = PasswordUtil.hashPassword("admin123");
                dao.updatePassword(dao.findByUsername("admin").getId(), hashed);
                System.out.println("Admin password reset to 'admin123'.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
