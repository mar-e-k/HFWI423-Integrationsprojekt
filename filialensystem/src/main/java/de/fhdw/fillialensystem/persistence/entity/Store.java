package de.fhdw.fillialensystem.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Store extends AbstractEntity {

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Store ID darf nicht leer sein")
    private String storeId;

    @Column(nullable = false)
    private String location = "Deutschland";

    public Store() {
        super();
    }

    public Store(String storeId, String name) {
        this.storeId = storeId;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
