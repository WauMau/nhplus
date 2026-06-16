package de.hitec.nhplus.datastorage;

import de.hitec.nhplus.logging.LogService;
import de.hitec.nhplus.model.Treatment;
import de.hitec.nhplus.utils.DateConverter;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements the Interface <code>DaoImp</code>. Overrides methods to generate specific <code>PreparedStatements</code>,
 * to execute the specific SQL Statements.
 */
public class TreatmentDao extends DaoImp<Treatment> {

    /**
     * The constructor initiates an object of <code>TreatmentDao</code> and passes the connection to its super class.
     *
     * @param connection Object of <code>Connection</code> to execute the SQL-statements.
     */
    public TreatmentDao(Connection connection) {
        super(connection);
    }

    /**
     * Intercepts the creation process to log when a new treatment is added.
     */
    @Override
    public void create(Treatment treatment) throws SQLException {
        super.create(treatment);

        String description = String.format("Neue Behandlung für Patient (PID: %d) am %s angelegt. Kurzbeschreibung: '%s'",
                treatment.getPid(), treatment.getDate(), treatment.getDescription());
        LogService.log("TREATMENT_CREATE", description);
    }

    /**
     * Intercepts the update process to log precise changes ("Before -> After")
     * including caregiver assignments before delegating execution to the superclass.
     */
    @Override
    public void update(Treatment newTreatment) throws SQLException {
        Treatment oldTreatment = this.read(newTreatment.getTid());
        super.update(newTreatment);

        if (oldTreatment != null) {
            StringBuilder changes = new StringBuilder();

            if (!oldTreatment.getDate().equals(newTreatment.getDate())) {
                changes.append(String.format("Datum: '%s' → '%s'; ", 
                        oldTreatment.getDate(), newTreatment.getDate()));
            }

            if (!oldTreatment.getBegin().equals(newTreatment.getBegin())) {
                changes.append(String.format("Beginn: '%s' → '%s'; ", 
                        oldTreatment.getBegin(), newTreatment.getBegin()));
            }

            if (!oldTreatment.getEnd().equals(newTreatment.getEnd())) {
                changes.append(String.format("Ende: '%s' → '%s'; ", 
                        oldTreatment.getEnd(), newTreatment.getEnd()));
            }

            if (!oldTreatment.getDescription().equals(newTreatment.getDescription())) {
                changes.append(String.format("Beschreibung: '%s' → '%s'; ", 
                        oldTreatment.getDescription(), newTreatment.getDescription()));
            }

            if (!oldTreatment.getRemarks().equals(newTreatment.getRemarks())) {
                changes.append(String.format("Bemerkung: '%s' → '%s'; ", 
                        oldTreatment.getRemarks(), newTreatment.getRemarks()));
            }

            if (oldTreatment.getCaregiverId() != newTreatment.getCaregiverId()) {
                String oldCaregiver = (oldTreatment.getCaregiverName() != null && !oldTreatment.getCaregiverName().equals("-")) 
                        ? String.format("%s (ID: %d)", oldTreatment.getCaregiverName(), oldTreatment.getCaregiverId()) 
                        : "Kein Pfleger";
                
                String newCaregiver = (newTreatment.getCaregiverName() != null && !newTreatment.getCaregiverName().equals("-")) 
                        ? String.format("%s (ID: %d)", newTreatment.getCaregiverName(), newTreatment.getCaregiverId()) 
                        : String.format("Pfleger (ID: %d)", newTreatment.getCaregiverId());

                changes.append(String.format("Zuweisung Pfleger: '%s' → '%s'; ", oldCaregiver, newCaregiver));
            }

            if (changes.length() > 0) {
                String logDescription = String.format("Behandlung (TID: %d) für Patient (PID: %d) geändert. Details: %s", 
                        newTreatment.getTid(), newTreatment.getPid(), changes.toString());
                
                LogService.log("TREATMENT_UPDATE", logDescription);
            }
        }
    }

