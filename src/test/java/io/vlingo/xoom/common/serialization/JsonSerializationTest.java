package io.vlingo.xoom.common.serialization;

import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
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

public class JsonSerializationTest {

  private static TimeZone defaultTimeZone = TimeZone.getDefault();

  @Test
  public void testItSerializesPlainText() {
    assertJson("\"plain text\"", JsonSerialization.serialized("plain text"));
  }

  @Test
  public void testItDeserializesPlainTextByClass() {
    assertEquals("plain text (class)", JsonSerialization.deserialized("\"plain text (class)\"", String.class));
  }

  @Test
  public void testItDeserializesPlainTextByType() {
    assertEquals("plain text (type)", JsonSerialization.deserialized("\"plain text (type)\"", (Type) String.class));
  }

  @Test
  public void testItSerializesNumbers() {
    assertJson("42", JsonSerialization.serialized(42));
  }

  @Test
  public void testItDeserializesNumbersByClass() {
    assertEquals(13, (int) JsonSerialization.deserialized("13", Integer.class));
  }

  @Test
  public void testItDeserializesNumbersByType() {
    assertEquals(13, (int) JsonSerialization.deserialized("13", (Type) Integer.class));
  }

  @Test
  public void testItDeserializesPreviouslySerializedObject() {
    final TestSerializationSubject expected = new TestSerializationSubject(new TestSerializationChild("Alice"), "lorem ipsum", 13);
    final TestSerializationSubject deserialized = JsonSerialization.deserialized(JsonSerialization.serialized(expected), TestSerializationSubject.class);
    assertEquals(expected, deserialized);
  }

  @Test
  public void testItSerializesObjects() {
    final String serialized = JsonSerialization.serialized(new TestSerializationSubject(new TestSerializationChild("Alice"), "lorem ipsum", 13));
    final String expected = "{\"child\":{\"name\":\"Alice\"},\"text\":\"lorem ipsum\",\"number\":13}";
    assertJson(expected, serialized);
  }

  @Test
  public void testItDeserializesObjectsByClass() {
    final TestSerializationSubject deserialized = JsonSerialization.deserialized("{\"child\":{\"name\":\"Bob\"},\"text\":\"lorem ipsum\",\"number\":42}", TestSerializationSubject.class);
    final TestSerializationSubject expected = new TestSerializationSubject(new TestSerializationChild("Bob"), "lorem ipsum", 42);
    assertEquals(expected, deserialized);
  }

  @Test
  public void testItDeserializesObjectsByType() {
    final TestSerializationSubject expected = new TestSerializationSubject(new TestSerializationChild("Bob"), "lorem ipsum", 42);
    final TestSerializationSubject deserialized = JsonSerialization.deserialized("{\"child\":{\"name\":\"Bob\"},\"text\":\"lorem ipsum\",\"number\":42}", (Type) TestSerializationSubject.class);
    assertEquals(expected, deserialized);
  }

  @Test
  public void testItSerializesCollections() {
    final Collection<TestSerializationSubject> collection = Collections.singletonList(new TestSerializationSubject(new TestSerializationChild("Bob"), "lorem ipsum", 42));
    final String serialized = JsonSerialization.serialized(collection);
    assertJson("[{\"child\":{\"name\":\"Bob\"},\"text\":\"lorem ipsum\",\"number\":42}]", serialized);
  }

  @Test
  public void testItSerializesLists() {
    final List<TestSerializationSubject> list = Collections.singletonList(new TestSerializationSubject(new TestSerializationChild("Bob"), "lorem ipsum", 42));
    final String serialized = JsonSerialization.serialized(list);
    assertJson("[{\"child\":{\"name\":\"Bob\"},\"text\":\"lorem ipsum\",\"number\":42}]", serialized);
  }

  @Test
  public void testItDeserializesLists() {
    final List<TestSerializationSubject> expected = Collections.singletonList(new TestSerializationSubject(new TestSerializationChild("Bob"), "lorem ipsum", 42));
    final List<TestSerializationSubject> deserialized = JsonSerialization.deserializedList("[{\"child\":{\"name\":\"Bob\"},\"text\":\"lorem ipsum\",\"number\":42}]", new TypeToken<List<TestSerializationSubject>>() {
    }.getType());
    assertEquals(expected, deserialized);
  }

  @Test
  public void testItSerializesClass() {
    final Class<TestSerializationSubject> clazz = TestSerializationSubject.class;
    final String serialized = JsonSerialization.serialized(clazz);
    final String expected = "\"io.vlingo.xoom.common.serialization.JsonSerializationTest$TestSerializationSubject\"";
    assertJson(expected, serialized);
  }

