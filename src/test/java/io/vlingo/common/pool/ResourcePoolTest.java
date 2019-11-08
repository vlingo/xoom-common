package io.vlingo.common.pool;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

abstract class ResourcePoolTest {

  private static ThreadLocalRandom Random = ThreadLocalRandom.current();

  static <R, A> int testConcurrent(ResourcePool<R, A> pool, int threads, int clients) {
    final ExecutorService exec = Executors.newFixedThreadPool(threads);
    CompletionService<Integer> completionService =
        new ExecutorCompletionService<>(exec);

    Callable<Integer> call = () -> {
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
      }

      return pool.stats().idle;
    };

    int maxIdle = 0;

    List<Future<Integer>> futures = IntStream.range(0, clients)
        .mapToObj((int i) -> completionService.submit(call))
        .collect(Collectors.toList());

    for (@SuppressWarnings("unused") Future<Integer> f : futures) {
      try {
        maxIdle = Math.max(maxIdle, completionService.take().get());
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
    }

    return maxIdle;
  }
}
