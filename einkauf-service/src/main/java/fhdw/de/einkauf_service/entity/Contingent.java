package fhdw.de.einkauf_service.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "contingent")
@Data
public class Contingent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long supplierId;

    // Referenz zum Artikel
    @Column(nullable = false)
    private Long articleId;


    @Column(nullable = false)
    private Integer availableQuantity;
}
