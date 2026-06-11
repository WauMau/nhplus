package de.hitec.nhplus.controller;

import de.hitec.nhplus.datastorage.UserDao;
import de.hitec.nhplus.model.User;
import de.hitec.nhplus.utils.PasswordUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;

import javafx.scene.control.TextField;
import java.sql.SQLException;

public class UserManagementController {

    @FXML
    private TableView<User> tableUsers;

    @FXML
    private TableColumn<User, String> columnUsername;

    @FXML
    private TableColumn<User, String> columnRole;

    @FXML
    private TableColumn<User, Boolean> columnActive;

    @FXML
    private TextField txtUsername;

    @FXML
    private ComboBox<String> cmbRole;

    private final UserDao userDao = new UserDao();

    @FXML
    public void initialize() {
        columnUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        columnRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        columnActive.setCellValueFactory(new PropertyValueFactory<>("active"));
        cmbRole.getItems().addAll("ADMIN", "PFLEGEKRAFT");
        loadUsers();
    }

    @FXML
    private void handleCreateUser() {
        String username = txtUsername.getText();
        String role = cmbRole.getValue();

        if (username == null || username.isBlank()) {
            return;
        }

        if (role == null) {
            return;
        }

        try {
            String initialPassword = PasswordUtil.generateSecurePassword();
            String salt = PasswordUtil.generateSalt();
            String hash = PasswordUtil.hash(initialPassword, salt);

            User user = new User(0,
                            username,
                            hash,
                            salt,
                            role,
                            true,
                            true);

            userDao.create(user);

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Benutzer erstellt");

            ButtonType closeButton = new ButtonType("Schließen", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().add(closeButton);

            TextField passwordField = new TextField(initialPassword);
            passwordField.setEditable(false);
            Button copyButton = new Button("Passwort kopieren");

            copyButton.setOnAction(event -> {
                ClipboardContent content = new ClipboardContent();
                content.putString(initialPassword);
                Clipboard.getSystemClipboard().setContent(content);
            });

            VBox content = new VBox(10);
            content.getChildren().addAll(new Label("Initialpasswort:"), passwordField, copyButton);

            dialog.getDialogPane().setContent(content);
            dialog.showAndWait();
            loadUsers();

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void loadUsers() {
        try {
            tableUsers.setItems(FXCollections.observableArrayList(userDao.findAll()));

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @FXML
    private void handleToggleUser() {
        User selectedUser = tableUsers.getSelectionModel().getSelectedItem();

        if (selectedUser == null) {
            return;
        }

        try {
            userDao.updateActiveStatus(selectedUser.getId(), !selectedUser.isActive());
            loadUsers();

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @FXML
    private void handleGenerateOneTimePassword() {
        User selectedUser = tableUsers.getSelectionModel().getSelectedItem();

        if (selectedUser == null) {
            return;
        }

        try {
            String password = PasswordUtil.generateSecurePassword();
            String salt = PasswordUtil.generateSalt();
            String hash = PasswordUtil.hash(password, salt);

            userDao.resetPassword(selectedUser.getId(), hash, salt);

            Dialog<Void> dialog = new Dialog<>();

            dialog.setTitle("Neues Einmal-Passwort");

            ButtonType closeButton = new ButtonType("Schließen", ButtonBar.ButtonData.OK_DONE);

            dialog.getDialogPane().getButtonTypes().add(closeButton);

            TextField passwordField = new TextField(password);
            passwordField.setEditable(false);
            Button copyButton = new Button("Passwort kopieren");

            copyButton.setOnAction(event -> {

                ClipboardContent content = new ClipboardContent();
                content.putString(password);

                Clipboard.getSystemClipboard().setContent(content);
            });

            VBox content = new VBox(10);
            content.getChildren().addAll(new Label("Neues Einmal-Passwort:"), passwordField, copyButton);

            dialog.getDialogPane().setContent(content);
            dialog.showAndWait();

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}