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
package org.spongepowered.common.data;

import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.CommandData;
import org.spongepowered.api.data.manipulator.mutable.MobSpawnerData;
import org.spongepowered.api.data.manipulator.mutable.entity.HealthData;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.mutable.CollectionValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.Entity;

import java.util.Optional;

/**
 * An implementation processor for handling a particular {@link BaseValue}.
 * Usually every {@linkplain ValueProcessor} will deal only with
 * {@link Value}s associated with the key returned by {@link #getKey()}.
 *
 * @param <E> The type of element within the value
 * @param <V> The type of Value
 */
public interface ValueProcessor<E, V extends BaseValue<E>> {

    /**
     * Gets the associated {@link Key} that this {@link ValueProcessor}
     * will handle.
     *
     * @return The associated key for this processor
     */
    Key<? extends BaseValue<E>> getKey();

    /**
     * Gets the priority of this processor. A single {@link Key} can have
     * multiple {@link ValueProcessor}s such that mods introducing
     * changes to the game can provide their own {@link ValueProcessor}s
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
     * Gets the underlying value as an {@link Optional}. This is the direct
     * implementation of {@link ValueContainer#get(Key)}. As well, since the
     * return type is {@link Optional}, this method too is applicable for
     * {@link ValueContainer#getOrNull(Key)} and
     * {@link ValueContainer#getOrElse(Key, Object)} as they are simply
     * additional methods called from {@link Optional#orElse(Object)} and
     * {@link Optional#orElse(Object)}.
     *
     * <p>To truly understand the use of this method, the thought process
     * is that a {@link ValueContainer} being mixed in to existing minecraft
     * classes will require a pseudo lookup of the {@link Key} to associate to
     * a field belonging to provided {@link ValueContainer}. Being that the
     * {@link Value} is very specific, the only assumption of mixed in
     * implementations will have no room for extra possibilities. Therefor,
     * this method serves as a "guarantee" that we've found the type of
     * {@link Value} we want to retrieve, and all we need to do is validate
     * that the {@link Value} is infact compatible with the provided
     * {@link ValueContainer}.</p>
     *
     * <p>An example of this type of interaction is getting the health from an
     * {@link Entity}. Normally, when you have an {@link Entity}, you can
     * grab health two ways: <ol><li><pre>{@code entity.get(Keys.HEALTH)}</pre>
     * </li><li><pre>{@code entity.get(HealthData.class).health()}</pre></li>
     * </ol>The advantage of the first is that the use of
     * {@link Entity#get(Key)} will result in a call to this method for a
     * {@link ValueProcessor} handling specifically the health value. The
     * second case will use the same {@link ValueProcessor} but be retrieving
     * the values directly from the {@link HealthData} {@link DataManipulator}.
     * This differs with object creation as the second method will result in an
     * unecessary {@link DataManipulator} to be created, and then finally a
     * {@link ValueProcessor} method call for the {@link DataManipulator}
     * instead of the {@link Entity} having a direct call.</p>
     *
     * <p>The cases where this type of value usage is not preferable is with
     * more complex {@link Value}s, such as {@link CollectionValue},
     * values found for {@link MobSpawnerData} and {@link CommandData}. Using
     * the {@link ValueContainer#get(Key)} and
     * {@link DataManipulator#set(Key, Object)} remains to be the optimal methods
     * to use as they do not rely on the implementation attempting to guess which
     * {@link Key}, {@link Value} and {@link ValueProcessor} is needed to
     * manipulate the data.</p>
     *
     * @param container The value container to retrieve the value froms
     * @return The value, if available and compatible
     */
    Optional<E> getValueFromContainer(ValueContainer<?> container);

    /**
     * Gets the actual {@link Value} object wrapping around the underlying value
     * desired from the provided {@link ValueContainer}. This is very similar to
     * {@link #getValueFromContainer(ValueContainer)} except that instead of an
     * actual value, a {@link Value} or extension there of is returned.
     *
     * @param container The container to get the API value from
     * @return The {@link Value} typed value
     */
    Optional<V> getApiValueFromContainer(ValueContainer<?> container);

    /**
     * Checks if the provided {@link ValueContainer} is compatible with the
     * value of data associated with this {@link ValueProcessor}.
     *
     * @param container The value container to check
     * @return True if the container supports the value
     */
    boolean supports(ValueContainer<?> container);

    /**
     * Offers the provided {@link BaseValue} containing a value of the
     * appropriate value type of this {@link ValueProcessor} to offer
     * back to the {@link ValueContainer}.
     *
     * @param container The value container
     * @param value The value
     * @return The transaction result
     */
    DataTransactionResult offerToStore(ValueContainer<?> container, E value);

    /**
     * Attempts to remove the known keyed data associated with this
     * {@link ValueProcessor} from the provided {@link ValueContainer}. If
     * the result is not possible, the result will be an expected
     * {@link org.spongepowered.api.data.DataTransactionResult.Type#FAILURE}.
     *
     * @param container The value container to remove data from
     * @return The transaction result
     */
    DataTransactionResult removeFrom(ValueContainer<?> container);

}
