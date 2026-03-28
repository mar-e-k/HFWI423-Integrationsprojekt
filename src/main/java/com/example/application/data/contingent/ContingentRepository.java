package com.example.application.data.contingent;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContingentRepository extends JpaRepository<Contingent, Long> {

    List<Contingent> findAllByArticleId(Long articleId);

    boolean existsByArticleId(Long articleId);


}
