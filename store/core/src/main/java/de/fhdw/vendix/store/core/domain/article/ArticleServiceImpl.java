package de.fhdw.vendix.store.core.domain.article;

import de.fhdw.vendix.store.core.caching.StoreRedissonCachingKey;
import de.fhdw.vendix.commons.spring.data.persistance.crud.AbstractCrudService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
class ArticleServiceImpl extends AbstractCrudService<Article, Long> implements ArticleService  {

    private final ArticleRepository articleRepository;

    ArticleServiceImpl(ArticleRepository articleRepository) {
        super(articleRepository);
        this.articleRepository = articleRepository;
    }

    @Cacheable(
            value = StoreRedissonCachingKey.Constants.ARTICLES,
            key = "'all'"
    )
    @Override
    @Transactional(readOnly = true)
    public List<Article> findAll() {
        return super.findAll();
    }

    @Cacheable(
            value = StoreRedissonCachingKey.Constants.ARTICLE_BY_ID,
            key = "#id",
            condition = "#id != null"
    )
    @Override
    @Transactional(readOnly = true)
    public Optional<Article> findById(Long id) {
        return super.findById(id);
    }

    @Cacheable(
            value = StoreRedissonCachingKey.Constants.ARTICLE_BY_GTIN,
            key = "#gtin",
            condition = "#gtin != null && !#gtin.isBlank()",
            unless = "#result == null"
    )
    @Override
    @Transactional(readOnly = true)
    public Optional<Article> findByGtin(String gtin) {
        if (gtin == null || !gtin.matches("^\\d{8}(\\d{4}|\\d{5}|\\d{6})?$")) {
            return Optional.empty();
        }

        return articleRepository.findByArticleNumber(gtin);
    }

    @CacheEvict(
            value = StoreRedissonCachingKey.Constants.ARTICLES,
            key = "'all'"
    )
    @Override
    @Transactional
    public Article create(Article entity) {
        return super.create(entity);
    }

    @CacheEvict(
            value = StoreRedissonCachingKey.Constants.ARTICLES,
            key = "'all'"
    )
    @Override
    @Transactional
    public List<Article> createAll(Iterable<Article> entities) {
        return super.createAll(entities);
    }

    @Caching(evict = {
            @CacheEvict(value = StoreRedissonCachingKey.Constants.ARTICLES, key = "'all'"),
            @CacheEvict(value = StoreRedissonCachingKey.Constants.ARTICLE_BY_ID, key = "#entity.id"),
            @CacheEvict(value = StoreRedissonCachingKey.Constants.ARTICLE_BY_GTIN, key = "#entity.articleNumber")
    })
    @Override
    @Transactional
    public Article update(Article entity) {
        return super.update(entity);
    }

    @Caching(evict = {
            @CacheEvict(value = StoreRedissonCachingKey.Constants.ARTICLES, key = "'all'"),
            @CacheEvict(value = StoreRedissonCachingKey.Constants.ARTICLE_BY_ID, allEntries = true),
            @CacheEvict(value = StoreRedissonCachingKey.Constants.ARTICLE_BY_GTIN, allEntries = true)
    })
    @Override
    @Transactional
    public List<Article> updateAll(Iterable<Article> entities) {
        return super.updateAll(entities);
    }

    @Caching(evict = {
            @CacheEvict(value = StoreRedissonCachingKey.Constants.ARTICLES, key = "'all'"),
            @CacheEvict(value = StoreRedissonCachingKey.Constants.ARTICLE_BY_ID, key = "#entity.id"),
            @CacheEvict(value = StoreRedissonCachingKey.Constants.ARTICLE_BY_GTIN, key = "#entity.articleNumber")
    })
    @Override
    @Transactional
    public void delete(Article entity) {
        super.delete(entity);
    }

    @Caching(evict = {
            @CacheEvict(value = StoreRedissonCachingKey.Constants.ARTICLES, key = "'all'"),
            @CacheEvict(value = StoreRedissonCachingKey.Constants.ARTICLE_BY_ID, key = "#id"),
            @CacheEvict(value = StoreRedissonCachingKey.Constants.ARTICLE_BY_GTIN, allEntries = true)
    })
    @Override
    @Transactional
    public void deleteById(Long id) {
        super.deleteById(id);
    }

    @Caching(evict = {
            @CacheEvict(value = StoreRedissonCachingKey.Constants.ARTICLES, allEntries = true),
            @CacheEvict(value = StoreRedissonCachingKey.Constants.ARTICLE_BY_ID, allEntries = true),
            @CacheEvict(value = StoreRedissonCachingKey.Constants.ARTICLE_BY_GTIN, allEntries = true)
    })
    @Override
    @Transactional
    public void deleteAll() {
        super.deleteAll();
    }

    @Caching(evict = {
            @CacheEvict(value = StoreRedissonCachingKey.Constants.ARTICLES, key = "'all'"),
            @CacheEvict(value = StoreRedissonCachingKey.Constants.ARTICLE_BY_ID, allEntries = true),
            @CacheEvict(value = StoreRedissonCachingKey.Constants.ARTICLE_BY_GTIN, allEntries = true)
    })
    @Override
    @Transactional
    public void deleteAll(Iterable<Article> entities) {
        super.deleteAll(entities);
    }

    @Caching(evict = {
            @CacheEvict(value = StoreRedissonCachingKey.Constants.ARTICLES, key = "'all'"),
            @CacheEvict(value = StoreRedissonCachingKey.Constants.ARTICLE_BY_ID, allEntries = true),
            @CacheEvict(value = StoreRedissonCachingKey.Constants.ARTICLE_BY_GTIN, allEntries = true)
    })
    @Override
    @Transactional
    public void deleteAllById(Iterable<Long> ids) {
        super.deleteAllById(ids);
    }
}