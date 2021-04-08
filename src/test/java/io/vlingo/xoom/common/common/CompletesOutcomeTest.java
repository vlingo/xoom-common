package io.vlingo.xoom.common;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CompletesOutcomeTest {

  @Test
  public void testWithTheSameTypeT() {
    CompletesOutcomeT<Exception, Integer> out0 =
        CompletesOutcomeT.of(addsOne(1));
    CompletesOutcomeT<Exception, Integer> out1 = out0.andThenTo(i ->
        CompletesOutcomeT.of(addsTwo(i)));
    out1.value().andFinallyConsume(o ->
        assertEquals(4L, (long) o.getOrNull()));
  }

  private Completes<Outcome<Exception, Integer>> addsOne(Integer x) {
    return Completes.withSuccess(Success.of(x + 1));
  }

  private Completes<Outcome<Exception, Integer>> addsTwo(Integer x) {
    return Completes.withSuccess(Success.of(x + 2));
  }

  @Test
  public void testWithDifferentTypeT() {
    CompletesOutcomeT<Exception, String> out0 =
        CompletesOutcomeT.of(toString(1));
    CompletesOutcomeT<Exception, Float> out1 = out0.andThenTo(s ->
        CompletesOutcomeT.of(toFloat(s)));
    out1.value().andFinallyConsume(o ->
        assertEquals(1F, (float) o.getOrNull(), 0));
  }

  private Completes<Outcome<Exception, String>> toString(Integer i) {
    return Completes.withSuccess(Success.of(String.valueOf(i)));
  }

  private Completes<Outcome<Exception, Float>> toFloat(String s) {
    return Completes.withSuccess(Success.of(Float.parseFloat(s)));
  }

  @Test
  public void testFailure() {
    CompletesOutcomeT.of(divZero(42)).value().andFinallyConsume(o ->
        assertTrue(o instanceof Failure));
  }

  private Completes<Outcome<ArithmeticException, Float>> divZero(float x) {
    final Outcome<ArithmeticException, Float> outcome;
    final float value = x / 0;
    if (value == Float.POSITIVE_INFINITY) {
      outcome = Failure.of(new ArithmeticException("division by zero"));
    } else {
      outcome = Success.of(value);
    }
    return Completes.withSuccess(outcome);
  }
}
