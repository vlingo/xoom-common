package io.vlingo.common.pool;

import java.util.Objects;

/**
 * {@link ResourcePool} statistics
 */
public final class ResourcePoolStats {

  /**
   * number of resource allocations
   */
  public final int allocations;

  /**
   * number of evicted resources
   */
  public final int evictions;

  /**
   * number of idle resources
   */
  public final int idle;

  /**
   * number of resources assigned to consumers
   */
  public final int inUse;

  /**
   * number of failures to acquire a resource from the pool
   */
  public final int misses;

  /**
   * The idle to inUse ratio
   */
  public final float idleToInUse;

  /**
   * @param allocations number of resource allocations
   * @param evictions   number of evicted resources
   * @param idle        number of idle resources
   * @param misses      number of failures to acquire a resource from the pool
   */
  public ResourcePoolStats(int allocations, int evictions, int idle, int misses) {
    this.allocations = allocations;
    this.evictions = evictions;
    this.idle = idle;
    this.inUse = allocations - evictions - idle;
    this.misses = misses;
    this.idleToInUse = (float) idle / Math.max(1, inUse);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ResourcePoolStats that = (ResourcePoolStats) o;
    return allocations == that.allocations &&
        evictions == that.evictions &&
        idle == that.idle &&
        misses == that.misses;
  }

  @Override
  public int hashCode() {
    return Objects.hash(allocations, evictions, idle, misses);
  }

  @Override
  public String toString() {
    return String.format("ResourcePoolStats(allocations: %d, evictions: %d, idle: %d, misses: %d, inUse: %d, idleToInUse: %f)",
        allocations, evictions, idle, misses, inUse, idleToInUse);
  }
}
