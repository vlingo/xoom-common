package io.vlingo.common.pool;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ElasticResourcePoolTest extends ResourcePoolTest {

  private static final int minIdle = 10;
  private static final int threads = minIdle * 10;
  private static final int clients = threads * 2;

  private final ResourcePool<TestResourceFactory.TestResource, Void> pool =
      new ElasticResourcePool<>(minIdle, new TestResourceFactory());

  @Test
  public void testInitialState() {
    assertEquals("the pool did not start with the desired number of resources",
        minIdle, pool.size());

    ResourcePoolStats stats = pool.stats();
    assertEquals("the number of idle resources in stats did not match the size of the pool",
        pool.size(), stats.idle);
    assertEquals("the number of allocations in stats did not match the desired number of initial resources",
        minIdle, stats.allocations);
    assertEquals("the number of evictions in stats did not start at 0",
        0, stats.evictions);
    assertEquals("the number of inUse in stats did not start at 0",
        0, stats.inUse);
  }

  @Test
  public void testConcurrent() {
    int maxIdle = testConcurrent(pool, threads, clients);
    ResourcePoolStats stats = pool.stats();
    assertEquals("the number of idle in stats is not equal to the pool size",
        stats.idle, pool.size());
    assertEquals("the number of inUse in stats is not equal to 0",
        0, stats.inUse);
    assertTrue("the pool did not scale up to accommodate more threads than the initial resources",
        maxIdle > minIdle);
    assertTrue("the pool did not scale down to release memory as threads exited",
        maxIdle > stats.idle);
    assertTrue("the pool shrank below the desired minimum number of resources",
        stats.idle >= minIdle);
    assertTrue("the pool didn't count evictions",
        0 < stats.evictions);
  }

  @Test
  public void testReleaseIdempotenceWithResourceLease() {
    ResourcePool<TestResourceFactory.TestResource, Void> pool =
        new ElasticResourcePool<>(200, new TestResourceFactory());
    releaseIdempotenceForResourceLeaseReleaseTest(pool);
    Set<TestResourceFactory.TestResource> unique = new HashSet<>(pool.size());
    IntStream.range(0, pool.size()).forEach((i) ->
      assertTrue("ResourcePool::release is not idempotent", unique.add(pool.acquire()))
    );
  }
}
