package de.fhdw.fillialensystem.persistence.entity;

import de.fhdw.fillialensystem.persistence.entity.imported.Article;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Entity
public class LinkArticleReceipt { //Dont extend, AbstractEntity. We dont want redundant info here

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Article article;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Receipt receipt;

    private Short articleAmount;

    private Short discountedAmount;

    @Min(value = 0, message = "Discounted amount must be greater than or equal 0")
    @Max(value = 100, message = "Discounted amount must be less than or equal 100")
    private Short discountedByPercent;

    public LinkArticleReceipt() {
        super();
    }
}