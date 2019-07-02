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
package org.spongepowered.common.data.datasync;

import com.google.common.base.MoreObjects;
import net.minecraft.entity.Entity;
import net.minecraft.network.datasync.DataParameter;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.bridge.packet.DataParameterBridge;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class DataParameterConverter<E> {

    private final DataParameter<E> parameter;

    @SuppressWarnings("unchecked")
    public DataParameterConverter(DataParameter<E> parameter) {
        this.parameter = parameter;
        ((DataParameterBridge<E>) parameter).bridge$setDataConverter(this);
    }

    public DataParameter<E> getParameter() {
        return this.parameter;
    }

    /**
     * Generates an appropriate {@link DataTransactionResult}. The
     * reason why it is a {@link List} and not a single value, is that in
     * certain cases, there can be a "compound" value to represent multiple
     * values, such as the {@link Entity} flags bitmasks 5 "flags" in one value.
     *
     *
     * @param entity
     * @param currentValue The current raw value
     * @return The list of immutable values converted
     */
    public abstract Optional<DataTransactionResult> createTransaction(Entity entity, E currentValue, E value);

    public abstract E getValueFromEvent(E originalValue, List<ImmutableValue<?>> immutableValues);

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("parameter", this.parameter)
            .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DataParameterConverter<?> that = (DataParameterConverter<?>) o;
        return Objects.equals(this.parameter, that.parameter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.parameter);
    }
}
