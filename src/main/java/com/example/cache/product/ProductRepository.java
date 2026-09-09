package com.example.cache.product;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ProductRepository {
    private final AtomicLong databaseCalls = new AtomicLong();

    @Value("${demo.database.delay-ms:1000}")
    private long databaseDelayMs;

    public Product findById(String id) {
        long call = databaseCalls.incrementAndGet();
        System.out.printf("🔥 DB CALL #%d product=%s%n", call, id);
        try {
            Thread.sleep(databaseDelayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Database call interrupted", e);
        }
        return new Product(id, "Mechanical Keyboard", 1);
    }

    public long getDatabaseCalls() { return databaseCalls.get(); }
    public void reset() { databaseCalls.set(0); }
}
