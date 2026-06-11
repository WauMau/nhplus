package de.hitec.nhplus.logging;

import de.hitec.nhplus.datastorage.DaoFactory;
import de.hitec.nhplus.datastorage.LogDao;
import de.hitec.nhplus.model.Log;
import de.hitec.nhplus.model.Session;

import java.time.LocalDateTime;

public class LogService {

    private static final LogDao logDao =
            DaoFactory.getDaoFactory().createLogDao();

    public static void log(String action, String description) {

        if (Session.getCurrentUser() == null) {
            return; // kein User → kein Log
        }

        String username = Session.getCurrentUser().getUsername();

        Log log = new Log(
                0,
                username,
                action,
                LocalDateTime.now().toString(),
                description
        );

        try {
            logDao.create(log);
        } catch (Exception e) {
            // Logging darf nie die App crashen
            e.printStackTrace();
        }
    }
}