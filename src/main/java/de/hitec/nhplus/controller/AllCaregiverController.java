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

/**
 * Controller for the caregiver overview ({@code AllCaregiverView.fxml}).
 *
 * <p>Implements the four CRUD use cases of the caregiver module: it shows all
 * caregivers in an editable table, adds new caregivers from the input fields,
 * saves in-place edits and deletes a selected caregiver after a confirmation
 * prompt. Input validation (mandatory fields) and the confirmation dialog cover
 * the acceptance criteria; all persistence goes through {@link CaregiverDao}.</p>
 *
 * <p>Single responsibility: presentation and user interaction for caregivers.
 * No SQL lives here &mdash; the controller talks only to the DAO, keeping the
 * MVP layers loosely coupled.</p>
 */
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

    /**
     * Initialises the table columns, makes the name and telephone columns
     * editable, binds the data and enables the delete button only while a row is
     * selected. Called automatically by JavaFX after the FXML is loaded.
     */
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

    /**
     * Reloads all caregivers from the database and shows them in the table.
     */
    private void readAllAndShowInTableView() {
        this.caregivers.clear();
        this.dao = DaoFactory.getDaoFactory().createCaregiverDao();
        try {
            this.caregivers.addAll(this.dao.readAll());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Event handler for editing the first name directly in the table.
     *
     * @param event the cell-edit event carrying the row and the new value
     */
    @FXML
    public void handleOnEditFirstname(TableColumn.CellEditEvent<Caregiver, String> event) {
        event.getRowValue().setFirstName(event.getNewValue());
        doUpdate(event);
    }

    /**
     * Event handler for editing the surname directly in the table.
     *
     * @param event the cell-edit event carrying the row and the new value
     */
    @FXML
    public void handleOnEditSurname(TableColumn.CellEditEvent<Caregiver, String> event) {
        event.getRowValue().setSurname(event.getNewValue());
        doUpdate(event);
    }

    /**
     * Event handler for editing the telephone number directly in the table.
     *
     * @param event the cell-edit event carrying the row and the new value
     */
    @FXML
    public void handleOnEditTelephone(TableColumn.CellEditEvent<Caregiver, String> event) {
        event.getRowValue().setTelephone(event.getNewValue());
        doUpdate(event);
    }

    /**
     * Persists the edited caregiver of a table cell-edit event through the DAO.
     *
     * @param event the cell-edit event whose row value is saved
     */
    private void doUpdate(TableColumn.CellEditEvent<Caregiver, String> event) {
        try {
            this.dao.update(event.getRowValue());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Event handler for the "add" button. Reads the input fields, rejects empty
     * mandatory fields with an error dialog, otherwise creates the caregiver via
     * the DAO, refreshes the table and clears the input fields.
     */
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

    /**
     * Event handler for the "delete" button. Asks for confirmation and, if
     * confirmed, deletes the selected caregiver via the DAO and removes it from
     * the table. Does nothing if no caregiver is selected.
     */
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

    /**
     * Clears the first name, surname and telephone input fields.
     */
    private void clearTextfields() {
        this.textFieldFirstName.clear();
        this.textFieldSurname.clear();
        this.textFieldTelephone.clear();
    }
}
