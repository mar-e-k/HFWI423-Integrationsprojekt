package com.example.application.services;

import com.example.application.data.storageLocation.StorageLocation;
import com.example.application.data.storageLocation.StorageLocationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StorageLocationService {

    private final StorageLocationRepository repository;

    public StorageLocationService(StorageLocationRepository repository) {
        this.repository = repository;
    }

    public List<StorageLocation> findAll() {
        return repository.findAll();
    }
}
