// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.completes;

import io.vlingo.common.Cancellable;
import io.vlingo.common.Completes;
import io.vlingo.common.Scheduled;
import io.vlingo.common.Scheduler;
import io.vlingo.common.Tuple2;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

public class CFCompletes<T> implements Completes<T> {
  private static final long NoTimeout = -1L;

  private CFCompletes<?> next;
  private CFCompletes<?> previous;
  private final State<T> state;

  public CFCompletes(final CompletesId id, final Scheduler scheduler) {
    this.state = new State<>(Completes.completesId(), scheduler, OutcomeType.Some);
    this.state.completes(this);
  }

  public CFCompletes(final Scheduler scheduler) {
    this(Completes.completesId(), scheduler);
  }

  public CFCompletes(final CompletesId id, final T outcome, final boolean successful) {
    this(id, (Scheduler) null);

    if (!successful) {
      useFailedOutcomeOf(outcome);
    }

    with(outcome);
  }

  public CFCompletes(final T outcome, final boolean successful) {
    this(Completes.completesId(), outcome, successful);
  }

  public CFCompletes(final CompletesId id, final T outcome) {
    this(id, outcome, true);
  }

  public CFCompletes(final T outcome) {
    this(Completes.completesId(), outcome);
  }

  public CFCompletes(final CompletesId id) {
    this(id, (Scheduler) null);
  }

  public CFCompletes() {
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
    // TODO Auto-generated method stub
    return null;
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
    state.complete(outcome);
    return (Completes<O>) this;
  }

  @Override
  public String toString() {
    return "CFCompletes [id=" + id() + ", next=" + (next != null ? next.id() : "(none)") + " state=" + state + "]";
  }

  private CFCompletes(final State<T> state) {
    this.state = state;
    this.state.completes(this);
    this.next = null;
    this.previous = null;
  }

  private void exceptional(final Throwable t) {
    state().exceptional(t);
  }

  private boolean hasNext() {
    return this.next != null;
  }

  @SuppressWarnings("unchecked")
  private CFCompletes<T> next() {
    return (CFCompletes<T>) this.next;
  }

  @SuppressWarnings("unchecked")
  private State<T> nextState() {
    return ((CFCompletes<T>) this.next).state;
  }

  private boolean hasPrevious() {
    return this.previous != null;
  }

  @SuppressWarnings({ "unchecked", "unused" })
  private CFCompletes<T> previous() {
    return (CFCompletes<T>) this.previous;
  }

  @SuppressWarnings("unchecked")
  private State<T> previousState() {
    return ((CFCompletes<T>) this.previous).state;
  }

  private State<T> state() {
    return this.state;
  }



  private static boolean DEBUG = false;

  @SuppressWarnings("unused")
  private static void debug(final String message) {
    if (DEBUG) {
      synchronized (CFCompletes.class) {
        System.out.println(message);
      }
    }
  }

  @SuppressWarnings("unused")
  private static String printLimitedTrace(final int startingWith, final int levels) {
    return printLimitedTrace(new Exception(), startingWith, levels);
  }

  @SuppressWarnings("unused")
  private static String printLimitedTrace(final int levels) {
    return printLimitedTrace(new Exception(), -1, levels);
  }

  @SuppressWarnings("unused")
  private static String printLimitedTrace(final Throwable t, final int levels) {
    return printLimitedTrace(t, -1, levels);
  }

  private static String printLimitedTrace(final Throwable t, final int startingWith, final int levels) {
    if (DEBUG) {
      final StringBuilder builder = new StringBuilder();
      final StackTraceElement[] trace = t.getStackTrace();
      int idx = startingWith;
      idx = (idx >= 0) ? idx : trace[1].toString().contains("CFCompletes.access$") ? 2 : 1;
      final int max = Math.min(levels + idx, trace.length);
      int count = 1;
      for ( ; idx < max; ++idx) {
        builder.append("\n").append("TRACE: ").append(count++).append(": ").append(trace[idx]);
      }
      return builder.toString();
    }
    return "";
  }







  //////////////////////////////////////////////////////
  // State
  //////////////////////////////////////////////////////

  private static class State<T> implements Scheduled<Object> {
    private Cancellable cancellable;
    private CFCompletes<T> completes;
    private final AtomicBoolean failed;
    private final AtomicReference<T> failureValue;
    private final CompletableFuture<T> future;
    private final boolean handlesFailure;
    private final CompletesId id;
    private final AtomicReference<Outcome<T>> outcome;
    private final OutcomeType outcomeType;
    private final Scheduler scheduler;
    private final AtomicBoolean timedOut = new AtomicBoolean(false);

