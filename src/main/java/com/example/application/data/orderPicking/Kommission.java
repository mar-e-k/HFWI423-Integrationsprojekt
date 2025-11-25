package com.example.application.data.orderPicking;

import com.example.application.data.AbstractEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
public class Kommission extends AbstractEntity {
    // Nummer der Kommission
    @NotNull
    @Column(name = "orderPicking_nr", nullable = false)
    private Integer orderPickingNumber;
    // Erstellungsdatum
    @NotNull
    @Column(name = "date", nullable = false)
    private LocalDateTime date;
    // Lagerbestand des Artikels
    @NotNull
    @Column(name = "status", nullable = false)
    private Boolean finished;
    // Lagerort des Artikels
    @Size(max = 255)
    @NotNull
    @Column(name = "store", nullable = false)
    private String store;

    public int getOrderPickingNumber() {
        return orderPickingNumber;
    }

    public void setOrderPickingNumber(int orderPickingNumber) {
        this.orderPickingNumber = orderPickingNumber;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }
    public LocalDateTime getDate() {
        return date;
    }


    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getStore(){return store;}
    public void setStore(String store){this.store=store;}
}
