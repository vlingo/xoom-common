// Copyright © 2012-2017 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.message;

import java.nio.ByteBuffer;

public class RawMessageBuilder {
  private ScanMode mode;
  private final RawMessage rawMessage;
  private final ByteBuffer workBuffer;

  public RawMessageBuilder(final int maxMessageSize) {
    this.rawMessage = new RawMessage(maxMessageSize);
    this.workBuffer = ByteBuffer.allocate(maxMessageSize);
    this.mode = ScanMode.READ_HEADER;
  }

  public final RawMessage currentRawMessage() {
    if (isCurrentMessageIncomplete()) {
      throw new IllegalStateException("The current raw message is incomplete.");
    }

    return rawMessage;
  }

  public boolean hasContent() {
    return workBuffer.position() > 0;
  }

  public boolean isCurrentMessageComplete() {
    final int length = length();
    final int expected = rawMessage.requiredMessageLength();

    return length != 0 && length == expected;
  }

  public boolean isCurrentMessageIncomplete() {
    return length() < rawMessage.requiredMessageLength();
  }

  public int length() {
    return rawMessage.length();
  }

  public RawMessageBuilder prepareContent() {
    workBuffer.flip();
    return this;
  }

  public RawMessageBuilder prepareForNextMessage() {
    rawMessage.reset();
    return this;
  }

  public void sync() {
    if (!underflow()) {
      final byte[] content = workBuffer.array();

      if (mode.isReadHeaderMode()) {
        rawMessage.headerFrom(workBuffer);
      }

      final int messageTotalLength = rawMessage.requiredMessageLength();
      final int missingRawMessageLength = messageTotalLength - rawMessage.length();
      final int contentPosition = workBuffer.position();
      final int availableContentLength = workBuffer.limit() - contentPosition;

      final int appendLength = Math.min(missingRawMessageLength, availableContentLength);

      rawMessage.append(content, contentPosition, appendLength);

      workBuffer.position(contentPosition + appendLength);

      if (availableContentLength == missingRawMessageLength) {
        workBuffer.clear();
        setMode(ScanMode.READ_HEADER);
      } else if (availableContentLength > missingRawMessageLength) {
        setMode(ScanMode.READ_HEADER);
      } else if (availableContentLength < missingRawMessageLength) {
        workBuffer.clear();
        setMode(ScanMode.REUSE_HEADER);
      }
    }
  }

  public final ByteBuffer workBuffer() {
    return this.workBuffer;
  }

  private void setMode(final ScanMode mode) {
    this.mode = mode;
  }

  private boolean underflow() {
    final int remainingContentLength = workBuffer.limit() - workBuffer.position();
    final int minimumRequiredLength = RawMessageHeader.BYTES + 1;

    if (rawMessage.requiredMessageLength() == 0 && remainingContentLength < minimumRequiredLength) {
      final byte[] content = workBuffer.array();
      System.arraycopy(content, workBuffer.position(), content, 0, remainingContentLength);
      workBuffer.position(0);
      workBuffer.limit(remainingContentLength);
      setMode(ScanMode.READ_HEADER);
      return true;
    }

    return false;
  }

  private enum ScanMode {
    READ_HEADER, REUSE_HEADER;

    boolean isReadHeaderMode() {
      return this == READ_HEADER;
    }
  }
}
