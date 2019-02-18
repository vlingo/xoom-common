// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Provide time-based notifications to a {@code Scheduled} once or any number of
 * times until cancellation. The implementor of the {@code Scheduled} protocol
 * is not assumed to be an {@code Actor} and may be a POJO, but the notifications
 * are quite effectively used in an {@code Actor}-based asynchronous environment.
 */
public class Scheduler {
  private final Timer timer;

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
    timer.schedule(schedulerTask, delayBefore, interval);
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
    final SchedulerTask<T> schedulerTask = new SchedulerTask<>(scheduled, data, true);
    timer.schedule(schedulerTask, delayBefore.toMillis(), interval.toMillis());
    return schedulerTask;
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
    timer.schedule(schedulerTask, delayBefore + interval);
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
    final SchedulerTask<T> schedulerTask = new SchedulerTask<>(scheduled, data, false);
    timer.schedule(schedulerTask, delayBefore.toMillis() + interval.toMillis());
    return schedulerTask;
  }

  /**
   * Construct my default state.
   */
  public Scheduler() {
    this.timer = new Timer();
  }

  /**
   * Close me canceling all schedule notifiers.
   */
  public void close() {
    timer.cancel();
  }

  /**
   * Wrapper for {@code TimerTask} to care for {@code Scheduled} instances.
   */
  private class SchedulerTask<T> extends TimerTask implements Cancellable {
    private final Scheduled<T> scheduled;
    private final T data;
    private final boolean repeats;
    
    SchedulerTask(final Scheduled<T> scheduled, final T data, final boolean repeats) {
      this.scheduled = scheduled;
      this.data = data;
      this.repeats = repeats;
    }
    
    @Override
    public void run() {
      scheduled.intervalSignal(scheduled, data);
      
      if (!repeats) {
        cancel();
      }
    }
  }
}
