package de.fhdw.vendix.store.core.domain.article.service;

import de.fhdw.vendix.commons.api.domain.article.ArticleDTO;
import de.fhdw.vendix.commons.api.structure.service.CrudCommandService;

interface ArticleCommandService extends CrudCommandService<ArticleDTO, Long> {}