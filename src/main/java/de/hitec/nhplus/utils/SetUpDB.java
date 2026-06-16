package de.hitec.nhplus.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import de.hitec.nhplus.datastorage.CaregiverDao;
import de.hitec.nhplus.datastorage.ConnectionBuilder;
import de.hitec.nhplus.datastorage.DaoFactory;
import de.hitec.nhplus.datastorage.PatientDao;
import de.hitec.nhplus.datastorage.TreatmentDao;
import de.hitec.nhplus.model.Caregiver;
import de.hitec.nhplus.model.Patient;
import de.hitec.nhplus.model.Treatment;
import static de.hitec.nhplus.utils.DateConverter.convertStringToLocalDate;
import static de.hitec.nhplus.utils.DateConverter.convertStringToLocalTime;

public class SetUpDB {

    public static void setUpDb() {
        Connection connection = ConnectionBuilder.getConnection();

        wipeDb(connection);

        setUpTablePatient(connection);
        setUpTableTreatment(connection);
        setUpTableUsers(connection);
        setUpTableLogs(connection);
        setUpTablePfleger(connection);

        setUpPatients();
        setUpTreatments();
        setUpUsers();
        setUpLogs();
        setUpPfleger();
    }

    public static void wipeDb(Connection connection) {
        try (Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS logs");
            statement.execute("DROP TABLE IF EXISTS treatment");
            statement.execute("DROP TABLE IF EXISTS patient");
            statement.execute("DROP TABLE IF EXISTS users");
            statement.execute("DROP TABLE IF EXISTS pfleger");
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private static void setUpTablePatient(Connection connection) {
        final String SQL = "CREATE TABLE IF NOT EXISTS patient (" +
                "pid INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "firstname TEXT NOT NULL, " +
                "surname TEXT NOT NULL, " +
                "dateOfBirth TEXT NOT NULL, " +
                "carelevel TEXT NOT NULL, " +
                "roomnumber TEXT NOT NULL, " +
                "assets TEXT NOT NULL, " +
                "archived INTEGER NOT NULL DEFAULT 0, " +
                "archiveDate TEXT" +
                ");";

        try (Statement statement = connection.createStatement()) {
            statement.execute(SQL);
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private static void setUpTableTreatment(Connection connection) {
        final String SQL = "CREATE TABLE IF NOT EXISTS treatment (" +
                "tid INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "pid INTEGER NOT NULL, " +
                "treatment_date TEXT NOT NULL, " +
                "begin TEXT NOT NULL, " +
                "end TEXT NOT NULL, " +
                "description TEXT NOT NULL, " +
                "remark TEXT NOT NULL, " +
                "caregiverId INTEGER, " +
                "FOREIGN KEY (pid) REFERENCES patient (pid) ON DELETE CASCADE" +
                ");";

        try (Statement statement = connection.createStatement()) {
            statement.execute(SQL);
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private static void setUpTableUsers(Connection connection) {
        final String SQL = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password_hash TEXT NOT NULL, " +
                "salt TEXT NOT NULL, " +
                "role TEXT NOT NULL, " +
                "active INTEGER NOT NULL DEFAULT 1, " +
                "must_change_password INTEGER NOT NULL DEFAULT 1" +
                ");";

        try (Statement statement = connection.createStatement()) {
            statement.execute(SQL);
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private static void setUpTableLogs(Connection connection) {
        final String SQL = "CREATE TABLE IF NOT EXISTS logs (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT NOT NULL, " +
                "action TEXT NOT NULL, " +
                "timestamp TEXT NOT NULL, " +
                "description TEXT" +
                ");";

        try (Statement statement = connection.createStatement()) {
            statement.execute(SQL);
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private static void setUpPatients() {
        try {
            PatientDao dao = DaoFactory.getDaoFactory().createPatientDao();

            dao.create(new Patient("Seppl", "Herberger", convertStringToLocalDate("1945-12-01"), "4", "202", "vermögend"));
            dao.create(new Patient("Martina", "Gerdsen", convertStringToLocalDate("1954-08-12"), "5", "010", "arm"));
            dao.create(new Patient("Gertrud", "Franzen", convertStringToLocalDate("1949-04-16"), "3", "002", "normal"));
            dao.create(new Patient("Ahmet", "Yilmaz", convertStringToLocalDate("1941-02-22"), "3", "013", "normal"));
            dao.create(new Patient("Hans", "Neumann", convertStringToLocalDate("1955-12-12"), "2", "001", "sehr vermögend"));
            dao.create(new Patient("Elisabeth", "Müller", convertStringToLocalDate("1958-03-07"), "5", "110", "arm"));

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    private static void setUpTreatments() {
        try {
            TreatmentDao dao = DaoFactory.getDaoFactory().createTreatmentDao();

            dao.create(new Treatment(1, 1, convertStringToLocalDate("2023-06-03"),
                    convertStringToLocalTime("11:00"), convertStringToLocalTime("15:00"),
                    "Gespräch", "Angstzustände"));

            dao.create(new Treatment(2, 1, convertStringToLocalDate("2023-06-05"),
                    convertStringToLocalTime("11:00"), convertStringToLocalTime("12:30"),
                    "Gespräch", "Unruhe"));

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    private static void setUpUsers() {
        try {
            Connection connection = ConnectionBuilder.getConnection();

            String sql = """
            INSERT INTO users
            (username, password_hash, salt, role)
            VALUES (?, ?, ?, ?)
            """;

            String adminSalt = PasswordUtil.generateSalt();
            String adminHash = PasswordUtil.hash("admin123", adminSalt);

            PreparedStatement admin = connection.prepareStatement(sql);
            admin.setString(1, "admin");
            admin.setString(2, adminHash);
            admin.setString(3, adminSalt);
            admin.setString(4, "ADMIN");
            admin.executeUpdate();

            String userSalt = PasswordUtil.generateSalt();
            String userHash = PasswordUtil.hash("pflege123", userSalt);

            PreparedStatement user = connection.prepareStatement(sql);
            user.setString(1, "pfleger1");
            user.setString(2, userHash);
            user.setString(3, userSalt);
            user.setString(4, "USER");
            user.executeUpdate();

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    private static void setUpLogs() {
        try {
            Connection connection = ConnectionBuilder.getConnection();

            String sql = """
            INSERT INTO logs (username, action, timestamp, description)
            VALUES (?, ?, ?, ?)
            """;

            PreparedStatement ps = connection.prepareStatement(sql);

            ps.setString(1, "admin");
            ps.setString(2, "INIT_DB");
            ps.setString(3, java.time.LocalDateTime.now().toString());
            ps.setString(4, "Datenbank initialisiert");
            ps.executeUpdate();

            ps.setString(1, "pfleger1");
            ps.setString(2, "LOGIN");
            ps.setString(3, java.time.LocalDateTime.now().toString());
            ps.setString(4, "Test Log Eintrag");
            ps.executeUpdate();

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    private static void setUpTablePfleger(Connection connection) {
        final String SQL = "CREATE TABLE IF NOT EXISTS pfleger (" +
                "cid INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "firstname TEXT NOT NULL, " +
                "surname TEXT NOT NULL, " +
                "telephone TEXT NOT NULL" +
                ");";

        try (Statement statement = connection.createStatement()) {
            statement.execute(SQL);
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private static void setUpPfleger() {
        try {
            CaregiverDao dao = DaoFactory.getDaoFactory().createCaregiverDao();
            dao.create(new Caregiver("Anna", "Schmidt", "0151-11223344"));
            dao.create(new Caregiver("Thomas", "Müller", "0152-55667788"));
            dao.create(new Caregiver("Maria", "Weber", "0153-99001122"));
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public static void main(String[] args) {
        setUpDb();
    }
}