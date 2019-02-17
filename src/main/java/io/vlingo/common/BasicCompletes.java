// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common;

import io.vlingo.common.completes.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

public class BasicCompletes<T> implements Completes<T> {
  private static final int DEFAULT_TIMEOUT = 1000;
  private final Scheduler scheduler;
  private Operation<T, T, ?> operation;
  private Operation<T, ?, ?> current;
  protected final Sink<T, T> sink;

  private BasicCompletes(Scheduler scheduler, T defaultValue, boolean succeed) {
    this.scheduler = scheduler;
    this.sink = new Sink<>();
    this.operation = AndThen.identity(sink);
    this.current = operation;

    if (defaultValue != null) {
      if (succeed) {
        sink.onOutcome(defaultValue);
      } else {
        sink.onFailure(defaultValue);
      }
    } else if (!succeed) {
      sink.onFailure(null);
    }
  }

  public BasicCompletes(Scheduler scheduler) {
    this(scheduler, null, true);
  }

  public BasicCompletes(T defaultValue, boolean succeed) {
    this(null, defaultValue, succeed);
  }

  public BasicCompletes(T defaultValue) {
    this(null, defaultValue, true);
  }

  @Override
  public Completes<T> andThen(long timeout, T failedOutcomeValue, Function<T, T> function) {
    final AndThen nextOp = new AndThen(function, failedOutcomeValue);
    nextOp.addSubscriber(sink);
    sink.pipeIfNeeded(nextOp);
    current.addSubscriber(nextOp);
    current = nextOp;
    return this;
  }

  @Override
  public Completes<T> andThen(T failedOutcomeValue, Function<T, T> function) {
    return andThen(DEFAULT_TIMEOUT, failedOutcomeValue, function);
  }

  @Override
  public Completes<T> andThen(long timeout, Function<T, T> function) {
    return andThen(timeout, null, function);
  }

  @Override
  public Completes<T> andThen(Function<T, T> function) {
    return andThen(DEFAULT_TIMEOUT, null, function);
  }

  @Override
  public Completes<T> andThenConsume(long timeout, T failedOutcomeValue, Consumer<T> consumer) {
    final AndThenConsume nextOp = new AndThenConsume(consumer);
    nextOp.addSubscriber(sink);
    sink.pipeIfNeeded(nextOp);
    current.addSubscriber(nextOp);
    current = nextOp;
    return this;
  }

  @Override
  public Completes<T> andThenConsume(T failedOutcomeValue, Consumer<T> consumer) {
    return andThenConsume(DEFAULT_TIMEOUT, null, consumer);
  }

  @Override
  public Completes<T> andThenConsume(long timeout, Consumer<T> consumer) {
    return andThenConsume(timeout, null, consumer);
  }

  @Override
  public Completes<T> andThenConsume(Consumer<T> consumer) {
    return andThenConsume(DEFAULT_TIMEOUT, null, consumer);
  }

  @Override
  public <F, O> O andThenTo(long timeout, F failedOutcomeValue, Function<T, O> function) {
    final AndThenTo nextOp = new AndThenTo(scheduler, timeout, function, failedOutcomeValue);
    nextOp.addSubscriber(sink);
    sink.pipeIfNeeded(nextOp);
    current.addSubscriber(nextOp);
    current = nextOp;
    return (O) this;
  }

  @Override
  public <F, O> O andThenTo(F failedOutcomeValue, Function<T, O> function) {
    return andThenTo(DEFAULT_TIMEOUT, failedOutcomeValue, function);
  }

  @Override
  public <O> O andThenTo(long timeout, Function<T, O> function) {
    return andThenTo(timeout, null, function);
  }

  @Override
  public <O> O andThenTo(Function<T, O> function) {
    return andThenTo(DEFAULT_TIMEOUT, null, function);
  }

  @Override
  public Completes<T> otherwise(Function<T, T> function) {
    final Otherwise nextOp = new Otherwise(function);
    nextOp.addSubscriber(sink);
    sink.pipeIfNeeded(nextOp);
    current.addSubscriber(nextOp);
    current = nextOp;
    return this;
  }

  @Override
  public Completes<T> otherwiseConsume(Consumer<T> consumer) {
    final OtherwiseConsume nextOp = new OtherwiseConsume(consumer);
    nextOp.addSubscriber(sink);
    sink.pipeIfNeeded(nextOp);
    current.addSubscriber(nextOp);
    current = nextOp;
    return this;
  }

  @Override
  public Completes<T> recoverFrom(Function<Exception, T> function) {
    final Recover nextOp = new Recover(function);
    nextOp.addSubscriber(sink);
    sink.pipeIfNeeded(nextOp);
    current.addSubscriber(nextOp);
    current = nextOp;
    return this;
  }

  @Override
  public T await() {
    return await(DEFAULT_TIMEOUT);
  }

  @Override
  public T await(long timeout) {
    if (this.scheduler != null) {
      AtomicBoolean didTimeout = new AtomicBoolean(false);
      Cancellable timeoutCancellable = this.scheduler.scheduleOnce((sc, data) -> {
        didTimeout.set(true);
        throw new IllegalStateException("Completes<T> raised a timeout after " + timeout + "ms");
      }, null, 0, timeout);

      while (!sink.isCompleted() && !didTimeout.get()) {
      }
      timeoutCancellable.cancel();
    } else {
      while (!sink.isCompleted()) {}
    }

    return sink.outcome();
  }

  @Override
  public boolean isCompleted() {
    return sink.isCompleted();
  }

  @Override
  public boolean hasFailed() {
    return sink.hasFailed();
  }

  @Override
  public void failed() {
    sink.onFailure(null);
  }

  @Override
  public boolean hasOutcome() {
    return sink.hasOutcome();
  }

  @Override
  public T outcome() {
    return sink.outcome();
  }

  @Override
  public Completes<T> repeat() {
    return this;
  }

  @Override
  public <O> Completes<O> with(O outcome) {
    if (sink.hasFailed() || sink.hasErrored()) {
      return (Completes<O>) this;
    }

    operation.onOutcome((T) outcome);
    return (Completes<O>) this;
  }
}
