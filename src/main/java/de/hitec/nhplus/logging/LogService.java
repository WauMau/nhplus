package de.hitec.nhplus.logging;

import java.time.LocalDateTime;

import de.hitec.nhplus.datastorage.DaoFactory;
import de.hitec.nhplus.datastorage.LogDao;
import de.hitec.nhplus.model.Log;
import de.hitec.nhplus.model.Session;

/**
 * Central entry point for writing audit-log entries.
 *
 * <p>Other classes call {@link #log(String, String)} whenever a noteworthy
 * action happens (login, user/caregiver changes, ...). The service stamps the
 * entry with the currently logged-in user (from {@link Session}) and the current
 * time, then persists it through {@link LogDao}. This keeps logging concerns out
 * of the controllers and DAOs that trigger it.</p>
 *
 * <p>Single responsibility: building and persisting log entries. Callers only
 * provide the "what" (action code and description); the "who" and "when" are
 * filled in here.</p>
 */
public class LogService {

    private static final LogDao logDao =
            DaoFactory.getDaoFactory().createLogDao();

    /**
     * Creates and stores an audit-log entry for the current user.
     *
     * <p>If no user is logged in (no active {@link Session}), nothing is logged.
     * Persistence errors are caught and printed so that logging never breaks the
     * calling operation.</p>
     *
     * @param action      short action code, e.g. {@code "LOGIN"} or {@code "USER_CREATE"}
     * @param description human-readable description of what happened
     */
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