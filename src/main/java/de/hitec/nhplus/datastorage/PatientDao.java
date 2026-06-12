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
 * Implements the Interface <code>DaoImp</code>. Overrides methods to generate specific <code>PreparedStatements</code>,
 * to execute the specific SQL Statements.
 */
public class PatientDao extends DaoImp<Patient> {

    /**
     * The constructor initiates an object of <code>PatientDao</code> and passes the connection to its super class.
     *
     * @param connection Object of <code>Connection</code> to execute the SQL-statements.
     */
    public PatientDao(Connection connection) {
        super(connection);
    }

    /**
     * Intercepts the update process to log precise changes ("Before -> After") 
     * before delegating the actual execution to the superclass.
     */
    @Override
    public void update(Patient newPatient) throws SQLException {
        // 1. Vor dem Update: Den alten Zustand frisch aus der DB lesen
        Patient oldPatient = this.read(newPatient.getPid());

        // 2. Das eigentliche SQL-Update über die Superklasse ausführen
        super.update(newPatient);

        // 3. Nach erfolgreichem Update: Werte vergleichen und Log-Text bauen
        if (oldPatient != null) {
            StringBuilder changes = new StringBuilder();

            if (!oldPatient.getFirstName().equals(newPatient.getFirstName())) {
                changes.append(String.format("Vorname: '%s' ➡️ '%s'; ", 
                    oldPatient.getFirstName(), newPatient.getFirstName()));
            }
            if (!oldPatient.getSurname().equals(newPatient.getSurname())) {
                changes.append(String.format("Nachname: '%s' ➡️ '%s'; ", 
                    oldPatient.getSurname(), newPatient.getSurname()));
            }
            if (!oldPatient.getDateOfBirth().equals(newPatient.getDateOfBirth())) {
                changes.append(String.format("Geburtsdatum: '%s' ➡️ '%s'; ", 
                    oldPatient.getDateOfBirth(), newPatient.getDateOfBirth()));
            }
            if (!oldPatient.getCareLevel().equals(newPatient.getCareLevel())) {
                changes.append(String.format("Pflegegrad: '%s' ➡️ '%s'; ", 
                    oldPatient.getCareLevel(), newPatient.getCareLevel()));
            }
            if (!oldPatient.getRoomNumber().equals(newPatient.getRoomNumber())) {
                changes.append(String.format("Raum: '%s' ➡️ '%s'; ", 
                    oldPatient.getRoomNumber(), newPatient.getRoomNumber()));
            }
            if (!oldPatient.getAssets().equals(newPatient.getAssets())) {
                changes.append(String.format("Vermögensstand: '%s' ➡️ '%s'; ", 
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
     * Overrides the delete process to log which patient was deleted.
     * 
     * @param pid ID of the patient to be deleted.
     */
    @Override
    public void deleteById(long pid) throws SQLException {
        // 1. Vor dem Löschen Daten lesen
        Patient patientToDelete = this.read(pid);

        // 2. Löschen ausführen
        super.deleteById(pid);

        // 3. Log schreiben
        if (patientToDelete != null) {
            String description = String.format(
                "Patient %s, %s (PID: %d) wurde gelöscht. Letzte Daten: Pflegegrad: %s, Raum: %s",
                patientToDelete.getSurname(),
                patientToDelete.getFirstName(),
                patientToDelete.getPid(),
                patientToDelete.getCareLevel(),
                patientToDelete.getRoomNumber()
            );
            
            // Nutzt den LogService analog zum Update
            LogService.log("PATIENT_DELETE", description);
        }
    }

    @Override
    protected PreparedStatement getCreateStatement(Patient patient) {
        PreparedStatement preparedStatement = null;
        try {
            final String SQL = "INSERT INTO patient (firstname, surname, dateOfBirth, carelevel, roomnumber, assets) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
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

    @Override
    protected Patient getInstanceFromResultSet(ResultSet result) throws SQLException {
        return new Patient(
                result.getInt("pid"),
                result.getString("firstname"),
                result.getString("surname"),
                DateConverter.convertStringToLocalDate(result.getString("dateOfBirth")),
                result.getString("carelevel"),
                result.getString("roomnumber"),
                result.getString("assets"));
    }

    @Override
    protected PreparedStatement getReadAllStatement() {
        PreparedStatement statement = null;
        try {
            final String SQL = "SELECT * FROM patient";
            statement = this.connection.prepareStatement(SQL);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return statement;
    }

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
            list.add(patient);
        }
        return list;
    }

    @Override
    protected PreparedStatement getUpdateStatement(Patient patient) {
        PreparedStatement preparedStatement = null;
        try {
            final String SQL =
                    "UPDATE patient SET " +
                            "firstname = ?, " +
                            "surname = ?, " +
                            "dateOfBirth = ?, " +
                            "carelevel = ?, " +
                            "roomnumber = ?, " +
                            "assets = ? " +
                            "WHERE pid = ?";
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