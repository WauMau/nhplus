package de.hitec.nhplus.datastorage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import de.hitec.nhplus.logging.LogService;
import de.hitec.nhplus.model.Caregiver;

/**
 * Data Access Object for {@link Caregiver} records in the {@code pfleger} table.
 *
 * <p>Provides the SQL building blocks required by the generic {@link DaoImp}
 * template (create / read / readAll / update / delete) and additionally overrides
 * {@link #create(Caregiver)}, {@link #update(Caregiver)} and
 * {@link #deleteById(long)} to write audit entries via
 * {@link LogService} &mdash; including a field-by-field "before &rarr; after"
 * description on update.</p>
 *
 * <p>Single responsibility: persistence of caregivers plus the related audit
 * logging. By reusing {@link DaoImp} it avoids duplicating the generic CRUD
 * boilerplate (DRY).</p>
 */
public class CaregiverDao extends DaoImp<Caregiver> {

    /**
     * Creates the DAO for the given database connection.
     *
     * @param connection the database connection to use
     */
    public CaregiverDao(Connection connection) {
        super(connection);
    }

    /**
     * Inserts a new caregiver and writes a {@code CAREGIVER_CREATE} audit entry.
     *
     * @param caregiver the caregiver to persist
     * @throws SQLException if the insert fails
     */
    @Override
    public void create(Caregiver caregiver) throws SQLException {
        super.create(caregiver);
        String description = String.format(
            "Neue Pflegekraft '%s %s' (Telefon: %s) wurde angelegt.",
            caregiver.getFirstName(), caregiver.getSurname(), caregiver.getTelephone()
        );
        LogService.log("CAREGIVER_CREATE", description);
    }

    /**
     * Updates an existing caregiver and writes a {@code CAREGIVER_UPDATE} audit
     * entry that lists exactly which fields changed ("before &rarr; after").
     *
     * <p>The old state is read first so the change set can be computed; if
     * nothing actually changed, no log entry is written.</p>
     *
     * @param newCaregiver the caregiver carrying the new values (matched by cid)
     * @throws SQLException if reading the old state or the update fails
     */
    @Override
    public void update(Caregiver newCaregiver) throws SQLException {
        Caregiver oldCaregiver = this.read(newCaregiver.getCid());
        super.update(newCaregiver);

        if (oldCaregiver != null) {
            StringBuilder changes = new StringBuilder();

            if (!oldCaregiver.getFirstName().equals(newCaregiver.getFirstName())) {
                changes.append(String.format("Vorname: '%s' → '%s'; ",
                    oldCaregiver.getFirstName(), newCaregiver.getFirstName()));
            }
            if (!oldCaregiver.getSurname().equals(newCaregiver.getSurname())) {
                changes.append(String.format("Nachname: '%s' → '%s'; ",
                    oldCaregiver.getSurname(), newCaregiver.getSurname()));
            }
            if (!oldCaregiver.getTelephone().equals(newCaregiver.getTelephone())) {
                changes.append(String.format("Telefon: '%s' → '%s'; ",
                    oldCaregiver.getTelephone(), newCaregiver.getTelephone()));
            }

            if (changes.length() > 0) {
                String description = String.format(
                    "Pflegekraft (ID: %d) geändert. Details: %s",
                    newCaregiver.getCid(), changes.toString()
                );
                LogService.log("CAREGIVER_UPDATE", description);
            }
        }
    }

    /**
     * Deletes a caregiver by id and writes a {@code CAREGIVER_DELETE} audit entry
     * that records the deleted caregiver's data.
     *
     * @param cid the id of the caregiver to delete
     * @throws SQLException if reading the caregiver or the delete fails
     */
    @Override
    public void deleteById(long cid) throws SQLException {
        Caregiver caregiverToDelete = this.read(cid);
        super.deleteById(cid);

        if (caregiverToDelete != null) {
            String description = String.format(
                "Pflegekraft '%s %s' (ID: %d, Telefon: %s) wurde gelöscht.",
                caregiverToDelete.getFirstName(),
                caregiverToDelete.getSurname(),
                caregiverToDelete.getCid(),
                caregiverToDelete.getTelephone()
            );
            LogService.log("CAREGIVER_DELETE", description);
        }
    }

