// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.message;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import io.vlingo.common.message.AsyncMessageQueue;
import io.vlingo.common.message.Message;
import io.vlingo.common.message.MessageQueueListener;

public class AsyncMessageQueueTest {

  private CountingDeadLettersQueue countingDeadLettersQueue;
  private CountingDeadLettersListener countingDeadLettersListener;
  private List<Message> deliveredMessages;
  private AsyncMessageQueue queue;
  private AsyncMessageQueue exceptionsQueue;

  @Test
  public void testEnqueue() {
    queue.enqueue(new Message() {});
    queue.enqueue(new Message() {});
    queue.enqueue(new Message() {});
    
    while (!queue.isEmpty()) ;
    
    assertEquals(3, deliveredMessages.size());
  }

  @Test
  public void testFlush() {
    for (int idx = 0; idx < 1000; ++idx) {
      queue.enqueue(new Message() {});
    }
    
    queue.flush();
    
    assertEquals(1000, deliveredMessages.size());
  }

  @Test
  public void testIsEmptyWithFlush() {
    for (int idx = 0; idx < 100000; ++idx) {
      queue.enqueue(new Message() {});
    }
    
    assertEquals(false, queue.isEmpty());
    queue.flush();
    assertEquals(true, queue.isEmpty());
  }

  @Test
  public void testClose() {
    for (int idx = 0; idx < 1000; ++idx) {
      queue.enqueue(new Message() {});
    }
    
    queue.close();
    
    queue.enqueue(new Message() {});
    
    queue.flush();
    
    assertNotEquals(1001, deliveredMessages.size());
    assertEquals(1000, deliveredMessages.size());
  }

  @Test
  public void testDeadLettersQueue() throws Exception {
    for (int idx = 0; idx < 5; ++idx) {
      exceptionsQueue.enqueue(new Message() {});
    }
    
    exceptionsQueue.close();
    
    assertEquals(5, countingDeadLettersQueue.enqueuedCount);
    assertEquals(5, countingDeadLettersListener.handledCount);
  }

  @Before
  public void setUp() {
    deliveredMessages = new ArrayList<Message>();
    
    queue = new AsyncMessageQueue();
    queue.registerListener(new ExceptionThrowingListener(false));
    
    countingDeadLettersListener = new CountingDeadLettersListener();
    countingDeadLettersQueue = new CountingDeadLettersQueue();
    countingDeadLettersQueue.registerListener(countingDeadLettersListener);
    
    exceptionsQueue = new AsyncMessageQueue(countingDeadLettersQueue);
    exceptionsQueue.registerListener(new ExceptionThrowingListener(true));
  }

  private class CountingDeadLettersListener implements MessageQueueListener {
    public int handledCount;

    @Override
    public void handleMessage(final Message message) throws Exception {
      ++handledCount;
    }    
  }

  private class ExceptionThrowingListener implements MessageQueueListener {
    private final boolean throwException;
    
    private ExceptionThrowingListener(final boolean throwException) {
      this.throwException = throwException;
    }

    @Override
    public void handleMessage(final Message message) throws Exception {
      if (throwException) {
        throw new Exception("test");
      } else {
        deliveredMessages.add(message);
      }
    }    
  }

  private class CountingDeadLettersQueue extends AsyncMessageQueue {
    public int enqueuedCount;
    
    @Override
    public void enqueue(final Message message) {
      ++enqueuedCount;
      super.enqueue(message);
    }
  }
}
