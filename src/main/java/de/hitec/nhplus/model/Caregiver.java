package de.hitec.nhplus.model;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Represents a caregiver ("Pflegekraft") of the nursing home.
 *
 * <p>Extends {@link Person} (first name, surname) and adds the caregiver-specific
 * data: a technical id ({@code cid}) and a {@code telephone} number. The fields
 * are JavaFX properties so they can be bound directly to table columns in the
 * caregiver view.</p>
 *
 * <p>Single responsibility: this class only models the caregiver data; reading
 * and writing caregivers is the job of
 * {@link de.hitec.nhplus.datastorage.CaregiverDao}.</p>
 */
public class Caregiver extends Person {

    private final SimpleLongProperty cid;
    private final SimpleStringProperty telephone;

    /**
     * Creates a new caregiver that has not been persisted yet (id defaults to 0).
     *
     * @param firstName the caregiver's first name
     * @param surname   the caregiver's surname
     * @param telephone the caregiver's telephone number
     */
    public Caregiver(String firstName, String surname, String telephone) {
        super(firstName, surname);
        this.cid = new SimpleLongProperty(0);
        this.telephone = new SimpleStringProperty(telephone);
    }

    /**
     * Creates a caregiver with a known id, typically when mapping a database row.
     *
     * @param cid       technical primary key of the caregiver
     * @param firstName the caregiver's first name
     * @param surname   the caregiver's surname
     * @param telephone the caregiver's telephone number
     */
    public Caregiver(long cid, String firstName, String surname, String telephone) {
        super(firstName, surname);
        this.cid = new SimpleLongProperty(cid);
        this.telephone = new SimpleStringProperty(telephone);
    }

    /**
     * Returns the technical primary key of this caregiver.
     *
     * @return the caregiver id
     */
    public long getCid() {
        return cid.get();
    }

    /**
     * Returns the id as a JavaFX property for binding to table columns.
     *
     * @return the {@code cid} property
     */
    public SimpleLongProperty cidProperty() {
        return cid;
    }

    /**
     * Returns the telephone number.
     *
     * @return the telephone number
     */
    public String getTelephone() {
        return telephone.get();
    }

    /**
     * Returns the telephone number as a JavaFX property for binding to table columns.
     *
     * @return the {@code telephone} property
     */
    public SimpleStringProperty telephoneProperty() {
        return telephone;
    }

    /**
     * Updates the telephone number.
     *
     * @param telephone the new telephone number
     */
    public void setTelephone(String telephone) {
        this.telephone.set(telephone);
    }
}
