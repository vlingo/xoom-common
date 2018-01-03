// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.message;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class AsyncMessageQueue implements MessageQueue, Runnable {

  private final MessageQueue deadLettersQueue;
  private boolean dispatching;
  private final ThreadPoolExecutor executor;
  private MessageQueueListener listener;
  private volatile boolean open;
  private final ConcurrentLinkedQueue<Message> queue;

  public AsyncMessageQueue() {
    this(null);
  }

  public AsyncMessageQueue(final MessageQueue deadLettersQueue) {
    this.deadLettersQueue = deadLettersQueue;
    this.dispatching = false;
    this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
    this.open = false;
    this.queue = new ConcurrentLinkedQueue<Message>();
  }

  public void close() {
    close(true);
  }

  public void close(final boolean flush) {
    if (open) {
      open = false;
      
      if (flush) {
        flush();
      }
      
      executor.shutdownNow();
    }
  }

  public void enqueue(final Message message) {
    if (open) {
      queue.add(message);
      executor.execute(this);
    }
  }

  public void flush() {
    try {
      while (!queue.isEmpty()) {
        Thread.sleep(1L);
      }
      
      while (dispatching) {
        Thread.sleep(1L);
      }
    } catch (Exception e) {
      // ignore
    }
  }

  public boolean isEmpty() {
    return queue.isEmpty() && !dispatching;
  }

  public void registerListener(final MessageQueueListener listener) {
    this.open = true;
    this.listener = listener;
  }

  public void run() {
    Message message = null;
    
    try {
      dispatching = true;
      message = dequeue();
      if (message != null) {
        listener.handleMessage(message);
      }
    } catch (Exception e) {
      // TODO: Log
      if (message != null && deadLettersQueue != null) {
        deadLettersQueue.enqueue(message);
      }
      System.out.println("AsyncMessageQueue: Dispatch to listener failed because: " + e.getMessage());
      e.printStackTrace();
    } finally {
      dispatching = false;
    }
  }

  private Message dequeue() {
    return queue.poll();
  }
}
