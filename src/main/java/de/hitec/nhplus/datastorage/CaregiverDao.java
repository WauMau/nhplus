package de.hitec.nhplus.datastorage;

import de.hitec.nhplus.model.Caregiver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class CaregiverDao extends DaoImp<Caregiver> {

    public CaregiverDao(Connection connection) {
        super(connection);
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
