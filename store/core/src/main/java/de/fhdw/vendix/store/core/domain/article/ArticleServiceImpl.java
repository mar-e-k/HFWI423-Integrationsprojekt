package de.fhdw.vendix.store.core.domain.article;

import de.fhdw.vendix.commons.spring.data.crud.AbstractCrudService;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

@Service
class ArticleServiceImpl extends AbstractCrudService<Article, Long> implements ArticleService  {

    private static final int MAX_ARTICLE_CACHE_SIZE = 10_000;

    private final ArticleRepository articleRepository;
    private final ConcurrentMap<String, Optional<Article>> articleByGtinCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, Optional<Article>> articleByIdCache = new ConcurrentHashMap<>();

    ArticleServiceImpl(ArticleRepository articleRepository) {
        super(articleRepository);
        this.articleRepository = articleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Article> findById(Long id) {
        if (id == null || id < 1) {
            return Optional.empty();
        }
        return cached(articleByIdCache, id, () -> articleRepository.findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Article> findByGtin(String gtin) {
        if (gtin == null || gtin.isEmpty() || !gtin.matches("^\\d{8}(\\d{4}|\\d{5}|\\d{6})?$")) {
            return Optional.empty();
        }
        return cached(articleByGtinCache, gtin, () -> articleRepository.findByArticleNumber(gtin));
    }

    private <K> Optional<Article> cached(
            ConcurrentMap<K, Optional<Article>> cache,
            K key,
            Supplier<Optional<Article>> loader
    ) {
        @Nullable Optional<Article> cached = cache.get(key);
        if (cached != null) {
            return cached;
        }

        Optional<Article> loaded = loader.get();
        if (cache.size() >= MAX_ARTICLE_CACHE_SIZE) {
            return loaded;
        }

        @Nullable Optional<Article> previous = cache.putIfAbsent(key, loaded);
        if (previous != null) {
            return previous;
        }
        return loaded;
    }
}
