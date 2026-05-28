package de.fhdw.vendix.orchestrator.core.domain.store;

import org.springframework.data.jpa.repository.JpaRepository;

interface StoreRepository extends JpaRepository<Store, Long> {}