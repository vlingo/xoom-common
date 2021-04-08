// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.common.message;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncMessageQueue implements MessageQueue, Runnable {

  private final MessageQueue deadLettersQueue;
  private AtomicBoolean dispatching;
  private final ThreadPoolExecutor executor;
  private MessageQueueListener listener;
  private AtomicBoolean open;
  private final ConcurrentLinkedQueue<Message> queue;

  public AsyncMessageQueue() {
    this(null);
  }

  public AsyncMessageQueue(final MessageQueue deadLettersQueue) {
    this.deadLettersQueue = deadLettersQueue;
    this.dispatching = new AtomicBoolean(false);
    this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
    this.open = new AtomicBoolean(false);
    this.queue = new ConcurrentLinkedQueue<Message>();
  }

  public void close() {
    close(true);
  }

  public void close(final boolean flush) {
    if (open.get()) {
      open.set(false);
      
      if (flush) {
        flush();
      }
      
      executor.shutdownNow();
    }
  }

  public void enqueue(final Message message) {
    if (open.get()) {
      queue.add(message);
      executor.execute(this);
    }
  }

  public void flush() {
    try {
      while (!queue.isEmpty()) {
        Thread.sleep(1L);
      }

      while (dispatching.get()) {
        Thread.sleep(1L);
      }
    } catch (Exception e) {
      // ignore
    }
  }

  public boolean isEmpty() {
    return queue.isEmpty() && !dispatching.get();
  }

  public void registerListener(final MessageQueueListener listener) {
    this.open.set(true);
    this.listener = listener;
  }

  public void run() {
    Message message = null;
    
    try {
      dispatching.set(true);
      message = dequeue();
      if (message != null) {
        listener.handleMessage(message);
      }
    } catch (Exception e) {
      // TODO: Log
      if (message != null && deadLettersQueue != null) {
        deadLettersQueue.enqueue(message);
      }
      System.out.println("AsyncMessageQueue: Dispatch to listener hasFailed because: " + e.getMessage());
      e.printStackTrace();
    } finally {
      dispatching.set(false);
    }
  }

  private Message dequeue() {
    return queue.poll();
  }
}
