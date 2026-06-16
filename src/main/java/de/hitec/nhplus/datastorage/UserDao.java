package de.hitec.nhplus.datastorage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.hitec.nhplus.logging.LogService;
import de.hitec.nhplus.model.User;

public class UserDao {

    private final Connection connection;

    public UserDao() {
        connection = ConnectionBuilder.getConnection();
    }

    /**
     * Hilfsmethode, um für das Logging den Zustand eines Benutzers anhand der ID zu laden.
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