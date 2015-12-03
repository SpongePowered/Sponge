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
import org.spongepowered.api.data.context.DataContext;
import org.spongepowered.api.data.context.DataContextual;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.merge.MergeFunction;

import java.util.Optional;

public interface ContextDataProcessor<M extends DataManipulator<M, I>, I extends ImmutableDataManipulator<I, M>> {

    /**
     * Gets the priority of this processor. A single {@link Key} can have
     * multiple {@link ContextDataProcessor}s such that mods introducing
     * changes to the game can provide their own {@link ContextDataProcessor}s
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

    boolean supports(DataContextual contextual, ContextViewer viewer, DataContext context);

    /**
     * Attempts to get the given {@link DataManipulator} of type {@code T} if
     * and only if the manipulator's required data exists from the
     * {@link DataContext}. This is conceptually different from
     * {@link ContextDataProcessor#createFrom(DataContextual, ContextViewer, DataContext)} since a new instance
     * isn't returned even if the {@link DataManipulator} is applicable.
     *
     * <p>This is a processor method for {@link DataContext#get(Class)}.</p>
     *
     * @param contextual The contextual object
     * @param context The data context
     * @return The manipulator, if available
     */
    Optional<M> from(DataContextual contextual, ContextViewer viewer, DataContext context);

    Optional<M> createFrom(DataContextual contextual, ContextViewer viewer, DataContext context);

    /**
     * Sets the data from the {@link DataManipulator}. Usually, if a
     * {@link DataContext} is being offered a {@link DataManipulator} with a
     * {@link MergeFunction}, the {@link MergeFunction} logic should always
     * be settled first, before offering the finalized {@link DataManipulator}
     * to the {@link DataContext}. In the case of implementation, what really
     * happens is that all pre-logic before data is actually "offered" to a
     * {@link DataContext}, the data is filtered for various reasons from
     * various plugins. After the data is finished being manipulated
     * by the various sources, including the possibly provided
     * {@link MergeFunction}, the {@link DataManipulator} is finally offered
     * to the {@link DataContext}. The resulting {@link DataTransactionResult}
     * is almost always made ahead of time.
     *
     * @param contextual The contextual object
     * @param context The context to set the data onto
     * @param manipulator The manipulator to set the data from
     * @return The transaction result
     */
    DataTransactionResult set(DataContextual contextual, ContextViewer viewer, DataContext context, M manipulator, MergeFunction function);

    /**
     * Attempts to remove the {@link DataManipulator} type from the given {@link DataContext}.
     *
     * <p>If the {@link DataContext} can not support removing the data outright,
     * {@code false} should be returned.</p>
     *
     * @param contextual The contextual object
     * @param context The context to remove the data from
     * @return If the data was removed successfully
     */
    DataTransactionResult remove(DataContextual contextual, ContextViewer viewer, DataContext context);

}
