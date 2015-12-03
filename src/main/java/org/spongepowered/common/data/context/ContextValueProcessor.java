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
package org.spongepowered.common.data.context;

import org.spongepowered.api.context.ContextViewer;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.context.DataContextual;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.ValueProcessor;

import java.util.Optional;

public interface ContextValueProcessor<E, V extends BaseValue<E>> {

    /**
     * Gets the associated {@link Key} that this {@link ContextValueProcessor}
     * will handle.
     *
     * @return The associated key for this processor
     */
    Key<? extends BaseValue<E>> getKey();

    /**
     * Gets the priority of this processor. A single {@link Key} can have
     * multiple {@link ContextValueProcessor}s such that mods introducing
     * changes to the game can provide their own {@link ContextValueProcessor}s
     * for specific cases. The notion is that the higher the priority, the
     * earlier the processor is used. If for any reason a processor's method
     * is returning an {@link Optional#empty()} or
     * {@link DataTransactionResult} with a failure, the next processor in
     * line will be used. By default, all Sponge processors are with a
     * priority of 100.
     *
     * @return The priority of the processor
     */
    int getPriority();

    /**
     * @param contextual The contextual object
     * @param container The value container to retrieve the value froms
     * @return The value, if available and compatible
     * @see {@link ValueProcessor#getValueFromContainer(ValueContainer)}
     */
    Optional<E> getValueFromContainer(DataContextual contextual, ContextViewer viewer, ValueContainer<?> container);

    /**
     * Gets the actual {@link Value} object wrapping around the underlying value
     * desired from the provided {@link ValueContainer}. This is very similar to
     * {@link #getValueFromContainer(DataContextual, ContextViewer, ValueContainer)} except that
     * instead of an actual value, a {@link Value} or extension there of is
     * returned.
     *
     * @param contextual The contextual object
     * @param container The container to get the API value from
     * @return The {@link Value} typed value
     * @see {@link ValueProcessor#getApiValueFromContainer(ValueContainer)}
     */
    Optional<V> getApiValueFromContainer(DataContextual contextual, ContextViewer viewer, ValueContainer<?> container);

    /**
     * Checks if the provided {@link ValueContainer} is compatible with the
     * value of data associated with this {@link ContextValueProcessor}.
     *
     * @param contextual The contextual object
     * @param container The value container to check
     * @return True if the container supports the value
     * @see {@link ValueProcessor#supports(ValueContainer)}
     */
    boolean supports(DataContextual contextual, ContextViewer viewer, ValueContainer<?> container);

    /**
     * Offers the provided {@link BaseValue} containing a value of the
     * appropriate value type of this {@link ContextValueProcessor} to
     * offer back to the {@link ValueContainer}.
     *
     * @param contextual The contextual object
     * @param container The value container
     * @param value The value
     * @return The transaction result
     * @see {@link ValueProcessor#offerToStore(ValueContainer, Object)}
     */
    DataTransactionResult offerToStore(DataContextual contextual, ContextViewer viewer, ValueContainer<?> container, E value);

    /**
     * Attempts to remove the known keyed data associated with this
     * {@link ContextValueProcessor} from the provided {@link ValueContainer}. If
     * the result is not possible, the result will be an expected
     * {@link DataTransactionResult.Type#FAILURE}.
     *
     * @param contextual The contextual object
     * @param container The value container to remove data from
     * @return The transaction result
     * @see {@link ValueProcessor#removeFrom(ValueContainer)}
     */
    DataTransactionResult removeFrom(DataContextual contextual, ContextViewer viewer, ValueContainer<?> container);

}
