// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Provide time-based notifications to a {@code Scheduled} once or any number of
 * times until cancellation. The implementor of the {@code Scheduled} protocol
 * is not assumed to be an {@code Actor} and may be a POJO, but the notifications
 * are quite effectively used in an {@code Actor}-based asynchronous environment.
 */
public class Scheduler {
  private final ScheduledExecutorService timer;

  /**
   * Answer a {@code Cancellable} for the repeating scheduled notifier.
   * @param scheduled the {@code Scheduled<T>} to receive notifications
   * @param data the T data to be sent with each notification
   * @param delayBefore the long number of milliseconds before notification interval timing will begin
   * @param interval the long number of milliseconds between each notification
   * @param <T> the type of data to be sent with each notification
   * @return Cancellable
   */
  public <T> Cancellable schedule(final Scheduled<T> scheduled, final T data, final long delayBefore, final long interval) {
    final SchedulerTask<T> schedulerTask = new SchedulerTask<>(scheduled, data, true);
    final ScheduledFuture<?> future = timer.scheduleWithFixedDelay(schedulerTask, delayBefore, interval, TimeUnit.MILLISECONDS);
    schedulerTask.setFuture(future);
    return schedulerTask;
  }

  /**
   * Answer a {@code Cancellable} for the repeating scheduled notifier.
   * @param scheduled the {@code Scheduled<T>} to receive notifications
   * @param data the T data to be sent with each notification
   * @param delayBefore the Duration before notification interval timing will begin
   * @param interval the Duration between each notification
   * @param <T> the type of data to be sent with each notification
   * @return Cancellable
   */
  public <T> Cancellable schedule(final Scheduled<T> scheduled, final T data, final Duration delayBefore, final Duration interval) {
    return schedule(scheduled, data, delayBefore.toMillis(), interval.toMillis());
  }

  /**
   * Answer a {@code Cancellable} for a single scheduled notifier.
   * @param scheduled the {@code Scheduled<T>} to receive notifications
   * @param data the T data to be sent with the notification
   * @param delayBefore the long number of milliseconds before the notification interval time will begin
   * @param interval the long number of milliseconds before the single notification
   * @param <T> the type of data to be sent with each notification
   * @return Cancellable
   */
  public <T> Cancellable scheduleOnce(final Scheduled<T> scheduled, final T data, final long delayBefore, final long interval) {
    final SchedulerTask<T> schedulerTask = new SchedulerTask<>(scheduled, data, false);
    final ScheduledFuture<?> future = timer.schedule(schedulerTask, delayBefore + interval, TimeUnit.MILLISECONDS);
    schedulerTask.setFuture(future);
    return schedulerTask;
  }

  /**
   * Answer a {@code Cancellable} for a single scheduled notifier.
   * @param scheduled the {@code Scheduled<T>} to receive notifications
   * @param data the T data to be sent with the notification
   * @param delayBefore the Duration before the notification interval time will begin
   * @param interval the Duration before the single notification
   * @param <T> the type of data to be sent with each notification
   * @return Cancellable
   */
  public <T> Cancellable scheduleOnce(final Scheduled<T> scheduled, final T data, final Duration delayBefore, final Duration interval) {
    return scheduleOnce(scheduled, data, delayBefore.toMillis(), interval.toMillis());
  }

  /**
   * Construct my default state.
   */
  public Scheduler() {
    this.timer = Executors.newScheduledThreadPool(1);
  }

  /**
   * Close me canceling all schedule notifiers.
   */
  public void close() {
    timer.shutdown();
  }

  /**
   * Wrapper for {@code TimerTask} to care for {@code Scheduled} instances.
   */
  private class SchedulerTask<T> implements Runnable, Cancellable {
    private boolean cancelled;
    private final Scheduled<T> scheduled;
    private final T data;
    private final boolean repeats;
    private ScheduledFuture<?> future;

    SchedulerTask(final Scheduled<T> scheduled, final T data, final boolean repeats) {
      this.scheduled = scheduled;
      this.data = data;
      this.repeats = repeats;
      this.cancelled = false;
    }

    @Override
    public void run() {
      scheduled.intervalSignal(scheduled, data);

      if (!repeats) {
        cancel();
      }
    }

    @Override
    public boolean cancel() {
      cancelled = true;
      if (future != null) {
        return future.cancel(false);
      }
      return cancelled;
    }

    void setFuture(final ScheduledFuture<?> future) {
      this.future = future;
      if (cancelled) {
        cancel();
      }
    }
  }
}
