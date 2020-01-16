// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
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
   * Lends a resource object from the pool.
   *
   * Acquiring a rough resource reference via this method might prove tough to manage in complicated work-flows.
   *
   * Unless you are writing synchronous code, with clear cut acquire / release semantics, prefer {@link ResourcePool#borrow()}.
   *
   * @return a resource
   */
  Resource acquire();

  /**
   * Lends a resource object from the pool.
   *
   * Acquiring a rough resource reference via this method might prove tough to manage in complicated work-flows.
   *
   * Unless you are writing synchronous code, with clear cut acquire / release semantics, prefer {@link ResourcePool#borrow()}.
   *
   * @param arguments the arguments
   * @return a resource
   */
  Resource acquire(Arguments arguments);


  /**
   * Answers with a {@link ResourceLease},
   * which manages the release of the encapsulated Resource.
   *
   * @return a ResourceLease
   */
  ResourceLease<Resource> borrow();

  /**
   * Answers with a {@link ResourceLease},
   * which manages the release of the encapsulated Resource.
   *
   * @param arguments the arguments
   * @return a ResourceLease
   */
  ResourceLease<Resource> borrow(Arguments arguments);

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
