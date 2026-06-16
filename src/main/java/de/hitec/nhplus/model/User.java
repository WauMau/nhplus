package de.hitec.nhplus.model;

public class User {
    private final int id;
    private final String username;
    private final String passwordHash;
    private final String salt;
    private final String role;
    private boolean active;
    private boolean mustChangePassword;

    public User(
            int id,
            String username,
            String passwordHash,
            String salt,
            String role,
            boolean active,
            boolean mustChangePassword) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.role = role;
        this.active = active;
        this.mustChangePassword = mustChangePassword;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public String getRole() {
        return role;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public boolean isUser() {
        return "PFLEGEKRAFT".equalsIgnoreCase(role);
    }

    public boolean isActive() {
        return active;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }
}