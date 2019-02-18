package io.vlingo.common.completes;

public interface Operation<Input, Output, NextOutput> {
    void onOutcome(Input outcome);
    void onFailure(Input outcome);
    void onError(Throwable ex);
    <LastOutput> void addSubscriber(Operation<Output, NextOutput, LastOutput> operation);
}
