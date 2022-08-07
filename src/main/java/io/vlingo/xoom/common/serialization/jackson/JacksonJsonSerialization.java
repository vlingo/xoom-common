package io.vlingo.xoom.common.serialization.jackson;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paranamer.ParanamerModule;
import io.vlingo.xoom.common.serialization.JsonSerializationStrategy;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.*;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class JacksonJsonSerialization implements JsonSerializationStrategy {
  private final static ObjectMapper objectMapper;
  private final static Pattern numericPattern = Pattern.compile("-?\\d+(\\.\\d+)?");

  static {
    objectMapper = JsonMapper.builder()
        .visibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        .visibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE)
        .visibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE)
        .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        .serializationInclusion(JsonInclude.Include.NON_NULL)
        .addModule(new ParanamerModule())
        .addModule(new JavaTimeModule())
        .addModule(xoomModule())
        .build();
  }

  @Override
  public <T> T deserialized(final String serialization, final Class<T> type) {
    try {
      return objectMapper.readValue(serialization, type);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public <T> T deserialized(final String serialization, final Type type) {
    try {
      return objectMapper.readValue(serialization, objectMapper.constructType(type));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public <T> List<T> deserializedList(final String serialization, final Type listOfType) {
    try {
      return objectMapper.readValue(serialization, objectMapper.constructType(listOfType));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String serialized(final Object instance) {
    try {
      return objectMapper.writeValueAsString(instance);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public <T> String serialized(final Collection<T> instance) {
    try {
      return objectMapper.writeValueAsString(instance);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public <T> String serialized(final List<T> instance) {
    try {
      return objectMapper.writeValueAsString(instance);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  private static Module xoomModule() {
    final SimpleModule xoomModule = new SimpleModule();
    xoomModule.addSerializer(Date.class, new DateSerializer());
    xoomModule.addSerializer(LocalDate.class, new LocalDateSerializer());
    xoomModule.addDeserializer(LocalDate.class, new LocalDateDeserializer());
    xoomModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
    xoomModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
    xoomModule.addSerializer(OffsetDateTime.class, new OffsetDateTimeSerializer());
    xoomModule.addDeserializer(OffsetDateTime.class, new OffsetDateTimeDeserializer());
    return xoomModule;
  }

  private static class DateSerializer extends StdSerializer<Date> {

    public DateSerializer() {
      super(Date.class);
    }

    @Override
    public void serialize(final Date date, final JsonGenerator generator, final SerializerProvider provider) throws IOException {
      generator.writeString(Long.toString(date.getTime()));
    }
  }

  private static class LocalDateSerializer extends StdSerializer<LocalDate> {

    public LocalDateSerializer() {
      super(LocalDate.class);
    }

    @Override
    public void serialize(final LocalDate date, final JsonGenerator generator, final SerializerProvider provider) throws IOException {
      generator.writeString(Long.toString(date.toEpochDay()));
    }
  }

  private static class LocalDateDeserializer extends StdDeserializer<LocalDate> {

    public LocalDateDeserializer() {
      super(LocalDate.class);
    }

    @Override
    public LocalDate deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {

      if (isNumericString(parser.getValueAsString())) {
        final long epochDay = Long.parseLong(parser.getValueAsString());
        return LocalDate.ofEpochDay(epochDay);
      }

      return LocalDate.parse(parser.getValueAsString());
    }
  }

  private static class LocalDateTimeSerializer extends StdSerializer<LocalDateTime> {

    public LocalDateTimeSerializer() {
      super(LocalDateTime.class);
    }

    @Override
    public void serialize(final LocalDateTime date, final JsonGenerator generator, final SerializerProvider provider) throws IOException {
      generator.writeString(Long.toString(date.atZone(ZoneOffset.UTC).toInstant().toEpochMilli()));
    }
  }

  private static class LocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {

    public LocalDateTimeDeserializer() {
      super(LocalDateTime.class);
    }

    @Override
    public LocalDateTime deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {

      if (isNumericString(parser.getValueAsString())) {
        final long milli = Long.parseLong(parser.getValueAsString());
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(milli), ZoneOffset.UTC);
      }

      return LocalDateTime.parse(parser.getValueAsString());
    }
  }

  private static class OffsetDateTimeSerializer extends StdSerializer<OffsetDateTime> {

    public OffsetDateTimeSerializer() {
      super(OffsetDateTime.class);
    }

    @Override
    public void serialize(final OffsetDateTime date, final JsonGenerator generator, final SerializerProvider provider) throws IOException {
      generator.writeString(date.toInstant().toEpochMilli() + ";" + date.getOffset().toString());
    }
  }

  private static class OffsetDateTimeDeserializer extends StdDeserializer<OffsetDateTime> {

    public OffsetDateTimeDeserializer() {
      super(OffsetDateTime.class);
    }

    @Override
    public OffsetDateTime deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
      final String[] encoding = parser.getValueAsString().split(";");
      final Date date = new Date(Long.parseLong(encoding[0]));
      return date.toInstant().atOffset(ZoneOffset.of(encoding[1]));
    }
  }

  private static boolean isNumericString(final String element) {
    return numericPattern.matcher(element).matches();
  }
}