    State(final CompletesId id, final Scheduler scheduler, final CompletableFuture<T> future, final T failedOutcomeValue, final boolean handlesFailure, final OutcomeType outcomeType) {
      this.id = id;
      this.scheduler = scheduler;
      this.failed = new AtomicBoolean(false);
      this.failureValue = new AtomicReference<>(failedOutcomeValue);
      this.handlesFailure = handlesFailure;
      this.future = future;
      this.outcome = new AtomicReference<>(new UncompletedOutcome<>());
      this.outcomeType = outcomeType;

      //debug("CTOR: " + this.id);
    }

    State(final CompletesId id, final Scheduler scheduler, final OutcomeType outcomeType) {
      this(id, scheduler, new CompletableFuture<>(), null, false, outcomeType);
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
        // debug("=============== AWAIT: EXECEPTION: " + e.getMessage() + "\n");
        e.printStackTrace();
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
        // debug("FAILURE: " + realOutcome);
        realOutcome = failureValue();
        failAll(realOutcome, isTimedOut());
        if (handlingFailure) {
          completes.nextState().failed.set(true);
        } else if (handleFailure(realOutcome)) {
          return;
        }
      }

      // debug("COMPLETE: " + id + " OUTCOME: " + realOutcome + printLimitedTrace(7));

      outcomeMaybeOverride(realOutcome);

      // debug("COMPLETE FUTURE: " + id + " DONE: " + future.isDone() + " OUTCOME: " + realOutcome);

      if (!future.isDone()) {
        // debug("COMPLETE FUTURE: " + id + " OUTCOME: " + realOutcome);
        future.complete(realOutcome);
      }
    }

    private void outcomeMaybeOverride(final T outcome) {
      // debug("OUTCOME MAYBE OVERRIDE: " + id + " CURRENT: " + this.outcome.get() + " OUTCOME: " + outcome);
      if (!hasOutcome()) {
        // debug("OUTCOME MAYBE OVERRIDE (HAS OUTCOME) SETTING: " + id + " OUTCOME: " + outcome);
        outcome(new CompletedOutcome<>(outcome));
      } else if (hasFailed() && isFailureValue(this.outcome.get().value())) {
        // debug("OUTCOME MAYBE OVERRIDE (HAS FAILED) SETTING: " + id + " OUTCOME: " + outcome);
        outcome(new CompletedOutcome<>(outcome));
      } else if (this.outcome.get().value() instanceof Completes) {
        outcome(new CompletedOutcome<>(outcome));
      }
    }

    void completes(CFCompletes<T> completes) {
      this.completes = completes;
    }

    void exceptional(final Throwable t) {
      failAll(outcome(), isTimedOut());
      future.completeExceptionally(t);
    }

