// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.pool;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An elastic {@link ResourcePool} implementation backed by a {@link ConcurrentLinkedQueue}.
 * <p>
 * This implementation will allocate new resource objects as needed in case the pool is exhausted.
 * <p>
 * Resource objects will return to the pool only when the idle to inUse ratio is less than the desired minimum idle resources.
 * When the idle to inUse ratio is higher than the minimum idle resources, the returning resources are evicted. Compaction
 * of the resource cache is automatically triggered when the idle count is greater than the number of desired resources.
 * <p>
 * Compaction attempts to half the size of the idle cache, reaching the desired minimum resource count as the cache drains.
 * <p>
 * See {@link Config} for configuration details.
 * <p>
 * Resource object allocation, reset and destruction is managed by {@link ResourceFactory} implementation for the same type of Resource and Arguments.
 *
 * @param <Resource>  the type of resource
 * @param <Arguments> the type of arguments for the {@link ResourceFactory}
 */
public class ElasticResourcePool<Resource, Arguments> extends AbstractResourcePool<Resource, Arguments> {

  private final AtomicInteger idle = new AtomicInteger(0);
  private final AtomicInteger allocations = new AtomicInteger(0);
  private final AtomicInteger evictions = new AtomicInteger(0);
  private final AtomicInteger misses = new AtomicInteger(0);

  private final ConcurrentLinkedQueue<Resource> cache = new ConcurrentLinkedQueue<>();

  private final int minIdle;

  /**
   * Creates an {@link ElasticResourcePool} instance initialized to pool from {@link Config#minIdle} resource objects.
   * <p>
   * Resource object instances will be created using {@link ResourceFactory#create(Object)}
   * with the default arguments specified in {@link ResourceFactory#defaultArguments()}.
   *
   * @param config  the Config parameters
   * @param factory the resource object factory
   */
  public ElasticResourcePool(Config config, ResourceFactory<Resource, Arguments> factory) {
    this(config.minIdle, factory);
  }

  ElasticResourcePool(int minIdle, ResourceFactory<Resource, Arguments> factory) {
    super(factory);
    this.minIdle = minIdle;

    this.initialize();
  }

  private void initialize() {
    for (int i = 0; i < minIdle; i++) {
      allocations.incrementAndGet();
      cache(factory.create(factory.defaultArguments()));
    }
  }

  private void cache(Resource resource) {
    idle.incrementAndGet();
    cache.offer(resource);
  }

  /**
   * Gets a resource object from the pool and resets it,
   * or creates a new one if the pool is exhausted.
   *
   * @param arguments the arguments
   * @return a resource object
   * @see ResourcePool#acquire(Object)
   * @see ResourceFactory#create(Object)
   * @see ResourceFactory#reset(Object, Object)
   */
  @Override
  public Resource acquire(Arguments arguments) {
    Resource resource = cache.poll();
    if (resource == null) {
      allocations.incrementAndGet();
      misses.incrementAndGet();
      resource = factory.create(arguments);
    } else {
      idle.decrementAndGet();
      resource = factory.reset(resource, arguments);
    }
    return resource;
  }

  /**
   * Releases the object back into the pool, or evicts it when the idle to inUse ratio
   * is higher than the desired minimum number of resources.
   *
   * @param resource the resource object
   * @see ResourceFactory#destroy(Object)
   */
  @Override
  public void release(final Resource resource) {
    final ResourcePoolStats stats = stats();
    if (stats.idleToInUse < minIdle) {
      idle.incrementAndGet();
      cache.offer(resource);
    } else if (idle.get() > minIdle) {
      evict(resource);
      compact();
    } else {
      evict(resource);
    }
  }

  private void evict(Resource resource) {
    evictions.incrementAndGet();
    factory.destroy(resource);
  }

  private void compact() {
    while (idle.get() > target()) {
      Resource resource = cache.poll();
      if (resource == null) {
        return;
      }
      if (idle.getAndDecrement() > target()) {
        evict(resource);
      } else {
        idle.incrementAndGet();
        cache.offer(resource);
      }
    }
  }

  private int target() {
    return Math.max(minIdle, (int) (idle.get() * 0.5));
  }

  @Override
  public int size() {
    return cache.size();
  }

  @Override
  public ResourcePoolStats stats() {
    return new ResourcePoolStats(
        allocations.get(), evictions.get(), idle.get(), misses.get());
  }

  /**
   * {@link ElasticResourcePool} configuration parameters.
   */
  public static final class Config {

    final int minIdle;

    /**
     * @param minIdle the minimum number of resource objects to retain in the idle cache
     */
    Config(int minIdle) {
      this.minIdle = minIdle;
    }

    public static Config of(int minIdle) {
      return new Config(minIdle);
    }
  }
}
