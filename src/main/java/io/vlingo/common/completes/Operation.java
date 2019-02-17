package io.vlingo.common.completes;

public interface Operation<I, O, NO> {
    void onOutcome(I outcome);
    void onFailure(I outcome);
    void onError(Throwable ex);
    <N2O> void addSubscriber(Operation<O, NO, N2O> operation);
}
