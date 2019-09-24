// Copyright © 2012-2019 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.common.completes.sources;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import io.vlingo.common.Completes;
import io.vlingo.common.completes.LazySource;
import io.vlingo.common.completes.Sink;

public class InMemorySource<Exposes> implements LazySource<Exposes> {
    private ConcurrentLinkedQueue<Consumer<Sink<Exposes>>> queue;
    private Sink<Exposes> subscriber;
    private AtomicBoolean active;

    public InMemorySource() {
        this.queue = new ConcurrentLinkedQueue<>();
        this.subscriber = null;
        this.active = new AtomicBoolean(false);
    }

    @Override
    public void emitOutcome(Exposes outcome) {
        if (active.get()) {
            this.subscriber.onOutcome(outcome);
        } else {
            this.queue.add(e -> e.onOutcome(outcome));
        }
    }

    @Override
    public void emitError(Exception cause) {
        if (active.get()) {
            this.subscriber.onError(cause);
        } else {
            this.queue.add(e -> e.onError(cause));
        }
    }

    @Override
    public void emitCompletion() {
        if (active.get()) {
            this.subscriber.onCompletion();
        } else {
            this.queue.add(Sink::onCompletion);
        }
    }

    @Override
    public void subscribe(Sink<Exposes> subscriber) {
        this.subscriber = subscriber;
    }

    @Override
    public void activate() {
        if (this.subscriber == null) {
            throw new UnsupportedOperationException("Source must have a subscriber before being able to activate it.");
        }

        if (this.active.get()) {
            return;
        }

        this.active.set(true);
        if (!this.queue.isEmpty()) {
            this.queue.forEach(e -> e.accept(subscriber));
            this.queue = null;
        }
    }

    public static <E> InMemorySource<E> fromCompletes(Completes<E> completes) {
        InMemorySource<E> source = new InMemorySource<>();
        completes.andThenConsume(source::emitOutcome);
        completes.andThenConsume(s -> source.emitCompletion());

        return source;
    }
}
