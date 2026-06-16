package de.hitec.nhplus.datastorage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import de.hitec.nhplus.model.Log;

/**
 * Data Access Object for {@link Log} audit entries in the {@code logs} table.
 *
 * <p>Implements the SQL building blocks required by the generic
 * {@link DaoImp} template: mapping rows to {@link Log} objects and providing the
 * prepared statements for insert, read-by-id, read-all and delete.</p>
 *
 * <p>Single responsibility: persistence of log entries only. Because audit
 * records must not be altered after the fact, {@link #getUpdateStatement(Log)}
 * is intentionally unsupported.</p>
 */
public class LogDao extends DaoImp<Log> {

    /**
     * Creates the DAO for the given database connection.
     *
     * @param connection the database connection to use
     */
    public LogDao(Connection connection) {
        super(connection);
    }

    /**
     * Maps the current row of the result set to a {@link Log} object.
     *
     * @param set result set positioned on a valid row
     * @return the mapped log entry
     * @throws SQLException if a column cannot be read
     */
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

    /**
     * Maps every remaining row of the result set to a list of {@link Log} objects.
     *
     * @param set result set to iterate over
     * @return list of all log entries in the result set
     * @throws SQLException if a column cannot be read
     */
    @Override
    protected ArrayList<Log> getListFromResultSet(ResultSet set) throws SQLException {
        ArrayList<Log> list = new ArrayList<>();
        while (set.next()) {
            list.add(getInstanceFromResultSet(set));
        }
        return list;
    }

    /**
     * Builds the {@code INSERT} statement for a new log entry.
     *
     * @param log the log entry to insert
     * @return the prepared insert statement
     */
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

    /**
     * Builds the statement that reads a single log entry by its id.
     *
     * @param key the id of the log entry
     * @return the prepared select statement
     */
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

    /**
     * Builds the statement that reads all log entries, newest first.
     *
     * @return the prepared select-all statement
     */
    @Override
    protected PreparedStatement getReadAllStatement() {
        try {
            return connection.prepareStatement("SELECT * FROM logs ORDER BY id DESC");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Not supported: audit log entries are immutable and must never be updated.
     *
     * @param log ignored
     * @return never returns normally
     * @throws UnsupportedOperationException always
     */
    @Override
    protected PreparedStatement getUpdateStatement(Log log) {
        throw new UnsupportedOperationException("Logs werden nicht geändert");
    }

    /**
     * Builds the statement that deletes a log entry by its id.
     *
     * @param key the id of the log entry to delete
     * @return the prepared delete statement
     */
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