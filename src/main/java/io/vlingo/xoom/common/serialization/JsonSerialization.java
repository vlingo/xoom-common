// Copyright © 2012-2021 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.common.serialization;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.*;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class JsonSerialization {
    public final static Pattern PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");

    public static void setEngine(JsonSerializationEngine engine) {
        JsonSerialization.engine = engine;
    }

    private static JsonSerializationEngine engine = new GsonJsonSerialization();

    public static <T> T deserialized(String serialization, final Class<T> type) {
        return engine.deserialized(serialization, type);
    }

    public static <T> T deserialized(String serialization, final Type type) {
        return engine.deserialized(serialization, type);
    }

    public static <T> List<T> deserializedList(String serialization, final Type listOfType) {
        return engine.deserializedList(serialization, listOfType);
    }

    public static String serialized(final Object instance) {
        return engine.serialized(instance);
    }

    public static <T> String serialized(final Collection<T> instance) {
        return engine.serialized(instance);
    }

    public static <T> String serialized(final List<T> instance) {
        return engine.serialized(instance);
    }

}
