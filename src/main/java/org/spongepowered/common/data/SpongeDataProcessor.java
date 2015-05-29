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

import com.google.common.base.Optional;
import org.spongepowered.api.data.Component;
import org.spongepowered.api.data.ComponentBuilder;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.service.persistence.DataBuilder;

public interface SpongeDataProcessor<T extends Component<T>> extends DataBuilder<T>, ComponentBuilder<T> {

    /**
     * Attempts to get the given {@link Component} of type {@code T} if
     * and only if the manipulator's required data exists from the
     * {@link DataHolder}. This is conceptually different from
     * {@link ComponentBuilder#createFrom(DataHolder)} since a new instance
     * isn't returned even if the {@link Component} is applicable.
     *
     * <p>This is a processor method for {@link DataHolder#getData(Class)}.</p>
     *
     * @param dataHolder The data holder
     * @return The manipulator, if available
     */
    Optional<T> getFrom(DataHolder dataHolder);

    /**
     * Attempts to fill the given {@link Component}
     * @param dataHolder
     * @param manipulator
     * @param priority
     * @return
     */
    Optional<T> fillData(DataHolder dataHolder, T manipulator, DataPriority priority);

    /**
     * Sets the data from the {@link Component} with the given {@link DataPriority}.
     *
     * @param dataHolder The data holder to set the data onto
     * @param manipulator The manipulator to set the data from
     * @param priority The priority
     * @return The transaction result
     */
    DataTransactionResult setData(DataHolder dataHolder, T manipulator, DataPriority priority);

    /**
     * Attempts to remove the {@link Component} type from the given {@link DataHolder}.
     *
     * <p>If the {@link DataHolder} can not support removing the data outright,
     * {@code false} should be returned.</p>
     *
     * @param dataHolder The data holder to remove the data from
     * @return If the data was removed successfully
     */
    boolean remove(DataHolder dataHolder);

}
