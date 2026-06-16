package de.hitec.nhplus.controller;

import java.io.IOException;
import java.sql.SQLException;

import de.hitec.nhplus.Main;
import de.hitec.nhplus.datastorage.UserDao;
import de.hitec.nhplus.logging.LogService;
import de.hitec.nhplus.model.Session;
import de.hitec.nhplus.model.User;
import de.hitec.nhplus.utils.PasswordUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controller for the login view ({@code LoginView.fxml}).
 *
 * <p>Validates the entered credentials against the {@code users} table via
 * {@link UserDao} and {@link PasswordUtil}. On success it stores the user in the
 * {@link Session}, writes a {@code LOGIN} audit entry and opens the main window;
 * if the account still requires a password change, it first forces the
 * change-password dialog. On failure it shows an error label.</p>
 *
 * <p>Single responsibility: it coordinates the login use case (input &rarr;
 * verification &rarr; navigation). It does not implement the password hashing
 * itself ({@link PasswordUtil}) nor the SQL ({@link UserDao}).</p>
 */
public class LoginController {

    @FXML
    private TextField textFieldUsername;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label labelError;

    private final UserDao userDao = new UserDao();

    /**
     * Event handler for the login button.
     *
     * <p>Reads username and password, looks up the user and verifies the password.
     * If the account is valid and active: forces a password change when required,
     * stores the user in the {@link Session}, logs the login and opens the main
     * window. Otherwise it shows an error message. Database errors are reported in
     * the error label.</p>
     */
    @FXML
    private void handleLogin() {

        try {
            String username = textFieldUsername.getText();
            String password = passwordField.getText();
            User user = userDao.findByUsername(username);

            if (user != null && user.isActive()
                    && PasswordUtil.verify(password, user.getSalt(), user.getPasswordHash())) {

                if (user.isMustChangePassword()) {
                    try {
                        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/de/hitec/nhplus/ChangePasswordView.fxml"));
                        Scene scene = new Scene(loader.load());
                        Stage stage = new Stage();
                        ChangePasswordController controller = loader.getController();
                        controller.setUser(user);
                        stage.setScene(scene);
                        stage.showAndWait();

                        if (user.isMustChangePassword()) {
                            return;
                        }

                    } catch (IOException exception) {
                        exception.printStackTrace();
                        labelError.setVisible(true);
                        labelError.setText("Passwortdialog konnte nicht geöffnet werden.");
                        return;
                    }
                }

                // Benutzer wird in der Session hinterlegt, damit das Logging-System weiß, wer eingeloggt ist
                Session.setCurrentUser(user);

                // Unterscheidung, ob sich ein Admin oder ein normaler User eingeloggt hat
                String roleText = user.isAdmin() ? "Admin" : "User";
                LogService.log("LOGIN", "Benutzer erfolgreich eingeloggt");
                openMainWindow();

            } else {
                labelError.setVisible(true);
                labelError.setText("Benutzername oder Passwort falsch");
                passwordField.clear();
            }

        } catch (SQLException exception) {
            labelError.setVisible(true);
            labelError.setText("Datenbankfehler");
            passwordField.clear();
            exception.printStackTrace();
        }
    }

    /**
     * Replaces the current scene with the main application window after a
     * successful login. Errors while loading the view are printed.
     */
    private void openMainWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Main.class.getResource("/de/hitec/nhplus/MainWindowView.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) textFieldUsername.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}