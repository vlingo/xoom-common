package io.vlingo.xoom.common;

import java.io.Serializable;
import java.util.function.Consumer;

@FunctionalInterface
public interface SerializableConsumer<T> extends Consumer<T>, Serializable {
}
