package com.example.application.data;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.Instant;


/*
* Tabelle zum Dokumentieren der Änderung am Bestand
* speichert alten bestand, Bestandsänderung, neuen Bestand
* speichert Art der Änderung (ComboBox -> feste Types) und Grund der Änderung (TBD)
 */
@Entity
public class StockChangeLog {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private ArticleInfo article;

    private Integer oldStock;
    private Integer changeAmount;
    private Integer newStock;

    private String changeType;
    private String reason;

    private Instant createdAt;
    private String createdBy;
}