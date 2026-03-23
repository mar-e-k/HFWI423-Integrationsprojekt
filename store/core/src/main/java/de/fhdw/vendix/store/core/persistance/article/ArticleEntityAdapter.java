package de.fhdw.vendix.store.core.persistance.article;

import de.fhdw.vendix.commons.spring.data.crud.AbstractEntityCrudAdapter;
import org.springframework.stereotype.Service;

@Service
class ArticleEntityAdapter extends AbstractEntityCrudAdapter<Article, Long> {

    private final ArticleRepository articleRepository;

    ArticleEntityAdapter(ArticleRepository articleRepository) {
        super(articleRepository);
        this.articleRepository = articleRepository;
    }
}