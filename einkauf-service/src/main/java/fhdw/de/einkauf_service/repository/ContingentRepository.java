package fhdw.de.einkauf_service.repository;

import fhdw.de.einkauf_service.entity.Contingent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContingentRepository extends JpaRepository<Contingent, Long> {

    List<Contingent> findAllByAvailableQuantity(Integer quantity);
}