    boolean hasFailed() {
      if (failed.get()) {
        // debug("HAS FAILED: " + id + printLimitedTrace(5));
        return true;
      }

      if (future.isDone()) {
        final T outcome = outcome();
        // debug("HAS FAILED: " + id + " IS DONE: " + (outcome == null ? "(null)":outcome));

        if (isFailureValue(outcome)) {
          // debug("HAS FAILED: " + id + " FAILED OUTCOME VALUE: " + (outcome == null ? "(null)":outcome) + printLimitedTrace(20));
          failed.set(true);
          return true;
        }
      }

      final boolean hasFailed = future.isCancelled() || future.isCompletedExceptionally();

      // if (hasFailed) debug("HAS FAILED: " + id + printLimitedTrace(5));

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
      // debug("CANDIDATE FAILURE VALUE: " + candidateFailureValue);
      if (isTimedOut()) {
        // debug("TIMED OUT FAILURE");
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
      // debug("FAILURE VALUE: NOT FAILED: " + candidateFailureValue);

      return false;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    CFCompletes<T> nextForConsumer(final long timeout, final T failedOutcomeValue, final Consumer<T> consumer, final boolean handlesFailure) {
      final Consumer<T> consumerWrapper = consumerWrapper(consumer, handlesFailure);
      final State<T> state = new State(Completes.completesId(), scheduler, future.thenAccept(consumerWrapper), failedOutcomeValue, handlesFailure, OutcomeType.None);
      if (future.isDone()) {
        state.outcome(outcome.get());
      }
      state.startTimer(timeout);
      return new CFCompletes<T>(state);
    }

    CFCompletes<T> nextForConsumer(final long timeout, final T failedOutcomeValue, final Consumer<T> consumer) {
      return nextForConsumer(timeout, failedOutcomeValue, consumer, false);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    CFCompletes<T> nextForExceptional(final Function<Throwable, T> function) {
      final Function<Throwable,T> functionWrapper = functionExceptionWrapper(function);
      final State<T> state = new State(Completes.completesId(), scheduler, future.exceptionally(functionWrapper), null, false, OutcomeType.Some);
      return new CFCompletes<T>(state);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    <O> CFCompletes<T> nextForFunction(final long timeout, final O failedOutcomeValue, final Function<T, O> function, final boolean handlesFailure) {
      final Function<T, O> functionWrapper = functionWrapper(function, handlesFailure, false);
      final State<T> state = new State(Completes.completesId(), scheduler, future.thenCompose(composableFunction(functionWrapper)), failedOutcomeValue, handlesFailure, OutcomeType.Some);
      state.startTimer(timeout);
      return new CFCompletes<T>(state);
    }

    <O> CFCompletes<T> nextForFunction(final long timeout, final O failedOutcomeValue, final Function<T, O> function) {
      return nextForFunction(timeout, failedOutcomeValue, function, false);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    <O> CFCompletes<T> nextForFunctionAsync(final long timeout, final O failedOutcomeValue, final Function<T, O> function, final boolean handlesFailure) {
      final Function<T, O> functionWrapper = functionWrapper(function, handlesFailure, true);
      final State<T> state = new State(Completes.completesId(), scheduler, future.thenComposeAsync(composableFunction(functionWrapper)), failedOutcomeValue, handlesFailure, OutcomeType.Some);
      state.startTimer(timeout);
      return new CFCompletes<T>(state);
    }

    <O> CFCompletes<T> nextForFunctionAsync(final long timeout, final O failedOutcomeValue, final Function<T, O> function) {
      return nextForFunctionAsync(timeout, failedOutcomeValue, function, false);
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

      // debug("OUTCOME SETTING: " + id + " OUTCOME: " + outcome + CFCompletes.printLimitedTrace(10));

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
      // debug("TIMER TIMED OUT!!!");
      if (future.isDone()) return;
      // debug("TIMER TIMED OUT NOT DONE!!!");
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
            ((Completes<T>) value).andFinallyConsume(result -> {
              acceptResults.accept(value);
            });
            return;
          }

          acceptResults.accept(value);

        } catch (Throwable t) {
          // debug("CONSUMER EXECEPTION: " + t);
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
          // debug("FUNCTION EX: " + id + " EXCEPTION: " + ex.getMessage() + printLimitedTrace(ex, 5));
          if (completes.hasNext()) {
            completes.next().exceptional(e);
          }
          return null;
        }
      };
    }

    private <O> Function<T, CompletableFuture<O>> composableFunction(final Function<T, O> userFunction) {
      return (T t) -> {
        O outcome = userFunction.apply(t);
        if (outcome instanceof CFCompletes) {
          return (CompletableFuture<O>) ((CFCompletes<?>) outcome).state().future;
        }
        return CompletableFuture.completedFuture(outcome);
      };
    }

    @SuppressWarnings("unchecked")
    private <O> Function<T, O> functionWrapper(final Function<T, O> userFunction, final boolean whenFailure, final boolean isAsync) {
      final Function<T, O> applyResult = (result) -> {
        final Completes<T> next = completes.hasNext() ? completes.next() : null;

        final T wrapped = (T) userFunction.apply(result);

        final Tuple2<Boolean, T> outcome = unwrap(wrapped);

        if (outcome._1 && !completes.hasOutcome()) {
          completes.state().outcome(new CompletedOutcome<>(outcome._2));
        }

        if (next != null && outcome._1) {
          next.with(outcome._2);
        }

        final O typedOutcome = (O) outcome._2;

        return typedOutcome;
      };

      return (value) -> {
        try {
          if (whenFailure && !hasFailed()) {
            return (O) value;
          }

          if (!whenFailure && hasFailed()) {
            return (O) value;
          }

          if (value instanceof Completes) {
            final Completes<T> outcomeCompletes = ((Completes<T>) value).andFinally(result -> {
              final O typedOutcome = applyResult.apply(result);
              return (T) typedOutcome;
            });
            return (O) outcomeCompletes;
          }

          return applyResult.apply(value);

        } catch (Exception e) {
          // debug("FUNCTION: " + id + " VALUE: " + value + " EXCEPTION: " + e.getMessage() + printLimitedTrace(e, 20));
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

      // debug("========== " + maybeOutcome);

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
        // debug("******** EXCEPTION: " + e);
        // fall through
      }

      return null;
    }

    @SuppressWarnings("unchecked")
    private Tuple2<Boolean, T> unwrap(final T outcome) {
      if (outcome instanceof Completes) {
        final CFCompletes<T> completes = (CFCompletes<T>) outcome;
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

  static enum OutcomeType {
    Some,
    None
  }

  static interface Outcome<T> {
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
