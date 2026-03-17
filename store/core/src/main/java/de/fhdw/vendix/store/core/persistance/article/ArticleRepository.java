package de.fhdw.vendix.store.core.persistance.article;

import org.springframework.data.jpa.repository.JpaRepository;

interface ArticleRepository extends JpaRepository<Article,Long> {}