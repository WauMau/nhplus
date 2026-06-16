package de.hitec.nhplus.datastorage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import de.hitec.nhplus.logging.LogService;
import de.hitec.nhplus.model.Caregiver;

public class CaregiverDao extends DaoImp<Caregiver> {

    public CaregiverDao(Connection connection) {
        super(connection);
    }

    /**
     * Intercepts the creation process to log when a new caregiver is added.
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
     * Intercepts the update process to log precise changes ("Before -> After")
     * before delegating the actual execution to the superclass.
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
     * Overrides the delete process to log which caregiver was deleted.
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

    @Override
    protected Caregiver getInstanceFromResultSet(ResultSet result) throws SQLException {
        return new Caregiver(
            result.getLong("cid"),
            result.getString("firstname"),
            result.getString("surname"),
            result.getString("telephone")
        );
    }

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