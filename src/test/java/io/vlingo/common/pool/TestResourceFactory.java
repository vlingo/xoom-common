package io.vlingo.common.pool;

import java.util.concurrent.ThreadLocalRandom;

class TestResourceFactory implements ResourceFactory<TestResourceFactory.TestResource, Void> {

  private static ThreadLocalRandom Random = ThreadLocalRandom.current();

  @Override
  public Class<TestResource> type() {
    return TestResource.class;
  }

  @Override
  public TestResource create(Void arguments) {
    return new TestResource();
  }

  @Override
  public Void defaultArguments() {
    return null;
  }

  @Override
  public TestResource reset(TestResource resource, Void arguments) {
    return resource;
  }

  @Override
  public void destroy(TestResource integer) {
  }

  static class TestResource {
    public TestResource() {
    }
  }
}