  @Test
  public void testItDeserializesClass() {
    final String serialized = "\"io.vlingo.xoom.common.serialization.JsonSerializationTest$TestSerializationSubject\"";
    final Class<?> deserialized = JsonSerialization.deserialized(serialized, Class.class);
    assertEquals(TestSerializationSubject.class, deserialized);
  }

  @Test
  public void testItThrowsIfClassIsNotFoundWhileDeserializing() {
    assertThrows(JsonParseException.class, () -> {
      final String serialized = "\"io.vlingo.xoom.common.serialization.JsonSerializationTest$MissingClass\"";
      JsonSerialization.deserialized(serialized, Class.class);
    });
  }

  @Test
  public void testItSerializesDate() {
    final String expected = "\"369100800000\"";
    final String serialized = JsonSerialization.serialized(new Date(81, 8, 12));
    assertJson(expected, serialized);
  }

  @Test
  public void testItDeserializesDate() {
    final Date expected = new Date(81, 8, 12);
    final Date deserialized = JsonSerialization.deserialized("\"369100800000\"", Date.class);
    assertEquals(expected, deserialized);
  }

  @Test
  public void testItSerializesLocalDate() {
    final String expected = "\"4241\"";
    final String serialized = JsonSerialization.serialized(LocalDate.of(1981, 8, 12));
    assertJson(expected, serialized);
  }

  @Test
  public void testItDeserializesLocalDate() {
    final LocalDate expected = LocalDate.of(1981, 8, 12);
    final LocalDate deserialized = JsonSerialization.deserialized("\"4241\"", LocalDate.class);
    assertEquals(expected, deserialized);
  }

  @Test
  public void testItDeserializesLocalDateFromFormattedString() {
    final LocalDate expected = LocalDate.of(1981, 8, 12);
    final LocalDate deserialized = JsonSerialization.deserialized("\"1981-08-12\"", LocalDate.class);
    assertEquals(expected, deserialized);
  }

  @Test
  public void testItSerializesLocalDateTime() {
    final String expected = "\"366454512000\"";
    final String serialized = JsonSerialization.serialized(LocalDateTime.of(1981, 8, 12, 8, 55, 12));
    assertJson(expected, serialized);
  }

  @Test
  public void testItDeserializesLocalDateTime() {
    final LocalDateTime expected = LocalDateTime.of(1981, 8, 12, 8, 55, 12);
    final LocalDateTime deserialized = JsonSerialization.deserialized("\"366454512000\"", LocalDateTime.class);
    assertEquals(expected, deserialized);
  }

  @Test
  public void testItDeserializesLocalDateTimeFromFormattedString() {
    final LocalDateTime expected = LocalDateTime.of(1981, 8, 12, 8, 55, 12);
    final LocalDateTime deserialized = JsonSerialization.deserialized("\"1981-08-12T08:55:12\"", LocalDateTime.class);
    assertEquals(expected, deserialized);
  }

  @Test
  public void testItSerializesOffsetDateTime() {
    final String expected = "\"366429312000;+07:00\"";
    final String serialized = JsonSerialization.serialized(OffsetDateTime.of(LocalDateTime.of(1981, 8, 12, 8, 55, 12), ZoneOffset.ofHours(7)));
    assertJson(expected, serialized);
  }

  @Test
  public void testItDeserializesOffsetDateTime() {
    final OffsetDateTime expected = OffsetDateTime.of(LocalDateTime.of(1981, 8, 12, 8, 55, 12), ZoneOffset.ofHours(7));
    final OffsetDateTime deserialized = JsonSerialization.deserialized("\"366429312000;+07:00\"", OffsetDateTime.class);
    assertEquals(expected, deserialized);
  }

  @Before
  public void setUp() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @After
  public void tearDown() {
    TimeZone.setDefault(defaultTimeZone);
  }

  private void assertJson(final String expected, final String actual) {
    try {
      JSONAssert.assertEquals(expected, actual, JSONCompareMode.STRICT);
    } catch (JSONException e) {
      throw new AssertionError(e);
    }
  }

  class TestSerializationSubject {
    private TestSerializationChild child;
    private String text;
    private int number;

    public TestSerializationSubject(final TestSerializationChild child, final String text, final int number) {
      this.child = child;
      this.text = text;
      this.number = number;
    }

    private TestSerializationSubject() {
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

  class TestSerializationChild {
    private String name;

    public TestSerializationChild(final String name) {
      this.name = name;
    }

    private TestSerializationChild() {
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
}
