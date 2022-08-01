package io.vlingo.xoom.common.serialization;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.*;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static io.vlingo.xoom.common.serialization.JsonSerialization.PATTERN;

class JacksonJsonSerialization implements JsonSerializationEngine {

    private final ObjectMapper mapper;

    {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Date.class, new JacksonJsonSerialization.DateJacksonSerializer());
        module.addDeserializer(Date.class, new JacksonJsonSerialization.DateJacksonDeserializer());
        module.addSerializer(LocalDate.class, new JacksonJsonSerialization.LocalDateJacksonSerializer());
        module.addDeserializer(LocalDate.class, new JacksonJsonSerialization.LocalDateJacksonDeserializer());
        module.addSerializer(LocalDateTime.class, new JacksonJsonSerialization.LocalDateTimeJacksonSerializer());
        module.addDeserializer(LocalDateTime.class, new JacksonJsonSerialization.LocalDateTimeJacksonDeserializer());
        module.addSerializer(OffsetDateTime.class, new JacksonJsonSerialization.OffsetDateTimeJacksonSerializer());
        module.addDeserializer(OffsetDateTime.class, new JacksonJsonSerialization.OffsetDateTimeJacksonDeserializer());

        mapper = JsonMapper.builder()
                .addModule(module)
                .addModule(new Jdk8Module())
                .build();

        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }


    @Override
    public <T> T deserialized(String serialization, Class<T> type) {
        T instance = null;
        try {
            instance = mapper.readValue(serialization, type);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return instance;
    }

    @Override
    public <T> T deserialized(String serialization, Type type) {
        T instance = null;
        try {
            instance = mapper.readValue(serialization, mapper.constructType(type));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return instance;
    }

    @Override
    public <T> List<T> deserializedList(String serialization, Type listOfType) {
        List<T> list = null;
        try {
            list = mapper.readValue(serialization, mapper.constructType(listOfType));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public String serialized(Object instance) {
        String serialization = null;
        try {
            serialization = mapper.writeValueAsString(instance);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return serialization;
    }

    @Override
    public <T> String serialized(Collection<T> instance) {
        String serialization = null;
        try {
            serialization = mapper.writeValueAsString(instance);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return serialization;
    }

    @Override
    public <T> String serialized(List<T> instance) {
        String serialization = null;
        try {
            serialization = mapper.writeValueAsString(instance);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return serialization;
    }

    private static class DateJacksonSerializer extends StdSerializer<Date> {

        public DateJacksonSerializer() {
            this(null);
        }

        public DateJacksonSerializer(Class<Date> t) {
            super(t);
        }

        @Override
        public void serialize(
                Date value, JsonGenerator jgen, SerializerProvider provider)
                throws IOException {
            jgen.writeString(Long.toString(value.getTime()));
        }
    }

    private static class DateJacksonDeserializer extends StdDeserializer<Date> {

        public DateJacksonDeserializer() {
            this(null);
        }

        public DateJacksonDeserializer(Class<Date> t) {
            super(t);
        }

        @Override
        public Date deserialize(com.fasterxml.jackson.core.JsonParser p, DeserializationContext ctxt) throws IOException {
            long time = Long.parseLong(p.getValueAsString());
            return new Date(time);
        }
    }

    private static class LocalDateJacksonSerializer extends StdSerializer<LocalDate> {

        public LocalDateJacksonSerializer() {
            this(null);
        }

        public LocalDateJacksonSerializer(Class<LocalDate> t) {
            super(t);
        }

        @Override
        public void serialize(
                LocalDate value, JsonGenerator jgen, SerializerProvider provider)
                throws IOException {
            jgen.writeString(Long.toString(value.toEpochDay()));
        }
    }

    private static class LocalDateJacksonDeserializer extends StdDeserializer<LocalDate> {

        public LocalDateJacksonDeserializer() {
            this(null);
        }

        public LocalDateJacksonDeserializer(Class<LocalDate> t) {
            super(t);
        }

        @Override
        public LocalDate deserialize(com.fasterxml.jackson.core.JsonParser p, DeserializationContext ctxt) throws IOException {

            if (isNumericString(p.getValueAsString())) {
                final long epochDay = Long.parseLong(p.getValueAsString());
                return LocalDate.ofEpochDay(epochDay);
            }

            return LocalDate.parse(p.getValueAsString());

        }
    }

    private static class LocalDateTimeJacksonSerializer extends StdSerializer<LocalDateTime> {

        public LocalDateTimeJacksonSerializer() {
            this(null);
        }

        public LocalDateTimeJacksonSerializer(Class<LocalDateTime> t) {
            super(t);
        }

        @Override
        public void serialize(
                LocalDateTime value, JsonGenerator jgen, SerializerProvider provider)
                throws IOException {
            jgen.writeString(Long.toString(value.atZone(ZoneOffset.UTC).toInstant().toEpochMilli()));
        }
    }

    private static class LocalDateTimeJacksonDeserializer extends StdDeserializer<LocalDateTime> {

        public LocalDateTimeJacksonDeserializer() {
            this(null);
        }

        public LocalDateTimeJacksonDeserializer(Class<LocalDateTime> t) {
            super(t);
        }

        @Override
        public LocalDateTime deserialize(com.fasterxml.jackson.core.JsonParser p, DeserializationContext ctxt) throws IOException {

            if (isNumericString(p.getValueAsString())) {
                final long milli = Long.parseLong(p.getValueAsString());
                return LocalDateTime.ofInstant(Instant.ofEpochMilli(milli), ZoneOffset.UTC);
            }

            return LocalDateTime.parse(p.getValueAsString());
        }
    }

    private static class OffsetDateTimeJacksonSerializer extends StdSerializer<OffsetDateTime> {

        public OffsetDateTimeJacksonSerializer() {
            this(null);
        }

        public OffsetDateTimeJacksonSerializer(Class<OffsetDateTime> t) {
            super(t);
        }

        @Override
        public void serialize(
                OffsetDateTime value, JsonGenerator jgen, SerializerProvider provider)
                throws IOException {
            jgen.writeString(value.toInstant().toEpochMilli() + ";" + value.getOffset().toString());
        }
    }

    private static class OffsetDateTimeJacksonDeserializer extends StdDeserializer<OffsetDateTime> {

        public OffsetDateTimeJacksonDeserializer() {
            this(null);
        }

        public OffsetDateTimeJacksonDeserializer(Class<OffsetDateTime> t) {
            super(t);
        }

        @Override
        public OffsetDateTime deserialize(com.fasterxml.jackson.core.JsonParser p, DeserializationContext ctxt) throws IOException {
            final String[] encoding = p.getValueAsString().split(";");
            final Date date = new Date(Long.parseLong(encoding[0]));
            return date.toInstant().atOffset(ZoneOffset.of(encoding[1]));
        }
    }

    private static boolean isNumericString(String element) {
        return PATTERN.matcher(element).matches();
    }
}
