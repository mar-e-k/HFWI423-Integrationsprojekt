package com.example.application.data.messagingEvent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface MessagingEventRepository extends JpaRepository<MessagingEvent, Long>, JpaSpecificationExecutor<MessagingEvent> {

    @Modifying
    @Transactional
    @Query("DELETE FROM MessagingEvent")
    void deleteAllBulk();
}
