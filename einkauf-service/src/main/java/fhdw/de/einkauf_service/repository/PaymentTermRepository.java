package fhdw.de.einkauf_service.repository;

import fhdw.de.einkauf_service.entity.PaymentTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentTermRepository extends JpaRepository<PaymentTerm, Long> {

    boolean existsByDefinition(String definition);
}
