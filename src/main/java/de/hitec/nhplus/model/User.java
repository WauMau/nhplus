package de.hitec.nhplus.model;

/**
 * Represents a single application user (login account).
 *
 * <p>A {@code User} carries the credentials needed for authentication
 * (a {@code passwordHash} together with its {@code salt}; the clear-text
 * password is never stored), the {@code role} that controls access, and two
 * status flags ({@code active}, {@code mustChangePassword}).</p>
 *
 * <p>Single responsibility: this class is a pure data holder. It performs no
 * hashing, no database access and no UI work &mdash; password security lives in
 * {@link de.hitec.nhplus.utils.PasswordUtil}, persistence in
 * {@link de.hitec.nhplus.datastorage.UserDao}.</p>
 */
public class User {
    private final int id;
    private final String username;
    private final String passwordHash;
    private final String salt;
    private final String role;
    private boolean active;
    private boolean mustChangePassword;

    /**
     * Creates a fully populated user, typically when mapping a database row.
     *
     * @param id                 technical primary key (0 for a not-yet-persisted user)
     * @param username           unique login name
     * @param passwordHash       salted hash of the password (never the clear text)
     * @param salt               salt that was used to produce {@code passwordHash}
     * @param role               role identifier, e.g. {@code "ADMIN"} or {@code "PFLEGEKRAFT"}
     * @param active             whether the account may currently log in
     * @param mustChangePassword whether the user must set a new password on next login
     */
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

    /**
     * Returns the technical primary key of this user.
     *
     * @return the user id
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the unique login name.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the stored salted password hash.
     *
     * @return the password hash (never the clear-text password)
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Returns the salt that belongs to {@link #getPasswordHash()}.
     *
     * @return the password salt
     */
    public String getSalt() {
        return salt;
    }

    /**
     * Returns the raw role identifier of this user.
     *
     * @return the role, e.g. {@code "ADMIN"} or {@code "PFLEGEKRAFT"}
     */
    public String getRole() {
        return role;
    }

    /**
     * Checks whether this user has the administrator role.
     *
     * @return {@code true} if the role is {@code "ADMIN"} (case-insensitive)
     */
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    /**
     * Checks whether this user has the regular caregiver role.
     *
     * @return {@code true} if the role is {@code "PFLEGEKRAFT"} (case-insensitive)
     */
    public boolean isUser() {
        return "PFLEGEKRAFT".equalsIgnoreCase(role);
    }

    /**
     * Indicates whether the account is currently allowed to log in.
     *
     * @return {@code true} if the account is active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Indicates whether the user is forced to change the password on next login.
     *
     * @return {@code true} if a password change is required
     */
    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    /**
     * Sets the "must change password" flag, e.g. after a successful password change.
     *
     * @param mustChangePassword {@code true} to require a password change, {@code false} otherwise
     */
    public void setMustChangePassword(boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }
}
