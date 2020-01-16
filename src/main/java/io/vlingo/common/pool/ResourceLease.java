package io.vlingo.common.pool;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

public final class ResourceLease<Resource> {

  private final AtomicBoolean isNew;
  private final AtomicInteger rfCount;
  private final Resource resource;
  private final Consumer<Resource> release;

  ResourceLease(final Resource resource, final Consumer<Resource> release) {
    this.isNew = new AtomicBoolean(true);
    this.rfCount = new AtomicInteger(0);
    this.resource = resource;
    this.release = release;
  }

  public final void use(Consumer<Resource> operation) {
    operation.accept(use());
  }

  public final <T> T use(Function<Resource, T> operation) {
    return operation.apply(use());
  }

  public final Resource use() {
    isNew.set(false);
    if (!isActive()) {
      throw new IllegalStateException("Attempt to use resource from a lease that has ended");
    }
    int a = rfCount.incrementAndGet();
    if (a > 0) {
      return resource;
    }
    else {
      rfCount.set(-1);
      throw new IllegalStateException("Attempt to use resource from a lease that has ended");
    }
  }

  public final boolean isActive() {
    return rfCount.get() >= 0;
  }

  public final boolean end() {
    int a = rfCount.get();
    if (a == -1) {
      // already closed
      return true;
    }
    else if (a == 0) {
      if (isNew.compareAndSet(true, false)) {
        // release unused
        release();
        return true;
      }
      else {
        // in use or someone else is releasing
        return rfCount.get() <= 0;
      }
    }
    else {
      // still used
      if (rfCount.decrementAndGet() == 0) {
        // last one closes the door
        release();
        return true;
      }
      else {
        return false;
      }
    }
  }

  private void release() {
    rfCount.set(-1);
    release.accept(resource);
  }
}
