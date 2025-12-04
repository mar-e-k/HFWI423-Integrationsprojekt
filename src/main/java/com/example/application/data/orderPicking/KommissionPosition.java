package com.example.application.data.orderPicking;

import com.example.application.data.AbstractEntity;
import com.example.application.data.articleInfo.ArticleInfo;
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
    private Integer amount;

    @Column(name = "storage")
    private String storageLocation;



    public Kommission getKommission() {return kommission;}
    public void setKommission(Kommission kommission) {
        this.kommission = kommission;
    }

    public ArticleInfo getArticle_id() {return article_id;}
    public void setArticle_id(ArticleInfo article_id) {this.article_id = article_id;}

    public Integer getAmount() {return amount;}
    public void setAmount(Integer amount) {this.amount = amount;}

    public String getStorageLocation() {return storageLocation;}
    public void setStorageLocation(String lagerplatz) {this.storageLocation = storageLocation;}

}
