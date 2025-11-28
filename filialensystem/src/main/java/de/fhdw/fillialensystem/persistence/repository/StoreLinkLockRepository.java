package de.fhdw.fillialensystem.persistence.repository;

import de.fhdw.fillialensystem.persistence.entity.StoreLinkLock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreLinkLockRepository extends JpaRepository<StoreLinkLock, Integer> { }