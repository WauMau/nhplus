package de.hitec.nhplus.datastorage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import de.hitec.nhplus.model.Log;

public class LogDao extends DaoImp<Log> {

    public LogDao(Connection connection) {
        super(connection);
    }

    @Override
    protected Log getInstanceFromResultSet(ResultSet set) throws SQLException {
        return new Log(
                set.getInt("id"),
                set.getString("username"),
                set.getString("action"),
                set.getString("timestamp"),
                set.getString("description")
        );
    }

    @Override
    protected ArrayList<Log> getListFromResultSet(ResultSet set) throws SQLException {
        ArrayList<Log> list = new ArrayList<>();
        while (set.next()) {
            list.add(getInstanceFromResultSet(set));
        }
        return list;
    }

    @Override
    protected PreparedStatement getCreateStatement(Log log) {
        try {
            String sql = "INSERT INTO logs (username, action, timestamp, description) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, log.getUsername());
            ps.setString(2, log.getAction());
            ps.setString(3, log.getTimestamp());
            ps.setString(4, log.getDescription());
            return ps;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected PreparedStatement getReadByIDStatement(long key) {
        try {
            String sql = "SELECT * FROM logs WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setLong(1, key);
            return ps;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected PreparedStatement getReadAllStatement() {
        try {
            return connection.prepareStatement("SELECT * FROM logs ORDER BY id DESC");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected PreparedStatement getUpdateStatement(Log log) {
        throw new UnsupportedOperationException("Logs werden nicht geändert");
    }

    @Override
    protected PreparedStatement getDeleteStatement(long key) {
        try {
            String sql = "DELETE FROM logs WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setLong(1, key);
            return ps;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}