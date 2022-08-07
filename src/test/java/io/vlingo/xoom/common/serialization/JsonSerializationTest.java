package io.vlingo.xoom.common.serialization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.gson.reflect.TypeToken;
import io.vlingo.xoom.common.serialization.gson.GsonJsonSerialization;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

abstract public class JsonSerializationTest {

  private static final TimeZone defaultTimeZone = TimeZone.getDefault();

  @Test
  public void itSerializesPlainText() {
    assertJson("\"plain text\"", JsonSerialization.serialized("plain text"));
  }

  @Test
  public void itDeserializesPlainTextByClass() {
    assertEquals("plain text (class)", JsonSerialization.deserialized("\"plain text (class)\"", String.class));
  }

  @Test
  public void itDeserializesPlainTextByType() {
    assertEquals("plain text (type)", JsonSerialization.deserialized("\"plain text (type)\"", (Type) String.class));
  }

  @Test
  public void itSerializesNumbers() {
    assertJson("42", JsonSerialization.serialized(42));
  }

  @Test
  public void itDeserializesNumbersByClass() {
    assertEquals(13, (int) JsonSerialization.deserialized("13", Integer.class));
  }

  @Test
  public void itDeserializesNumbersByType() {
    assertEquals(13, (int) JsonSerialization.deserialized("13", (Type) Integer.class));
  }

  @Test
  public void itDeserializesPreviouslySerializedObject() {
    final TestSerializationSubject expected = new TestSerializationSubject(new TestSerializationChild("Alice"), "lorem ipsum", 13);
    final TestSerializationSubject deserialized = JsonSerialization.deserialized(JsonSerialization.serialized(expected), TestSerializationSubject.class);
    assertEquals(expected, deserialized);
  }

  @Test
  public void itSerializesObjects() {
    final String serialized = JsonSerialization.serialized(new TestSerializationSubject(new TestSerializationChild("Alice"), "lorem ipsum", 13));
    final String expected = "{\"child\":{\"name\":\"Alice\"},\"text\":\"lorem ipsum\",\"number\":13}";
    assertJson(expected, serialized);
  }

  @Test
  public void itDeserializesObjectsByClass() {
    final TestSerializationSubject deserialized = JsonSerialization.deserialized("{\"child\":{\"name\":\"Bob\"},\"text\":\"lorem ipsum\",\"number\":42}", TestSerializationSubject.class);
    final TestSerializationSubject expected = new TestSerializationSubject(new TestSerializationChild("Bob"), "lorem ipsum", 42);
    assertEquals(expected, deserialized);
  }

  @Test
  public void itDeserializesObjectsByType() {
    final TestSerializationSubject expected = new TestSerializationSubject(new TestSerializationChild("Bob"), "lorem ipsum", 42);
    final TestSerializationSubject deserialized = JsonSerialization.deserialized("{\"child\":{\"name\":\"Bob\"},\"text\":\"lorem ipsum\",\"number\":42}", (Type) TestSerializationSubject.class);
    assertEquals(expected, deserialized);
  }

  @Test
  public void itSerializesCollections() {
    final Collection<TestSerializationSubject> collection = Collections.singletonList(new TestSerializationSubject(new TestSerializationChild("Bob"), "lorem ipsum", 42));
    final String serialized = JsonSerialization.serialized(collection);
    assertJson("[{\"child\":{\"name\":\"Bob\"},\"text\":\"lorem ipsum\",\"number\":42}]", serialized);
  }

  @Test
  public void itSerializesLists() {
    final List<TestSerializationSubject> list = Collections.singletonList(new TestSerializationSubject(new TestSerializationChild("Bob"), "lorem ipsum", 42));
    final String serialized = JsonSerialization.serialized(list);
    assertJson("[{\"child\":{\"name\":\"Bob\"},\"text\":\"lorem ipsum\",\"number\":42}]", serialized);
  }

  @Test
  public void itDeserializesLists() {
    final List<TestSerializationSubject> expected = Collections.singletonList(new TestSerializationSubject(new TestSerializationChild("Bob"), "lorem ipsum", 42));
    final List<TestSerializationSubject> deserialized = JsonSerialization.deserializedList("[{\"child\":{\"name\":\"Bob\"},\"text\":\"lorem ipsum\",\"number\":42}]", new TypeToken<List<TestSerializationSubject>>() {
    }.getType());
    assertEquals(expected, deserialized);
  }

  @Test
  public void itSerializesClass() {
    final Class<TestSerializationSubject> clazz = TestSerializationSubject.class;
    final String serialized = JsonSerialization.serialized(clazz);
    final String expected = "\"io.vlingo.xoom.common.serialization.JsonSerializationTest$TestSerializationSubject\"";
    assertJson(expected, serialized);
  }

  @Test
  public void itDeserializesClass() {
    final String serialized = "\"io.vlingo.xoom.common.serialization.JsonSerializationTest$TestSerializationSubject\"";
    final Class<?> deserialized = JsonSerialization.deserialized(serialized, Class.class);
    assertEquals(TestSerializationSubject.class, deserialized);
  }

  @Test
  public void itThrowsIfClassIsNotFoundWhileDeserializing() {
    assertThrows(RuntimeException.class, () -> {
      final String serialized = "\"io.vlingo.xoom.common.serialization.JsonSerializationTest$MissingClass\"";
      JsonSerialization.deserialized(serialized, Class.class);
    });
  }

