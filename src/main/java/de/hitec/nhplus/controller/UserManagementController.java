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

/**
 * Controller for the user-management view ({@code UserManagementView.fxml}),
 * available to administrators.
 *
 * <p>Lists all users in a table and offers the administrative actions: create a
 * new user (with a generated initial password), activate / deactivate an account
 * and generate a new one-time password. Generated passwords are shown once in a
 * read-only dialog with a "copy to clipboard" button &mdash; this is the only
 * point where a clear-text password is ever displayed; only salt and hash are
 * stored ({@link PasswordUtil}, {@link UserDao}).</p>
 *
 * <p><strong>SRP note:</strong> this controller carries several
 * responsibilities &mdash; table presentation, the create/activate/reset use
 * cases, and the manual building of the password dialogs. It is still small
 * enough to stay readable, but the password-dialog construction (duplicated in
 * {@link #handleCreateUser()} and {@link #handleGenerateOneTimePassword()}) would
 * be the first candidate to extract into a helper if the class grows further.</p>
 */
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

    /**
     * Initialises the table columns, fills the role combo box and loads the
     * current list of users. Called automatically by JavaFX after the FXML is loaded.
     */
    @FXML
    public void initialize() {
        columnUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        columnRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        columnActive.setCellValueFactory(new PropertyValueFactory<>("active"));
        cmbRole.getItems().addAll("ADMIN", "PFLEGEKRAFT");
        loadUsers();
    }

    /**
     * Event handler for the "create user" button.
     *
     * <p>Validates the entered username and selected role, generates a secure
     * initial password (salt + hash), persists the new user via {@link UserDao}
     * and then shows the generated clear-text password once in a read-only dialog
     * with a copy button so the administrator can hand it to the new user.</p>
     */
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

    /**
     * Reloads all users from the database into the table.
     */
    private void loadUsers() {
        try {
            tableUsers.setItems(FXCollections.observableArrayList(userDao.findAll()));

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Event handler for the activate/deactivate button. Flips the active status
     * of the currently selected user and refreshes the table. Does nothing if no
     * user is selected.
     */
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

    /**
     * Event handler for the "generate one-time password" button.
     *
     * <p>For the selected user it generates a new secure password (salt + hash),
     * stores it via {@link UserDao#resetPassword(int, String, String)} (which also
     * forces a password change on next login) and shows the clear-text password
     * once in a read-only dialog with a copy button. Does nothing if no user is
     * selected.</p>
     */
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