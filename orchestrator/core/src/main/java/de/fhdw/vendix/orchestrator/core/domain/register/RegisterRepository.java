package de.fhdw.vendix.orchestrator.core.domain.register;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface RegisterRepository extends JpaRepository<Register, Long> {
    List<Register> findAllByStoreId(Long storeId);
}