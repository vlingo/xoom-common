// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.message;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import io.vlingo.common.message.ByteBufferPool;
import io.vlingo.common.message.ByteBufferPool.PooledByteBuffer;

public class ByteBufferPoolTest {

  @Test
  public void testAccessAvailableRelease() {
    ByteBufferPool pool = new ByteBufferPool(10, 100);
    
    assertEquals(10, pool.available());
    
    PooledByteBuffer buffer1 = pool.access();
    PooledByteBuffer buffer2 = pool.access();
    PooledByteBuffer buffer3 = pool.access();
    
    assertEquals(7, pool.available());
    
    buffer1.release();
    
    assertEquals(8, pool.available());
    
    buffer2.release();
    buffer3.release();
    
    assertEquals(10, pool.available());
  }
  
  @Test
  public void testPooledByteBuffer() {
    final String testText = "Hello, PooledByteBuffer";
    
    ByteBufferPool pool = new ByteBufferPool(10, 100);
    
    PooledByteBuffer buffer1 = pool.access();
    
    buffer1.buffer().put(testText.getBytes());
    
    assertEquals(testText, new String(buffer1.flip().array(), 0, buffer1.limit()));
    
    buffer1.release();
  }
}
