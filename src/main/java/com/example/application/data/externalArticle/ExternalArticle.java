package com.example.application.data.externalArticle;

import jakarta.persistence.*;

@Entity
@Table(name = "article", schema = "artikel") // Tabellenname in Neon DB
public class ExternalArticle {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "article_number")
    private String articleNumber;

    @Column(name = "name")
    private String name;


    public Long getId() {
        return id;
    }

    public String getArticleNumber() {
        return articleNumber;
    }

    public String getName() {
        return name;
    }

}
