package de.hitec.nhplus.datastorage;

import de.hitec.nhplus.model.Patient;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PatientDaoTest {

    private static Connection connection;
    private PatientDao dao;

    @BeforeAll
    static void setUpDatabase() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");

        try (Statement statement = connection.createStatement()) {
            statement.execute(
                    "CREATE TABLE patient (" +
                            "pid INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "firstname TEXT NOT NULL, " +
                            "surname TEXT NOT NULL, " +
                            "dateOfBirth TEXT NOT NULL, " +
                            "carelevel TEXT NOT NULL, " +
                            "roomnumber TEXT NOT NULL, " +
                            "telephone TEXT NOT NULL DEFAULT '', " +
                            "archived INTEGER NOT NULL DEFAULT 0, " +
                            "archiveDate TEXT)"
            );
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        dao = new PatientDao(connection);
        try (Statement statement = connection.createStatement()) {
            statement.execute("DELETE FROM patient");
        }
    }

    @AfterAll
    static void tearDown() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    @DisplayName("create() und readAll(): Ein neuer Patient wird gespeichert und kann gelesen werden")
    void createAndReadAll() throws SQLException {
        Patient patient = new Patient(
                "Max", "Mustermann",
                LocalDate.of(1990, 5, 15),
                "3", "101"
        );

        dao.create(patient);
        List<Patient> allPatients = dao.readAll();

        assertEquals(1, allPatients.size(), "Es sollte genau ein Patient in der Datenbank sein");

        Patient loaded = allPatients.get(0);
        assertEquals("Max", loaded.getFirstName());
        assertEquals("Mustermann", loaded.getSurname());
        assertEquals("3", loaded.getCareLevel());
        assertEquals("101", loaded.getRoomNumber());
    }

    @Test
    @DisplayName("deleteById(): Ein gelöschter Patient ist nicht mehr auffindbar")
    void deleteById() throws SQLException {
        Patient patient = new Patient(
                "Erika", "Musterfrau",
                LocalDate.of(1985, 3, 20),
                "2", "202"
        );
        dao.create(patient);

        List<Patient> allPatients = dao.readAll();
        assertEquals(1, allPatients.size());
        long pid = allPatients.get(0).getPid();

        dao.deleteById(pid);

        List<Patient> afterDelete = dao.readAll();
        assertTrue(afterDelete.isEmpty(), "Nach dem Löschen sollte kein Patient mehr vorhanden sein");
    }
}
