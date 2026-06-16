package de.hitec.nhplus.datastorage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

import de.hitec.nhplus.logging.LogService;
import de.hitec.nhplus.model.Patient;
import de.hitec.nhplus.utils.DateConverter;

/**
 * Data Access Object (DAO) für die Verwaltung von Patientendaten in der Datenbank.
 */
public class PatientDao extends DaoImp<Patient> {

    /**
     * Erstellt eine neue Instanz des PatientDao mit der angegebenen Verbindung.
     *
     * @param connection Die SQL-Verbindung zur Datenbank.
     */
    public PatientDao(Connection connection) {
        super(connection);
    }

    /**
     * Erstellt einen neuen Patienten in der Datenbank und protokolliert die Aktion.
     *
     * @param patient Der neu anzulegende Patient.
     * @throws SQLException Bei Fehlern während des Datenbankzugriffs.
     */
    @Override
    public void create(Patient patient) throws SQLException {
        super.create(patient);

        String description = String.format(
            "Neuer Patient '%s, %s' (Geburtsdatum: %s, Pflegegrad: %s, Raum: %s) wurde angelegt.",
            patient.getSurname(), patient.getFirstName(), patient.getDateOfBirth(), patient.getCareLevel(), patient.getRoomNumber()
        );
        LogService.log("PATIENT_CREATE", description);
    }

    /**
     * Aktualisiert die Daten eines Patienten und protokolliert alle Änderungen im Detail.
     *
     * @param newPatient Das Objekt mit den aktualisierten Patientendaten.
     * @throws SQLException Bei Fehlern während des Datenbankzugriffs.
     */
    @Override
    public void update(Patient newPatient) throws SQLException {
        Patient oldPatient = this.read(newPatient.getPid());

        super.update(newPatient);

        if (oldPatient != null) {
            StringBuilder changes = new StringBuilder();

            if (!oldPatient.getFirstName().equals(newPatient.getFirstName())) {
                changes.append(String.format("Vorname: '%s' zu '%s'; ", 
                    oldPatient.getFirstName(), newPatient.getFirstName()));
            }
            if (!oldPatient.getSurname().equals(newPatient.getSurname())) {
                changes.append(String.format("Nachname: '%s' zu '%s'; ", 
                    oldPatient.getSurname(), newPatient.getSurname()));
            }
            if (!oldPatient.getDateOfBirth().equals(newPatient.getDateOfBirth())) {
                changes.append(String.format("Geburtsdatum: '%s' zu '%s'; ", 
                    oldPatient.getDateOfBirth(), newPatient.getDateOfBirth()));
            }
            if (!oldPatient.getCareLevel().equals(newPatient.getCareLevel())) {
                changes.append(String.format("Pflegegrad: '%s' zu '%s'; ", 
                    oldPatient.getCareLevel(), newPatient.getCareLevel()));
            }
            if (!oldPatient.getRoomNumber().equals(newPatient.getRoomNumber())) {
                changes.append(String.format("Raum: '%s' zu '%s'; ", 
                    oldPatient.getRoomNumber(), newPatient.getRoomNumber()));
            }
            if (!oldPatient.getAssets().equals(newPatient.getAssets())) {
                changes.append(String.format("Vermögensstand: '%s' zu '%s'; ", 
                    oldPatient.getAssets(), newPatient.getAssets()));
            }

            if (changes.length() > 0) {
                String logDescription = String.format("Patient (ID: %d) geändert. Details: %s", 
                    newPatient.getPid(), changes.toString());
                LogService.log("PATIENT_UPDATE", logDescription);
            }
        }
    }

    /**
     * Löscht einen Patienten anhand seiner ID und protokolliert den Löschvorgang.
     *
     * @param pid Die ID des zu löschenden Patienten.
     * @throws SQLException Bei Fehlern während des Datenbankzugriffs.
     */
    @Override
    public void deleteById(long pid) throws SQLException {
        Patient patientToDelete = this.read(pid);

        super.deleteById(pid);

        if (patientToDelete != null) {
            String description = String.format(
                "Patient %s, %s (PID: %d) wurde gelöscht. Letzte Daten: Pflegegrad: %s, Raum: %s",
                patientToDelete.getSurname(),
                patientToDelete.getFirstName(),
                patientToDelete.getPid(),
                patientToDelete.getCareLevel(),
                patientToDelete.getRoomNumber()
            );
            LogService.log("PATIENT_DELETE", description);
        }
    }

