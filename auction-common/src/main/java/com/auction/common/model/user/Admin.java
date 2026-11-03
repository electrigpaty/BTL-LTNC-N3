package com.auction.common.model.user;

public class Admin extends User {
    private static final long serialVersionUID = 1L;

    private String department; // Phòng ban quản lý

    public Admin() {
        super();
    }

    public Admin(String id, String username, String password, String email, String department) {
        super(id, username, password, email);
        this.department = department;
    }

    @Override
    public String getRoleName() {
        return "ADMIN";
    }

    @Override
    public String printInfo() {
        return super.printInfo() + String.format(" | Phòng ban: %s", department);
    }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}