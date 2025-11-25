package com.example.application.data.orderPicking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KommissionPositionRepository extends JpaRepository<KommissionPosition, Long> {
    List<KommissionPosition> findByKommissionId(Long kommissionId);
}