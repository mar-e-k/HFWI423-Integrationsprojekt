package de.fhdw.vendix.store.core.domain.register;

import de.fhdw.vendix.store.core.domain.store.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegisterRepository extends JpaRepository<Register, Long> {
    List<Register> findAllByStore(Store store);
}