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

public class PatientDao extends DaoImp<Patient> {

    public PatientDao(Connection connection) {
        super(connection);
    }

    @Override
    public void create(Patient patient) throws SQLException {
        super.create(patient);

        String description = String.format(
            "Neuer Patient '%s, %s' (Geburtsdatum: %s, Pflegegrad: %s, Raum: %s) wurde angelegt.",
            patient.getSurname(), patient.getFirstName(), patient.getDateOfBirth(), patient.getCareLevel(), patient.getRoomNumber()
        );
        LogService.log("PATIENT_CREATE", description);
    }

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

            if (changes.length() > 0) {
                String logDescription = String.format("Patient (ID: %d) geändert. Details: %s",
                    newPatient.getPid(), changes.toString());
                LogService.log("PATIENT_UPDATE", logDescription);
            }
        }
    }

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

    @Override
    protected PreparedStatement getCreateStatement(Patient patient) {
        PreparedStatement preparedStatement = null;
        try {
            final String SQL = "INSERT INTO patient (firstname, surname, dateOfBirth, carelevel, roomnumber) VALUES (?, ?, ?, ?, ?)";
            preparedStatement = this.connection.prepareStatement(SQL);
            preparedStatement.setString(1, patient.getFirstName());
            preparedStatement.setString(2, patient.getSurname());
            preparedStatement.setString(3, patient.getDateOfBirth());
            preparedStatement.setString(4, patient.getCareLevel());
            preparedStatement.setString(5, patient.getRoomNumber());
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
        Patient patient = new Patient(
                result.getInt("pid"),
                result.getString("firstname"),
                result.getString("surname"),
                DateConverter.convertStringToLocalDate(result.getString("dateOfBirth")),
                result.getString("carelevel"),
                result.getString("roomnumber"));
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
                    result.getString("roomnumber"));
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

    @Override
    protected PreparedStatement getUpdateStatement(Patient patient) {
        PreparedStatement preparedStatement = null;
        try {
            final String SQL = "UPDATE patient SET firstname = ?, surname = ?, dateOfBirth = ?, carelevel = ?, roomnumber = ? WHERE pid = ?";
            preparedStatement = this.connection.prepareStatement(SQL);
            preparedStatement.setString(1, patient.getFirstName());
            preparedStatement.setString(2, patient.getSurname());
            preparedStatement.setString(3, patient.getDateOfBirth());
            preparedStatement.setString(4, patient.getCareLevel());
            preparedStatement.setString(5, patient.getRoomNumber());
            preparedStatement.setLong(6, patient.getPid());
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return preparedStatement;
    }

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
