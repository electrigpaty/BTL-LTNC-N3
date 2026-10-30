package com.auction.common.model.user;

import com.auction.common.model.Entity;

public abstract class User extends Entity {
    private static final long serialVersionUID = 1L;

    private String username;
    private String password;
    private String email;

    protected User() {
        super();
    }

    protected User(String id, String username, String password, String email) {
        super(id);
        this.username = username;
        this.password = password;
        this.email = email;
    }
    
    public abstract String getRoleName();

    @Override
    public String printInfo() {
        return String.format("[%s] ID: %s | User: %s | Email: %s",
                getRoleName(), getId(), username, email);
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}