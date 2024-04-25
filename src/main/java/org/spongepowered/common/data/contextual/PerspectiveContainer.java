/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.data.contextual;

import com.google.common.collect.Maps;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.DataPerspective;
import org.spongepowered.api.data.DataPerspectiveResolver;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.common.accessor.world.entity.EntityAccessor;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.util.CopyHelper;
import org.spongepowered.plugin.PluginContainer;

import java.io.Closeable;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings("unchecked")
public abstract class PerspectiveContainer<H extends ContextualDataOwner<?>, P extends DataPerspective> implements ValueContainer, Closeable {

    private final PerspectiveType perspectiveType;
    protected final H holder;
    protected final P perspective;

    private final Map<Key<?>, Map<Object, ?>> valuesByOwner;
    protected final Map<Key<?>, Object> activeValues;

    private long entityDataFlags;

    protected PerspectiveContainer(final PerspectiveType perspectiveType, final H holder, final P perspective) {
        this.perspectiveType = perspectiveType;
        this.holder = holder;
        this.perspective = perspective;

        this.valuesByOwner = Maps.newHashMap();
        this.activeValues = Maps.newHashMap();

        ((ContextualData) perspective).linkContextualOwner(this.holder);
    }

    public final long entityDataFlags() {
        return this.entityDataFlags;
    }

    final <E> DataTransactionResult offer(final PluginContainer pluginContainer, final Key<? extends Value<E>> key, final E value) {
        final @Nullable DataPerspectiveResolver<Value<E>, E> resolver = SpongeDataManager.getDataPerspectiveResolverRegistry().get(key);
        if (resolver == null) {
            return DataTransactionResult.failResult(Value.immutableOf(key, value));
        }

        return this.offer(this.perspectiveType, pluginContainer, resolver, value);
    }

    final <E> DataTransactionResult offer(final PerspectiveType perspectiveType, final Object owner, final DataPerspectiveResolver<Value<E>, E> resolver, final E value) {
        final Map<Object, E> valueMap = (Map<Object, E>) this.valuesByOwner.computeIfAbsent(resolver.key(), k -> Maps.newLinkedHashMap());
        if (Objects.equals(value, valueMap.put(owner, value))) {
            return DataTransactionResult.successResult(Value.immutableOf(resolver.key(), value));
        }

        if (resolver.key() == (Key<?>)Keys.CUSTOM_NAME) {
            this.entityDataFlags |= 1L << EntityAccessor.accessor$DATA_CUSTOM_NAME().getId();
        }

        final E mergedValue = valueMap.size() == 1 ? value : resolver.merge(valueMap.values());
        this.offer(perspectiveType, resolver, mergedValue);
        return DataTransactionResult.successResult(Value.immutableOf(resolver.key(), mergedValue));
    }

    protected abstract <E> void offer(final PerspectiveType perspectiveType, final DataPerspectiveResolver<Value<E>, E> resolver, final E value);

    final DataTransactionResult remove(final PluginContainer pluginContainer, final Key<?> key) {
        final @Nullable DataPerspectiveResolver<Value<?>, ?> resolver = SpongeDataManager.getDataPerspectiveResolverRegistry().get((Key) key);
        if (resolver == null) {
            return DataTransactionResult.failNoData();
        }

        return this.remove(this.perspectiveType, pluginContainer, resolver);
    }

    final DataTransactionResult remove(final PerspectiveType perspectiveType, final Object owner, final DataPerspectiveResolver<Value<?>, ?> resolver) {
        final @Nullable Map<Object, Object> valueMap = (Map<Object, Object>) this.valuesByOwner.get(resolver.key());
        if (valueMap == null) {
            return DataTransactionResult.failNoData();
        }

        final @Nullable Object value = valueMap.remove(owner);
        if (value == null) {
            return DataTransactionResult.failNoData();
        }

        if (resolver.key() == (Key<?>)Keys.CUSTOM_NAME) {
            this.entityDataFlags &= ~(1L << EntityAccessor.accessor$DATA_CUSTOM_NAME().getId());
        }

        if (valueMap.isEmpty()) {
            this.valuesByOwner.remove(resolver.key());
            this.activeValues.remove(resolver.key());
            this.remove(perspectiveType, resolver);
            return DataTransactionResult.successNoData();
        }

        final Object mergedValue = valueMap.size() == 1 ? valueMap.values().iterator().next() : ((DataPerspectiveResolver) resolver).merge(valueMap.values());
        this.offer(perspectiveType, owner, (DataPerspectiveResolver) resolver, mergedValue);
        return DataTransactionResult.successNoData();
    }

    protected abstract void remove(final PerspectiveType perspectiveType, final DataPerspectiveResolver<Value<?>, ?> resolver);

    protected abstract void addPerspective(final DataPerspective perspective);
    protected abstract void removePerspective(final DataPerspective perspective);

    @Override
    public <E> Optional<E> get(final Key<? extends Value<E>> key) {
        Objects.requireNonNull(key, "key");
        final @Nullable E value = (E) this.activeValues.get(key);
        return Optional.ofNullable(CopyHelper.copy(value));
    }

    @Override
    public <E, V extends Value<E>> Optional<V> getValue(final Key<V> key) {
        Objects.requireNonNull(key, "key");
        final @Nullable E value = (E) this.activeValues.get(key);
        return Optional.of(Value.genericMutableOf(key, CopyHelper.copy(value)));
    }

    @Override
    public boolean supports(Key<?> key) {
        return false;
    }

    @Override
    public Set<Key<?>> getKeys() {
        return Collections.emptySet();
    }

    @Override
    public Set<Value.Immutable<?>> getValues() {
        return Collections.emptySet();
    }

    @Override
    public final void close() {
        ((ContextualData) this.perspective).unlinkContextualOwner(this.holder);
    }
}
