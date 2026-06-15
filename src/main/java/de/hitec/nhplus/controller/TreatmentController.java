package de.hitec.nhplus.controller;

import de.hitec.nhplus.datastorage.CaregiverDao;
import de.hitec.nhplus.datastorage.DaoFactory;
import de.hitec.nhplus.datastorage.PatientDao;
import de.hitec.nhplus.datastorage.TreatmentDao;
import de.hitec.nhplus.model.Caregiver;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import de.hitec.nhplus.model.Patient;
import de.hitec.nhplus.model.Treatment;
import de.hitec.nhplus.utils.DateConverter;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TreatmentController {

    @FXML
    private Label labelPatientName;

    @FXML
    private Label labelCareLevel;

    @FXML
    private ComboBox<String> comboBoxCaregiver;

    @FXML
    private Label labelCaregiverTelephone;

    @FXML
    private TextField textFieldBegin;

    @FXML
    private TextField textFieldEnd;

    @FXML
    private TextField textFieldDescription;

    @FXML
    private TextArea textAreaRemarks;

    @FXML
    private DatePicker datePicker;

    private AllTreatmentController controller;
    private Stage stage;
    private Patient patient;
    private Treatment treatment;
    private List<Caregiver> caregiverListe;

    public void initializeController(AllTreatmentController controller, Stage stage, Treatment treatment) {
        this.stage = stage;
        this.controller = controller;
        PatientDao pDao = DaoFactory.getDaoFactory().createPatientDao();
        try {
            this.patient = pDao.read(treatment.getPid());
            this.treatment = treatment;
            ladePflegerInCombobox();
            showData();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    // Lädt alle Pfleger in die ComboBox und setzt einen Listener für die Telefon-Anzeige
    private void ladePflegerInCombobox() {
        caregiverListe = new ArrayList<>();
        ObservableList<String> items = FXCollections.observableArrayList();
        items.add("kein Pfleger");

        CaregiverDao dao = DaoFactory.getDaoFactory().createCaregiverDao();
        try {
            caregiverListe = dao.readAll();
            for (Caregiver c : caregiverListe) {
                items.add(c.getFirstName() + " " + c.getSurname());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        comboBoxCaregiver.setItems(items);

        // Wenn ein anderer Pfleger gewählt wird, die Telefonnummer sofort aktualisieren
        comboBoxCaregiver.getSelectionModel().selectedIndexProperty().addListener(
            (observable, altIndex, neuerIndex) -> aktualisiereTelefon(neuerIndex.intValue())
        );
    }

    private void aktualisiereTelefon(int index) {
        if (index <= 0 || caregiverListe == null || index > caregiverListe.size()) {
            labelCaregiverTelephone.setText("-");
        } else {
            labelCaregiverTelephone.setText(caregiverListe.get(index - 1).getTelephone());
        }
    }

    private void showData() {
        this.labelPatientName.setText(patient.getSurname() + ", " + patient.getFirstName());
        this.labelCareLevel.setText(patient.getCareLevel());
        LocalDate date = DateConverter.convertStringToLocalDate(treatment.getDate());
        this.datePicker.setValue(date);
        this.textFieldBegin.setText(this.treatment.getBegin());
        this.textFieldEnd.setText(this.treatment.getEnd());
        this.textFieldDescription.setText(this.treatment.getDescription());
        this.textAreaRemarks.setText(this.treatment.getRemarks());

        // Aktuell zugewiesenen Pfleger in der ComboBox vorauswählen
        long aktuelleId = treatment.getCaregiverId();
        int vorausgewaehlt = 0;
        for (int i = 0; i < caregiverListe.size(); i++) {
            if (caregiverListe.get(i).getCid() == aktuelleId) {
                vorausgewaehlt = i + 1; // +1 wegen "kein Pfleger" an Index 0
                break;
            }
        }
        comboBoxCaregiver.getSelectionModel().select(vorausgewaehlt);
    }

    @FXML
    public void handleChange() {
        this.treatment.setDate(this.datePicker.getValue().toString());
        this.treatment.setBegin(textFieldBegin.getText());
        this.treatment.setEnd(textFieldEnd.getText());
        this.treatment.setDescription(textFieldDescription.getText());
        this.treatment.setRemarks(textAreaRemarks.getText());

        // Ausgewählten Pfleger speichern
        int selectedIndex = comboBoxCaregiver.getSelectionModel().getSelectedIndex();
        long caregiverId = 0;
        if (selectedIndex > 0 && caregiverListe != null && selectedIndex <= caregiverListe.size()) {
            caregiverId = caregiverListe.get(selectedIndex - 1).getCid();
        }
        this.treatment.setCaregiverId(caregiverId);

        doUpdate();
        controller.readAllAndShowInTableView();
        stage.close();
    }

    private void doUpdate() {
        TreatmentDao dao = DaoFactory.getDaoFactory().createTreatmentDao();
        try {
            dao.update(treatment);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @FXML
    public void handleCancel() {
        stage.close();
    }
}
