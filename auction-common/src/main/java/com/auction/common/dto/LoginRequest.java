package com.auction.common.dto;

import java.io.Serializable;


public class LoginRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String username;
    private final String password;

    public LoginRequest(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống.");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống.");
        }
        this.username = username.trim();
        this.password = password;
    }

    public String getUsername() { return username; }

    public String getPassword() { return password; }


    @Override
    public String toString() {
        return "LoginRequest{username=" + username + ", password=***}";
    }
}