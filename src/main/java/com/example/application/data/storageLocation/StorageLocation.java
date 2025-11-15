package com.example.application.data.storageLocation;

import com.example.application.data.AbstractEntity;
import jakarta.persistence.*;

@Entity
@Table(
        name = "storage_location",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uc_zone_shelf_compartment",
                        columnNames = {"storageZone", "shelfID", "compartmentID"}
                )
        }
)
public class StorageLocation extends  AbstractEntity {

    private String storageZone;     // "Zone 1" - "Zone 4"

    private Integer shelfID;

    private Integer compartmentID;

    private String storageStatus;

    @PrePersist
    public void prePersist() {
        if (storageStatus == null || storageStatus.isBlank()) {
            storageStatus = "Available";
        }
    }

    @Transient
    public String getGeneralId() {
        if (storageZone == null || shelfID == null || compartmentID == null) {
            return "";
        }
        String zoneNumber = storageZone.replace("Zone", "").trim();
        return "Z" + zoneNumber + ".S" + shelfID + ".C" + compartmentID;
    }

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