    /**
     * Erzeugt das SQL-Statement zum Einfügen eines neuen Patienten.
     *
     * @param patient Der einzufügende Patient.
     * @return Das vorbereitete PreparedStatement.
     */
    @Override
    protected PreparedStatement getCreateStatement(Patient patient) {
        PreparedStatement preparedStatement = null;
        try {
            final String SQL = "INSERT INTO patient (firstname, surname, dateOfBirth, carelevel, roomnumber, assets) VALUES (?, ?, ?, ?, ?, ?)";
            preparedStatement = this.connection.prepareStatement(SQL);
            preparedStatement.setString(1, patient.getFirstName());
            preparedStatement.setString(2, patient.getSurname());
            preparedStatement.setString(3, patient.getDateOfBirth());
            preparedStatement.setString(4, patient.getCareLevel());
            preparedStatement.setString(5, patient.getRoomNumber());
            preparedStatement.setString(6, patient.getAssets());
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return preparedStatement;
    }

    /**
     * Erzeugt das SQL-Statement zum Auslesen eines Patienten anhand seiner ID.
     *
     * @param pid Die ID des gesuchten Patienten.
     * @return Das vorbereitete PreparedStatement.
     */
    @Override
    protected PreparedStatement getReadByIDStatement(long pid) {
        PreparedStatement preparedStatement = null;
        try {
            final String SQL = "SELECT * FROM patient WHERE pid = ?";
            preparedStatement = this.connection.prepareStatement(SQL);
            preparedStatement.setLong(1, pid);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return preparedStatement;
    }

    /**
     * Erstellt ein Patienten-Objekt aus den Daten der aktuellen Zeile eines ResultSets.
     *
     * @param result Das ResultSet der Datenbankabfrage.
     * @return Das erzeugte Patient-Objekt.
     * @throws SQLException Bei Fehlern beim Lesen der Spalten.
     */
    @Override
    protected Patient getInstanceFromResultSet(ResultSet result) throws SQLException {
        Patient patient = new Patient(
                result.getInt("pid"),
                result.getString("firstname"),
                result.getString("surname"),
                DateConverter.convertStringToLocalDate(result.getString("dateOfBirth")),
                result.getString("carelevel"),
                result.getString("roomnumber"),
                result.getString("assets"));
        try {
            int archived = result.getInt("archived");
            patient.setArchived(archived == 1);
        } catch (SQLException ignored) {
        }
        try {
            String archiveDateStr = result.getString("archiveDate");
            if (archiveDateStr != null) {
                patient.setArchiveDate(DateConverter.convertStringToLocalDate(archiveDateStr));
            }
        } catch (SQLException ignored) {
        }
        return patient;
    }

    /**
     * Erzeugt das SQL-Statement zum Auslesen aller Patienten.
     *
     * @return Das vorbereitete PreparedStatement.
     */
    @Override
    protected PreparedStatement getReadAllStatement() {
        PreparedStatement statement = null;
        try {
            final String SQL = "SELECT * FROM patient WHERE archived = 0";
            statement = this.connection.prepareStatement(SQL);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return statement;
    }

    /**
     * Transformiert ein ResultSet in eine Liste von Patienten-Objekten.
     *
     * @param result Das ResultSet mit den Datensätzen.
     * @return Eine Liste aller ausgelesenen Patienten.
     * @throws SQLException Bei Fehlern während der Iteration oder des Auslesens.
     */
    @Override
    protected ArrayList<Patient> getListFromResultSet(ResultSet result) throws SQLException {
        ArrayList<Patient> list = new ArrayList<>();
        while (result.next()) {
            LocalDate date = DateConverter.convertStringToLocalDate(result.getString("dateOfBirth"));
            Patient patient = new Patient(
                    result.getInt("pid"),
                    result.getString("firstname"),
                    result.getString("surname"),
                    date,
                    result.getString("carelevel"),
                    result.getString("roomnumber"),
                    result.getString("assets"));
            try {
                int archived = result.getInt("archived");
                patient.setArchived(archived == 1);
            } catch (SQLException ignored) {
            }
            try {
                String archiveDateStr = result.getString("archiveDate");
                if (archiveDateStr != null) {
                    patient.setArchiveDate(DateConverter.convertStringToLocalDate(archiveDateStr));
                }
            } catch (SQLException ignored) {
            }
            list.add(patient);
        }
        return list;
    }

    public ArrayList<Patient> readAllArchived() throws SQLException {
        final String SQL = "SELECT * FROM patient WHERE archived = 1";
        try (PreparedStatement statement = this.connection.prepareStatement(SQL);
             ResultSet result = statement.executeQuery()) {
            return getListFromResultSet(result);
        }
    }

    public void archive(long pid) throws SQLException {
        final String SQL = "UPDATE patient SET archived = 1, archiveDate = ? WHERE pid = ?";
        try (PreparedStatement statement = this.connection.prepareStatement(SQL)) {
            statement.setString(1, java.time.LocalDate.now().toString());
            statement.setLong(2, pid);
            statement.executeUpdate();
        }
        LogService.log("PATIENT_ARCHIVE", String.format("Patient (PID: %d) archiviert.", pid));
    }

    public void reactivate(long pid) throws SQLException {
        final String SQL = "UPDATE patient SET archived = 0, archiveDate = NULL WHERE pid = ?";
        try (PreparedStatement statement = this.connection.prepareStatement(SQL)) {
            statement.setLong(1, pid);
            statement.executeUpdate();
        }
        LogService.log("PATIENT_REACTIVATE", String.format("Patient (PID: %d) reaktiviert.", pid));
    }

    /**
     * Erzeugt das SQL-Statement zur Aktualisierung eines bestehenden Patienten.
     *
     * @param patient Der zu aktualisierende Patient.
     * @return Das vorbereitete PreparedStatement.
     */
    @Override
    protected PreparedStatement getUpdateStatement(Patient patient) {
        PreparedStatement preparedStatement = null;
        try {
            final String SQL = "UPDATE patient SET firstname = ?, surname = ?, dateOfBirth = ?, carelevel = ?, roomnumber = ?, assets = ? WHERE pid = ?";
            preparedStatement = this.connection.prepareStatement(SQL);
            preparedStatement.setString(1, patient.getFirstName());
            preparedStatement.setString(2, patient.getSurname());
            preparedStatement.setString(3, patient.getDateOfBirth());
            preparedStatement.setString(4, patient.getCareLevel());
            preparedStatement.setString(5, patient.getRoomNumber());
            preparedStatement.setString(6, patient.getAssets());
            preparedStatement.setLong(7, patient.getPid());
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return preparedStatement;
    }

    /**
     * Ermittelt das Datum und die Uhrzeit der letzten Behandlung eines Patienten.
     *
     * @param pid Die ID des Patienten.
     * @return Ein String mit Datum und Beginn der Behandlung oder "-" falls keine existiert.
     * @throws SQLException Bei Fehlern während der Datenbankabfrage.
     */
    public String getLastTreatmentDate(long pid) throws SQLException {
        final String SQL = "SELECT treatment_date || ', ' || begin as letzte FROM treatment WHERE pid = ? ORDER BY treatment_date DESC, begin DESC LIMIT 1";
        try (PreparedStatement statement = this.connection.prepareStatement(SQL)) {
            statement.setLong(1, pid);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return result.getString("letzte");
            }
        }
        return "-";
    }

    /**
     * Erzeugt das SQL-Statement zum Löschen eines Patienten anhand seiner ID.
     *
     * @param pid Die ID des zu löschenden Patienten.
     * @return Das vorbereitete PreparedStatement.
     */
    @Override
    protected PreparedStatement getDeleteStatement(long pid) {
        PreparedStatement preparedStatement = null;
        try {
            final String SQL = "DELETE FROM patient WHERE pid = ?";
            preparedStatement = this.connection.prepareStatement(SQL);
            preparedStatement.setLong(1, pid);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return preparedStatement;
    }
}