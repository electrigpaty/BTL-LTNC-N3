package com.auction.common.dto;

import java.io.Serializable;
import java.util.Locale;
import java.util.regex.Pattern;


public class RegisterRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String ROLE_BIDDER = "BIDDER";
    public static final String ROLE_SELLER = "SELLER";

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{3,30}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int MIN_PASSWORD_LENGTH = 6;

    private final String username;
    private final String password;
    private final String email;
    private final String role;

    public RegisterRequest(String username, String password, String email, String role) {
        if (username == null || !USERNAME_PATTERN.matcher(username.trim()).matches()) {
            throw new IllegalArgumentException(
                    "Tên đăng nhập gồm 3-30 ký tự: chữ, số hoặc dấu gạch dưới.");
        }
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Mật khẩu phải có ít nhất " + MIN_PASSWORD_LENGTH + " ký tự.");
        }
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Email không đúng định dạng.");
        }
        if (role == null) {
            throw new IllegalArgumentException("Vai trò không được để trống.");
        }
        String normalizedRole = role.trim().toUpperCase(Locale.ROOT);
        if (!ROLE_BIDDER.equals(normalizedRole) && !ROLE_SELLER.equals(normalizedRole)) {
            throw new IllegalArgumentException("Vai trò đăng ký chỉ có thể là BIDDER hoặc SELLER.");
        }
        this.username = username.trim();
        this.password = password;
        this.email = email.trim();
        this.role = normalizedRole;
    }

    public String getUsername() { return username; }

    public String getPassword() { return password; }

    public String getEmail() { return email; }

    public String getRole() { return role; }


    @Override
    public String toString() {
        return "RegisterRequest{username=" + username + ", email=" + email
                + ", role=" + role + ", password=***}";
    }
}