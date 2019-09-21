package io.vlingo.common.completes;

public interface LazySource<Exposes> extends Source<Exposes> {
    void activate();
}
