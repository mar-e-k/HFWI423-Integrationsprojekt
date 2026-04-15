package com.example.application.services;

import com.example.application.data.messagingEvent.MessagingEvent;
import com.example.application.data.messagingEvent.MessagingEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MessagingEventService {

    private final MessagingEventRepository repository;

    public MessagingEventService(MessagingEventRepository repository) {
        this.repository = repository;
    }

    public Optional<MessagingEvent> get(Long id) {
        return repository.findById(id);
    }

    public MessagingEvent save(MessagingEvent entity) {
        return repository.save(entity);
    }

    public List<MessagingEvent> saveAll(List<MessagingEvent> entities) {
        return repository.saveAll(entities);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public void deleteAll() {
        repository.deleteAllBulk();
    }

    public Page<MessagingEvent> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<MessagingEvent> list(Pageable pageable, Specification<MessagingEvent> filter) {
        return repository.findAll(filter, pageable);
    }

    public List<MessagingEvent> findAll() {
        return repository.findAll();
    }

    public int count() {
        return (int) repository.count();
    }
}
