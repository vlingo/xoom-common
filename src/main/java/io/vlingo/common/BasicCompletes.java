// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common;

import io.vlingo.common.completes.*;

import java.util.function.Consumer;
import java.util.function.Function;

public class BasicCompletes<T> implements Completes<T> {
  private static final int DEFAULT_TIMEOUT = 1000;
  private final Scheduler scheduler;
  private Operation<T, T, ?> operation;
  private Operation<T, ?, ?> current;
  protected final Sink<T, T> sink;

  protected BasicCompletes(Scheduler scheduler, T defaultValue, boolean succeed, Sink<T, T> sink) {
    this.scheduler = scheduler;
    this.sink = sink;
    this.operation = AndThen.identity(scheduler, sink);
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
    this(scheduler, null, true, new Sink<>());
  }

  public BasicCompletes(T defaultValue, boolean succeed) {
    this(null, defaultValue, succeed, new Sink<>());
  }

  public BasicCompletes(T defaultValue) {
    this(null, defaultValue, true, new Sink<>());
  }

  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public <O> Completes<O> andThen(long timeout, O failedOutcomeValue, Function<T, O> function) {
    return apply(new AndThen(scheduler, timeout, function, failedOutcomeValue));
  }

  @Override
  public <O> Completes<O> andThen(O failedOutcomeValue, Function<T, O> function) {
    return andThen(DEFAULT_TIMEOUT, failedOutcomeValue, function);
  }

  @Override
  public <O> Completes<O> andThen(long timeout, Function<T, O> function) {
    return andThen(timeout, null, function);
  }

  @Override
  public <O> Completes<O> andThen(Function<T, O> function) {
    return andThen(DEFAULT_TIMEOUT, null, function);
  }

  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Completes<T> andThenConsume(long timeout, T failedOutcomeValue, Consumer<T> consumer) {
    return apply(new AndThenConsume(scheduler, timeout, consumer));
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
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public <F, O> O andThenTo(long timeout, F failedOutcomeValue, Function<T, O> function) {
    return (O) apply(new AndThenTo(scheduler, timeout, function, failedOutcomeValue));
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
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Completes<T> otherwise(Function<T, T> function) {
    return apply(new Otherwise(function));
  }

  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Completes<T> otherwiseConsume(Consumer<T> consumer) {
    return apply(new OtherwiseConsume(consumer));
  }

  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Completes<T> recoverFrom(Function<Exception, T> function) {
    return apply(new Recover(function));
  }

  @Override
  public T await() {
    return await(DEFAULT_TIMEOUT);
  }

  @Override
  public T await(long timeout) {
    return sink.await(timeout);
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
  @SuppressWarnings("unchecked")
  public <O> Completes<O> with(O outcome) {
    if (sink.hasFailed() || sink.hasErrored()) {
      return (Completes<O>) this;
    }

    operation.onOutcome((T) outcome);
    return (Completes<O>) this;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private <O> Completes<O> apply(Operation nextOp) {
    nextOp.addSubscriber(sink);
    sink.pipeIfNeeded(nextOp);
    current.addSubscriber(nextOp);
    current = nextOp;
    return (Completes<O>) this;
  }
}
