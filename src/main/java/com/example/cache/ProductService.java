package com.example.cache;

import com.example.cache.product.Product;
import com.example.cache.product.ProductRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

  private final ProductRepository repository;
  private final Cache<String, Product> cache;

  public ProductService(ProductRepository repository) {
    this.repository = repository;
    this.cache = Caffeine.newBuilder()
        .maximumSize(10000)
        .expireAfterWrite(30, TimeUnit.SECONDS)
        .build();
  }

/*  public Product getProduct(String id) {
    return repository.findById(id);
  }*/

  public Product getProduct(String id) {

    Product cached = cache.getIfPresent(id);

    if (cached != null) {
      System.out.println("🟢 CACHE HIT: " + id);
      return cached;
    }

    System.out.println("🟡 CACHE MISS: " + id);

    Product product = repository.findById(id);

    cache.put(id, product);

    return product;
  }
 /* public CompletableFuture<Product> getProduct(String id) {
    Product cached = cache.getIfPresent(id);

    if (cached != null) {
      System.out.println("🟢 CACHE HIT: " + id);
      return CompletableFuture.completedFuture(cached);
    }
    return cached;
  }*/
}
