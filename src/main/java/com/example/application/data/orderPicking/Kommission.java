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
    @Column(name = "order_picking_nr", nullable = false)
    private Integer orderPickingNumber;
    // Erstellungsdatum
    @NotNull
    @Column(name = "date", nullable = false)
    private LocalDateTime date;
    // Status der Kommission
    @NotNull
    @Column(name = "finished", nullable = false)
    private Boolean finished;
    // Filiale für Lieferung
    @Size(max = 255)
    @NotNull
    @Column(name = "store", nullable = false)
    private String store;

    @Column(name = "version", nullable = true)
    private Integer version;


    public Integer getOrderPickingNumber() {return orderPickingNumber;}
    public void setOrderPickingNumber(Integer orderPickingNumber) {
        this.orderPickingNumber = orderPickingNumber;
    }

    public Boolean getFinished() {
        return finished;
    }
    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    public LocalDateTime getDate() {return date;}
    public void setDate(LocalDateTime date) {this.date = date;}

    public String getStore(){return store;}
    public void setStore(String store){this.store=store;}
}
