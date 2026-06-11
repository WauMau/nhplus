package de.hitec.nhplus.utils;

import java.sql.Connection;
import java.time.LocalDateTime;

import de.hitec.nhplus.datastorage.ConnectionBuilder;
import de.hitec.nhplus.datastorage.LogDao;
import de.hitec.nhplus.model.Log;
import de.hitec.nhplus.model.Session;

public class LoggerUtil {

    private static final Connection connection = ConnectionBuilder.getConnection();
    private static final LogDao logDao = new LogDao(connection);

    public static void log(String action, String description) {
        try {
            String username = Session.getCurrentUser() != null
                    ? Session.getCurrentUser().getUsername()
                    : "UNKNOWN";

            String timestamp = LocalDateTime.now().toString();

            Log log = new Log(0, username, action, timestamp, description);
            logDao.create(log);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}