module de.hitec.nhplus {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;

    requires transitive org.controlsfx.controls;
    requires transitive java.sql;
    requires transitive org.xerial.sqlitejdbc;

    opens de.hitec.nhplus to javafx.fxml;
    opens de.hitec.nhplus.controller to javafx.fxml;
    opens de.hitec.nhplus.model to javafx.base;

    exports de.hitec.nhplus;
    exports de.hitec.nhplus.controller;
    exports de.hitec.nhplus.model;
}