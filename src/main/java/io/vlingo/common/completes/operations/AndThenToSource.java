package io.vlingo.common.completes.operations;

import io.vlingo.common.completes.Operation;
import io.vlingo.common.completes.Sink;
import io.vlingo.common.completes.Source;

import java.util.function.Function;

public class AndThenToSource<Receives, Exposes> extends Operation<Receives, Exposes> {
    private final Function<Receives, Source<Exposes>> mapper;

    public AndThenToSource(Function<Receives, Source<Exposes>> mapper) {
        this.mapper = mapper;
    }

    @Override
    public void onOutcome(Receives receives) {
        try {
            Source<Exposes> sourceToSubscribe = mapper.apply(receives);
            sourceToSubscribe.subscribe(new Sink<Exposes>() {
                @Override
                public void onOutcome(Exposes exposes) {
                    emitOutcome(exposes);
                }

                @Override
                public void onError(Throwable cause) {
                    emitError(cause);
                }

                @Override
                public void onCompletion() {
                    // this completion does not trigger the parent completion
                }
            });
        } catch (Throwable ex) {
            emitError(ex);
        }
    }
}
