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
    private ArticleInfo artikel;

    @Column(nullable = false)
    private Integer menge;

    @Column
    private String lagerplatz;

    @Column(name = "position_index")
    private Integer positionIndex = 0;

    @Column
    private String deviation_text;

    public Kommission getKommission() {return kommission;}
    public void setKommission(Kommission kommission) {
        this.kommission = kommission;
    }

    public ArticleInfo getArtikel() {return artikel;}
    public void setArtikel(ArticleInfo artikel) {this.artikel = artikel;}

    public Integer getMenge() {return menge;}
    public void setMenge(Integer menge) {this.menge = menge;}

    public String getLagerplatz() {return lagerplatz;}
    public void setLagerplatz(String lagerplatz) {this.lagerplatz = lagerplatz;}

    public String getDeviation_text() {return deviation_text;}
    public void setDeviation_text(String deviation_text) {this.deviation_text = deviation_text;}

    public Integer getPositionIndex() {return positionIndex;}
    public void setPositionIndex(Integer positionIndex) {this.positionIndex = positionIndex;}
}
