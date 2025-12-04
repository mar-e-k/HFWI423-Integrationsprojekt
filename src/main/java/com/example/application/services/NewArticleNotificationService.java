package com.example.application.services;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class NewArticleNotificationService {

    private final AtomicInteger counter = new AtomicInteger(0);

    public void increment() {
        counter.incrementAndGet();
    }

    public int getCount() {
        return counter.get();
    }

    public void reset() {
        counter.set(0);
    }
}
