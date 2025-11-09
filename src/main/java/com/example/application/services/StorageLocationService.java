package com.example.application.services;

import com.example.application.data.storageLocation.StorageLocation;
import com.example.application.data.storageLocation.StorageLocationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StorageLocationService {
    private final StorageLocationRepository repo;
    public StorageLocationService(StorageLocationRepository repo) { this.repo = repo; }

    public List<StorageLocation> findAll() { return repo.findAll(); }
    public StorageLocation save(StorageLocation s) { return repo.save(s); }
}

