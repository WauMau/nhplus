package de.hitec.nhplus.datastorage;

import de.hitec.nhplus.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    private final Connection connection;

    public UserDao() {
        connection = ConnectionBuilder.getConnection();
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
    }

    public void updateActiveStatus(int userId, boolean active) throws SQLException {
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
    }

    public void resetPassword(int userId, String hash, String salt) throws SQLException {
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
    }
}