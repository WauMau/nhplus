package de.hitec.nhplus.controller;

import de.hitec.nhplus.Main;
import de.hitec.nhplus.model.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class MainWindowController {

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private Button buttonUserManagement;

    @FXML
    public void initialize() {
        if (!Session.getCurrentUser().isAdmin()) {
            if (buttonUserManagement != null) {
                buttonUserManagement.setVisible(false);
            }
        }
    }

    @FXML
    private void handleShowAllPatient(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/de/hitec/nhplus/AllPatientView.fxml"));

        try {
            mainBorderPane.setCenter(loader.load());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @FXML
    private void handleShowAllTreatments(ActionEvent event) {

        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/de/hitec/nhplus/AllTreatmentView.fxml"));

        try {
            mainBorderPane.setCenter(loader.load());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @FXML
    private void handleUserManagement(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/de/hitec/nhplus/UserManagementView.fxml"));

        try {
            mainBorderPane.setCenter(loader.load());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}