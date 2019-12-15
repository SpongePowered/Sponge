package org.spongepowered.common.data.provider;

import org.spongepowered.api.data.DataProvider;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;

import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class DataProviderRegistryBuilder {

    private final DataProviderRegistry registry;

    public DataProviderRegistryBuilder(DataProviderRegistry registry) {
        this.registry = registry;
        this.register();
    }

    protected <T, R> ObjectConverter<T, R> identity() {
        return new ObjectConverter<T, R>() {
            @Override
            public R to(T element) {
                return (R) element;
            }
            @Override
            public T from(R value) {
                return (T) value;
            }
        };
    }

    protected interface ObjectConverter<E, T> {

        T to(E element);

        E from(T value);
    }

    protected <E, H> void register(Class<H> target, Key<? extends Value<E>> key,
            Function<H, E> getter) {

    }

    protected <E, H> void register(Class<H> target, Key<? extends Value<E>> key,
            Function<H, E> getter, BiConsumer<H, E> setter) {

    }

    protected <E, H, T> void register(Class<H> target, Key<? extends Value<E>> key,
            Function<H, T> getter, BiConsumer<H, T> setter, ObjectConverter<E, T> converter) {

    }

    protected <E, G, S> void register(Class<G> getterTarget, Class<S> setterTarget,
            Key<? extends Value<E>> key, Function<G, E> getter, BiConsumer<S, E> setter) {

    }

    protected void register(DataProvider<?,?> provider) {

    }

    protected abstract void register();
}
