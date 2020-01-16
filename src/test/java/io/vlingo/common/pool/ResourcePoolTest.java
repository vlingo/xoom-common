package io.vlingo.common.pool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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

  static <R, A> void releaseIdempotenceForResourceLeaseReleaseTest(ResourcePool<R, A> pool) {
    int split = pool.size()/2;
    final Collection<ResourceLease<R>> used = take(pool, split % 2 == 0 ? split : split + 1);
    final Collection<ResourceLease<R>> rest = drain(pool);
    final ExecutorService exec = Executors.newFixedThreadPool(4);
    CompletionService<ResourceLease<R>> completionService =
        new ExecutorCompletionService<>(exec);

    CyclicBarrier barrier = new CyclicBarrier(2);

    List<Future<ResourceLease<R>>> futures = used.stream()
        .flatMap((lease) -> Stream.of(lease, lease))
        .map((lease) -> completionService.submit(runEndLease(lease, barrier), lease))
        .collect(Collectors.toCollection(ArrayList::new));

    for (Future<ResourceLease<R>> ignored : futures) {
      try {
        completionService.take().get();
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
    }

    rest.forEach(ResourceLease::end);
  }

  static <R, A> Collection<ResourceLease<R>> take(ResourcePool<R, A> pool, int take) {
    return IntStream.range(0, take).mapToObj((i) -> pool.borrow())
        .collect(Collectors.toCollection(ArrayList::new));
  }

  static <R, A> Collection<ResourceLease<R>> drain(ResourcePool<R, A> pool) {
    return take(pool, pool.size());
  }

  private static <R, A> Runnable runEndLease(ResourceLease<R> lease, CyclicBarrier barrier) {
    return () -> {
      try {
        barrier.await();
      } catch (InterruptedException | BrokenBarrierException e) {
        e.printStackTrace();
      } finally {
        lease.end();
      }
    };
  }
}
