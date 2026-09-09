package com.example.cache.web;

import com.example.cache.cache.ProductCacheService;
import com.example.cache.product.Product;
import com.example.cache.product.ProductRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api")
public class ProductController {
    private final ProductCacheService cacheService;
    private final ProductRepository repository;

    public ProductController(ProductCacheService cacheService, ProductRepository repository) {
        this.cacheService = cacheService;
        this.repository = repository;
    }

    @GetMapping("/products/{id}")
    public CompletableFuture<Product> getProduct(@PathVariable String id) {
        return cacheService.getProduct(id);
    }

  @GetMapping("/stats")
    public Map<String, Object> stats() {
        return Map.of(
                "databaseCalls", repository.getDatabaseCalls(),
                "cacheSize", cacheService.cacheSize(),
                "inFlightRequests", cacheService.inFlightRequests());
    }

    @PostMapping("/cache/clear")
    public void clearCache() { cacheService.clearCache(); }

    @PostMapping("/stats/reset")
    public void resetStats() {
        repository.reset();
        cacheService.clearCache();
    }
}
