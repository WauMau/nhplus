package de.hitec.nhplus.datastorage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.hitec.nhplus.logging.LogService;
import de.hitec.nhplus.model.User;

/**
 * Data Access Object for {@link User} records in the {@code users} table.
 *
 * <p>Encapsulates all SQL needed to read users (by id, by username, all) and to
 * create or modify them (create, change password, reset password, activate /
 * deactivate). Every modifying operation also writes an audit entry via
 * {@link LogService}, so that user-management actions are traceable.</p>
 *
 * <p>Single responsibility: it is the only place that knows the {@code users}
 * table layout. It does not implement the generic {@link Dao} interface because
 * users need additional, login-specific queries beyond plain CRUD; keeping it
 * separate avoids forcing unrelated operations into the shared DAO contract.</p>
 */
public class UserDao {

    private final Connection connection;

    /**
     * Creates the DAO using the application's shared database connection.
     */
    public UserDao() {
        connection = ConnectionBuilder.getConnection();
    }

    /**
     * Loads a single user by technical id.
     *
     * <p>Used mainly by the other modifying methods to resolve the username for
     * the audit log.</p>
     *
     * @param id the technical primary key of the user
     * @return the matching {@link User}, or {@code null} if none exists
     * @throws SQLException if the query fails
     */
    public User findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, id);
        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            return mapUser(resultSet);
        }
        return null;
    }

    /**
     * Loads a single user by login name, e.g. during login.
     *
     * @param username the unique login name
     * @return the matching {@link User}, or {@code null} if none exists
     * @throws SQLException if the query fails
     */
    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";

        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, username);
        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            return mapUser(resultSet);
        }
        return null;
    }

    /**
     * Loads all users ordered by username, e.g. for the user-management table.
     *
     * @return a list of all users (empty if there are none)
     * @throws SQLException if the query fails
     */
    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY username";
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            users.add(mapUser(resultSet));
        }
        return users;
    }

    /**
     * Inserts a new user and writes a {@code USER_CREATE} audit entry.
     *
     * @param user the user to persist (its hash and salt must already be set)
     * @throws SQLException if the insert fails
     */
    public void create(User user) throws SQLException {
        String sql =
                """
                INSERT INTO users
                (username, password_hash, salt, role, active, must_change_password)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setString(1, user.getUsername());
        statement.setString(2, user.getPasswordHash());
        statement.setString(3, user.getSalt());
        statement.setString(4, user.getRole());
        statement.setBoolean(5, user.isActive());
        statement.setBoolean(6, user.isMustChangePassword());

        statement.executeUpdate();

        String description = String.format("Neuer Benutzer '%s' mit der Rolle '%s' wurde angelegt.",
                user.getUsername(), user.getRole());
        LogService.log("USER_CREATE", description);
    }

    /**
     * Maps the current row of a result set to a {@link User} object.
     *
     * @param rs the result set positioned on a valid row
     * @return the mapped user
     * @throws SQLException if a column cannot be read
     */
    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password_hash"),
                rs.getString("salt"),
                rs.getString("role"),
                rs.getBoolean("active"),
                rs.getBoolean("must_change_password")
        );
    }

    /**
     * Stores a new password hash and salt for a user and clears the
     * "must change password" flag. Writes a {@code USER_PASSWORD_CHANGE} audit entry.
     *
     * @param userId       id of the user whose password is updated
     * @param passwordHash the new salted password hash
     * @param salt         the salt belonging to the new hash
     * @throws SQLException if the update fails
     */
    public void updatePassword(int userId, String passwordHash, String salt) throws SQLException {

        User user = this.findById(userId);
        String username = (user != null) ? user.getUsername() : "Unbekannt (ID: " + userId + ")";

        String sql =
                """
                UPDATE users
                SET password_hash = ?,
                    salt = ?,
                    must_change_password = 0
                WHERE id = ?
                """;

        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setString(1, passwordHash);
        statement.setString(2, salt);
        statement.setInt(3, userId);
        statement.executeUpdate();

        String description = String.format("Das Passwort für Benutzer '%s' wurde aktualisiert.", username);
        LogService.log("USER_PASSWORD_CHANGE", description);
    }

    /**
     * Activates or deactivates a user account and writes a
     * {@code USER_STATUS_CHANGE} audit entry.
     *
     * @param userId id of the user to change
     * @param active {@code true} to activate, {@code false} to deactivate
     * @throws SQLException if the update fails
     */
    public void updateActiveStatus(int userId, boolean active) throws SQLException {

        User user = this.findById(userId);
        String username = (user != null) ? user.getUsername() : "Unbekannt (ID: " + userId + ")";

        String sql =
                """
                UPDATE users
                SET active = ?
                WHERE id = ?
                """;

        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setBoolean(1, active);
        statement.setInt(2, userId);
        statement.executeUpdate();

        String statusText = active ? "aktiviert" : "deaktiviert";
        String description = String.format("Benutzer '%s' (Rolle: %s) wurde %s.",
                username, (user != null ? user.getRole() : "unbekannt"), statusText);
        LogService.log("USER_STATUS_CHANGE", description);
    }

    /**
     * Sets a new one-time password (hash and salt) for a user and forces a
     * password change on next login. Writes a {@code USER_PASSWORD_RESET} audit entry.
     *
     * @param userId id of the user whose password is reset
     * @param hash   the salted hash of the generated one-time password
     * @param salt   the salt belonging to {@code hash}
     * @throws SQLException if the update fails
     */
    public void resetPassword(int userId, String hash, String salt) throws SQLException {

        User user = this.findById(userId);
        String username = (user != null) ? user.getUsername() : "Unbekannt (ID: " + userId + ")";

        String sql =
                """
                UPDATE users
                SET password_hash = ?,
                    salt = ?,
                    must_change_password = 1
                WHERE id = ?
                """;

        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setString(1, hash);
        statement.setString(2, salt);
        statement.setInt(3, userId);
        statement.executeUpdate();

        String description = String.format("Einmal-Passwort für Benutzer '%s' wurde erzeugt (Passwort-Reset angefordert).", username);
        LogService.log("USER_PASSWORD_RESET", description);
    }
}
