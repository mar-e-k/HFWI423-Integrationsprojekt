package com.example.application.data.storageLocation;

import com.example.application.data.AbstractEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "storage_location",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"storageZone", "shelfID", "compartmentID"})}
)
public class StorageLocation extends AbstractEntity {

    private String storageZone;
    private Integer compartmentID;
    private Integer shelfID;
    private String storageStatus;

    public String getStorageZone() {
        return storageZone;
    }

    public void setStorageZone(String storageZone) {
        this.storageZone = storageZone;
    }

    public Integer getCompartmentID() {
        return compartmentID;
    }

    public void setCompartmentID(Integer compartmentID) {
        this.compartmentID = compartmentID;
    }

    public Integer getShelfID() {
        return shelfID;
    }

    public void setShelfID(Integer shelfID) {
        this.shelfID = shelfID;
    }

    public String getStorageStatus() {
        return storageStatus;
    }

    public void setStorageStatus(String storageStatus) {
        this.storageStatus = storageStatus;
    }

}