package de.fhdw.fillialensystem.persistence.repository;

import de.fhdw.fillialensystem.persistence.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<Store, Long> {
}