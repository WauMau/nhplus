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

public class ChangePasswordController {

    @FXML
    private PasswordField txtPassword;

    @FXML
    private PasswordField txtRepeatPassword;

    @FXML
    private Button btnSave;
    private User user;
    private final UserDao userDao = new UserDao();

    public void setUser(User user) {
        this.user = user;
    }

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

    private void showError(String message) {
        Alert alert =
                new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Fehler");
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();
    }
}