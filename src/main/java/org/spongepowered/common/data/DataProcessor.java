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

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.entity.EntityType;

import java.util.Optional;

public interface DataProcessor<M extends DataManipulator<M, I>, I extends ImmutableDataManipulator<I, M>> {

    /**
     * Gets the priority of this processor. A single {@link Key} can have
     * multiple {@link DataProcessor}s such that mods introducing
     * changes to the game can provide their own {@link DataProcessor}s
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

    boolean supports(DataHolder dataHolder);

    boolean supports(EntityType entityType);

    /**
     * Attempts to get the given {@link DataManipulator} of type {@code T} if
     * and only if the manipulator's required data exists from the
     * {@link DataHolder}. This is conceptually different from
     * {@link DataProcessor#createFrom(DataHolder)} since a new instance
     * isn't returned even if the {@link DataManipulator} is applicable.
     *
     * <p>This is a processor method for {@link DataHolder#get(Class)}.</p>
     *
     * @param dataHolder The data holder
     * @return The manipulator, if available
     */
    Optional<M> from(DataHolder dataHolder);

    Optional<M> createFrom(DataHolder dataHolder);

    Optional<M> fill(DataHolder dataHolder, M manipulator, MergeFunction overlap);

    Optional<M> fill(DataContainer container, M m);

    /**
     * Sets the data from the {@link DataManipulator}. Usually, if a
     * {@link DataHolder} is being offered a {@link DataManipulator} with a
     * {@link MergeFunction}, the {@link MergeFunction} logic should always
     * be settled first, before offering the finalized {@link DataManipulator}
     * to the {@link DataHolder}. In the case of implementation, what really
     * happens is that all pre-logic before data is actually "offered" to a
     * {@link DataHolder}, the data is filtered for various reasons from
     * various plugins. After the data is finished being manipulated
     * by the various sources, including the possibly provided
     * {@link MergeFunction}, the {@link DataManipulator} is finally offered
     * to the {@link DataHolder}. The resulting {@link DataTransactionResult}
     * is almost always made ahead of time.
     *
     * @param dataHolder The data holder to set the data onto
     * @param manipulator The manipulator to set the data from
     * @return The transaction result
     */
    DataTransactionResult set(DataHolder dataHolder, M manipulator, MergeFunction function);

    Optional<I> with(Key<? extends BaseValue<?>> key, Object value, I immutable);

    /**
     * Attempts to remove the {@link DataManipulator} type from the given {@link DataHolder}.
     *
     * <p>If the {@link DataHolder} can not support removing the data outright,
     * {@code false} should be returned.</p>
     *
     * @param dataHolder The data holder to remove the data from
     * @return If the data was removed successfully
     */
    DataTransactionResult remove(DataHolder dataHolder);
}
