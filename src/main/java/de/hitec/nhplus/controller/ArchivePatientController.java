package de.hitec.nhplus.controller;

import de.hitec.nhplus.datastorage.DaoFactory;
import de.hitec.nhplus.datastorage.PatientDao;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import de.hitec.nhplus.model.Patient;

import java.sql.SQLException;

/**
 * Controller for the archive patient dialog.
 */
public class ArchivePatientController {

    @FXML
    private TableView<Patient> tableView;

    @FXML
    private TableColumn<Patient, Integer> columnId;

    @FXML
    private TableColumn<Patient, String> columnFirstName;

    @FXML
    private TableColumn<Patient, String> columnSurname;

    @FXML
    private TableColumn<Patient, String> columnDateOfBirth;

    @FXML
    private TableColumn<Patient, String> columnCareLevel;

    @FXML
    private TableColumn<Patient, String> columnRoomNumber;

    @FXML
    private TableColumn<Patient, String> columnAssets;

    @FXML
    private TableColumn<Patient, String> columnLastTreatment;

    @FXML
    private TableColumn<Patient, String> columnArchiveDate;

    @FXML
    private Button buttonReactivate;

    @FXML
    private Button buttonClose;

    private final ObservableList<Patient> archivedPatients = FXCollections.observableArrayList();
    private PatientDao dao;
    private AllPatientController mainController;

    public void initialize() {
        this.columnId.setCellValueFactory(new PropertyValueFactory<>("pid"));
        this.columnFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        this.columnSurname.setCellValueFactory(new PropertyValueFactory<>("surname"));
        this.columnDateOfBirth.setCellValueFactory(new PropertyValueFactory<>("dateOfBirth"));
        this.columnCareLevel.setCellValueFactory(new PropertyValueFactory<>("careLevel"));
        this.columnRoomNumber.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        this.columnAssets.setCellValueFactory(new PropertyValueFactory<>("assets"));
        this.columnLastTreatment.setCellValueFactory(new PropertyValueFactory<>("lastTreatmentDate"));
        this.columnArchiveDate.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getArchiveDate() != null ? cell.getValue().getArchiveDate().toString() : "-"));

        this.tableView.setItems(this.archivedPatients);

        this.buttonReactivate.setDisable(true);
        this.tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Patient>() {
            @Override
            public void changed(ObservableValue<? extends Patient> observableValue, Patient oldPatient, Patient newPatient) {
                ArchivePatientController.this.buttonReactivate.setDisable(newPatient == null);
            }
        });

        this.readAllArchivedAndShowInTableView();
    }

    private void readAllArchivedAndShowInTableView() {
        this.archivedPatients.clear();
        this.dao = DaoFactory.getDaoFactory().createPatientDao();
        try {
            this.archivedPatients.addAll(this.dao.readAllArchived());
            for (Patient patient : this.archivedPatients) {
                patient.setLastTreatmentDate(this.dao.getLastTreatmentDate(patient.getPid()));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @FXML
    public void handleReactivate() {
        Patient selectedItem = this.tableView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            try {
                this.dao.reactivate(selectedItem.getPid());
                this.tableView.getItems().remove(selectedItem);
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        }
    }

    @FXML
    public void handleClose() {
        Stage stage = (Stage) buttonClose.getScene().getWindow();
        stage.close();
    }

    public void setMainController(AllPatientController mainController) {
        this.mainController = mainController;
    }
}
