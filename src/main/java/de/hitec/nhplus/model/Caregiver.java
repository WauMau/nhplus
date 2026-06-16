package de.hitec.nhplus.model;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

public class Caregiver extends Person {

    private final SimpleLongProperty cid;
    private final SimpleStringProperty telephone;

    public Caregiver(String firstName, String surname, String telephone) {
        super(firstName, surname);
        this.cid = new SimpleLongProperty(0);
        this.telephone = new SimpleStringProperty(telephone);
    }

    public Caregiver(long cid, String firstName, String surname, String telephone) {
        super(firstName, surname);
        this.cid = new SimpleLongProperty(cid);
        this.telephone = new SimpleStringProperty(telephone);
    }

    public long getCid() {
        return cid.get();
    }

    public SimpleLongProperty cidProperty() {
        return cid;
    }

    public String getTelephone() {
        return telephone.get();
    }

    public SimpleStringProperty telephoneProperty() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone.set(telephone);
    }
}
