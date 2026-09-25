package com.bdbank.model;

import java.io.Serializable;

public class AdminUser implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String adminId;
    private String name;
    private String passwordHash;

    public AdminUser(String adminId, String name, String passwordHash) {
        this.adminId = adminId;
        this.name = name;
        this.passwordHash = passwordHash;
    }

    public String getAdminId() { return adminId; }
    public String getName() { return name; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String v) { this.passwordHash = v; }
}
