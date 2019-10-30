package io.vlingo.common.pool;

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
