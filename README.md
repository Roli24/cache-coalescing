# Cache Coalescing Demo

Spring Boot demo showing:

1. Local in-memory cache with Caffeine
2. Request coalescing / single-flight with ConcurrentHashMap + CompletableFuture
3. Timeout protection with CompletableFuture.orTimeout()

## Run

Requires Java 17+ and Gradle 8.14+.

```bash
gradle wrapper --gradle-version 8.14.4
./gradlew test
./gradlew bootRun
```

## Try it

First request hits the simulated database:

```bash
curl http://localhost:8081/api/products/1
```

Second request is served by Caffeine:

```bash
curl http://localhost:8081/api/products/1
```

Stats:

```bash
curl http://localhost:8081/api/stats
```

## Request coalescing demo

Clear the cache, reset the DB counter and fire 100 concurrent requests for the same key:

```bash
./scripts/load-test.sh 100 100
```

Expected behavior: roughly one database call, not 100.

## Timeout demo

Change `demo.database.delay-ms` to 3000 while keeping `timeout-ms` at 1500, restart, and call:

```bash
curl -i http://localhost:8081/api/products/1
```

Important: `CompletableFuture.orTimeout()` limits completion of the future. For real JDBC, also configure a database/query timeout and a bounded connection pool; it does not magically cancel arbitrary blocking work.

## Architecture

```text
             HTTP REQUEST
                   |
                   v
            +-------------+
            | Caffeine L1 |
            +------+------+ 
                   |
             cache miss
                   |
                   v
      +---------------------------+
      | ConcurrentHashMap         |
      | key -> CompletableFuture  |
      +-------------+-------------+
                    |
                    v
              ONE DB REQUEST
                    |
                 timeout
                    |
                    v
                   DB
```

## Important limitation

The in-flight map and cache are local to one JVM. With multiple Spring Boot instances, each JVM has its own cache and coalescing state. That is the natural next step for a distributed-cache/Redis version.
