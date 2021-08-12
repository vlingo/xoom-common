// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.common.completes;

import io.vlingo.xoom.common.Cancellable;
import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.common.Scheduled;
import io.vlingo.xoom.common.Scheduler;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

public class FutureCompletes<T> implements Completes<T> {
  private static final long NoTimeout = -1L;

  private final State<T> state;

  public FutureCompletes(final CompletesId id, final Scheduler scheduler) {
    this.state = new State<>(id, scheduler, OutcomeType.Some);
  }

  public FutureCompletes(final Scheduler scheduler) {
    this(Completes.completesId(), scheduler);
  }

  public FutureCompletes(final CompletesId id, final T outcome, final boolean successful) {
    this(id, (Scheduler) null);

    if (!successful) {
      useFailedOutcomeOf(outcome);
    }

    with(outcome);
  }

  public FutureCompletes(final T outcome, final boolean successful) {
    this(Completes.completesId(), outcome, successful);
  }

  public FutureCompletes(final CompletesId id, final T outcome) {
    this(id, outcome, true);
  }

  public FutureCompletes(final T outcome) {
    this(Completes.completesId(), outcome);
  }

  public FutureCompletes(final CompletesId id) {
    this(id, (Scheduler) null);
  }

  public FutureCompletes() {
    this(Completes.completesId());
  }

  @Override
  public <O> Completes<O> andThen(final long timeout, final O failedOutcomeValue, final Function<T, O> function) {
    return new FutureCompletes<>(state.nextForFunction(failedOutcomeValue, function)).timeoutWithin(timeout);
  }

  @Override
  public <O> Completes<O> andThen(final O failedOutcomeValue, final Function<T, O> function) {
    return andThen(NoTimeout, failedOutcomeValue, function);
  }

  @Override
  public <O> Completes<O> andThen(final long timeout, final Function<T, O> function) {
    return andThen(timeout, null, function);
  }

  @Override
  public <O> Completes<O> andThen(final Function<T, O> function) {
    return andThen(NoTimeout, null, function);
  }

  @Override
  public Completes<T> andThenConsume(final long timeout, final T failedOutcomeValue, final Consumer<T> consumer) {
    return new FutureCompletes<>(state.nextForConsumer(failedOutcomeValue, consumer)).timeoutWithin(timeout);
  }

  @Override
  public Completes<T> andThenConsume(final T failedOutcomeValue, final Consumer<T> consumer) {
    return andThenConsume(NoTimeout, failedOutcomeValue, consumer);
  }

  @Override
  public Completes<T> andThenConsume(final long timeout, final Consumer<T> consumer) {
    return andThenConsume(timeout, null, consumer);
  }

