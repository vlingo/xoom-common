// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.common.pool;

/**
 * Manages creation, reset and destruction of resource objects created
 * in combination with a {@link ResourcePool} implementation.
 *
 * @param <Resource>  the type of resource
 * @param <Arguments> the type fo arguments to create and reset methods
 */
public interface ResourceFactory<Resource, Arguments> {
  /**
   * @return the type of resource
   */
  Class<Resource> type();

  /**
   * Creates a resource object.
   *
   * @param arguments the arguments
   * @return a new resource object
   */
  Resource create(Arguments arguments);

  /**
   * The default arguments to use for initial resource creation.
   *
   * @return the arguments
   */
  Arguments defaultArguments();

  /**
   * Resets a resource object for others to use.
   *
   * @param resource  the resource object
   * @param arguments the arguments
   * @return the reset resource object
   */
  Resource reset(Resource resource, Arguments arguments);

  /**
   * Destroys a resource object.
   *
   * @param resource the resource object
   */
  void destroy(Resource resource);
}
