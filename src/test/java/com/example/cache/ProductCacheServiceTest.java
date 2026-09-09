package com.example.cache;

import com.example.cache.cache.ProductCacheService;
import com.example.cache.product.Product;
import com.example.cache.product.ProductRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductCacheServiceTest {
  @Test
  void secondRequestShouldUseLocalCache() {
    ProductRepository repo = new ProductRepository();
    ProductCacheService service = new ProductCacheService(repo, 1000, 30, 2000);
    try {
      Product first = service.getProduct("1").join();
      Product second = service.getProduct("1").join();
      assertEquals(first, second);
      assertEquals(1, repo.getDatabaseCalls());
    } finally {
      service.clearCache();
    }
  }

    /*@Test
    void concurrentSameKeyRequestsShouldUseOneDbCall() {
        ProductRepository repo = new ProductRepository();
        ProductCacheService service = new ProductCacheService(repo, 1000, 30, 5000);
        ExecutorService executor = Executors.newFixedThreadPool(50);
        try {
            List<CompletableFuture<Product>> futures = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                futures.add(CompletableFuture.supplyAsync(() -> service.getProduct("123").join(), executor));
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            assertEquals(1, repo.getDatabaseCalls());
        } finally {
            executor.shutdownNow();
            service.clearCache();
        }
    }
}*/
}
