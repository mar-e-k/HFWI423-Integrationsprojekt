package de.fhdw.vendix.store.core.persistance.article.port;

import de.fhdw.vendix.commons.api.domain.article.ArticleDTO;
import de.fhdw.vendix.commons.api.structure.service.CrudCommandService;

interface ArticleCommandService extends CrudCommandService<ArticleDTO, Long> {}