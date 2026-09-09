package com.example.cache.cache;

import com.example.cache.product.Product;
import com.example.cache.product.ProductRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class ProductCacheService {
  private final ProductRepository repository;
  private final Cache<String, Product> cache;
  private final ConcurrentHashMap<String, CompletableFuture<Product>> inFlight = new ConcurrentHashMap<>();
  private final long timeoutMs;

  public ProductCacheService(
      ProductRepository repository,
      @Value("${demo.cache.max-size:10000}") long maxSize,
      @Value("${demo.cache.ttl-seconds:30}") long ttlSeconds,
      @Value("${demo.database.timeout-ms:1500}") long timeoutMs) {
    this.repository = repository;
    this.timeoutMs = timeoutMs;
    this.cache = Caffeine.newBuilder()
        .maximumSize(maxSize)
        .expireAfterWrite(ttlSeconds, TimeUnit.SECONDS)
        .build();
  }

    public CompletableFuture<Product> getProduct(String id) {
        Product cached = cache.getIfPresent(id);
        if (cached != null) {
            System.out.println("✅ CACHE HIT: " + id);
            return CompletableFuture.completedFuture(cached);
        }

        System.out.println("❌ CACHE MISS: " + id);
        return loadWithCoalescing(id);
    }

  private CompletableFuture<Product> loadWithCoalescing(String id) {
    return inFlight.computeIfAbsent(id, key -> {
      System.out.println("🚀 START DB LOAD: " + key);

      CompletableFuture<Product> future = CompletableFuture
          .supplyAsync(() ->
              // cache.get() is atomic: load-if-absent, thread-safe, flushes write buffer
              // before returning so the next getIfPresent() is guaranteed to hit
              cache.get(key, k -> repository.findById(k))
          )
          .orTimeout(timeoutMs, TimeUnit.MILLISECONDS);

      future.whenComplete((result, error) -> {
        inFlight.remove(key, future);
        if (error == null) {
          System.out.println("✅ DB LOAD COMPLETE: " + key);
        } else {
          cache.invalidate(key); // don't cache failed/timed-out loads
          System.out.println("❌ DB LOAD FAILED: " + key + " -> " + error);
        }
      });
      return future;
    });
  }

  public void clearCache() {
    cache.invalidateAll();
  }

  public long cacheSize() {
    return cache.estimatedSize();
  }

  public int inFlightRequests() {
    return inFlight.size();
  }
}
