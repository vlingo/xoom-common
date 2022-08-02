package io.vlingo.xoom.common.serialization.gson;

import io.vlingo.xoom.common.serialization.JsonSerializationStrategy;
import io.vlingo.xoom.common.serialization.JsonSerializationTest;

public class GsonJsonSerializationTest extends JsonSerializationTest {
  @Override
  protected JsonSerializationStrategy serializationStrategy() {
    return new GsonJsonSerialization();
  }
}
