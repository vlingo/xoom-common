package io.vlingo.xoom.common.serialization;

import com.google.gson.reflect.TypeToken;
import org.junit.Test;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class JsonSerializationTest {

    @Test
    public void canSerializeAString() {
        assertEquals("\"some text\"", JsonSerialization.serialized("some text"));
    }

    @Test
    public void canDeserializeAString() {
        assertEquals("some text", JsonSerialization.deserialized("\"some text\"", String.class));
        assertEquals("some text", JsonSerialization.deserialized("\"some text\"", (Type) String.class));
    }

    @Test
    public void canSerializeAnInt() {
        assertEquals("5", JsonSerialization.serialized(5));
    }

    @Test
    public void canDeserializeAnInt() {
        assertEquals(5, (int) JsonSerialization.deserialized("5", Integer.TYPE));
        assertEquals(5, (int) JsonSerialization.deserialized("5", (Type) Integer.TYPE));
    }

    @Test
    public void canSerializeTestObject() {
        assertEquals("{\"myString\":\"the string\",\"myInt\":6}", JsonSerialization.serialized(new TestObject("the string", 6)));
    }

    @Test
    public void canDeserializeTestObject() {
        assertEquals(new TestObject("the string", 6), JsonSerialization.deserialized("{\"myString\":\"the string\",\"myInt\":6}", TestObject.class));
        assertEquals(new TestObject("the string", 6), JsonSerialization.deserialized("{\"myString\":\"the string\",\"myInt\":6}", (Type) TestObject.class));
    }

    @Test
    public void canSerializeAClass() {
        assertEquals("\"io.vlingo.xoom.common.serialization.JsonSerializationTest$TestObject\"", JsonSerialization.serialized(TestObject.class));
    }

    @Test
    public void canDeserializeAClass() {
        assertEquals(TestObject.class, JsonSerialization.deserialized("\"io.vlingo.xoom.common.serialization.JsonSerializationTest$TestObject\"", Class.class));
        assertEquals(TestObject.class, JsonSerialization.deserialized("\"io.vlingo.xoom.common.serialization.JsonSerializationTest$TestObject\"", (Type) Class.class));
    }

    @Test
    public void canSerializeADate() {
        assertEquals("\"61598790000000\"", JsonSerialization.serialized(new Date(2021, 11, 28)));
    }

    @Test
    public void canDeserializeADate() {
        assertEquals(new Date(2021, 11, 28), JsonSerialization.deserialized("\"61598790000000\"", Date.class));
        assertEquals(new Date(2021, 11, 28), JsonSerialization.deserialized("\"61598790000000\"", (Type) Date.class));
    }

    @Test
    public void canSerializeLocalDate() {
        assertEquals("\"18959\"", JsonSerialization.serialized(LocalDate.of(2021, 11, 28)));
    }

    @Test
    public void canDeserializeLocalDate() {
        assertEquals(LocalDate.of(2021, 11, 28), JsonSerialization.deserialized("\"18959\"", LocalDate.class));
        assertEquals(LocalDate.of(2021, 11, 28), JsonSerialization.deserialized("\"18959\"", (Type) LocalDate.class));
    }

    @Test
    public void canDeserializeLocalDateFromJson() {
        assertEquals(LocalDate.of(2021, 11, 28), JsonSerialization.deserialized("2021-11-28", LocalDate.class));
        assertEquals(LocalDate.of(2021, 11, 28), JsonSerialization.deserialized("2021-11-28", (Type) LocalDate.class));
    }

    @Test
    public void canSerializeLocalDateTime() {
        assertEquals("\"1638102780000\"", JsonSerialization.serialized(LocalDateTime.of(2021, 11, 28, 12, 33)));
    }

    @Test
    public void canDeserializeLocalDateTime() {
        assertEquals(LocalDateTime.of(2021, 11, 28, 12, 33), JsonSerialization.deserialized("\"1638102780000\"", LocalDateTime.class));
        assertEquals(LocalDateTime.of(2021, 11, 28, 12, 33), JsonSerialization.deserialized("\"1638102780000\"", (Type) LocalDateTime.class));
    }

    @Test
    public void canDeserializeLocalDateTimeFromJson() {
        assertEquals(LocalDateTime.of(2021, 11, 28, 12, 33, 5), JsonSerialization.deserialized("\"2021-11-28T12:33:05\"", LocalDateTime.class));
        assertEquals(LocalDateTime.of(2021, 11, 28, 12, 33, 5), JsonSerialization.deserialized("\"2021-11-28T12:33:05\"", (Type) LocalDateTime.class));
    }

    @Test
    public void canSerializeOffsetDateTime() {
        assertEquals("\"1638095585000;+02:00\"", JsonSerialization.serialized(OffsetDateTime.of(2021, 11, 28, 12, 33, 05, 30, ZoneOffset.of("+02:00"))));
    }

    @Test
    public void canDeserializeOffsetDateTime() {
        assertEquals(OffsetDateTime.of(2021, 11, 28, 12, 33, 05, 00, ZoneOffset.of("+02:00")), JsonSerialization.deserialized("\"1638095585000;+02:00\"", OffsetDateTime.class));
        assertEquals(OffsetDateTime.of(2021, 11, 28, 12, 33, 05, 00, ZoneOffset.of("+02:00")), JsonSerialization.deserialized("\"1638095585000;+02:00\"", (Type) OffsetDateTime.class));
    }

    @Test
    public void canSerializeAList() {
        List<TestObject> list = new ArrayList<>();
        list.add(new TestObject("text1", 1));
        list.add(new TestObject("text2", 2));
        list.add(new TestObject("text3", 3));
        assertEquals("[{\"myString\":\"text1\",\"myInt\":1},{\"myString\":\"text2\",\"myInt\":2},{\"myString\":\"text3\",\"myInt\":3}]", JsonSerialization.serialized(list));
    }

    @Test
    public void canSerializeACollection() {
        Collection<TestObject> list = new ArrayList<>();
        list.add(new TestObject("text1", 1));
        list.add(new TestObject("text2", 2));
        list.add(new TestObject("text3", 3));
        assertEquals("[{\"myString\":\"text1\",\"myInt\":1},{\"myString\":\"text2\",\"myInt\":2},{\"myString\":\"text3\",\"myInt\":3}]", JsonSerialization.serialized(list));
    }

    @Test
    public void canDeserializeAList() {
        Collection<TestObject> expectedList = new ArrayList<>();
        expectedList.add(new TestObject("text1", 1));
        expectedList.add(new TestObject("text2", 2));
        expectedList.add(new TestObject("text3", 3));

        assertEquals(expectedList, JsonSerialization.deserializedList("[{\"myString\":\"text1\",\"myInt\":1},{\"myString\":\"text2\",\"myInt\":2},{\"myString\":\"text3\",\"myInt\":3}]", new TypeToken<ArrayList<TestObject>>() {
        }.getType()));
    }

    private static class TestObject {
        private String myString;
        private int myInt;

        public TestObject(String myString, int myInt) {
            this.myString = myString;
            this.myInt = myInt;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestObject that = (TestObject) o;
            return myInt == that.myInt && myString.equals(that.myString);
        }

        @Override
        public int hashCode() {
            return Objects.hash(myString, myInt);
        }
    }
}
