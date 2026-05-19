package fhdw.de.einkauf_service.repository;

import fhdw.de.einkauf_service.entity.ReceivedDealNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReceivedDealNotificationRepository extends JpaRepository<ReceivedDealNotification, Long> {

    List<ReceivedDealNotification> findAllByOrderByReceivedAtDesc();

    List<ReceivedDealNotification> findAllByReadFalse();

    long countByReadFalse();

    boolean existsByExternalEventId(String externalEventId);
}
