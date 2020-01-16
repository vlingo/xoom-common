package io.vlingo.common.pool;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class ResourceLeaseTest {

  private static final Logger log =
      LoggerFactory.getLogger(ResourceLeaseTest.class);

  private final Random random = new Random();

  private Consumer<String> release(AtomicInteger count) {
    return (r) -> count.incrementAndGet();
  }

  @Test
  public void testUnusedLeaseRelease() {
    AtomicInteger count = new AtomicInteger(0);
    ResourceLease<String> lease = new ResourceLease<>("hello", release(count));
    assert lease.end();
    assert !lease.isActive();
    assert count.get() == 1;
  }

  @Test(expected = IllegalStateException.class)
  public void testReleasedLeaseCantBeUsed() {
    AtomicInteger count = new AtomicInteger(0);
    ResourceLease<String> lease = new ResourceLease<>("hello", release(count));
    assert lease.end();
    lease.use();
  }


  @Test
  public void testUsedLeaseRelease() {
    AtomicInteger count = new AtomicInteger(0);
    ResourceLease<String> lease = new ResourceLease<>("hello", release(count));
    assertTrue("Lease is not active", lease.isActive());

    assertEquals("Wrong value", "hello", lease.use());
    assertEquals("Wrong value", "hello", lease.use());
    assertEquals("Wrong value", "hello", lease.use());

    assertTrue("Lease is not active", lease.isActive());

    assertFalse(lease.end());
    assertFalse(lease.end());
    assertTrue(lease.end());

    assertFalse("Lease remained active", lease.isActive());

    assertNotEquals("Was not released", 0, count.get());
    assertEquals("Was released more than once", 1, count.get());
    assertFalse("Lease still active", lease.isActive());
  }

  @Test
  public void testParallel() {

    AtomicInteger count = new AtomicInteger(0);
    ResourceLease<String> lease = new ResourceLease<>("hello", release(count));

    final ExecutorService exec = Executors.newFixedThreadPool(4);
    CompletionService<String> completionService =
        new ExecutorCompletionService<>(exec);


    List<Future<String>> futures0 = IntStream.range(0, 16).mapToObj(i -> completionService.submit(() -> {
      try {
        lease.use((s) -> {
          try {
            Thread.sleep(random.nextInt(100));
            assert lease.isActive();
            assert count.get() == 0;
          } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
          }
        });
      } finally {
        lease.end();
      }
    }, "")).collect(Collectors.toCollection(ArrayList::new));

    for (Future<String> ignored : futures0) {
      try {
        completionService.take().get();
      } catch (InterruptedException | ExecutionException e) {
        log.error(e.getMessage(), e);
      }
    }

    assertNotEquals("Was not released", 0, count.get());
    assertEquals("Was released more than once", 1, count.get());
    assertFalse("Lease still active", lease.isActive());
  }

  @Test
  public void testAsynchronous() throws InterruptedException {

    final CountDownLatch latch = new CountDownLatch(2);

    final AtomicInteger count = new AtomicInteger(0);
    final ResourceLease<String> lease = new ResourceLease<>("hello", release(count));
    final BiFunction<ResourceLease<String>, Integer, Consumer<SignalType>> release = (l, i) -> s -> {
      log.debug("release {}", i);
      l.end();
      latch.countDown();
    };

    final Scheduler scheduler = Schedulers.newParallel("parallel-scheduler", 4);

    final Function<Integer, Function<String, Mono<? extends String>>> stringMonoFunction = i -> resource -> Mono.fromCallable(() -> {
      final int sleep = random.nextInt(100);
      log.debug("doing {} for {}", i, sleep);
      Thread.sleep(sleep);
      return resource;
    }).subscribeOn(scheduler);

    // sequential async operations
    Mono.just(lease.use())
        .flatMap(stringMonoFunction.apply(1))
        .flatMap(stringMonoFunction.apply(2))
        // use more than once or release early
        .flatMap(s -> lease.use(stringMonoFunction.apply(3))
            .doFinally(release.apply(lease, 3)))
        .flatMap(stringMonoFunction.apply(4))
        .flatMap(stringMonoFunction.apply(5))
        .flatMap(stringMonoFunction.apply(6))
        .flatMap(stringMonoFunction.apply(7))
        .doFinally(release.apply(lease, 0))
        .block();

    latch.await(1, TimeUnit.SECONDS);

    assertNotEquals("Was not released", 0, count.get());
    assertEquals("Was released more than once", 1, count.get());
    assertFalse("Lease still active", lease.isActive());

  }
}