    /**
     * Overrides the delete process to log which treatment was deleted.
     */
    @Override
    public void deleteById(long tid) throws SQLException {
        Treatment treatmentToDelete = this.read(tid);
        super.deleteById(tid);

        if (treatmentToDelete != null) {
            String description = String.format(
                "Behandlung (TID: %d) für Patient (PID: %d) am %s komplett gelöscht. Letzte Daten: '%s'",
                treatmentToDelete.getTid(),
                treatmentToDelete.getPid(),
                treatmentToDelete.getDate(),
                treatmentToDelete.getDescription()
            );
            
            LogService.log("TREATMENT_DELETE", description);
        }
    }

    /**
     * Generates a <code>PreparedStatement</code> to persist the given object of <code>Treatment</code>.
     */
    @Override
    protected PreparedStatement getCreateStatement(Treatment treatment) {
        PreparedStatement preparedStatement = null;
        try {
            final String SQL = "INSERT INTO treatment (pid, treatment_date, begin, end, description, remark, caregiverId) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
            preparedStatement = this.connection.prepareStatement(SQL);
            preparedStatement.setLong(1, treatment.getPid());
            preparedStatement.setString(2, treatment.getDate());
            preparedStatement.setString(3, treatment.getBegin());
            preparedStatement.setString(4, treatment.getEnd());
            preparedStatement.setString(5, treatment.getDescription());
            preparedStatement.setString(6, treatment.getRemarks());
            preparedStatement.setLong(7, treatment.getCaregiverId());
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return preparedStatement;
    }

    /**
     * Generates a <code>PreparedStatement</code> to query a treatment by a given treatment id (tid).
     */
    @Override
    protected PreparedStatement getReadByIDStatement(long tid) {
        PreparedStatement preparedStatement = null;
        try {
            final String SQL =
                "SELECT t.tid, t.pid, t.treatment_date, t.begin, t.end, t.description, t.remark, " +
                "COALESCE(t.caregiverId, 0) as caregiverId, " +
                "COALESCE(p.firstname || ' ' || p.surname, '-') as caregiver_name, " +
                "COALESCE(p.telephone, '-') as caregiver_telephone " +
                "FROM treatment t LEFT JOIN pfleger p ON t.caregiverId = p.cid " +
                "WHERE t.tid = ?";
            preparedStatement = this.connection.prepareStatement(SQL);
            preparedStatement.setLong(1, tid);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return preparedStatement;
    }

    /**
     * Maps a <code>ResultSet</code> of one treatment to an object of <code>Treatment</code>.
     */
    @Override
    protected Treatment getInstanceFromResultSet(ResultSet result) throws SQLException {
        LocalDate date = DateConverter.convertStringToLocalDate(result.getString("treatment_date"));
        LocalTime begin = DateConverter.convertStringToLocalTime(result.getString("begin"));
        LocalTime end = DateConverter.convertStringToLocalTime(result.getString("end"));
        return new Treatment(result.getLong("tid"), result.getLong("pid"),
                date, begin, end, result.getString("description"), result.getString("remark"),
                result.getLong("caregiverId"), result.getString("caregiver_name"), result.getString("caregiver_telephone"));
    }

    /**
     * Generates a <code>PreparedStatement</code> to query all treatments.
     */
    @Override
    protected PreparedStatement getReadAllStatement() {
        PreparedStatement statement = null;
        try {
            final String SQL =
                "SELECT t.tid, t.pid, t.treatment_date, t.begin, t.end, t.description, t.remark, " +
                "COALESCE(t.caregiverId, 0) as caregiverId, " +
                "COALESCE(p.firstname || ' ' || p.surname, '-') as caregiver_name, " +
                "COALESCE(p.telephone, '-') as caregiver_telephone " +
                "FROM treatment t LEFT JOIN pfleger p ON t.caregiverId = p.cid";
            statement = this.connection.prepareStatement(SQL);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return statement;
    }

    /**
     * Maps a <code>ResultSet</code> of all treatments to an <code>ArrayList</code> with objects of class
     * <code>Treatment</code>.
     */
    @Override
    protected ArrayList<Treatment> getListFromResultSet(ResultSet result) throws SQLException {
        ArrayList<Treatment> list = new ArrayList<Treatment>();
        while (result.next()) {
            LocalDate date = DateConverter.convertStringToLocalDate(result.getString("treatment_date"));
            LocalTime begin = DateConverter.convertStringToLocalTime(result.getString("begin"));
            LocalTime end = DateConverter.convertStringToLocalTime(result.getString("end"));
            Treatment treatment = new Treatment(result.getLong("tid"), result.getLong("pid"),
                    date, begin, end, result.getString("description"), result.getString("remark"),
                    result.getLong("caregiverId"), result.getString("caregiver_name"), result.getString("caregiver_telephone"));
            list.add(treatment);
        }
        return list;
    }

    /**
     * Generates a <code>PreparedStatement</code> to query all treatments of a patient with a given patient id (pid).
     */
    private PreparedStatement getReadAllTreatmentsOfOnePatientByPid(long pid) {
        PreparedStatement preparedStatement = null;
        try {
            final String SQL =
                "SELECT t.tid, t.pid, t.treatment_date, t.begin, t.end, t.description, t.remark, " +
                "COALESCE(t.caregiverId, 0) as caregiverId, " +
                "COALESCE(p.firstname || ' ' || p.surname, '-') as caregiver_name, " +
                "COALESCE(p.telephone, '-') as caregiver_telephone " +
                "FROM treatment t LEFT JOIN pfleger p ON t.caregiverId = p.cid " +
                "WHERE t.pid = ?";
            preparedStatement = this.connection.prepareStatement(SQL);
            preparedStatement.setLong(1, pid);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return preparedStatement;
    }

    /**
     * Queries all treatments of a given patient id (pid) and maps the results to an <code>ArrayList</code> with
     * objects of class <code>Treatment</code>.
     */
    public List<Treatment> readTreatmentsByPid(long pid) throws SQLException {
        try (PreparedStatement statement = getReadAllTreatmentsOfOnePatientByPid(pid);
             ResultSet result = statement.executeQuery()) {
            return getListFromResultSet(result);
        }
    }

    /**
     * Generates a <code>PreparedStatement</code> to update the given treatment, identified
     * by the id of the treatment (tid).
     */
    @Override
    protected PreparedStatement getUpdateStatement(Treatment treatment) {
        PreparedStatement preparedStatement = null;
        try {
            final String SQL =
                    "UPDATE treatment SET " +
                            "pid = ?, " +
                            "treatment_date = ?, " +
                            "begin = ?, " +
                            "end = ?, " +
                            "description = ?, " +
                            "remark = ?, " +
                            "caregiverId = ? " +
                            "WHERE tid = ?";
            preparedStatement = this.connection.prepareStatement(SQL);
            preparedStatement.setLong(1, treatment.getPid());
            preparedStatement.setString(2, treatment.getDate());
            preparedStatement.setString(3, treatment.getBegin());
            preparedStatement.setString(4, treatment.getEnd());
            preparedStatement.setString(5, treatment.getDescription());
            preparedStatement.setString(6, treatment.getRemarks());
            preparedStatement.setLong(7, treatment.getCaregiverId());
            preparedStatement.setLong(8, treatment.getTid());
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return preparedStatement;
    }

    /**
     * Generates a <code>PreparedStatement</code> to delete a treatment with the given id.
     */
    @Override
    protected PreparedStatement getDeleteStatement(long tid) {
        PreparedStatement preparedStatement = null;
        try {
            final String SQL =
                    "DELETE FROM treatment WHERE tid = ?";
            preparedStatement = this.connection.prepareStatement(SQL);
            preparedStatement.setLong(1, tid);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return preparedStatement;
    }
}