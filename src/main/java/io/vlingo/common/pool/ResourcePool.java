// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.pool;

/**
 * @param <Resource>  the type of the pooled resource
 * @param <Arguments> the type of the arguments to the acquire method
 */
public interface ResourcePool<Resource, Arguments> {
  /**
   * Lends resource object from the pool.
   *
   * @return a resource
   */
  Resource acquire();

  /**
   * Lends resource object from the pool.
   *
   * @param arguments the arguments
   * @return a resource
   */
  Resource acquire(Arguments arguments);

  /**
   * Returns the lease of a resource object to the pool.
   *
   * @param resource the resource object
   */
  void release(Resource resource);

  /**
   * The number of available resource objects.
   *
   * @return number
   */
  int size();

  /**
   * Statistics at the time of invocation.
   *
   * @return the {@link ResourcePoolStats}
   */
  ResourcePoolStats stats();
}
