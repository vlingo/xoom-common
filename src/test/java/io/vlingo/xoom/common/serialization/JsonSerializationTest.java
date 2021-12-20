package io.vlingo.xoom.common.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.reflect.TypeToken;
import io.vlingo.xoom.common.Completes;
import io.vlingo.xoom.common.completes.FutureCompletes;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JsonSerializationTest {

    void executeJsonTest(Runnable assertFunction){
        JsonSerialization.setEngine(new JacksonJsonSerialization());
        assertFunction.run();
        JsonSerialization.setEngine(new GsonJsonSerialization());
        assertFunction.run();
    }

    @Test
    public void canSerializeAString() {
        executeJsonTest(() -> assertEquals("\"some text\"", JsonSerialization.serialized("some text")));
    }

    @Test
    public void canDeserializeAString() {
        executeJsonTest(() -> assertEquals("some text", JsonSerialization.deserialized("\"some text\"", String.class)));
        executeJsonTest(() -> assertEquals("some text", JsonSerialization.deserialized("\"some text\"", (Type) String.class)));
    }

    @Test
    public void canSerializeAnInt() {
        executeJsonTest(() -> assertEquals("5", JsonSerialization.serialized(5)));
    }

    @Test
    public void canDeserializeAnInt() {
        executeJsonTest(() -> assertEquals(5, (int) JsonSerialization.deserialized("5", Integer.TYPE)));
        executeJsonTest(() -> assertEquals(5, (int) JsonSerialization.deserialized("5", (Type) Integer.TYPE)));
    }

    @Test
    public void canSerializeTestObject() {
        executeJsonTest(() -> assertEquals("{\"myString\":\"the string\",\"myInt\":6}", JsonSerialization.serialized(new TestObject("the string", 6))));
    }

    @Test
    public void canDeserializeTestObject() {
        executeJsonTest(() -> assertEquals(new TestObject("the string", 6), JsonSerialization.deserialized("{\"myString\":\"the string\",\"myInt\":6}", TestObject.class)));
        executeJsonTest(() -> assertEquals(new TestObject("the string", 6), JsonSerialization.deserialized("{\"myString\":\"the string\",\"myInt\":6}", (Type) TestObject.class)));
    }

    @Test
    public void canSerializeAClass() {
        executeJsonTest(() -> assertEquals("\"io.vlingo.xoom.common.serialization.JsonSerializationTest$TestObject\"", JsonSerialization.serialized(TestObject.class)));
    }

    @Test
    public void canDeserializeAClass() {
        executeJsonTest(() -> assertEquals(TestObject.class, JsonSerialization.deserialized("\"io.vlingo.xoom.common.serialization.JsonSerializationTest$TestObject\"", Class.class)));
        executeJsonTest(() -> assertEquals(TestObject.class, JsonSerialization.deserialized("\"io.vlingo.xoom.common.serialization.JsonSerializationTest$TestObject\"", (Type) Class.class)));
    }

    @Test
    @Ignore
    public void canSerializeADate() {
        executeJsonTest(() -> assertEquals("\"61598790000000\"", JsonSerialization.serialized(new Date(2021, 11, 28))));
    }

    @Test
    @Ignore
    public void canDeserializeADate() {
        executeJsonTest(() -> assertEquals(new Date(2021, 11, 28), JsonSerialization.deserialized("\"61598790000000\"", Date.class)));
        executeJsonTest(() -> assertEquals(new Date(2021, 11, 28), JsonSerialization.deserialized("\"61598790000000\"", (Type) Date.class)));
    }

    @Test
    public void canSerializeLocalDate() {
        executeJsonTest(() -> assertEquals("\"18959\"", JsonSerialization.serialized(LocalDate.of(2021, 11, 28))));
    }

    @Test
    public void canDeserializeLocalDate() {
        executeJsonTest(() -> assertEquals(LocalDate.of(2021, 11, 28), JsonSerialization.deserialized("\"18959\"", LocalDate.class)));
        executeJsonTest(() -> assertEquals(LocalDate.of(2021, 11, 28), JsonSerialization.deserialized("\"18959\"", (Type) LocalDate.class)));
    }

    @Test
    public void canDeserializeLocalDateFromJson() {
        executeJsonTest(() -> assertEquals(LocalDate.of(2021, 11, 28), JsonSerialization.deserialized("\"2021-11-28\"", LocalDate.class)));
        executeJsonTest(() -> assertEquals(LocalDate.of(2021, 11, 28), JsonSerialization.deserialized("\"2021-11-28\"", (Type) LocalDate.class)));
    }

    @Test
    public void canSerializeLocalDateTime() {
        executeJsonTest(() -> assertEquals("\"1638102780000\"", JsonSerialization.serialized(LocalDateTime.of(2021, 11, 28, 12, 33))));
    }

    @Test
    public void canDeserializeLocalDateTime() {
        executeJsonTest(() -> assertEquals(LocalDateTime.of(2021, 11, 28, 12, 33), JsonSerialization.deserialized("\"1638102780000\"", LocalDateTime.class)));
        executeJsonTest(() -> assertEquals(LocalDateTime.of(2021, 11, 28, 12, 33), JsonSerialization.deserialized("\"1638102780000\"", (Type) LocalDateTime.class)));
    }

    @Test
    public void canDeserializeLocalDateTimeFromJson() {
        executeJsonTest(() -> assertEquals(LocalDateTime.of(2021, 11, 28, 12, 33, 5), JsonSerialization.deserialized("\"2021-11-28T12:33:05\"", LocalDateTime.class)));
        executeJsonTest(() -> assertEquals(LocalDateTime.of(2021, 11, 28, 12, 33, 5), JsonSerialization.deserialized("\"2021-11-28T12:33:05\"", (Type) LocalDateTime.class)));
    }

    @Test
    public void canSerializeOffsetDateTime() {
        executeJsonTest(() -> assertEquals("\"1638095585000;+02:00\"", JsonSerialization.serialized(OffsetDateTime.of(2021, 11, 28, 12, 33, 05, 30, ZoneOffset.of("+02:00")))));
    }

    @Test
    public void canDeserializeOffsetDateTime() {
        executeJsonTest(() -> assertEquals(OffsetDateTime.of(2021, 11, 28, 12, 33, 05, 00, ZoneOffset.of("+02:00")), JsonSerialization.deserialized("\"1638095585000;+02:00\"", OffsetDateTime.class)));
        executeJsonTest(() -> assertEquals(OffsetDateTime.of(2021, 11, 28, 12, 33, 05, 00, ZoneOffset.of("+02:00")), JsonSerialization.deserialized("\"1638095585000;+02:00\"", (Type) OffsetDateTime.class)));
    }

    @Test
    public void canSerializeAList() {
        List<TestObject> list = new ArrayList<>();
        list.add(new TestObject("text1", 1));
        list.add(new TestObject("text2", 2));
        list.add(new TestObject("text3", 3));
        executeJsonTest(() -> assertEquals("[{\"myString\":\"text1\",\"myInt\":1},{\"myString\":\"text2\",\"myInt\":2},{\"myString\":\"text3\",\"myInt\":3}]", JsonSerialization.serialized(list)));
    }

    @Test
    public void canSerializeACollection() {
        Collection<TestObject> list = new ArrayList<>();
        list.add(new TestObject("text1", 1));
        list.add(new TestObject("text2", 2));
        list.add(new TestObject("text3", 3));
        executeJsonTest(() -> assertEquals("[{\"myString\":\"text1\",\"myInt\":1},{\"myString\":\"text2\",\"myInt\":2},{\"myString\":\"text3\",\"myInt\":3}]", JsonSerialization.serialized(list)));

    }

    @Test
    public void canDeserializeAList() {
        Collection<TestObject> expectedList = new ArrayList<>();
        expectedList.add(new TestObject("text1", 1));
        expectedList.add(new TestObject("text2", 2));
        expectedList.add(new TestObject("text3", 3));

        executeJsonTest(() -> assertEquals(expectedList, JsonSerialization.deserializedList("[{\"myString\":\"text1\",\"myInt\":1},{\"myString\":\"text2\",\"myInt\":2},{\"myString\":\"text3\",\"myInt\":3}]", new TypeToken<ArrayList<TestObject>>() {
        }.getType())));
    }

    @Test
    @Ignore
    public void canSerializeCompletes()  {
        JsonSerialization.setEngine(new GsonJsonSerialization());
        //assertEquals("{\"state\":{\"futureFactory\":{},\"future\":{\"value\":{\"result\":\"Success\"}},\"failed\":false,\"failureValue\":{},\"handlesFailure\":false,\"id\":{\"id\":\"1\"},\"outcome\":{\"value\":{}},\"outcomeType\":\"Some\",\"timedOut\":false,\"repeats\":false}}", JsonSerialization.serialized(Completes.withSuccess("Success")));
        JsonSerialization.setEngine(new JacksonJsonSerialization());
        //assertEquals("{\"state\":{\"futureFactory\":{},\"future\":{\"done\":true,\"numberOfDependents\":0,\"cancelled\":false,\"completedExceptionally\":false},\"failed\":false,\"failureValue\":null,\"handlesFailure\":false,\"id\":{\"id\":\"2\"},\"outcome\":{\"outcome\":\"Success\",\"completed\":true},\"outcomeType\":\"Some\",\"timedOut\":false,\"repeats\":false},\"completed\":true}", JsonSerialization.serialized(Completes.withSuccess("Success")));
    }

    @Test
    @Ignore
    public void canSerializeOptionals() {
       // assertEquals("{\"myOpt\":\"test\",\"next\":null}", JsonSerialization.serialized(new MyOptional(Optional.of("test"), Optional.empty())));
       // assertEquals("{\"myOpt\":{\"value\":\"test\"},\"next\":{}}", JsonSerialization.serialized(new MyOptional(Optional.of("test"), Optional.empty())));

    }

    private static class TestObject {
        private String myString;
        private int myInt;

        public TestObject(String myString, int myInt) {
            this.myString = myString;
            this.myInt = myInt;
        }

        private TestObject() {}

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