    /**
     * Builds the {@code INSERT} statement for a new caregiver.
     *
     * @param caregiver the caregiver to insert
     * @return the prepared insert statement, or {@code null} if it could not be built
     */
    @Override
    protected PreparedStatement getCreateStatement(Caregiver caregiver) {
        PreparedStatement statement = null;
        try {
            final String SQL = "INSERT INTO pfleger (firstname, surname, telephone) VALUES (?, ?, ?)";
            statement = this.connection.prepareStatement(SQL);
            statement.setString(1, caregiver.getFirstName());
            statement.setString(2, caregiver.getSurname());
            statement.setString(3, caregiver.getTelephone());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statement;
    }

    /**
     * Builds the statement that reads a single caregiver by id.
     *
     * @param cid the id of the caregiver
     * @return the prepared select statement, or {@code null} if it could not be built
     */
    @Override
    protected PreparedStatement getReadByIDStatement(long cid) {
        PreparedStatement statement = null;
        try {
            final String SQL = "SELECT * FROM pfleger WHERE cid = ?";
            statement = this.connection.prepareStatement(SQL);
            statement.setLong(1, cid);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statement;
    }

    /**
     * Maps the current row of the result set to a {@link Caregiver} object.
     *
     * @param result result set positioned on a valid row
     * @return the mapped caregiver
     * @throws SQLException if a column cannot be read
     */
    @Override
    protected Caregiver getInstanceFromResultSet(ResultSet result) throws SQLException {
        return new Caregiver(
            result.getLong("cid"),
            result.getString("firstname"),
            result.getString("surname"),
            result.getString("telephone")
        );
    }

    /**
     * Builds the statement that reads all caregivers.
     *
     * @return the prepared select-all statement, or {@code null} if it could not be built
     */
    @Override
    protected PreparedStatement getReadAllStatement() {
        PreparedStatement statement = null;
        try {
            final String SQL = "SELECT * FROM pfleger";
            statement = this.connection.prepareStatement(SQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statement;
    }

    /**
     * Maps every remaining row of the result set to a list of {@link Caregiver} objects.
     *
     * @param result result set to iterate over
     * @return list of all caregivers in the result set
     * @throws SQLException if a column cannot be read
     */
    @Override
    protected ArrayList<Caregiver> getListFromResultSet(ResultSet result) throws SQLException {
        ArrayList<Caregiver> list = new ArrayList<>();
        while (result.next()) {
            list.add(new Caregiver(
                result.getLong("cid"),
                result.getString("firstname"),
                result.getString("surname"),
                result.getString("telephone")
            ));
        }
        return list;
    }

    /**
     * Builds the {@code UPDATE} statement for an existing caregiver.
     *
     * @param caregiver the caregiver carrying the new values (matched by cid)
     * @return the prepared update statement, or {@code null} if it could not be built
     */
    @Override
    protected PreparedStatement getUpdateStatement(Caregiver caregiver) {
        PreparedStatement statement = null;
        try {
            final String SQL = "UPDATE pfleger SET firstname = ?, surname = ?, telephone = ? WHERE cid = ?";
            statement = this.connection.prepareStatement(SQL);
            statement.setString(1, caregiver.getFirstName());
            statement.setString(2, caregiver.getSurname());
            statement.setString(3, caregiver.getTelephone());
            statement.setLong(4, caregiver.getCid());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statement;
    }

    /**
     * Builds the statement that deletes a caregiver by id.
     *
     * @param cid the id of the caregiver to delete
     * @return the prepared delete statement, or {@code null} if it could not be built
     */
    @Override
    protected PreparedStatement getDeleteStatement(long cid) {
        PreparedStatement statement = null;
        try {
            final String SQL = "DELETE FROM pfleger WHERE cid = ?";
            statement = this.connection.prepareStatement(SQL);
            statement.setLong(1, cid);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statement;
    }
}