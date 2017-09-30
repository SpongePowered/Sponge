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
package org.spongepowered.common.data.manipulator.mutable.common;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.manipulator.immutable.ImmutableMappedData;
import org.spongepowered.api.data.manipulator.mutable.MappedData;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.mutable.MapValue;
import org.spongepowered.api.util.CollectionUtils;
import org.spongepowered.common.data.value.mutable.SpongeMapValue;
import org.spongepowered.common.util.ReflectionUtil;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A common implementation for {@link MappedData}s provided by the API.
 *
 * @param <K> The type of key in the map
 * @param <V> The type of value in the map
 * @param <M> The type of {@link DataManipulator}
 * @param <I> The type of {@link ImmutableDataManipulator}
 */
@SuppressWarnings("unchecked")
public abstract class AbstractMappedData<K, V, M extends MappedData<K, V, M, I>, I extends ImmutableMappedData<K, V, I, M>>
        extends AbstractSingleData<Map<K, V>, M, I> implements MappedData<K, V, M, I> {

    private final Class<? extends I> immutableClass;

    public AbstractMappedData(Class<M> manipulatorClass, Map<K, V> value,
        Key<? extends BaseValue<Map<K, V>>> usedKey,
        Class<? extends I> immutableClass) {
        super(manipulatorClass, CollectionUtils.copyMap(value), usedKey);
        checkArgument(!Modifier.isAbstract(immutableClass.getModifiers()), "The immutable class cannot be abstract!");
        checkArgument(!Modifier.isInterface(immutableClass.getModifiers()), "The immutable class cannot be an interface!");
        this.immutableClass = immutableClass;
    }

    @Override
    public M copy() {
        return (M) ReflectionUtil.createInstance(this.getClass(), getValue());
    }

    @Override
    public I asImmutable() {
        return ReflectionUtil.createInstance(this.immutableClass, getValue());
    }

    @Override
    protected MapValue<K, V> getValueGetter() {
        return new SpongeMapValue<>(this.usedKey, this.getValue());
    }

    @Override
    protected Map<K, V> getValue() {
        return Maps.newHashMap(super.getValue());
    }

    @Override
    protected M setValue(Map<K, V> value) {
        return super.setValue(Maps.newHashMap(value));
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Objects.hashCode(this.getValue());
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        final AbstractMappedData other = (AbstractMappedData) obj;
        return Objects.equal(this.getValue(), other.getValue());
    }

    @Override
    public Optional<V> get(K key) {
        return Optional.of(super.getValue().get(checkNotNull(key)));
    }

    @Override
    public Set<K> getMapKeys() {
        return ImmutableSet.copyOf(super.getValue().keySet());
    }

    @Override
    public MapValue<K, V> getMapValue() {
        return getValueGetter();
    }

    @Override
    public Map<K, V> asMap() {
        return getValue();
    }

    @Override
    public M put(K key, V value) {
        super.getValue().put(checkNotNull(key, "Key cannot be null!"), checkNotNull(value, "Value cannot be null!"));
        return (M) this;
    }

    @Override
    public M putAll(Map<? extends K, ? extends V> map) {
        super.getValue().putAll(checkNotNull(map, "Map cannot be null!"));
        return (M) this;
    }

    @Override
    public M remove(K key) {
        super.getValue().remove(checkNotNull(key, "Key cannot be null!"));
        return (M) this;
    }
}
