package io.vlingo.common.pool;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ElasticResourcePoolTest extends ResourcePoolTest {

  private static final int minIdle = 10;
  private static final int threads = minIdle * 10;
  private static final int clients = threads * 2;

  private final ResourcePool<Integer, Void> pool =
      new ElasticResourcePool<>(minIdle, new TestResourceFactory());


  @Test
  public void testInitialState() {
    assertEquals(minIdle, pool.size());
    ResourcePoolStats stats = pool.stats();
    assertEquals(pool.size(), stats.idle);
    assertEquals(minIdle, stats.allocations);
    assertEquals(0, stats.evictions);
    assertEquals(0, stats.inUse);
  }

  @Test
  public void testConcurrent() {
    testConcurrent(pool, threads, clients);
    ResourcePoolStats stats = pool.stats();
    assertEquals(stats.allocations - stats.idle, stats.evictions);
    assertEquals(threads - minIdle, stats.misses);
    assertEquals(stats.misses, stats.evictions);
    assertEquals(0, stats.inUse);
  }
}
