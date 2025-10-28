package com.example.application.data;

import jakarta.persistence.Entity;

@Entity
public class StoragePlaceInfo extends AbstractEntity {

    private String storageZone;
    private Integer storagePlaceID;
    private Integer shelfID;
    private String storageStatus;

    public String getStorageZone() {
        return storageZone;
    }
    public void setStorageZone(String storageZone) {
        this.storageZone = storageZone;
    }
    public Integer getStoragePlaceID() {
        return storagePlaceID;
    }
    public void setStoragePlaceID(Integer storagePlaceID) {
        this.storagePlaceID = storagePlaceID;
    }
    public Integer getShelfID() { return shelfID; }
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