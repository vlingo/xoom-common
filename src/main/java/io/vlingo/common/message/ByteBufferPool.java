// Copyright © 2012-2017 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.message;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ByteBufferPool {
  private final int maxBufferSize;
  private final List<PooledByteBuffer> pool;
  
  public ByteBufferPool(final int poolSize, final int maxBufferSize) {
    this.pool = new ArrayList<PooledByteBuffer>(poolSize);
    this.maxBufferSize = maxBufferSize;
    
    for (int idx = 0; idx < poolSize; ++idx) {
      pool.add(new PooledByteBuffer(maxBufferSize));
    }
  }
  
  public int available() {
    // this is not an accurate calculation because the number
    // of in-use buffers could change before the loop completes
    
    int available = pool.size();
    
    for (PooledByteBuffer buffer : pool) {
      if (buffer.isInUse()) {
        --available;
      }
    }
    
    return available;
  }
  
  // TODO: use pool structure that does not require synchronization (?)
  
  public synchronized PooledByteBuffer access() {
    while (true) {
      for (int idx = 0; idx < maxBufferSize; ++idx) {
        final PooledByteBuffer buffer = pool.get(idx);
        if (!buffer.isInUse()) {
          buffer.inUse();
          return buffer;
        }
      }
    }
  }
  
  public class PooledByteBuffer {
    private final ByteBuffer buffer;
    private volatile boolean inUse;
    
    PooledByteBuffer(final int maxBufferSize) {
      this.buffer = ByteBuffer.allocate(maxBufferSize);
      this.inUse = false;
    }
    
    public ByteBuffer buffer() {
      return buffer;
    }
    
    public ByteBuffer flip() {
      buffer.flip();
      
      return buffer;
    }
    
    public int limit() {
      return buffer.limit();
    }
    
    public ByteBuffer put(final ByteBuffer source) {
      return buffer.put(source);
    }
    
    public void release() {
      notInUse();
    }
    
    private void inUse() {
      buffer.clear();
      
      inUse = true;
    }
    
    private void notInUse() {
      inUse = false;
    }
    
    private boolean isInUse() {
      return inUse;
    }
  }
}
