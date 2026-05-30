package de.fhdw.vendix.store.core.domain.article;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings("NullAway")
class ArticleServiceImplTest {

    @Test
    void findByGtinRejectsInvalidInputWithoutRepositoryCall() {
        ArticleRepository repository = mock(ArticleRepository.class);
        ArticleServiceImpl service = new ArticleServiceImpl(repository);

        assertThat(service.findByGtin(null)).isEmpty();
        assertThat(service.findByGtin("abc")).isEmpty();
        assertThat(service.findByGtin("123")).isEmpty();
        verifyNoInteractions(repository);
    }

    @Test
    void findByGtinDelegatesValidGtinToRepository() {
        ArticleRepository repository = mock(ArticleRepository.class);
        Article article = new Article(1L, "12345678", "Fresh", "ACME", "Milk", 1.0, 1.99,
                10, "Supplier", 19.0, "pcs", true, false);
        when(repository.findByArticleNumber("12345678")).thenReturn(Optional.of(article));

        assertThat(new ArticleServiceImpl(repository).findByGtin("12345678")).contains(article);
        verify(repository).findByArticleNumber("12345678");
    }
}
