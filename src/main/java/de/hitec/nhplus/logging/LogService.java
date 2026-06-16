package de.hitec.nhplus.logging;

import java.time.LocalDateTime;

import de.hitec.nhplus.datastorage.DaoFactory;
import de.hitec.nhplus.datastorage.LogDao;
import de.hitec.nhplus.model.Log;
import de.hitec.nhplus.model.Session;

public class LogService {

    private static final LogDao logDao =
            DaoFactory.getDaoFactory().createLogDao();

    public static void log(String action, String description) {

        if (Session.getCurrentUser() == null) {
            return;
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
            e.printStackTrace();
        }
    }
}