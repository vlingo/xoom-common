package io.vlingo.common.pool;

/**
 * An abstract {@link ResourcePool} that implements {@link ResourcePool#acquire()}
 * using the default arguments from {@link ResourceFactory#defaultArguments()}.
 *
 * @param <Resource>  the type of the pooled resource
 * @param <Arguments> the type of the arguments to the acquire method
 */
abstract class AbstractResourcePool<Resource, Arguments> implements ResourcePool<Resource, Arguments> {

  final ResourceFactory<Resource, Arguments> factory;

  AbstractResourcePool(ResourceFactory<Resource, Arguments> factory) {
    this.factory = factory;
  }

  /**
   * Uses {@link ResourceFactory#defaultArguments()} to {@link ResourcePool#acquire(Object)} a resource object.
   *
   * @return a resource object with default arguments
   * @see ResourceFactory#defaultArguments()
   */
  @Override
  public Resource acquire() {
    return acquire(factory.defaultArguments());
  }
}
