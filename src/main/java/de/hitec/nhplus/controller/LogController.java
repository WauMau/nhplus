package de.hitec.nhplus.controller;

import java.sql.Connection;

import de.hitec.nhplus.datastorage.ConnectionBuilder;
import de.hitec.nhplus.datastorage.LogDao;
import de.hitec.nhplus.model.Log;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

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

    public LogController() {
        Connection connection = ConnectionBuilder.getConnection();
        this.logDao = new LogDao(connection);
    }

    @FXML
    public void initialize() {
        columnUser.setCellValueFactory(new PropertyValueFactory<>("username"));
        columnAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        columnTimestamp.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        columnDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        refresh();
    }

    private void refresh() {
        try {
            tableLogs.getItems().setAll(logDao.readAll());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}