  @Override
  public Completes<T> andThenConsume(final Consumer<T> consumer) {
    return andThenConsume(NoTimeout, null, consumer);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <F, O> O andThenTo(final long timeout, final F failedOutcomeValue, final Function<T, O> function) {
    return (O) new FutureCompletes<>(state.nextForFunctionAsync((O) failedOutcomeValue, function)).timeoutWithin(timeout);
  }

  @Override
  public <F, O> O andThenTo(final F failedOutcomeValue, final Function<T, O> function) {
    return andThenTo(NoTimeout, failedOutcomeValue, function);
  }

  @Override
  public <O> O andThenTo(final long timeout, final Function<T, O> function) {
    return andThenTo(timeout, null, function);
  }

  @Override
  public <O> O andThenTo(final Function<T, O> function) {
    return andThenTo(NoTimeout, null, function);
  }

  @Override
  @SuppressWarnings({"unchecked", "rawtypes"})
  public <E> Completes<T> otherwise(final Function<E, T> function) {
    return new FutureCompletes<>(state.nextForFunction(null, (Function) function, true));
  }

  @Override
  public Completes<T> otherwiseConsume(final Consumer<T> consumer) {
    return new FutureCompletes<>(state.nextForConsumer(null, consumer, true));
  }

  @Override
  public Completes<T> recoverFrom(final Function<Throwable, T> function) {
    return new FutureCompletes<>(state.nextForExceptional(function));
  }

  @Override
  @SuppressWarnings("unchecked")
  public <O> Completes<O> andFinally() {
    // no-op
    return (Completes<O>) this;
  }

  @Override
  public <O> Completes<O> andFinally(final Function<T, O> function) {
    return new FutureCompletes<>(state.nextForFunction(null, function));
  }

  @Override
  public void andFinallyConsume(final Consumer<T> consumer) {
    new FutureCompletes<>(state.nextForConsumer(null, consumer));
  }

  @Override
  public <O> O await() {
    return state.await();
  }

  @Override
  public <O> O await(final long timeout) {
    return state.await(timeout);
  }

  @Override
  public boolean isCompleted() {
    return state.isCompleted();
  }

  @Override
  public boolean hasFailed() {
    return state.hasFailed();
  }

  @Override
  public void failed() {
    with(state.failureValue());
  }

  @Override
  public void failed(final Exception exception) {
    state.exceptional(exception);
  }

  @Override
  public CompletesId id() {
    return state.id();
  }

  @Override
  public boolean hasOutcome() {
    return state.hasOutcome();
  }

  @Override
  public T outcome() {
    return state.outcome();
  }

  @Override
  public Completes<T> repeat() {
    state.repeat();
    return this;
  }

  @Override
  public Completes<T> timeoutWithin(final long timeout) {
    state.startTimer(timeout);
    return this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <F> Completes<T> useFailedOutcomeOf(final F failedOutcomeValue) {
    state.registerFailureOutcomeValue((T) failedOutcomeValue);
    return this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <O> Completes<O> with(final O outcome) {
    state.resetAll();
    state.complete(outcome);
    return (Completes<O>) this;
  }

  @Override
  public String toString() {
    return "FutureCompletes [id=" + id() + ", next=" + (state.next != null ? state.next.id() : "(none)") + " state=" + state + "]";
  }

  private FutureCompletes(final State<T> state) {
    this.state = state;
  }

  private CompletableFuture<T> asCompletableFuture() {
    return state.future();
  }

  //////////////////////////////////////////////////////
  // State
  //////////////////////////////////////////////////////

  private static class State<T> implements Scheduled<Object> {
    private Cancellable cancellable;
    private State<T> next;
    private final State<T> previous;
    private final Function<State<T>, CompletableFuture<T>> futureFactory;
    private final AtomicReference<CompletableFuture<T>> future;
    private final AtomicBoolean failed;
    private final AtomicReference<T> failureValue;
    private final boolean handlesFailure;
    private final CompletesId id;
    private final AtomicReference<Outcome<T>> outcome;
    private final OutcomeType outcomeType;
    private final Scheduler scheduler;
    private final AtomicBoolean timedOut = new AtomicBoolean(false);
    private final AtomicBoolean repeats = new AtomicBoolean(false);

    State(final CompletesId id, final State<T> previous, final Scheduler scheduler, final Function<State<T>, CompletableFuture<T>> futureFactory, final T failedOutcomeValue, final boolean handlesFailure, final OutcomeType outcomeType) {
      this.id = id;
      this.previous = previous;
      this.scheduler = scheduler;
      this.failed = new AtomicBoolean(false);
      this.failureValue = new AtomicReference<>(failedOutcomeValue);
      this.handlesFailure = handlesFailure;
      this.outcome = new AtomicReference<>(UncompletedOutcome.instance());
      this.outcomeType = outcomeType;
      this.futureFactory = futureFactory;
      this.future = new AtomicReference<>(this.futureFactory.apply(this));
      if (this.previous != null) {
        this.previous.next = this;
      }
    }

    State(final CompletesId id, final Scheduler scheduler, final OutcomeType outcomeType) {
      this(id, null, scheduler, (state) -> new CompletableFuture<>(), null, false, outcomeType);
    }

    @SuppressWarnings("unchecked")
    <O> O await() {
      if (isCompleted()) {
        return (O) outcome();
      }

      try {
        future().get();
        return (O) outcome();
      } catch (Exception e) {
        if (this.hasFailed()) {
          return (O) outcome();
        }
        // fall through
      }

      return null;
    }

    @SuppressWarnings("unchecked")
    <O> O await(final long timeout) {
      if (isCompleted()) {
        return (O) outcome();
      }

      try {
        future().get(timeout, TimeUnit.MILLISECONDS);
        return (O) outcome();
      } catch (Exception e) {
        if (this.hasFailed()) {
          return (O) outcome();
        }
        // fall through
      }

      return null;
    }

    @SuppressWarnings("unchecked")
    <O> void complete(final O outcome) {
      if (outcome instanceof Throwable) {
        exceptional((Throwable) outcome);
        return;
      }

      T realOutcome = (T) outcome;

      if (isFailureValue(realOutcome)) {
        realOutcome = failureValue();
        fail(realOutcome, isTimedOut());
      }

      if (!hasOutcome()) {
        outcome(new CompletedOutcome<>(realOutcome));
      }

      if (!future().isDone()) {
        future().complete(realOutcome);
      }
    }

    void repeat() {
      this.repeats.set(true);
      if (hasPrevious()) {
        previous().repeat();
      }
    }

    void resetAll() {
      if (isCompleted() && hasRepeats()) {
        first().resetAllFollowing();
      }
    }

    void exceptional(final Throwable t) {
      future().completeExceptionally(t);
    }

    T outcome() {
      if (isCompleted()) {
        return ultimateOutcome();
      }
      return null;
    }

    void outcome(final Outcome<T> outcome) {
      this.outcome.set(outcome);
    }

    boolean hasOutcome() {
      return outcome.get().value() != null;
    }

    boolean isCompleted() {
      return outcome.get().isCompleted() || future().isDone();
    }

    boolean hasFailed() {
      if (failed.get()) {
        return true;
      }

      final boolean hasFailed = future().isCancelled() || future().isCompletedExceptionally();

      failed.set(hasFailed);

      return hasFailed;
    }

    T failureValue() {
      return failureValue.get();
    }

    boolean isFailureValue(final T candidateFailureValue) {
      if (isTimedOut()) {
        return true;
      }

      final T currentFailureValue = failureValue.get();

      if (currentFailureValue == candidateFailureValue) return true;

      return currentFailureValue != null && currentFailureValue.equals(candidateFailureValue);
    }

    void registerFailureOutcomeValue(final T failedOutcomeValue) {
      final T currentFailureValue = failureValue.get();

      if (currentFailureValue == failedOutcomeValue) return;

      if (currentFailureValue != null && failedOutcomeValue == null) return;

      if (currentFailureValue != null && currentFailureValue.equals(failedOutcomeValue)) return;

      failureValue.set(failedOutcomeValue);
    }

    void startTimer(final long timeout) {
      if (timeout > 0 && scheduler != null && cancellable == null) {
        // 2L delayBefore prevents timeout until after return from here
        cancellable = scheduler.scheduleOnce(this, null, 0L, timeout);
      }
    }

    void cancelTimer() {
      if (cancellable != null) {
        cancellable.cancel();
        cancellable = null;
      }
    }

    void timedOut() {
      failAllFollowing(failureValue(), true);
    }

    boolean isTimedOut() {
      return timedOut.get();
    }

    @Override
    public void intervalSignal(final Scheduled<Object> scheduled, final Object data) {
      cancelTimer();
      if (future().isDone()) return;
      timedOut();
    }

    @Override
    public String toString() {
      return "State [id=" + id
              + ", outcome=" + (outcome == null ? "(none)" : outcome.get())
              + ", failed=" + failed.get()
              + ", handlesFailure=" + handlesFailure
              + ", timedOut=" + timedOut.get() + "]";
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    State<T> nextForConsumer(final T failedOutcomeValue, final Consumer<T> consumer, final boolean handlesFailure) {
      Function<State<T>, CompletableFuture<Void>> factory = (State<T> state) -> state.previousFuture().thenAccept(state.consumerWrapper(consumer));
      return new State(Completes.completesId(), this, scheduler, factory, failedOutcomeValue, handlesFailure, OutcomeType.None);
    }

    State<T> nextForConsumer(final T failedOutcomeValue, final Consumer<T> consumer) {
      return nextForConsumer(failedOutcomeValue, consumer, false);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    State<T> nextForExceptional(final Function<Throwable, T> function) {
      Function<State<T>, CompletableFuture<T>> factory = (State<T> state) -> state.previousFuture().exceptionally(state.functionExceptionWrapper(function));
      return new State(Completes.completesId(), this, scheduler, factory, null, false, OutcomeType.Some);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    <O> State<O> nextForFunction(final O failedOutcomeValue, final Function<T, O> function, final boolean handlesFailure) {
      Function<State<T>, CompletableFuture<O>> factory = (State<T> state) -> state.previousFuture().thenApply(state.functionWrapper(function));
      return new State(Completes.completesId(), this, scheduler, factory, failedOutcomeValue, handlesFailure, OutcomeType.Some);
    }

    <O> State<O> nextForFunction(final O failedOutcomeValue, final Function<T, O> function) {
      return nextForFunction(failedOutcomeValue, function, false);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    <O> State<O> nextForFunctionAsync(final O failedOutcomeValue, final Function<T, O> function) {
      Function<State<T>, CompletableFuture<O>> factory = (State<T> state) -> state.previousFuture().thenComposeAsync(state.composableFunction(state.functionWrapper(function)));
      return new State(Completes.completesId(), this, scheduler, factory, failedOutcomeValue, false, OutcomeType.Some);
    }

    @SuppressWarnings("unchecked")
    private <O> Function<T, CompletableFuture<O>> composableFunction(final Function<T, O> userFunction) {
      return (T value) -> {
        O outcome = userFunction.apply(value);
        if (outcome instanceof FutureCompletes) {
          return ((FutureCompletes<O>) outcome).asCompletableFuture();
        }
        return CompletableFuture.completedFuture(outcome);
      };
    }

    private Consumer<T> consumerWrapper(final Consumer<T> userConsumer) {
      return (value) -> {
        try {

          // the current `future` is unsafe to reference here since it might not be initialised yet

          if (previous.outcomeType == OutcomeType.None) {
            value = previous.outcome();
          }

          if (handlesFailure && !previous.hasFailed()) {
            return;
          }

          if (previous.hasFailed() && !previous.handlesFailure) {
            fail(previous.failureValue(), previous.isTimedOut());
            if (!handlesFailure) {
              return;
            }
          } else if (isFailureValue(value)) {
            fail(failureValue(), isTimedOut());
            if (!handlesFailure) {
              return;
            }
          }

          userConsumer.accept(value);

        } catch (Throwable cause) {
          fail(failureValue(), isTimedOut());
          throw cause;
        }
      };
    }

    private Function<Throwable, T> functionExceptionWrapper(final Function<Throwable, T> userFunction) {
      return (Throwable e) -> {
          // the current `future` is unsafe to reference here since it might not be initialised yet

          timedOut.set(previous.isTimedOut());
          failed.set(true);

          return userFunction.apply(unwrap(e));
      };
    }

    @SuppressWarnings("unchecked")
    private <O> Function<T, O> functionWrapper(final Function<T, O> userFunction) {
      return (value) -> {
        try {
          // the current `future` is unsafe to reference here since it might not be initialised yet

          if (previous.outcomeType == OutcomeType.None) {
            value = previous.outcome();
          }

          if (handlesFailure && !previous.hasFailed()) {
            return (O) value;
          }

          if (previous.hasFailed() && !previous.handlesFailure) {
            fail(previous.failureValue(), previous.isTimedOut());
            if (!handlesFailure) {
              return (O) previous.failureValue();
            }
          } else if (isFailureValue(value)) {
            fail(failureValue(), isTimedOut());
            if (!handlesFailure) {
              return (O) failureValue();
            }
          }

          return userFunction.apply(value);
        } catch (Exception cause) {
          fail(failureValue(), isTimedOut());
          throw cause;
        }
      };
    }

    private void resetAllFollowing() {
      this.outcome.set(UncompletedOutcome.instance());
      this.timedOut.set(false);
      this.failed.set(false);
      this.future(futureFactory.apply(this));
      if (hasNext()) {
        next().resetAllFollowing();
      }
    }

    private void failAllFollowing(final T failureValue, final boolean hasTimedOut) {
      // could overwrite possible otherwise(v -> f(v))
      // computed outcome unless use short circuit here
      if (handlesFailure) return;

      fail(failureValue, hasTimedOut);

      if (hasNext()) {
        next().failAllFollowing(failureValue, hasTimedOut);
      }
    }

    private void fail(final T failureValue, final boolean hasTimedOut) {
      this.failureValue.set(failureValue);
      this.timedOut.set(hasTimedOut);
      this.failed.set(true);
      if (!handlesFailure) {
        outcome(new CompletedOutcome<>(failureValue));
      }
    }

    private T ultimateOutcome() {
      if (hasNext()) {
        final State<T> nextState = next();
        if (nextState.isCompleted()) {
          return nextState.ultimateOutcome();
        }
      }

      final Outcome<T> maybeOutcome = this.outcome.get();

      if (maybeOutcome.isCompleted()) {
        return maybeOutcome.value();
      }

      if (outcomeType == OutcomeType.None) {
        Outcome<T> outcome = previousOutcome();
        if (null != outcome && outcome.isCompleted()) {
          outcome(outcome);
          return outcome.value();
        }
      }

      return currentOutcome().value();
    }

    private Outcome<T> previousOutcome() {
      if (hasPrevious()) {
        if (previous().outcomeType == OutcomeType.Some) {
          return previous().currentOutcome();
        }
        return previous().previousOutcome();
      }
      return null;
    }

    private Outcome<T> currentOutcome() {
      final Outcome<T> maybeOutcome = this.outcome.get();

      if (maybeOutcome.isCompleted()) {
        return maybeOutcome;
      }

      try {
        if (future().isDone()) {
          outcome(new CompletedOutcome<>(future().get()));
        }
      } catch (Exception e) {
        // fall through
      }

      return this.outcome.get();
    }

    private boolean hasRepeats() {
      return this.repeats.get();
    }

    private boolean hasPrevious() {
      return this.previous != null;
    }

    private State<T> previous() {
      return this.previous;
    }

    private State<?> first() {
      State<?> first = this;
      while (first.previous != null) {
        first = first.previous;
      }
      return first;
    }

    private boolean hasNext() {
      return this.next != null;
    }

    private State<T> next() {
      return this.next;
    }

    private CompletableFuture<T> future() {
      return future.get();
    }

    private void future(CompletableFuture<T> previousFuture) {
      future.set(previousFuture);
    }

    private CompletableFuture<T> previousFuture() {
      return previous().future();
    }

    private Throwable unwrap(final Throwable t) {
      Throwable inner = (t instanceof CompletionException) ? t.getCause() : t;
      inner = (inner instanceof CancellationException) ? t.getCause() : inner;
      return inner;
    }

    public CompletesId id() {
      return id;
    }
  }

  enum OutcomeType {
    Some,
    None
  }

  interface Outcome<T> {
    default boolean isCompleted() {
      return false;
    }

    default T value() {
      return null;
    }
  }

  static class BaseOutcome<T> implements Outcome<T> {
    @Override
    public String toString() {
      return getClass().getSimpleName() + " [ completed=" + isCompleted() + " value=" + value() + "]";
    }
  }

  static class CompletedOutcome<T> extends BaseOutcome<T> {
    private final AtomicReference<T> outcome;

    CompletedOutcome(final T outcome) {
      this.outcome = new AtomicReference<>(outcome);
    }

    @Override
    public boolean isCompleted() {
      return true;
    }

    @Override
    public T value() {
      return outcome.get();
    }
  }

  static class UncompletedOutcome<T> extends BaseOutcome<T> {
    static final UncompletedOutcome<?> instance = new UncompletedOutcome<>();

    @SuppressWarnings("unchecked")
    static <T> T instance() {
      return (T) instance;
    }

    UncompletedOutcome() {
    }
  }
}
