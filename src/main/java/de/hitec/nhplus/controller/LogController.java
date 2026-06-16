package de.hitec.nhplus.controller;

import java.sql.Connection;

import de.hitec.nhplus.datastorage.ConnectionBuilder;
import de.hitec.nhplus.datastorage.LogDao;
import de.hitec.nhplus.model.Log;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controller for the audit-log view ({@code LogView.fxml}).
 *
 * <p>Displays the audit entries read through {@link LogDao} in a read-only table
 * (user, action, timestamp, description). The raw ISO timestamp is reformatted
 * for display into a more readable {@code yyyy-MM-dd HH:mm:ss} form.</p>
 *
 * <p>Single responsibility: presenting existing log entries. It never creates or
 * modifies logs &mdash; writing is done by {@link de.hitec.nhplus.logging.LogService}.</p>
 */
public class LogController {

    @FXML
    private TableView<Log> tableLogs;

    @FXML
    private TableColumn<Log, String> columnUser;

    @FXML
    private TableColumn<Log, String> columnAction;

    @FXML
    private TableColumn<Log, String> columnTimestamp;

    @FXML
    private TableColumn<Log, String> columnDescription;

    private final LogDao logDao;

    /**
     * Creates the controller and its {@link LogDao} on the shared database connection.
     */
    public LogController() {
        Connection connection = ConnectionBuilder.getConnection();
        this.logDao = new LogDao(connection);
    }

    /**
     * Initialises the table columns (including the readable timestamp formatting)
     * and loads the current log entries. Called automatically by JavaFX after the
     * FXML is loaded.
     */
    @FXML
    public void initialize() {
        columnUser.setCellValueFactory(new PropertyValueFactory<>("username"));
        columnAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        columnTimestamp.setCellValueFactory(cellData -> {
            String ts = cellData.getValue().getTimestamp();
            // "2026-06-16T10:49:38.748148" -> "2026-06-16 10:49:38"
            if (ts != null && ts.contains("T")) {
                String ohneNanosekunden = ts.contains(".") ? ts.substring(0, ts.indexOf(".")) : ts;
                return new SimpleStringProperty(ohneNanosekunden.replace("T", " "));
            }
            return new SimpleStringProperty(ts);
        });
        columnDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        refresh();
    }

    /**
     * Reloads all log entries from the database into the table.
     */
    private void refresh() {
        try {
            tableLogs.getItems().setAll(logDao.readAll());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}