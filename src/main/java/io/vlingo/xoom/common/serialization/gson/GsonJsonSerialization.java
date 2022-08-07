// Copyright © 2012-2022 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.common.serialization.gson;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.vlingo.xoom.common.serialization.JsonSerializationStrategy;

import java.lang.reflect.Type;
import java.time.*;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class GsonJsonSerialization implements JsonSerializationStrategy {
  private final static Gson gson;
  private final static Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

  static {
    gson = new GsonBuilder()
        .registerTypeAdapter(Class.class, new ClassSerializer())
        .registerTypeAdapter(Class.class, new ClassDeserializer())
        .registerTypeAdapter(Date.class, new DateSerializer())
        .registerTypeAdapter(Date.class, new DateDeserializer())
        .registerTypeAdapter(LocalDate.class, new LocalDateSerializer())
        .registerTypeAdapter(LocalDate.class, new LocalDateDeserializer())
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
        .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeSerializer())
        .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeDeserializer())
        .create();
  }

  @Override
  public <T> T deserialized(final String serialization, final Class<T> type) {
    T instance = gson.fromJson(serialization, type);
    return instance;
  }

  @Override
  public <T> T deserialized(final String serialization, final Type type) {
    T instance = gson.fromJson(serialization, type);
    return instance;
  }

  @Override
  public <T> List<T> deserializedList(final String serialization, final Type listOfType) {
    final List<T> list = gson.fromJson(serialization, listOfType);
    return list;
  }

  @Override
  public String serialized(final Object instance) {
    final String serialization = gson.toJson(instance);
    return serialization;
  }

  @Override
  public <T> String serialized(final Collection<T> instance) {
    final Type collectionOfT = new TypeToken<Collection<T>>() {}.getType();
    final String serialization = gson.toJson(instance, collectionOfT);
    return serialization;
  }

  @Override
  public <T> String serialized(final List<T> instance) {
    final Type listOfT = new TypeToken<List<T>>() {}.getType();
    final String serialization = gson.toJson(instance, listOfT);
    return serialization;
  }

  @SuppressWarnings("rawtypes")
  private static class ClassSerializer implements JsonSerializer<Class> {
    @Override
    public JsonElement serialize(Class source, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(source.getName());
    }
  }

  @SuppressWarnings("rawtypes")
  private static class ClassDeserializer implements JsonDeserializer<Class> {
    @Override
    public Class deserialize(JsonElement json, Type typeOfTarget, JsonDeserializationContext context) throws JsonParseException {
      final String classname = json.getAsJsonPrimitive().getAsString();
      try {
        return Class.forName(classname);
      } catch (ClassNotFoundException e) {
        throw new JsonParseException(e);
      }
    }
  }

  private static class DateSerializer implements JsonSerializer<Date> {
    @Override
    public JsonElement serialize(Date source, Type typeOfSource, JsonSerializationContext context) {
      return new JsonPrimitive(Long.toString(source.getTime()));
    }
  }

  private static class DateDeserializer implements JsonDeserializer<Date> {
    @Override
    public Date deserialize(JsonElement json, Type typeOfTarget, JsonDeserializationContext context) throws JsonParseException {
      long time = Long.parseLong(json.getAsJsonPrimitive().getAsString());
      return new Date(time);
    }
  }

  private static class LocalDateSerializer implements JsonSerializer<LocalDate> {
    public JsonElement serialize(LocalDate source, Type typeOfSource, JsonSerializationContext context) {
      return new JsonPrimitive(Long.toString(source.toEpochDay()));
    }
  }

  private static class LocalDateDeserializer implements JsonDeserializer<LocalDate> {
    public LocalDate deserialize(JsonElement json, Type typeOfTarget, JsonDeserializationContext context) throws JsonParseException {
      if (isNumericString(json.getAsJsonPrimitive().getAsString())) {
        final long epochDay = Long.parseLong(json.getAsJsonPrimitive().getAsString());
        return LocalDate.ofEpochDay(epochDay);
      }

      return LocalDate.parse(json.getAsJsonPrimitive().getAsString());
    }
  }

  private static class LocalDateTimeSerializer implements JsonSerializer<LocalDateTime> {
    public JsonElement serialize(final LocalDateTime source, Type typeOfSource, JsonSerializationContext context) {
      return new JsonPrimitive(Long.toString(source.atZone(ZoneOffset.UTC).toInstant().toEpochMilli()));
    }
  }

  private static class LocalDateTimeDeserializer implements JsonDeserializer<LocalDateTime> {
    public LocalDateTime deserialize(JsonElement json, Type typeOfTarget, JsonDeserializationContext context) throws JsonParseException {
      if (isNumericString(json.getAsJsonPrimitive().getAsString())) {
        final long milli = Long.parseLong(json.getAsJsonPrimitive().getAsString());
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(milli), ZoneOffset.UTC);
      }

      return LocalDateTime.parse(json.getAsJsonPrimitive().getAsString());
    }
  }

  private static class OffsetDateTimeSerializer implements JsonSerializer<OffsetDateTime> {
    @Override
    public JsonElement serialize(OffsetDateTime source, Type typeOfSource, JsonSerializationContext context) {
      return new JsonPrimitive(source.toInstant().toEpochMilli() + ";" + source.getOffset().toString());
    }
  }

  private static class OffsetDateTimeDeserializer implements JsonDeserializer<OffsetDateTime> {
    @Override
    public OffsetDateTime deserialize(JsonElement json, Type typeOfTarget, JsonDeserializationContext context) throws JsonParseException {
      final String[] encoding = json.getAsJsonPrimitive().getAsString().split(";");
      final Date date = new Date(Long.parseLong(encoding[0]));
      return date.toInstant().atOffset(ZoneOffset.of(encoding[1]));
    }
  }

  private static boolean isNumericString(String element) {
    return pattern.matcher(element).matches();
  }
}
