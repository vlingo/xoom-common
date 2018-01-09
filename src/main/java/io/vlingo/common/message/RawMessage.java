// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.message;

import java.nio.ByteBuffer;

/**
 * Reusable raw message with header. Assume one instance per client channel.
 * Thus, the header and the bytes are reused to avoid ridicules GC.
 */
public class RawMessage {
  private final byte[] bytes;       // reused
  private RawMessageHeader header;  // reused
  private int index;

  public static RawMessage copy(final RawMessage original) {
    return new RawMessage(original);
  }

  public static RawMessage from(final int nodeId, final int type, final int length) {
    return new RawMessage(RawMessageHeader.from(nodeId, type, length), length);
  }

  public static RawMessage from(final int nodeId, final int type, final String textMessage) {
    final byte[] textBytes = Converters.textToBytes(textMessage);
    final RawMessageHeader header = RawMessageHeader.from(nodeId, type, textBytes.length);
    final RawMessage message = new RawMessage(header, textBytes.length);
    message.append(textBytes, 0, textBytes.length);
    
    return message;
  }

  public static RawMessage from(final ByteBuffer buffer) {
    final RawMessageHeader header = RawMessageHeader.from(buffer);
    final RawMessage message = new RawMessage(header, header.length());
    message.putRemaining(buffer);
    return message;
  }

  public RawMessage(final RawMessageHeader header, final int maxMessageSize) {
    this.bytes = new byte[maxMessageSize];
    this.header = header;
    this.index = 0;
  }

  public RawMessage(final int maxMessageSize) {
    this(new RawMessageHeader(), maxMessageSize);
  }

  public RawMessage(final byte[] bytes) {
    this.bytes = bytes;
    this.header = new RawMessageHeader();
    this.index = bytes.length;
  }
  
  public RawMessage(final RawMessage copy) {
    this(copy.length());
    this.append(copy.bytes, 0, copy.length());
    this.header(RawMessageHeader.from(copy.header));
  }

  public void append(final byte[] sourceBytes, final int sourceIndex, final int sourceLength) {
    System.arraycopy(sourceBytes, sourceIndex, this.bytes, this.index, sourceLength);
    this.index += sourceLength;
  }

  public final byte[] asBinaryMessage() {
    return this.bytes;
  }

  public final ByteBuffer asByteBuffer() {
    final ByteBuffer buffer = ByteBuffer.allocate(RawMessageHeader.BYTES + bytes.length);
    copyBytesTo(buffer);
    buffer.flip();
    return buffer;
  }

  public final String asTextMessage() {
    return Converters.bytesToText(bytes, 0, length());
  }

  public void copyBytesTo(final ByteBuffer buffer) {
    header.copyBytesTo(buffer);
    buffer.put(bytes);
  }

  public final RawMessageHeader header() {
    return header;
  }

  public void header(final RawMessageHeader header) {
    this.header = header;
  }

  public void headerFrom(final ByteBuffer buffer) {
    header.read(buffer);
  }

  public int length() {
    return index;
  }

  public void put(final ByteBuffer buffer, final boolean flip) {
    if (flip) {
      buffer.flip();
    }
    final int length = buffer.limit();
    System.arraycopy(buffer.array(), 0, this.bytes, 0, length);
    this.index = length;
  }

  public void put(final ByteBuffer buffer) {
    put(buffer, true);
  }

  public void putRemaining(final ByteBuffer buffer) {
    final int position = buffer.position();
    final int length = buffer.limit() - position;
    System.arraycopy(buffer.array(), position, this.bytes, 0, length);
    this.index = length;
  }

  public int requiredMessageLength() {
    return header.length();
  }

  public final RawMessage reset() {
    index = 0;
    return this;
  }

  @Override
  public String toString() {
    return "RawMessage[header=" + header + " length=" + length() + "]";
  }
}
