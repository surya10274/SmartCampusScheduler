package com.campus.scheduler.model;

import java.io.Serializable;

/**
 * Represents a user of the scheduler system - a student, faculty member,
 * or campus administrator.
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Role {
        STUDENT, FACULTY, ADMIN
    }

    private final String userId;
    private String name;
    private String email;
    private Role role;

    public User(String userId, String name, String email, Role role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String toRecord() {
        return String.join("|", userId, name, email, role.name());
    }

    public static User fromRecord(String record) {
        String[] parts = record.split("\\|");
        return new User(parts[0], parts[1], parts[2], Role.valueOf(parts[3]));
    }

    @Override
    public String toString() {
        return String.format("[%s] %-20s Email:%-25s Role:%s", userId, name, email, role);
    }
}
