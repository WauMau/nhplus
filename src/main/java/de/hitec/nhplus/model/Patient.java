package de.hitec.nhplus.model;

import de.hitec.nhplus.utils.DateConverter;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.ObjectProperty;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Patient extends Person {
    private SimpleLongProperty pid;
    private final SimpleStringProperty dateOfBirth;
    private final SimpleStringProperty careLevel;
    private final SimpleStringProperty roomNumber;
    private final SimpleStringProperty telephone;
    private final SimpleStringProperty lastTreatmentDate;
    private final List<Treatment> allTreatments = new ArrayList<>();
    private final SimpleBooleanProperty archived;
    private final SimpleObjectProperty<LocalDate> archiveDate;

    public Patient(String firstName, String surname, LocalDate dateOfBirth, String careLevel, String roomNumber) {
        this(firstName, surname, dateOfBirth, careLevel, roomNumber, "");
    }

    public Patient(String firstName, String surname, LocalDate dateOfBirth, String careLevel, String roomNumber, String telephone) {
        super(firstName, surname);
        this.pid = new SimpleLongProperty(0);
        this.dateOfBirth = new SimpleStringProperty(DateConverter.convertLocalDateToString(dateOfBirth));
        this.careLevel = new SimpleStringProperty(careLevel);
        this.roomNumber = new SimpleStringProperty(roomNumber);
        this.telephone = new SimpleStringProperty(telephone);
        this.lastTreatmentDate = new SimpleStringProperty("-");
        this.archived = new SimpleBooleanProperty(false);
        this.archiveDate = new SimpleObjectProperty<>(null);
    }

    public Patient(long pid, String firstName, String surname, LocalDate dateOfBirth, String careLevel, String roomNumber) {
        this(pid, firstName, surname, dateOfBirth, careLevel, roomNumber, "");
    }

    public Patient(long pid, String firstName, String surname, LocalDate dateOfBirth, String careLevel, String roomNumber, String telephone) {
        super(firstName, surname);
        this.pid = new SimpleLongProperty(pid);
        this.dateOfBirth = new SimpleStringProperty(DateConverter.convertLocalDateToString(dateOfBirth));
        this.careLevel = new SimpleStringProperty(careLevel);
        this.roomNumber = new SimpleStringProperty(roomNumber);
        this.telephone = new SimpleStringProperty(telephone);
        this.lastTreatmentDate = new SimpleStringProperty("-");
        this.archived = new SimpleBooleanProperty(false);
        this.archiveDate = new SimpleObjectProperty<>(null);
    }

    public long getPid() {
        return pid.get();
    }

    public SimpleLongProperty pidProperty() {
        return pid;
    }

    public String getDateOfBirth() {
        return dateOfBirth.get();
    }

    public SimpleStringProperty dateOfBirthProperty() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth.set(dateOfBirth);
    }

    public String getCareLevel() {
        return careLevel.get();
    }

    public SimpleStringProperty careLevelProperty() {
        return careLevel;
    }

    public void setCareLevel(String careLevel) {
        this.careLevel.set(careLevel);
    }

    public String getRoomNumber() {
        return roomNumber.get();
    }

    public SimpleStringProperty roomNumberProperty() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber.set(roomNumber);
    }

    public String getTelephone() {
        return telephone.get();
    }

    public SimpleStringProperty telephoneProperty() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone.set(telephone);
    }

    public String getLastTreatmentDate() {
        return lastTreatmentDate.get();
    }

    public SimpleStringProperty lastTreatmentDateProperty() {
        return lastTreatmentDate;
    }

    public void setLastTreatmentDate(String lastTreatmentDate) {
        this.lastTreatmentDate.set(lastTreatmentDate);
    }

    public boolean isArchived() {
        return archived.get();
    }

    public SimpleBooleanProperty archivedProperty() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived.set(archived);
    }

    public LocalDate getArchiveDate() {
        return archiveDate.get();
    }

    public ObjectProperty<LocalDate> archiveDateProperty() {
        return archiveDate;
    }

    public void setArchiveDate(LocalDate date) {
        this.archiveDate.set(date);
    }

    public boolean add(Treatment treatment) {
        if (this.allTreatments.contains(treatment)) {
            return false;
        }
        this.allTreatments.add(treatment);
        return true;
    }

    public String toString() {
        return "Patient" + "\nMNID: " + this.pid +
                "\nFirstname: " + this.getFirstName() +
                "\nSurname: " + this.getSurname() +
                "\nBirthday: " + this.dateOfBirth +
                "\nCarelevel: " + this.careLevel +
                "\nRoomnumber: " + this.roomNumber +
                "\nTelephone: " + this.telephone +
                "\n";
    }
}
