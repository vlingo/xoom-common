package io.vlingo.common.completes.exceptions;

public class AlreadySubscribedSinkException extends RuntimeException {
    public AlreadySubscribedSinkException() {
        super("Sinks can only receive one subscriber. Please check your code for multiple subscriptions to a chain.");
    }
}
