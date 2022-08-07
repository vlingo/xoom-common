package io.vlingo.xoom.common.serialization.jackson;

import io.vlingo.xoom.common.serialization.JsonSerializationStrategy;
import io.vlingo.xoom.common.serialization.JsonSerializationTest;

public class JacksonJsonSerializationTest extends JsonSerializationTest {
  @Override
  protected JsonSerializationStrategy serializationStrategy() {
    return new JacksonJsonSerialization();
  }
}
