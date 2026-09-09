package com.example.cache;

import com.example.cache.cache.ProductCacheService;
import com.example.cache.product.Product;
import com.example.cache.product.ProductRepository;
import org.junit.jupiter.api.Test;

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

}