  @SuppressWarnings("deprecation")
  @Test
  public void itSerializesDate() {
    final String expected = "\"369100800000\"";
    final String serialized = JsonSerialization.serialized(new Date(81, Calendar.SEPTEMBER, 12));
    assertJson(expected, serialized);
  }

  @SuppressWarnings("deprecation")
  @Test
  public void itDeserializesDate() {
    final Date expected = new Date(81, Calendar.SEPTEMBER, 12);
    final Date deserialized = JsonSerialization.deserialized("\"369100800000\"", Date.class);
    assertEquals(expected, deserialized);
  }

  @Test
  public void itSerializesLocalDate() {
    final String expected = "\"4241\"";
    final String serialized = JsonSerialization.serialized(LocalDate.of(1981, 8, 12));
    assertJson(expected, serialized);
  }

  @Test
  public void itDeserializesLocalDate() {
    final LocalDate expected = LocalDate.of(1981, 8, 12);
    final LocalDate deserialized = JsonSerialization.deserialized("\"4241\"", LocalDate.class);
    assertEquals(expected, deserialized);
  }

  @Test
  public void itDeserializesLocalDateFromFormattedString() {
    final LocalDate expected = LocalDate.of(1981, 8, 12);
    final LocalDate deserialized = JsonSerialization.deserialized("\"1981-08-12\"", LocalDate.class);
    assertEquals(expected, deserialized);
  }

  @Test
  public void itSerializesLocalDateTime() {
    final String expected = "\"366454512000\"";
    final String serialized = JsonSerialization.serialized(LocalDateTime.of(1981, 8, 12, 8, 55, 12));
    assertJson(expected, serialized);
  }

  @Test
  public void itDeserializesLocalDateTime() {
    final LocalDateTime expected = LocalDateTime.of(1981, 8, 12, 8, 55, 12);
    final LocalDateTime deserialized = JsonSerialization.deserialized("\"366454512000\"", LocalDateTime.class);
    assertEquals(expected, deserialized);
  }

  @Test
  public void itDeserializesLocalDateTimeFromFormattedString() {
    final LocalDateTime expected = LocalDateTime.of(1981, 8, 12, 8, 55, 12);
    final LocalDateTime deserialized = JsonSerialization.deserialized("\"1981-08-12T08:55:12\"", LocalDateTime.class);
    assertEquals(expected, deserialized);
  }

  @Test
  public void itSerializesOffsetDateTime() {
    final String expected = "\"366429312000;+07:00\"";
    final String serialized = JsonSerialization.serialized(OffsetDateTime.of(LocalDateTime.of(1981, 8, 12, 8, 55, 12), ZoneOffset.ofHours(7)));
    assertJson(expected, serialized);
  }

  @Test
  public void itDeserializesOffsetDateTime() {
    final OffsetDateTime expected = OffsetDateTime.of(LocalDateTime.of(1981, 8, 12, 8, 55, 12), ZoneOffset.ofHours(7));
    final OffsetDateTime deserialized = JsonSerialization.deserialized("\"366429312000;+07:00\"", OffsetDateTime.class);
    assertEquals(expected, deserialized);
  }

  @Test
  public void itSerializesByIgnoringGetters() {
    final String serialized = JsonSerialization.serialized(new ClassWithGetters());
    assertJson("{\"property\":\"value\"}", serialized);
  }

  @Test
  public void itIgnoresNullValues() {
    final String serialized = JsonSerialization.serialized(Collections.singletonMap("key", null));
    assertJson("{}", serialized);
  }

  abstract protected JsonSerializationStrategy serializationStrategy();

  private JsonSerializationStrategy defaultStrategy() {
    return new GsonJsonSerialization();
  }

  @Before
  public void configureStrategy() {
    JsonSerialization.use(serializationStrategy());
  }

  @After
  public void restoreStrategy() {
    JsonSerialization.use(defaultStrategy());
  }

  @Before
  public void configureTimeZone() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @After
  public void restoreTimeZone() {
    TimeZone.setDefault(defaultTimeZone);
  }

  private void assertJson(final String expected, final String actual) {
    try {
      final String message = String.format("Expected: `%s`, but got: `%s`.", expected, actual);
      JSONAssert.assertEquals(message, expected, actual, JSONCompareMode.STRICT);
    } catch (JSONException e) {
      throw new AssertionError(e);
    }
  }

  static class TestSerializationSubject {
    private final TestSerializationChild child;
    private final String text;
    private final int number;

    public TestSerializationSubject(final TestSerializationChild child, final String text, final int number) {
      this.child = child;
      this.text = text;
      this.number = number;
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      final TestSerializationSubject that = (TestSerializationSubject) o;
      return number == that.number && child.equals(that.child) && text.equals(that.text);
    }

    @Override
    public int hashCode() {
      return Objects.hash(child, text, number);
    }
  }

  static class TestSerializationChild {
    private final String name;

    // Unfortunately, single argument constructors are not auto-recognised by jackson
    // @see https://github.com/FasterXML/jackson-databind/issues/1498
    // @see https://github.com/FasterXML/jackson-module-parameter-names/issues/21#issuecomment-110994068
    @JsonCreator
    public TestSerializationChild(final String name) {
      this.name = name;
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      final TestSerializationChild that = (TestSerializationChild) o;
      return name.equals(that.name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name);
    }
  }

  static class ClassWithGetters {
    public String property = "value";

    public String getIgnored() {
      return "ignored";
    }

    public boolean isIgnored() {
      return false;
    }
  }
}
