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
package org.spongepowered.common.data.value.mutable;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableCollectionValue;
import org.spongepowered.api.data.value.mutable.CollectionValue;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

@SuppressWarnings("unchecked")
public abstract class SpongeCollectionValue<Element,
    CollectionType extends Collection<Element>,
    CollectionValueType extends CollectionValue<Element, CollectionType, CollectionValueType, ImmutableType>,
    ImmutableType extends ImmutableCollectionValue<Element, CollectionType, ImmutableType, CollectionValueType>>
    extends SpongeValue<CollectionType> implements CollectionValue<Element, CollectionType, CollectionValueType, ImmutableType> {


    public SpongeCollectionValue(Key<? extends BaseValue<CollectionType>> key, CollectionType defaultValue) {
        super(key, defaultValue);
    }

    public SpongeCollectionValue(Key<? extends BaseValue<CollectionType>> key, CollectionType defaultValue, CollectionType actualValue) {
        super(key, defaultValue, actualValue);
    }

    @Override
    public CollectionValueType set(CollectionType value) {
        this.actualValue = checkNotNull(value);
        return (CollectionValueType) this;
    }

    @Override
    public CollectionValueType transform(Function<CollectionType, CollectionType> function) {
        this.actualValue = checkNotNull(function).apply(this.actualValue);
        return (CollectionValueType) this;
    }

    @Override
    public int size() {
        return this.actualValue.size();
    }

    @Override
    public boolean isEmpty() {
        return this.actualValue.isEmpty();
    }

    @Override
    public CollectionValueType add(Element element) {
        this.actualValue.add(checkNotNull(element));
        return (CollectionValueType) this;
    }

    @Override
    public CollectionValueType addAll(Iterable<Element> elements) {
        for (Element element : checkNotNull(elements)) {
            this.actualValue.add(checkNotNull(element));
        }
        return (CollectionValueType) this;
    }

    @Override
    public CollectionValueType remove(Element element) {
        this.actualValue.remove(checkNotNull(element));
        return (CollectionValueType) this;
    }

    @Override
    public CollectionValueType removeAll(Iterable<Element> elements) {
        for (Element element : elements) {
            this.actualValue.remove(checkNotNull(element));
        }
        return (CollectionValueType) this;
    }

    @Override
    public CollectionValueType removeAll(Predicate<Element> predicate) {
        for (Iterator<Element> iterator = this.actualValue.iterator(); iterator.hasNext(); ) {
            if (checkNotNull(predicate).test(iterator.next())) {
                iterator.remove();
            }
        }
        return (CollectionValueType) this;
    }

    @Override
    public boolean contains(Element element) {
        return this.actualValue.contains(checkNotNull(element));
    }

    @Override
    public boolean containsAll(Collection<Element> iterable) {
        return this.actualValue.containsAll(iterable);
    }

    @Override
    public boolean exists() {
        return this.actualValue != null;
    }

    @Override
    public abstract ImmutableType asImmutable();

    @Override
    public abstract CollectionValueType copy();

    @Override
    public Optional<CollectionType> getDirect() {
        return Optional.of(this.actualValue);
    }

    @Override
    public Iterator<Element> iterator() {
        return this.actualValue.iterator();
    }
}
