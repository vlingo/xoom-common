// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.common.message;

import java.math.BigDecimal;
import java.util.Date;

public class MessageExchangeReader extends MessageReader {
  private final Message message;

  public static MessageExchangeReader from(final Message message) {
    return new MessageExchangeReader(message);
  }

  //==============================================
  // message header
  //==============================================

  public String id() {
    return message.id();
  }

  public long idAsLong() {
    return Long.parseLong(id());
  }

  public String type() {
    return message.type();
  }

  //==============================================
  // message payload
  //==============================================

  public BigDecimal payloadBigDecimalValue(final String... keys) {
    String stringValue = stringValue(keys);
    return stringValue == null ? null : new BigDecimal(stringValue);
  }

  public Boolean payloadBooleanValue(final String... keys) {
    String stringValue = stringValue(keys);
    return stringValue == null ? null : Boolean.parseBoolean(stringValue);
  }

  public Date payloadDateValue(final String... keys) {
    String stringValue = stringValue(keys);
    return stringValue == null ? null : new Date(Long.parseLong(stringValue));
  }

  public Double payloadDoubleValue(final String... keys) {
    String stringValue = stringValue(keys);
    return stringValue == null ? null : Double.parseDouble(stringValue);
  }

  public Float payloadFloatValue(final String... keys) {
    String stringValue = stringValue(keys);
    return stringValue == null ? null : Float.parseFloat(stringValue);
  }

  public Integer payloadIntegerValue(final String... keys) {
    String stringValue = stringValue(keys);
    return stringValue == null ? null : Integer.parseInt(stringValue);
  }

  public Long payloadLongValue(final String... keys) {
    String stringValue = stringValue(keys);
    return stringValue == null ? null : Long.parseLong(stringValue);
  }

  public String payloadStringValue(final String... keys) {
    String stringValue = stringValue(keys);
    return stringValue;
  }

  private MessageExchangeReader(final Message message) {
    super((String) message.payload());

    this.message = message;
  }
}
