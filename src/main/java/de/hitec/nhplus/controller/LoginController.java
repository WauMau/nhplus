package de.hitec.nhplus.controller;
import de.hitec.nhplus.Main;
import de.hitec.nhplus.datastorage.UserDao;
import de.hitec.nhplus.model.Session;
import de.hitec.nhplus.model.User;
import de.hitec.nhplus.utils.PasswordUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField textFieldUsername;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label labelError;
    private final UserDao userDao = new UserDao();

    @FXML
    private void handleLogin() {

        try {
            String username = textFieldUsername.getText();
            String password = passwordField.getText();
            User user = userDao.findByUsername(username);

            if (user != null && user.isActive() && PasswordUtil.verify(password, user.getSalt(), user.getPasswordHash())) {
                if (user.isMustChangePassword()) {
                    try {
                        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/de/hitec/nhplus/ChangePasswordView.fxml"));

                        Parent root = loader.load();
                        ChangePasswordController controller = loader.getController();
                        controller.setUser(user);

                        Stage stage = new Stage();
                        stage.setTitle("Passwort ändern");
                        stage.setScene(new Scene(root));
                        stage.setResizable(false);
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
                Session.setCurrentUser(user);
                openMainWindow();
            } else {
                labelError.setVisible(true);
                labelError.setText("Benutzername oder Passwort falsch");
            }

        } catch (SQLException exception) {
            labelError.setVisible(true);
            labelError.setText("Datenbankfehler");
            exception.printStackTrace();
        }
    }

    private void openMainWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/de/hitec/nhplus/MainWindowView.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) textFieldUsername.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}