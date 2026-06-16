package de.hitec.nhplus.model;

/**
 * Holds the currently logged-in user for the whole application.
 *
 * <p>This is a simple, process-wide session store implemented with static
 * state, so that any part of the application (for example the
 * {@link de.hitec.nhplus.logging.LogService}) can find out who is acting
 * without having to pass the {@link User} through every method call.</p>
 *
 * <p>Single responsibility: it only stores and exposes the active user; it does
 * not perform authentication itself.</p>
 */
public class Session {

    private static User currentUser;

    /**
     * Returns the user that is currently logged in.
     *
     * @return the current {@link User}, or {@code null} if nobody is logged in
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Stores the user that has just logged in as the current session user.
     *
     * @param user the authenticated user to remember
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    /**
     * Clears the session, e.g. on logout, so that no user is considered active.
     */
    public static void clear() {
        currentUser = null;
    }
}
