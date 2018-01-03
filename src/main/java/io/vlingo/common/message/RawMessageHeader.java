// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.message;

import java.nio.ByteBuffer;

public final class RawMessageHeader {
  private static final int SHORT_FIELDS = 5;
  private static final int INT_FIELDS = 1;
  private static final int SHORT_BYTES = Short.SIZE / Byte.SIZE;
  private static final int INT_BYTES = Integer.SIZE / Byte.SIZE;
  public static final int BYTES = (SHORT_BYTES * SHORT_FIELDS) + (INT_BYTES + INT_FIELDS);
  private static final short HEADER_ID = 3730 | 0x01; // version 1

  private int length;
  private short nodeId;
  private short type;

  public static final RawMessageHeader from(final ByteBuffer buffer) {
    RawMessageHeader header = new RawMessageHeader();

    return header.read(buffer);
  }

  public static final RawMessageHeader from(final short nodeId, final short type, final int length) {
    return new RawMessageHeader(nodeId, type, length);
  }

  public static final RawMessageHeader from(final short nodeId, final int type, final int length) {
    return new RawMessageHeader(nodeId, (short) type, length);
  }

  public static final RawMessageHeader from(final int nodeId, final int type, final int length) {
    return new RawMessageHeader((short) nodeId, (short) type, length);
  }
  
  public static final RawMessageHeader from(final RawMessageHeader copy) {
    return from(copy.nodeId, copy.type, copy.length);
  }

  public RawMessageHeader() {
    this((short) -1, (short) -1, (short) -1);
  }

  public RawMessageHeader(final short nodeId, final short type, final int length) {
    this.nodeId = nodeId;
    this.type = type;
    this.length = length;
  }

  public RawMessageHeader(final int nodeId, final int type, final int length) {
    this((short) nodeId, (short) type, length);
  }

  public int length() {
    return length;
  }

  public short nodeId() {
    return nodeId;
  }

  @Override
  public boolean equals(Object other) {
    if (other == null || other.getClass() != RawMessageHeader.class) {
      return false;
    }
    
    final RawMessageHeader otherHeader = (RawMessageHeader) other;
    
    return this.nodeId == otherHeader.nodeId &&
            this.type == otherHeader.type &&
            this.length == otherHeader.length;
  }
  
  @Override
  public String toString() {
    return "RawMessageHeader[headerId=" + HEADER_ID + " nodeId=" + nodeId + " type=" + type + " length=" + length + "]";
  }

  public short type() {
    return type;
  }
  
  public void copyBytesTo(final ByteBuffer buffer) {
    buffer
        .putShort(HEADER_ID)
        .putShort(nodeId)
        .putShort(type)
        .putInt(length)
        .putShort(Short.MAX_VALUE)
        .putShort(Short.MAX_VALUE);
  }

  public final RawMessageHeader read(final ByteBuffer buffer) {
    final short headerId = buffer.getShort();

    if (headerId != HEADER_ID) {
      throw new IllegalArgumentException("Invalid raw message header.");
    }

    final short nodeId = buffer.getShort();
    final short type = buffer.getShort();
    final int length = buffer.getInt();
    buffer.getShort(); // unused1
    buffer.getShort(); // unused2

    return this.setAll(nodeId, type, length);
  }

  protected final RawMessageHeader setAll(final short nodeId, final short type, final int length) {
    this.nodeId = nodeId;
    this.type = type;
    this.length = length;

    return this;
  }
}
