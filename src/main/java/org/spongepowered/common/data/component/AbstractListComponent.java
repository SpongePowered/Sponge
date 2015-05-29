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
package org.spongepowered.common.data.component;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.component.ListComponent;
import org.spongepowered.common.util.NonNullArrayList;

import java.util.List;

@SuppressWarnings("unchecked")
public abstract class AbstractListComponent<E, T extends ListComponent<E, T>> extends SpongeAbstractComponent<T> implements ListComponent<E, T> {

    protected NonNullArrayList<E> elementList = new NonNullArrayList<E>();

    protected AbstractListComponent(Class<T> manipulatorClass) {
        super(manipulatorClass);
    }

    @Override
    public List<E> getAll() {
        return ImmutableList.copyOf(this.elementList);
    }

    @Override
    public Optional<E> get(int index) {
        checkArgument(index >= 0, "Index must be greater than zero!");
        return Optional.fromNullable(this.elementList.get(index));
    }

    @Override
    public boolean contains(E element) {
        return this.elementList.contains(checkNotNull(element));
    }

    @Override
    public T set(E... elements) {
        this.elementList = new NonNullArrayList<E>();
        for (E element : elements) {
            this.elementList.add(element);
        }
        return (T) this;
    }

    @Override
    public T set(Iterable<E> elements) {
        this.elementList = new NonNullArrayList<E>();
        for (E element : elements) {
            this.elementList.add(element);
        }
        return (T) this;
    }

    @Override
    public T set(int index, E element) {
        this.elementList.set(index, element);
        return (T) this;
    }

    @Override
    public T add(E element) {
        this.elementList.add(checkNotNull(element));
        return (T) this;
    }

    @Override
    public T add(int index, E element) {
        checkArgument(index >= 0);
        this.elementList.add(index, element);
        return (T) this;
    }

    @Override
    public T remove(int index) {
        this.elementList.remove(index);
        return (T) this;
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + Objects.hashCode(this.elementList);
    }

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
        final AbstractListComponent other = (AbstractListComponent) obj;
        return Objects.equal(this.elementList, other.elementList);
    }

    @Override
    public T reset() {
        this.elementList = new NonNullArrayList<E>();
        return (T) this;
    }
}
