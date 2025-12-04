package com.example.application.services;

/**
 * steht als Platzhalter für einen Artikel, der in Contingent vorhanden ist, aber noch keinen Eintrag in ArticleInfo hat.
 */
public class NewArticleCandidate {

    private Long articleId;
    private String articleNumber;
    private String name;

    // Getter/Setter

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public String getArticleNumber() {
        return articleNumber;
    }

    public void setArticleNumber(String articleNumber) {
        this.articleNumber = articleNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}