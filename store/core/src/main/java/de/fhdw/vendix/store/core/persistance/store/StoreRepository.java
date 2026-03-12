package de.fhdw.vendix.store.core.persistance.store;

import org.springframework.data.jpa.repository.JpaRepository;

interface StoreRepository extends JpaRepository<Store, Long> {}