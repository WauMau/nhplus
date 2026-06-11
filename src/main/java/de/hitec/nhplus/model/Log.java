package de.hitec.nhplus.model;

public class Log {

    private final int id;
    private final String username;
    private final String action;
    private final String timestamp;
    private final String description;

    public Log(int id, String username, String action, String timestamp, String description) {
        this.id = id;
        this.username = username;
        this.action = action;
        this.timestamp = timestamp;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getAction() {
        return action;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }
}