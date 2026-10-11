package com.alm.service;

import com.alm.dao.UserDAO;
import com.alm.model.User;
import com.alm.util.PasswordUtil;

public class AuthService {
    private UserDAO userDAO = new UserDAO();

    public User login(String username, String password) {
        User user = userDAO.findByUsername(username);
        if (user != null) {
            boolean verified = PasswordUtil.verifyPassword(password, user.getPassword());
            if (verified) {
                System.out.println("Login success for: " + username);
                return user;
            } else {
                System.out.println("Invalid password for: " + username);
            }
        }
        return null;
    }

    public String hashPassword(String plainPassword) {
        return com.alm.util.PasswordUtil.hashPassword(plainPassword);
    }
}
