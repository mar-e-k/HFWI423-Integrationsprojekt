package com.example.application.data.orderPicking;

import com.example.application.data.AbstractEntity;
import com.example.application.data.article.ArticleInfo;
import jakarta.persistence.*;

@Entity
@Table(name = "kommission_position")
public class KommissionPosition extends AbstractEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "order_picking_nr")
    private Kommission kommission;

    @ManyToOne(optional = false)
    @JoinColumn(name = "article_id")
    private ArticleInfo article_id;

    @Column(name = "amount", nullable = false)
    private int amount;

    @Column(name = "storage")
    private String storageLocation;



    public Kommission getKommission() {return kommission;}
    public void setKommission(Kommission kommission) {
        this.kommission = kommission;
    }

    public ArticleInfo getArticle_id() {return article_id;}
    public void setArticle_id(ArticleInfo article_id) {this.article_id = article_id;}

    public int getAmount() {return amount;}
    public void setAmount(int amount) {this.amount = amount;}

    public String getLagerplatz() {return storageLocation;}
    public void setLagerplatz(String lagerplatz) {this.storageLocation = storageLocation;}

}
