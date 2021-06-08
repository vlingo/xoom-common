// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.common.message;

import java.util.Date;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import io.vlingo.xoom.common.serialization.JsonSerialization;

public class MessageExchangeReaderTest {

  @Test
  public void testThatMessageParses() {
    final C complex = new C(new A("One", 2), new B(3, 4));

    final String id = UUID.randomUUID().toString();

    final String serializationOfC = JsonSerialization.serialized(complex);

    final Message message = new TestMessage(id, "JSON", new Header("TYPE", "basic"), serializationOfC);

    final String serializationOfMessage = JsonSerialization.serialized(message);

    System.out.println("SER: " + serializationOfMessage);

    final Message restored = JsonSerialization.deserialized(serializationOfMessage, TestMessage.class);

    final MessageExchangeReader reader = MessageExchangeReader.from(restored);

    Assert.assertEquals(id, reader.id());

    Assert.assertEquals("JSON", reader.type());

    final TestMessage testMessage = reader.message();

    Assert.assertEquals("TYPE", testMessage.header.key);
    Assert.assertEquals("basic", testMessage.header.value);

    Assert.assertEquals("One", reader.payloadStringValue("a", "text"));
    Assert.assertEquals(2, reader.payloadIntegerValue("a", "value").intValue());

    Assert.assertEquals(3, reader.payloadIntegerValue("b", "value1").intValue());
    Assert.assertEquals(4, reader.payloadIntegerValue("b", "value2").intValue());
    Assert.assertEquals(7, reader.payloadIntegerValue("b", "value3").intValue());

    Assert.assertEquals("One 7", reader.payloadStringValue("message"));
  }

  static final class A {
    public final String text;
    public final int value;

    A(final String text, final int value) {
      this.text = text;
      this.value = value;
    }
  }

  static final class B {
    public final int value1;
    public final int value2;
    public final int value3;

    B(final int value1, final int value2) {
      this.value1 = value1;
      this.value2 = value2;
      this.value3 = value1 + value2;
    }
  }

  static final class C {
    public final A a;
    public final B b;
    public final String message;

    C(A a, B b) {
      this.a = a;
      this.b = b;
      this.message = a.text + " " + b.value3;
    }
  }

  static final class Header {
    public final String key;
    public final String value;

    Header(final String key, final String value) {
      this.key = key;
      this.value = value;
    }
  }

  static final class TestMessage implements Message {
    private final Header header;
    private final String id;
    private final Date occurredOn;
    private final String payload;
    private final String type;
    private final String version;

    TestMessage(final String id, final String type, final Header header, final String payload) {
      this.id = id;
      this.type = type;
      this.header = header;
      this.payload = payload;
      this.occurredOn = new Date();
      this.version = "1.0.0";
    }

    public Header header() {
      return header;
    }

    @Override
    public String id() {
      return id;
    }

    @Override
    public Date occurredOn() {
      return occurredOn;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T payload() {
      return (T) payload;
    }

    @Override
    public String type() {
      return type;
    }

    @Override
    public String version() {
      return version;
    }
  }
}
