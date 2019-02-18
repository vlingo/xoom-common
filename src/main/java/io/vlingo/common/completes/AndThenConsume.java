package io.vlingo.common.completes;

import io.vlingo.common.Scheduler;
import io.vlingo.common.completes.barrier.TimeBarrier;

import java.util.function.Consumer;

public class AndThenConsume<I, NO> implements Operation<I, I, NO> {
    private final TimeBarrier timeBarrier;
    private final Consumer<I> consumer;
    private Operation<I, NO, ?> nextOperation;

    public AndThenConsume(Scheduler scheduler, long timeout, Consumer<I> consumer) {
        this.timeBarrier = new TimeBarrier(scheduler, timeout);
        this.consumer = consumer;
    }

    @Override
    public void onOutcome(I outcome) {
        this.timeBarrier.initialize();
        this.timeBarrier.execute(() -> {
            try {
                consumer.accept(outcome);
                nextOperation.onOutcome(outcome);
            } catch (Throwable ex) {
                nextOperation.onError(ex);
            }
        }, nextOperation);
    }

    @Override
    public void onFailure(I outcome) {
        nextOperation.onFailure(outcome);
    }

    @Override
    public void onError(Throwable ex) {
        nextOperation.onError(ex);
    }

    @Override
    public <N2O> void addSubscriber(Operation<I, NO, N2O> operation) {
        this.nextOperation = operation;
    }
}
