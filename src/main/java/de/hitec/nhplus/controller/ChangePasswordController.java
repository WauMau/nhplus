package de.hitec.nhplus.controller;

import de.hitec.nhplus.datastorage.UserDao;
import de.hitec.nhplus.model.User;
import de.hitec.nhplus.utils.PasswordUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.sql.SQLException;

/**
 * Controller for the change-password dialog ({@code ChangePasswordView.fxml}).
 *
 * <p>Lets a user set a new password: it checks that both entries match and that
 * the new password fulfils the policy ({@link PasswordUtil#isValidPassword(String)}),
 * then stores a freshly salted hash through {@link UserDao} and clears the
 * "must change password" flag. Validation problems are shown as error alerts.</p>
 *
 * <p>Single responsibility: it drives the change-password use case for a single
 * {@link User}; hashing and persistence are delegated to {@link PasswordUtil}
 * and {@link UserDao}.</p>
 */
public class ChangePasswordController {

    @FXML
    private PasswordField txtPassword;

    @FXML
    private PasswordField txtRepeatPassword;

    @FXML
    private Button btnSave;
    private User user;
    private final UserDao userDao = new UserDao();

    /**
     * Sets the user whose password is going to be changed. Must be called before
     * the dialog is shown.
     *
     * @param user the user to update
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Event handler for the save button.
     *
     * <p>Validates the two password fields (non-empty, equal, policy-compliant),
     * generates a new salt and hash, persists them via
     * {@link UserDao#updatePassword(int, String, String)}, clears the
     * "must change password" flag and closes the dialog. Any problem is shown to
     * the user as an error alert.</p>
     */
    @FXML
    private void handleSave() {
        String password = txtPassword.getText();
        String repeatPassword = txtRepeatPassword.getText();

        if (password == null || password.isBlank()) {
            showError("Bitte Passwort eingeben.");
            return;
        }

        if (!password.equals(repeatPassword)) {
            showError("Passwörter stimmen nicht überein.");
            return;
        }

        if (!PasswordUtil.isValidPassword(password)) {
            showError(
                    "Das Passwort muss folgende Anforderungen erfüllen:\n\n" +
                            "- mindestens 8 Zeichen\n" +
                            "- mindestens einen Großbuchstaben (A-Z)\n" +
                            "- mindestens einen Kleinbuchstaben (a-z)\n" +
                            "- mindestens ein Sonderzeichen (!@#$%^&* usw.)"
            );
            return;
        }

        try {
            String salt = PasswordUtil.generateSalt();
            String hash = PasswordUtil.hash(password, salt);

            userDao.updatePassword(user.getId(), hash, salt);
            user.setMustChangePassword(false);

            Stage stage = (Stage) btnSave.getScene().getWindow();
            stage.close();

        } catch (SQLException e) {
            showError("Fehler beim Speichern.");
        }
    }

    /**
     * Shows a modal error alert with the given message.
     *
     * @param message the error text to display
     */
    private void showError(String message) {
        Alert alert =
                new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Fehler");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}