// Copyright © 2012-2017 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.message;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Converters {
  private static Charset CHARSET_VALUE = Charset.forName(StandardCharsets.UTF_8.name());

  public static String bytesToText(final byte[] bytes, final int index, final int length) {
    return new String(bytes, index, length, CHARSET_VALUE);
  }

  public static void changeCharset(final String charsetName) {
    CHARSET_VALUE = Charset.forName(charsetName);
  }
  
  public static byte[] textToBytes(final String text) {
    return text.getBytes(CHARSET_VALUE);
  }

  /** Uses buffer.flip() and then buffer.clear()
   * @param sendingNodeId short
   * @param buffer ByteBuffer
   * @return RawMessage
   */
  public static RawMessage toRawMessage(final short sendingNodeId, final ByteBuffer buffer) {
    buffer.flip();

    final RawMessage message = new RawMessage(buffer.limit());

    message.put(buffer, false);

    buffer.clear();

    final RawMessageHeader header = new RawMessageHeader(sendingNodeId, (short) 0, (short) message.length());

    message.header(header);

    return message;
  }
}
