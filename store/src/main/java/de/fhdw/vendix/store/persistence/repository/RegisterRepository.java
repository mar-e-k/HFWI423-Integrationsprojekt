package de.fhdw.vendix.store.persistence.repository;

import de.fhdw.vendix.store.persistence.entity.Register;
import de.fhdw.vendix.store.persistence.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegisterRepository extends JpaRepository<Register, Long> {
    List<Register> findAllByStore(Store store);
}