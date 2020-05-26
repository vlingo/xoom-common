// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

public class BasicCompletes<T> implements Completes<T> {
  private static boolean DEBUG = false;

  protected final ActiveState<T> state;

  public BasicCompletes(final Scheduler scheduler) {
    this(new BasicActiveState<T>(scheduler), true);
  }

  public BasicCompletes(final Scheduler scheduler, ActiveState<T> parent) {
    this(new BasicActiveState<T>(scheduler, parent), true);
  }

  public BasicCompletes(final T outcome, final boolean succeeded) {
    this(new BasicActiveState<T>(), outcome, succeeded);
  }

  public BasicCompletes(final T outcome) {
    this(new BasicActiveState<T>(), outcome);
  }

  protected BasicCompletes(final ActiveState<T> state) {
    this.state = state;
  }

  protected BasicCompletes(final ActiveState<T> state, final T outcome, final boolean succeeded) {
    this.state = state;
    if (succeeded) {
      this.state.completedWith(outcome);
    } else {
      this.state.failedValue(outcome);
      this.state.failed();
    }
  }

  protected BasicCompletes(final ActiveState<T> state, final T outcome) {
    this.state = state;
    this.state.outcome(outcome);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <O> Completes<O> andThen(final long timeout, final O failedOutcomeValue, final Function<T,O> function) {
    debug("AT3-TO-FO-FN: " + state.id() + ": " + failedOutcomeValue);
    state.failedValue(failedOutcomeValue);
    state.registerWithExecution(Action.with(function), timeout, state);
    return (Completes<O>) this;
  }

  @Override
  public <O> Completes<O> andThen(final O failedOutcomeValue, final Function<T,O> function) {
    debug("AT2-FO-FN: " + state.id() + ": " + failedOutcomeValue);
    return andThen(-1L, failedOutcomeValue, function);
  }

  @Override
  public <O> Completes<O> andThen(final long timeout, final Function<T,O> function) {
    debug("AT2-TO-FN: " + state.id());
    return andThen(timeout, null, function);
  }

  @Override
  public <O> Completes<O> andThen(final Function<T,O> function) {
    debug("AT1-FN: " + state.id());
    return andThen(-1L, null, function);
  }

  @Override
  public Completes<T> andThenConsume(final long timeout, final T failedOutcomeValue, final Consumer<T> consumer) {
    state.failedValue(failedOutcomeValue);
    state.registerWithExecution(Action.with(consumer), timeout, state);
    return this;
  }

  @Override
  public Completes<T> andThenConsume(final long timeout, final Consumer<T> consumer) {
    return andThenConsume(timeout, null, consumer);
  }

  @Override
  public Completes<T> andThenConsume(final T failedOutcomeValue, final Consumer<T> consumer) {
    return andThenConsume(-1, failedOutcomeValue, consumer);
  }

  @Override
  public Completes<T> andThenConsume(final Consumer<T> consumer) {
    return andThenConsume(-1, null, consumer);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <F,O> O andThenTo(final long timeout, final F failedOutcomeValue, final Function<T, O> function) {
    debug("ATT FV3: " + state.id() + ": " + failedOutcomeValue);

    debug("ATT FV3 NESTING: " + state.id());

    final BasicCompletes<O> nestedCompletes = new BasicCompletes<>((BasicActiveState<O>) state, false);
    nestedCompletes.state.failedValue(failedOutcomeValue);
    state.registerWithExecution((Action<T>) Action.with(function, nestedCompletes), timeout, state);
    return (O) nestedCompletes;
  }

  @Override
  public <F,O> O andThenTo(final F failedOutcomeValue, final Function<T,O> function) {
    debug("ATT2-FO-FN FV2: " + state.id() + ": " + failedOutcomeValue);
    return andThenTo(-1, failedOutcomeValue, function);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <O> O andThenTo(final long timeout, final Function<T,O> function) {
    debug("ATT2-TO-FN FV: " + state.id());
    return andThenTo(timeout, (O) BasicActiveState.UnfailedValue, function);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <O> O andThenTo(final Function<T,O> function) {
    debug("ATT1-FN: " + state.id());
    return andThenTo(-1, (O) BasicActiveState.UnfailedValue, function);
  }

  @Override
  public <E> Completes<T> otherwise(final Function<E,T> function) {
    debug("OW-FN: " + state.id() + ": " + printLimitedTrace(7));
    state.failureAction(Action.with(function));
    return this;
  }

  @Override
  public Completes<T> otherwiseConsume(final Consumer<T> consumer) {
    state.failureAction(Action.with(consumer));
    return this;
  }

  @Override
  public Completes<T> recoverFrom(final Function<Exception,T> function) {
    state.exceptionAction(function);
    return this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <O> O await() {
    state.await();
    return (O) outcome();
  }

  @Override
  @SuppressWarnings("unchecked")
  public <O> O await(final long timeout) {
    if (state.await(timeout)) {
      return (O) outcome();
    }
    return null;
  }

  @Override
  public boolean isCompleted() {
    return state.isOutcomeKnown();
  }

  @Override
  public boolean hasFailed() {
    return state.hasFailed();
  }

  @Override
  public void failed() {
    with(state.failedValue());
  }

  @Override
  public void failed(final Exception exception) {
    state.handleException(exception);
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
    throw new UnsupportedOperationException();
  }

  @Override
  public Completes<T> timeoutWithin(final long timeout) {
    state.startTimer(timeout);
    return this;
  }

  @Override
  public <F> Completes<T> useFailedOutcomeOf(final F failedOutcomeValue) {
    state.failedValue(failedOutcomeValue);
    return this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <O> Completes<O> with(final O outcome) {
    if (!state.handleFailure((T) outcome)) {
      debug("SUCCEESS WITH: " + state.id() + " :" + outcome);
      state.completedWith((T) outcome);
    } else debug("FAILED WITH: " + outcome);

    return (Completes<O>) this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <O> Completes<O> andFinally() {
    return andFinally(value -> (O) value);
  }

  @Override
  public <O> Completes<O> andFinally(final Function<T,O> function) {
    return andThen(function);
  }

  @Override
  public void andFinallyConsume(final Consumer<T> consumer) {
    andThenConsume(consumer);
  }

  private BasicCompletes(final BasicActiveState<T> parent, final boolean root) {
    if (root) {
      this.state = parent;
    } else {
      this.state = new BasicActiveState<T>(parent.scheduler(), parent);
    }
  }

  private static void debug(final String message) {
    if (DEBUG) System.out.println(message);
  }

  private static String printLimitedTrace(final int levels) {
    if (DEBUG) {
      final StringBuilder builder = new StringBuilder();
      final StackTraceElement[] trace = new Exception().getStackTrace();
      int idx = trace[1].toString().contains("BasicCompletes.access$") ? 2 : 1;
      final int max = Math.min(levels + idx, trace.length);
      int count = 1;
      for ( ; idx < max; ++idx) {
        builder.append("\n").append("TRACE: ").append(count++).append(": ").append(trace[idx]);
      }
      return builder.toString();
    }
    return "";
  }

  protected static class Action<T> {
    private final T defaultValue;
    private final boolean hasDefaultValue;
    private final Object function;
    private final Completes<T> nestedCompletes;

    static <T> Action<T> with(final Object function) {
      return new Action<T>(function);
    }

    static <T> Action<T> with(final Object function, Completes<T> nestedCompletes) {
      return new Action<T>(function, nestedCompletes);
    }

    static <T> Action<T> with(final Object function, final T defaultValue, Completes<T> nestedCompletes) {
      return new Action<T>(function, defaultValue, nestedCompletes);
    }

    Action(final Object function) {
      this.function = function;
      this.defaultValue = null;
      this.hasDefaultValue = false;
      this.nestedCompletes = null;
    }

    Action(final Object function, final T defaultValue) {
      this.function = function;
      this.defaultValue = defaultValue;
      this.hasDefaultValue = true;
      this.nestedCompletes = null;
    }

    Action(final Object function, Completes<T> nestedCompletes) {
      this.function = function;
      this.defaultValue = null;
      this.hasDefaultValue = false;
      this.nestedCompletes = nestedCompletes;
    }

    Action(final Object function, final T defaultValue, Completes<T> nestedCompletes) {
      this.function = function;
      this.defaultValue = defaultValue;
      this.hasDefaultValue = true;
      this.nestedCompletes = nestedCompletes;
    }

    @SuppressWarnings("unchecked")
    <F> F function() {
      return (F) function;
    }

    @SuppressWarnings("unchecked")
    Consumer<T> asConsumer() {
      return (Consumer<T>) function;
    }

    boolean isConsumer() {
      return (function instanceof Consumer);
    }

    @SuppressWarnings("unchecked")
    Function<T,T> asFunction() {
      return (Function<T,T>) function;
    }

    boolean isFunction() {
      return (function instanceof Function);
    }

    boolean hasNestedCompletes() {
      return nestedCompletes != null;
    }

    Completes<T> nestedCompletes() {
      return nestedCompletes;
    }
  }

  protected static interface ActiveState<T> {
    void await();
    boolean await(final long timeout);
    void backUp(final Action<T> action);
    void cancelTimer();
    ActiveState<T> child();
    void child(final ActiveState<T> child);
    boolean hasChild();
    void completedWith(final T outcome);
    boolean executeFailureAction();
    boolean isExecutable();
    boolean hasFailed();
    void failed();
    <F> void failedValue(final F failedOutcomeValue);
    T failedValue();
    void failureAction(final Action<T> action);
    List<Action<T>> failureActions();
    void failureActions(final List<Action<T>> actions);
    boolean handleFailure(final T outcome);
    void exceptionAction(final Function<Exception,T> function);
    void handleException();
    void handleException(final Exception e);
    boolean hasException();
    boolean hasOutcome();
    String id();
    void outcome(final T outcome);
    <O> O outcome();
    boolean isOutcomeKnown();
    void outcomeKnown(final boolean flag);
    boolean outcomeMustDefault();
    void registerWithExecution(final Action<T> action, final long timeout, final ActiveState<T> state);
    boolean isRepeatable();
    void repeat();
    <F> boolean replaceFailedOutcomeValue(F nextFailedOutcomeValue);
    void restore();
    void restore(final Action<T> action);
    Scheduler scheduler();
    void startTimer(final long timeout);
  }

  private static class Executables<T> {
    private AtomicBoolean accessible;
    private Queue<Action<T>> actions;
    private AtomicBoolean readyToExecute;
    private AtomicBoolean wasExecutable;

    Executables() {
      this.accessible = new AtomicBoolean(false);
      this.actions = new ConcurrentLinkedQueue<>();
      this.readyToExecute = new AtomicBoolean(false);
      this.wasExecutable = new AtomicBoolean(false);
    }

    int count() {
      return actions.size();
    }

    void execute(final ActiveState<T> state) {
      while (true) {
        if (accessible.compareAndSet(false, true)) {
          readyToExecute.set(true);
          wasExecutable.set(true);
          executeActions(state);
          accessible.set(false);
          break;
        }
      }
    }

    boolean isReadyToExecute() {
      return readyToExecute.get();
    }

    void registerWithExecution(final Action<T> action, final long timeout, final ActiveState<T> state) {
      while (true) {
        if (accessible.compareAndSet(false, true)) {
          actions.add(action);
          if (isReadyToExecute()) {
            executeActions(state);
          } else {
            state.startTimer(timeout);
          }
          accessible.set(false);
          break;
        }
      }
    }

    void reset() {
      readyToExecute.set(false);
      actions.clear();
    }

    void restore(final Action<T> action) {
      actions.add(action);
    }

    private boolean hasAction() {
      return !actions.isEmpty();
    }

    @SuppressWarnings("unchecked")
    private void executeActions(final ActiveState<T> state) {
      while (hasAction()) {
        if (state.hasOutcome() && state.hasFailed()) {
          state.executeFailureAction();
          return;
        } else if (state.hasException()) {
          state.handleException();
          return;
        }

        final Action<T> action = actions.poll();
        state.backUp(action);

        if (action.hasDefaultValue && state.outcomeMustDefault()) {
          state.outcome(action.defaultValue);
        } else {
          try {
            if (action.isConsumer()) {
              action.asConsumer().accept((T) state.outcome());
            } else if (action.isFunction()) {
              if (action.hasNestedCompletes()) {
                ((Completes<T>) action.asFunction().apply((T) state.outcome()))
                  .andThenConsume(value -> action.nestedCompletes().with(value));
              } else {
                state.outcome(action.asFunction().apply(state.outcome()));
              }
            }
          } catch (Exception e) {
            state.handleException(e);
            break;
          }
        }
      }
    }

    boolean wasExecutable() {
      return wasExecutable.get();
    }
  }

  protected static class BasicActiveState<T> implements ActiveState<T>, Scheduled<Object> {
    private static final Object UnfailedValue = new Object();

    private Cancellable cancellable;
    private ActiveState<T> child;
    private final Executables<T> executables;
    private final AtomicBoolean failed;
    private AtomicReference<T> failedOutcomeValue;
    private final List<Action<T>> failureActions;
    private AtomicReference<Exception> exception;
    private Function<Exception,?> exceptionAction;
    private final AtomicReference<Object> outcome;
    private CountDownLatch outcomeKnown;
    private final ActiveState<T> parent;
    private Scheduler scheduler;
    private final AtomicBoolean timedOut;

    protected BasicActiveState(final Scheduler scheduler, final ActiveState<T> parent) {
      this.scheduler = scheduler;
      this.parent = parent;
      this.executables = new Executables<>();
      this.failed = isFailed(parent);
      this.failedOutcomeValue = failedValueOf(parent);
      this.failureActions = failureActions(parent);
      this.exception = new AtomicReference<>(null);
      this.exceptionAction = (e) -> null;
      this.outcome = new AtomicReference<>(null);
      this.outcomeKnown = new CountDownLatch(1);
      this.timedOut = new AtomicBoolean(false);

      this.id = id(parent);

      if (parent != null) {
        parent.child(this);
      }

      debug("BAS NEW: " + id() + ": " + printLimitedTrace(10));
    }

    private List<Action<T>> failureActions(final ActiveState<T> parent) {
      if (parent != null) {
        return new CopyOnWriteArrayList<>(parent.failureActions());
      }
      return new CopyOnWriteArrayList<>();
    }

    @SuppressWarnings("unchecked")
    private AtomicReference<T> failedValueOf(final ActiveState<T> parent) {
      if (parent != null && parent.hasFailed()) {
        new AtomicReference<>(parent.failedValue());
      }
      return new AtomicReference<>((T) UnfailedValue);
    }

    private AtomicBoolean isFailed(final ActiveState<T> parent) {
      if (parent != null && parent.hasFailed()) {
        return new AtomicBoolean(true);
      }
      return new AtomicBoolean(false);
    }

    static final AtomicInteger nextId = new AtomicInteger(0);
    final String id;
    int nextChildId = 1;

    @Override
    public String id() {
      return id;
    }
    private String id(final ActiveState<T> parent) {
      if (parent != null) {
        BasicActiveState<T> bs = (BasicActiveState<T>) parent;
        final int childId = bs.nextChildId++;
        return bs.id + "." + childId;
      }
      return "" + nextId.incrementAndGet();
    }

    protected BasicActiveState(final Scheduler scheduler) {
      this(scheduler, null);
    }

    protected BasicActiveState() {
      this(null);
    }

    @Override
    public void await() {
      try {
        outcomeKnown.await();
      } catch (InterruptedException e) {
        // fall through
      }
    }

    @Override
    public boolean await(final long timeout) {
      try {
        return outcomeKnown.await(timeout, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        return false;
      }
    }

    @Override
    public void backUp(final Action<T> action) {
      // unused; see RepeatableCompletes
    }

    @Override
    public void cancelTimer() {
      if (cancellable != null) {
        cancellable.cancel();
        cancellable = null;
      }
    }

    @Override
    public ActiveState<T> child() {
      return child;
    }

    @Override
    public void child(final ActiveState<T> child) {
      this.child = child;
    }

    @Override
    public boolean hasChild() {
      return child != null;
    }

    @Override
    public void completedWith(final T outcome) {
      cancelTimer();

      if (!timedOut.get()) {
        this.outcome.set(outcome);
      }

      executables.execute(this);

      outcomeKnown(true);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean executeFailureAction() {
      boolean executed = false;
      debug("EXEC-FA-1: " + id() + ": " + printLimitedTrace(10));
      for (int idx = 0; idx < failureActions.size(); ) {
        final Action<T> failureAction = failureActions.get(idx++);
        if (failureAction != null) {
          debug("EXEC-FA-1.1: " + id);
          final Action<T> executeFailureAction = failureAction;
          failed.set(true);
          try {
            if (executeFailureAction.isConsumer()) {
              debug("EXEC-FA-C1.1.1-BEGIN: " + id);
              executeFailureAction.asConsumer().accept((T) outcome.get());
              debug("EXEC-FA-C1.1.1-END: " + id);
            } else {
              debug("EXEC-FA-F1.1.2-BEGIN: " + id);
              outcome.set(executeFailureAction.asFunction().apply((T) outcome.get()));
              debug("EXEC-FA-F1.1.2-END: " + id);
            }
            executed = true;
          } catch (Throwable t) {
            // ignore and iterate
            debug("EXEC-FA-F1.1.3-EXECEPTION: " + id + ": CONTINUING");
          }
        }
//        if (parent != null) {
//          debug("EXEC-FA-1.2: DELEGATE TO PARENT: " + id);
//          // bubble up
//          return parent.handleFailure(failedValue());
//        }
      }

      if (!executed && child != null) {
        debug("EXEC-FA-1.2.1: DELEGATE TO CHILD: " + id);
        final Object failedOutcome = outcome.get();
        child.failedValue(failedOutcome);
        if (child.handleFailure((T) failedOutcome)) {
          debug("EXEC-FA-1.2.2: CHILD HANDLED FOR: " + id);
          return true;
        }
      }

      debug("EFA-1.3: " + id + ": ACTION: " + executed);
      return executed;
    }

    @Override
    public boolean isExecutable() {
      debug("EXECUTABLE: " + id + ": " + executables.wasExecutable());
      return executables.hasAction() || executables.wasExecutable();
    }

    @Override
    public boolean hasFailed() {
      return failed.get();
    }

    @Override
    public void failed() {
      handleFailure(failedOutcomeValue.get());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <F> void failedValue(final F failedOutcomeValue) {
      final T failureValue = this.failedOutcomeValue.get();
      if (failedOutcomeValue == null && failureValue != null && failureValue != UnfailedValue) {
        debug("FAILED VALUE: NOT SETTING: " + id + ": " + failedOutcomeValue);
        return;
      }
      this.failedOutcomeValue.set((T) failedOutcomeValue);
    }

    @Override
    public T failedValue() {
      return failedOutcomeValue.get();
    }

    @Override
    public void failureAction(final Action<T> action) {
      debug("FAILURE ACTION: " + id() + (action == null ? ": NULL" : ": FN()") + ": "
              + printLimitedTrace(10));
      this.failureActions.add(action);
      debug("FAILURE-ACTION REGISTERED: " + id + ": TO: " + action + ": TOTAL: " + this.failureActions.size());
      if (isOutcomeKnown() && hasFailed()) {
        debug("FAILURE-ACTION EXECUTING: " + id);
        executeFailureAction();
      } else {
        debug("FAILURE-ACTION: " + id + ": NOT EXECUTING: OUTCOME? " + isOutcomeKnown() + " FAILED? " + hasFailed());
      }
    }

    @Override
    public List<Action<T>> failureActions() {
      return failureActions;
    }

    @Override
    public void failureActions(final List<Action<T>> actions) {
      this.failureActions.addAll(actions);
    }

    @Override
    public boolean handleFailure(final T outcome) {
      if (isOutcomeKnown() && hasFailed()) {
        debug("FV-ALREADY: " + id() + ": " + printLimitedTrace(10));
        return true; // already reached below
      }
      final T tempFailureOutcomeValue = failedOutcomeValue.get();
      debug("FV: " + id + " FV: (" + tempFailureOutcomeValue + ") O: (" + outcome + ") FVSTD: " + BasicActiveState.UnfailedValue
              + printLimitedTrace(10));
      boolean handle = false;
      if (outcome == tempFailureOutcomeValue) {
        debug("FV==: " + id);
        handle = true;
      } else if (outcome != null && tempFailureOutcomeValue != null && tempFailureOutcomeValue.equals(outcome)) {
        debug("FVEQ: " + id);
        handle = true;
      }
      if (handle) {
        failed.set(true);
        executables.reset();
        this.outcome.set(tempFailureOutcomeValue);
        outcomeKnown(true);
        executeFailureAction();
      } else if (hasChild()) {
        return child().handleFailure(outcome);
      }
      return handle;
    }

    @Override
    public void exceptionAction(final Function<Exception,T> function) {
      exceptionAction = function;
      handleException();
    }

    @Override
    public void handleException() {
      if (hasException()) {
        handleException(exception.get());
      }
    }

    @Override
    public void handleException(final Exception e) {
      exception.set(e);
      if (exceptionAction != null) {
        failed.set(true);
        executables.reset();
        outcome.set(exceptionAction.apply(e));
        outcomeKnown(true);
      } else if (parent != null) {
        // bubble up
        parent.handleException(e);
      } else {
        debug("[WARN] Exception doesn't have (yet?) appropriate exceptionAction specified!" +
                " Exception type: " + e.getClass().getName() + "." +
                " Exception message: " + e.getMessage() + "." +
                " Exception thrown from: " + e.getStackTrace()[0].toString());
      }
    }

    @Override
    public boolean hasException() {
      return exception.get() != null;
    }

    @Override
    public boolean hasOutcome() {
      return outcome.get() != null;
    }

    @Override
    public void outcome(final T outcome) {
      this.outcome.set(outcome);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <O> O outcome() {
      return (O) outcome.get();
    }

    @Override
    public boolean isOutcomeKnown() {
      return this.outcomeKnown.getCount() == 0;
    }

    @Override
    public void outcomeKnown(final boolean flag) {
      if (flag) {
        outcomeKnown.countDown();
      } else {
        outcomeKnown = new CountDownLatch(1);
      }
    }

    @Override
    public boolean outcomeMustDefault() {
      return outcome() == null;
    }

    @Override
    public void registerWithExecution(final Action<T> action, final long timeout, final ActiveState<T> state) {
      executables.registerWithExecution(action, timeout, state);
    }

    @Override
    public boolean isRepeatable() {
      return false;
    }

    @Override
    public void repeat() {
      throw new UnsupportedOperationException();
    }

    @Override
    public <F> boolean replaceFailedOutcomeValue(final F failedOutcomeValue) {
      if (failedValue() == UnfailedValue) {
        failedValue(failedOutcomeValue);
        return true;
      }
      return false;
    }

    @Override
    public void restore() {
      // unused; see RepeatableCompletes
    }

    @Override
    public void restore(final Action<T> action) {
      executables.restore(action);
    }

    @Override
    public Scheduler scheduler() {
      return scheduler;
    }

    @Override
    public void startTimer(final long timeout) {
      if (timeout > 0 && scheduler != null && cancellable == null) {
        // 2L delayBefore prevents timeout until after return from here
        cancellable = scheduler.scheduleOnce(this, null, 2L, timeout);
      }
    }

    @Override
    public void intervalSignal(final Scheduled<Object> scheduled, final Object data) {
      // favor success over failure when
      // completing and timeout race
      if (isOutcomeKnown() || executables.isReadyToExecute()) {
        ;
      } else {
        timedOut.set(true);
        failed();
      }
    }

    @Override
    public String toString() {
      return "BasicActiveState[id= " + id + "actions=" + executables.count() + "]";
    }
  }
}
