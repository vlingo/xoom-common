package io.vlingo.xoom.common.pool;

import java.util.concurrent.ThreadLocalRandom;

class TestResourceFactory implements ResourceFactory<Integer, Void> {

  private static ThreadLocalRandom Random = ThreadLocalRandom.current();

  @Override
  public Class<Integer> type() {
    return Integer.class;
  }

  @Override
  public Integer create(Void arguments) {
    return Random.nextInt();
  }

  @Override
  public Void defaultArguments() {
    return null;
  }

  @Override
  public Integer reset(Integer resource, Void arguments) {
    return resource;
  }

  @Override
  public void destroy(Integer integer) {
  }
}
