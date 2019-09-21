package io.vlingo.common.completes.test;

import io.vlingo.common.completes.Sink;
import io.vlingo.common.completes.Source;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TestSource<Expose> implements Source<Expose> {
    private final List<Consumer<Sink<Expose>>> operations;
    private boolean waitingForSubscription;
    private Sink<Expose> subscriber;

    public TestSource() {
        this.operations = new ArrayList<>();
        this.waitingForSubscription = false;
    }

    public void flush() {
        if (subscriber == null) {
            this.waitingForSubscription = true;
        } else {
            operations.forEach(c -> c.accept(subscriber));
            operations.clear();
            this.waitingForSubscription = false;
        }
    }

    @Override
    public void emitOutcome(Expose outcome) {
        operations.add(s -> s.onOutcome(outcome));
    }

    @Override
    public void emitError(Exception cause) {
        operations.add(s -> s.onError(cause));
    }

    @Override
    public void emitCompletion() {
        operations.add(Sink::onCompletion);
    }

    @Override
    public void subscribe(Sink<Expose> subscriber) {
        this.subscriber = subscriber;
        if (waitingForSubscription) {
            flush();
        }
    }
}
