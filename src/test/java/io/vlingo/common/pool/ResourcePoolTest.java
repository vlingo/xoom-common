package io.vlingo.common.pool;

import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

abstract class ResourcePoolTest {

  private static ThreadLocalRandom Random = ThreadLocalRandom.current();

  static <R, A> void testConcurrent(ResourcePool<R, A> pool, int threads, int clients) {
    final ExecutorService exec = Executors.newFixedThreadPool(threads);
    CompletionService<Void> completionService =
        new ExecutorCompletionService<>(exec);

    Callable<Void> call = () -> {
      R resource = null;
      try {
        do {
          resource = pool.acquire();
        } while (resource == null);
        Thread.sleep(Random.nextInt(10, 100));
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        pool.release(resource);
        ResourcePoolStats stats = pool.stats();
        assertEquals(stats.allocations, stats.evictions + stats.idle + stats.inUse);
      }
      return null;
    };

    IntStream.range(0, clients)
        .mapToObj((int i) -> completionService.submit(call))
        .collect(Collectors.toList())
        .forEach((f) -> {
          try {
            completionService.take().get();
          } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
          }
        });

    assertEquals(pool.stats().idle, pool.size());
  }
}
