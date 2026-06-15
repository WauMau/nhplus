package de.hitec.nhplus.controller;

import de.hitec.nhplus.datastorage.CaregiverDao;
import de.hitec.nhplus.datastorage.DaoFactory;
import de.hitec.nhplus.model.Caregiver;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

import java.sql.SQLException;
import java.util.Optional;

public class AllCaregiverController {

    @FXML
    private TableView<Caregiver> tableView;

    @FXML
    private TableColumn<Caregiver, Long> columnId;

    @FXML
    private TableColumn<Caregiver, String> columnFirstName;

    @FXML
    private TableColumn<Caregiver, String> columnSurname;

    @FXML
    private TableColumn<Caregiver, String> columnTelephone;

    @FXML
    private Button buttonDelete;

    @FXML
    private TextField textFieldFirstName;

    @FXML
    private TextField textFieldSurname;

    @FXML
    private TextField textFieldTelephone;

    private final ObservableList<Caregiver> caregivers = FXCollections.observableArrayList();
    private CaregiverDao dao;

    public void initialize() {
        readAllAndShowInTableView();

        this.columnId.setCellValueFactory(new PropertyValueFactory<>("cid"));

        this.columnFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        this.columnFirstName.setCellFactory(TextFieldTableCell.forTableColumn());

        this.columnSurname.setCellValueFactory(new PropertyValueFactory<>("surname"));
        this.columnSurname.setCellFactory(TextFieldTableCell.forTableColumn());

        this.columnTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        this.columnTelephone.setCellFactory(TextFieldTableCell.forTableColumn());

        this.tableView.setItems(this.caregivers);

        // Löschen-Button ist erst aktiv wenn eine Zeile ausgewählt wurde
        this.buttonDelete.setDisable(true);
        this.tableView.getSelectionModel().selectedItemProperty().addListener(
            (observable, alterWert, neuerWert) -> this.buttonDelete.setDisable(neuerWert == null)
        );
    }

    private void readAllAndShowInTableView() {
        this.caregivers.clear();
        this.dao = DaoFactory.getDaoFactory().createCaregiverDao();
        try {
            this.caregivers.addAll(this.dao.readAll());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Wird aufgerufen wenn der Vorname in der Tabelle geändert wird
    @FXML
    public void handleOnEditFirstname(TableColumn.CellEditEvent<Caregiver, String> event) {
        event.getRowValue().setFirstName(event.getNewValue());
        doUpdate(event);
    }

    // Wird aufgerufen wenn der Nachname in der Tabelle geändert wird
    @FXML
    public void handleOnEditSurname(TableColumn.CellEditEvent<Caregiver, String> event) {
        event.getRowValue().setSurname(event.getNewValue());
        doUpdate(event);
    }

    // Wird aufgerufen wenn die Telefonnummer in der Tabelle geändert wird
    @FXML
    public void handleOnEditTelephone(TableColumn.CellEditEvent<Caregiver, String> event) {
        event.getRowValue().setTelephone(event.getNewValue());
        doUpdate(event);
    }

    private void doUpdate(TableColumn.CellEditEvent<Caregiver, String> event) {
        try {
            this.dao.update(event.getRowValue());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleAdd() {
        String firstName = this.textFieldFirstName.getText();
        String surname = this.textFieldSurname.getText();
        String telephone = this.textFieldTelephone.getText();

        // Fehlermeldung wenn ein Pflichtfeld leer ist (A_5)
        if (firstName.isBlank() || surname.isBlank() || telephone.isBlank()) {
            Alert fehler = new Alert(Alert.AlertType.ERROR);
            fehler.setTitle("Eingabe fehlt");
            fehler.setHeaderText("Pflichtfelder ausfüllen");
            fehler.setContentText("Bitte Vorname, Nachname und Telefonnummer eingeben.");
            fehler.showAndWait();
            return;
        }

        try {
            this.dao.create(new Caregiver(firstName, surname, telephone));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        readAllAndShowInTableView();
        clearTextfields();
    }

    @FXML
    public void handleDelete() {
        Caregiver ausgewaehlterPfleger = this.tableView.getSelectionModel().getSelectedItem();
        if (ausgewaehlterPfleger == null) {
            return;
        }

        // Sicherheitsfrage bevor gelöscht wird (A_8)
        Alert bestaetigung = new Alert(Alert.AlertType.CONFIRMATION);
        bestaetigung.setTitle("Pfleger/in löschen");
        bestaetigung.setHeaderText("Wirklich löschen?");
        bestaetigung.setContentText(ausgewaehlterPfleger.getFirstName() + " " +
            ausgewaehlterPfleger.getSurname() + " wird endgültig aus der Datenbank entfernt.");

        Optional<ButtonType> antwort = bestaetigung.showAndWait();
        if (antwort.isPresent() && antwort.get() == ButtonType.OK) {
            try {
                this.dao.deleteById(ausgewaehlterPfleger.getCid());
                this.caregivers.remove(ausgewaehlterPfleger);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void clearTextfields() {
        this.textFieldFirstName.clear();
        this.textFieldSurname.clear();
        this.textFieldTelephone.clear();
    }
}
