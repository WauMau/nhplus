package de.hitec.nhplus.model;

/**
 * Represents a single immutable audit-log entry.
 *
 * <p>Each entry records <em>who</em> ({@code username}) did <em>what</em>
 * ({@code action} plus a human-readable {@code description}) and <em>when</em>
 * ({@code timestamp}). Log entries are created by the
 * {@link de.hitec.nhplus.logging.LogService} and never modified afterwards,
 * which is why all fields are {@code final}.</p>
 *
 * <p>Single responsibility: pure data holder for one log record &mdash; it does
 * not write itself to the database (that is {@link de.hitec.nhplus.datastorage.LogDao}).</p>
 */
public class Log {

    private final int id;
    private final String username;
    private final String action;
    private final String timestamp;
    private final String description;

    /**
     * Creates a log entry.
     *
     * @param id          technical primary key (0 for a not-yet-persisted entry)
     * @param username    login name of the user who triggered the action
     * @param action      short action code, e.g. {@code "LOGIN"} or {@code "USER_CREATE"}
     * @param timestamp   point in time the action happened, stored as a string
     * @param description human-readable description of what happened
     */
    public Log(int id, String username, String action, String timestamp, String description) {
        this.id = id;
        this.username = username;
        this.action = action;
        this.timestamp = timestamp;
        this.description = description;
    }

    /**
     * Returns the technical primary key of this log entry.
     *
     * @return the log id
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the login name of the acting user.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the short action code of this entry.
     *
     * @return the action code
     */
    public String getAction() {
        return action;
    }

    /**
     * Returns the moment the action happened.
     *
     * @return the timestamp as a string
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the human-readable description of the action.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }
}
