// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.completes;

import io.vlingo.common.*;

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

  private FutureCompletes<?> next;
  private FutureCompletes<?> previous;
  private final State<T> state;

  public FutureCompletes(final CompletesId id, final Scheduler scheduler) {
    this.state = new State<>(id, scheduler, OutcomeType.Some);
    this.state.completes(this);
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
  @SuppressWarnings("unchecked")
  public <O> Completes<O> andThen(final long timeout, final O failedOutcomeValue, final Function<T, O> function) {
    this.next = state.nextForFunction(timeout, failedOutcomeValue, function);
    this.next.previous = this;
    return (Completes<O>) this.next;
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
  @SuppressWarnings("unchecked")
  public Completes<T> andThenConsume(final long timeout, final T failedOutcomeValue, final Consumer<T> consumer) {
    this.next = state.nextForConsumer(timeout, failedOutcomeValue, consumer);
    this.next.previous = this;
    return (Completes<T>) this.next;
  }

  @Override
  public Completes<T> andThenConsume(final T failedOutcomeValue, final Consumer<T> consumer) {
    return andThenConsume(-1, failedOutcomeValue, consumer);
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
    this.next = state.nextForFunctionAsync(timeout, (O) failedOutcomeValue, function);
    this.next.previous = this;
    return (O) this.next;
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
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public <E> Completes<T> otherwise(final Function<E, T> function) {
    this.next = state.nextForFunction(NoTimeout, null, (Function) function, true);
    this.next.previous = this;
    return (Completes<T>) this.next;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Completes<T> otherwiseConsume(final Consumer<T> consumer) {
    this.next = state.nextForConsumer(NoTimeout, null, consumer, true);
    this.next.previous = this;
    return (Completes<T>) this.next;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Completes<T> recoverFrom(final Function<Throwable, T> function) {
    this.next = state.nextForExceptional(function);
    this.next.previous = this;
    return (Completes<T>) this.next;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <O> Completes<O> andFinally() {
    // no-op
    return (Completes<O>) this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <O> Completes<O> andFinally(final Function<T, O> function) {
    this.next = state.nextForFunction(NoTimeout, null, function);
    this.next.previous = this;
    return (Completes<O>) this.next;
  }

  @Override
  public void andFinallyConsume(final Consumer<T> consumer) {
    this.next = state.nextForConsumer(NoTimeout, null, consumer);
    this.next.previous = this;
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
    return state.future.isDone();
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
    return state.id;
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
    if (hasPrevious()) {
      previous.repeat();
    }
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
    state.reset();
    state.complete(outcome);
    return (Completes<O>) this;
  }

  @Override
  public String toString() {
    return "FutureCompletes [id=" + id() + ", next=" + (next != null ? next.id() : "(none)") + " state=" + state + "]";
  }

  private FutureCompletes(final State<T> state) {
    this.state = state;
    this.state.completes(this);
    this.next = null;
    this.previous = null;
  }

  private void exceptional(final Throwable t) {
    state().exceptional(t);
  }

  private FutureCompletes<?> first() {
    FutureCompletes<?> first = this;
    while (first.previous != null) {
      first = first.previous;
    }
    return first;
  }

  private State<?> firstState() {
    return first().state;
  }

  private boolean hasNext() {
    return this.next != null;
  }

  @SuppressWarnings("unchecked")
  private FutureCompletes<T> next() {
    return (FutureCompletes<T>) this.next;
  }

  @SuppressWarnings({"unchecked", "RedundantCast"})
  private State<T> nextState() {
    return ((FutureCompletes<T>) this.next).state;
  }

  private boolean hasPrevious() {
    return this.previous != null;
  }

  @SuppressWarnings({"unchecked", "unused"})
  private FutureCompletes<T> previous() {
    return (FutureCompletes<T>) this.previous;
  }

  @SuppressWarnings({"unchecked", "RedundantCast"})
  private State<T> previousState() {
    return ((FutureCompletes<T>) this.previous).state;
  }

  private State<T> state() {
    return this.state;
  }

  //////////////////////////////////////////////////////
  // State
  //////////////////////////////////////////////////////

  private static class State<T> implements Scheduled<Object> {
    private Cancellable cancellable;
    private FutureCompletes<T> completes;
    private CompletableFuture<T> future;
    private final AtomicBoolean failed;
    private final AtomicReference<T> failureValue;
    private final Function<CompletableFuture<?>, CompletableFuture<T>> futureFactory;
    private final boolean handlesFailure;
    private final CompletesId id;
    private final AtomicReference<Outcome<T>> outcome;
    private final OutcomeType outcomeType;
    private final Scheduler scheduler;
    private final AtomicBoolean timedOut = new AtomicBoolean(false);
    private final AtomicBoolean repeats = new AtomicBoolean(false);

    State(final CompletesId id, final Scheduler scheduler, final Function<CompletableFuture<?>, CompletableFuture<T>> futureFactory, final CompletableFuture<T> parentFuture, final T failedOutcomeValue, final boolean handlesFailure, final OutcomeType outcomeType) {
      this.id = id;
      this.scheduler = scheduler;
      this.failed = new AtomicBoolean(false);
      this.failureValue = new AtomicReference<>(failedOutcomeValue);
      this.handlesFailure = handlesFailure;
      this.futureFactory = futureFactory;
      this.future = futureFactory.apply(parentFuture);
      this.outcome = new AtomicReference<>(UncompletedOutcome.instance());
      this.outcomeType = outcomeType;
    }

    @SuppressWarnings("unchecked")
    State(final CompletesId id, final Scheduler scheduler, final OutcomeType outcomeType) {
      this(id, scheduler, (f) -> (CompletableFuture<T>) f, new CompletableFuture<>(), null, false, outcomeType);
    }

    @SuppressWarnings("unchecked")
    <O> O await() {
      if (hasOutcome()) {
        return (O) outcome();
      }

      try {
        T awaitedOutcome = future.get();

        if (awaitedOutcome instanceof Completes) {
          awaitedOutcome = ((Completes<T>) awaitedOutcome).await();
          outcomeMaybeOverride(awaitedOutcome);
        }

        return (O) outcome();
      } catch (Exception e) {
        if (this.hasFailed()) {
          return (O) outcome();
        }
        // fall through
      }

      throw new IllegalStateException("Unexpected await termination");
    }

    @SuppressWarnings("unchecked")
    <O> O await(final long timeout) {
      if (hasOutcome()) {
        return (O) outcome();
      }

      try {
        future.get(timeout, TimeUnit.MILLISECONDS);
        return (O) outcome();
      } catch (Exception e) {
        if (this.hasFailed()) {
          return (O) outcome();
        }
        // fall through
      }

      return null;
    }

    void cancelTimer() {
      if (cancellable != null) {
        cancellable.cancel();
        cancellable = null;
      }
    }

    <O> void complete(final O outcome) {
      complete(outcome, false);
    }

    @SuppressWarnings("unchecked")
    <O> void complete(final O outcome, final boolean handlingFailure) {
      if (outcome instanceof Throwable) {
        exceptional((Throwable) outcome);
        return;
      }

      T realOutcome = (T) outcome;

      if (isFailureValue(realOutcome)) {
        realOutcome = failureValue();
        failAll(realOutcome, isTimedOut());
        if (handlingFailure) {
          completes.nextState().failed.set(true);
        } else if (handleFailure(realOutcome)) {
          return;
        }
      }

      outcomeMaybeOverride(realOutcome);

      if (!future.isDone()) {
        future.complete(realOutcome);
      }
    }

    private void outcomeMaybeOverride(final T outcome) {
      if (!hasOutcome()) {
        outcome(new CompletedOutcome<>(outcome));
      } else if (hasFailed() && isFailureValue(this.outcome.get().value())) {
        outcome(new CompletedOutcome<>(outcome));
      } else if (this.outcome.get().value() instanceof Completes) {
        outcome(new CompletedOutcome<>(outcome));
      }
    }

    void completes(FutureCompletes<T> completes) {
      this.completes = completes;
    }

    private void repeat() {
      this.repeats.set(true);
    }

    private boolean hasRepeats() {
      return this.repeats.get();
    }

    private void reset() {
      if (hasOutcome() && hasRepeats()) {
        completes.firstState().reset(new CompletableFuture<>());
      }
    }

    private void reset(CompletableFuture<T> previousFuture) {
      this.outcome.set(UncompletedOutcome.instance());
      this.failed.set(false);
      this.future = futureFactory.apply(previousFuture);
      if (completes.hasNext()) {
        completes.nextState().reset(this.future);
      }
    }

    void exceptional(final Throwable t) {
      failAll(outcome(), isTimedOut());
      future.completeExceptionally(t);
    }

    boolean hasFailed() {
      if (failed.get()) {
        return true;
      }

      if (future.isDone()) {
        final T outcome = outcome();

        if (isFailureValue(outcome)) {
          failed.set(true);
          return true;
        }
      }

      final boolean hasFailed = future.isCancelled() || future.isCompletedExceptionally();

      failed.set(hasFailed);

      return hasFailed;
    }

    T failureValue() {
      if (handlesFailure) {
        return outcome();
      }
      return failureValue.get();
    }

    boolean isFailureValue(final T candidateFailureValue) {
      if (isTimedOut()) {
        return true;
      }

      final T currentFailureValue = failureValue.get();

      if (currentFailureValue == candidateFailureValue) return true;

      if (currentFailureValue != null && currentFailureValue.equals(candidateFailureValue)) return true;

      if (completes.hasNext()) {
        final State<T> nextState = completes.nextState();
        if (nextState.isFailureValue(candidateFailureValue)) {
          failureValue.set(nextState.failureValue());
          return true;
        }
      }

      return false;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    FutureCompletes<T> nextForConsumer(final long timeout, final T failedOutcomeValue, final Consumer<T> consumer, final boolean handlesFailure) {
      final Consumer<T> consumerWrapper = consumerWrapper(consumer, handlesFailure);
      final Function<CompletableFuture<T>, CompletableFuture<Void>> futureFactory = f -> f.thenAccept(consumerWrapper);
      final State<T> state = new State(Completes.completesId(), scheduler, futureFactory, future, failedOutcomeValue, handlesFailure, OutcomeType.None);
      if (future.isDone()) {
        state.outcome(outcome.get());
      }
      state.startTimer(timeout);
      return new FutureCompletes<>(state);
    }

    FutureCompletes<T> nextForConsumer(final long timeout, final T failedOutcomeValue, final Consumer<T> consumer) {
      return nextForConsumer(timeout, failedOutcomeValue, consumer, false);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    FutureCompletes<T> nextForExceptional(final Function<Throwable, T> function) {
      final Function<Throwable,T> functionWrapper = functionExceptionWrapper(function);
      final Function<CompletableFuture<T>, CompletableFuture<T>> futureFactory = f -> f.exceptionally(functionWrapper);
      final State<T> state = new State(Completes.completesId(), scheduler, futureFactory, future, null, false, OutcomeType.Some);
      return new FutureCompletes<>(state);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    <O> FutureCompletes<T> nextForFunction(final long timeout, final O failedOutcomeValue, final Function<T, O> function, final boolean handlesFailure) {
      Function<T, CompletableFuture<O>> functionWrapper = composableFunction(functionWrapper(function, handlesFailure));
      final Function<CompletableFuture<T>, CompletableFuture<O>> futureFactory = f ->  f.thenCompose(functionWrapper);
      final State<T> state = new State(Completes.completesId(), scheduler, futureFactory, future, failedOutcomeValue, handlesFailure, OutcomeType.Some);
      state.startTimer(timeout);
      return new FutureCompletes<>(state);
    }

    <O> FutureCompletes<T> nextForFunction(final long timeout, final O failedOutcomeValue, final Function<T, O> function) {
      return nextForFunction(timeout, failedOutcomeValue, function, false);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    <O> FutureCompletes<T> nextForFunctionAsync(final long timeout, final O failedOutcomeValue, final Function<T, O> function) {
      Function<T, CompletableFuture<O>> functionWrapper = composableFunction(functionWrapper(function, handlesFailure));
      final Function<CompletableFuture<T>, CompletableFuture<O>> futureFactory = f -> f.thenComposeAsync(functionWrapper);
      final State<T> state = new State(Completes.completesId(), scheduler, futureFactory, future, failedOutcomeValue, handlesFailure, OutcomeType.Some);
      state.startTimer(timeout);
      return new FutureCompletes<>(state);
    }

    T outcome() {
      if (hasOutcome()) {
        return ultimateOutcome();
      }

      try {
        if (future.isDone()) {
          final T outcome = future.get(1, TimeUnit.MILLISECONDS);

          outcome(new CompletedOutcome<>(outcome));

          return ultimateOutcome();
        }
      } catch (Throwable t) {
        if (completes.hasNext()) {
          completes.next().exceptional(t);
        }
      }

      return null;
    }

    void outcome(final Outcome<T> outcome) {
      if (!outcome.isCompleted()) {
        return;
      }

      this.outcome.set(outcome);
    }

    boolean hasOutcome() {
      return outcome.get().isCompleted() || future.isDone();
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

    void timedOut() {
      this.failAll(failureValue(), true);
      outcome(new CompletedOutcome<>(failureValue()));
    }

    boolean isTimedOut() {
      return timedOut.get();
    }

    @Override
    public void intervalSignal(final Scheduled<Object> scheduled, final Object data) {
      cancelTimer();
      if (future.isDone()) return;
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

    @SuppressWarnings("unchecked")
    private Consumer<T> consumerWrapper(final Consumer<T> userConsumer, final boolean whenFailure) {
      final Consumer<T> acceptResults = (result) -> {
        if (!completes.hasOutcome()) {
          completes.state().outcome(new CompletedOutcome<>(result));
        }

        userConsumer.accept(result);

        if (completes.hasNext()) {
          completes.next().with(outcome());
        }
      };

      return (value) -> {
        try {
          if (whenFailure && !hasFailed()) {
            return;
          } else if (!whenFailure && hasFailed()) {
            return;
          }

          if (value instanceof Completes) {
            ((Completes<T>) value).andFinallyConsume(result -> acceptResults.accept(value));
            return;
          }

          acceptResults.accept(value);

        } catch (Throwable t) {
          if (completes.hasNext()) {
            completes.next().exceptional(t);
          }
        }
      };
    }

    private Function<Throwable, T> functionExceptionWrapper(final Function<Throwable, T> userFunction) {
      return (Throwable e) -> {
        try {
          final Throwable inner = unwrap(e);

          final Completes<T> next = completes.hasNext() ? completes.next() : null;

          final T wrapped = userFunction.apply(inner);

          final Tuple2<Boolean, T> outcome = unwrap(wrapped);

          completes.state().outcomeMaybeOverride(outcome._2);

          if (next != null) {
            next.with(outcome._2);
          }

          return outcome._2;
        } catch (Exception ex) {
          if (completes.hasNext()) {
            completes.next().exceptional(e);
          }
          return null;
        }
      };
    }

    @SuppressWarnings("unchecked")
    private <O> Function<T, CompletableFuture<O>> composableFunction(final Function<T, O> userFunction) {
      return (T t) -> {
        O outcome = userFunction.apply(t);
        if (outcome instanceof FutureCompletes) {
          return (CompletableFuture<O>) ((FutureCompletes<?>) outcome).state().future;
        }
        return CompletableFuture.completedFuture(outcome);
      };
    }

    @SuppressWarnings("unchecked")
    private <O> Function<T, O> functionWrapper(final Function<T, O> userFunction, final boolean whenFailure) {
      final Function<T, O> applyResult = (result) -> {
        final T wrapped = (T) userFunction.apply(result);

        final Tuple2<Boolean, T> outcome = unwrap(wrapped);

        return (O) outcome._2;
      };

      return (value) -> {
        try {
          if (whenFailure && !hasFailed()) {
            return (O) value;
          }

          if (!whenFailure && hasFailed()) {
            return (O) value;
          }

          return applyResult.apply(value);

        } catch (Exception e) {
          if (completes.hasNext()) {
            completes.next().exceptional(e);
          }
          return null;
        }
      };
    }

    private void failAll(final T failureValue, final boolean hasTimedOut) {
      // could overwrite possible otherwise(v -> f(v))
      // computed outcome unless use short circuit here
      if (handlesFailure) return;

      this.failureValue.set(failureValue);
      this.failed.set(true);
      this.timedOut.set(hasTimedOut);
      outcome(new CompletedOutcome<>(failureValue));

      if (completes.hasNext()) {
        final State<T> nextState = completes.nextState();
        nextState.failAll(failureValue, hasTimedOut);
      }
    }

    private boolean handleFailure(final T outcome) {
      if (completes.hasNext()) {
        if (completes.nextState().handlesFailure) {
          completes.state().complete(outcome, true);
          return true;
        }
        return completes.nextState().handleFailure(outcome);
      }

      return false;
    }

    private T previousOutcome() {
      if (completes.hasPrevious()) {
        if (completes.previousState().outcomeType == OutcomeType.Some) {
          outcome(completes.previousState().outcome.get());
        }
        final T previousOutcome = completes.previousState().previousOutcome();

        outcome(new CompletedOutcome<>(previousOutcome));
      }
      return null;
    }

    private T ultimateOutcome() {
      if (completes.hasNext()) {
        final State<T> nextState = completes.nextState();
        if (nextState.hasOutcome()) {
          return nextState.ultimateOutcome();
        }
      }

      final Outcome<T> maybeOutcome = this.outcome.get();

      if (maybeOutcome.isCompleted()) {
        final Tuple2<Boolean, T> unwrapped = unwrap(maybeOutcome.value());
        return unwrapped._1 ? unwrapped._2 : null;
      }

      if (outcomeType == OutcomeType.None) {
        outcome(new CompletedOutcome<>(previousOutcome()));
      }

      try {
        // may be a race where outcome is demanded
        // but not yet set by future pipeline
        if (future.isDone()) {
          final Tuple2<Boolean, T> possibleOutcome = unwrap(future.get());
          if (possibleOutcome._1) {
            outcome(new CompletedOutcome<>(possibleOutcome._2));
            return possibleOutcome._2;
          }
        }
      } catch (Exception e) {
        // fall through
      }

      return null;
    }

    @SuppressWarnings("unchecked")
    private Tuple2<Boolean, T> unwrap(final T outcome) {
      if (outcome instanceof Completes) {
        final FutureCompletes<T> completes = (FutureCompletes<T>) outcome;
        if (completes.isCompleted() && !completes.hasFailed()) {
          return Tuple2.from(true, completes.outcome());
        }
        return Tuple2.from(false, outcome);
      }
      return Tuple2.from(true, outcome);
    }

    private Throwable unwrap(final Throwable t) {
      Throwable inner = (t instanceof CompletionException) ? t.getCause() : t;
      inner = (inner instanceof CancellationException) ? t.getCause() : inner;
      return inner;
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

    UncompletedOutcome() { }
  }
}
