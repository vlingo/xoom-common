package io.vlingo.common.completes.exceptions;

public class FailedOperationException extends Exception {
    public final Object failureValue;

    public FailedOperationException(Object failureValue) {
        this.failureValue = failureValue;
    }
}
