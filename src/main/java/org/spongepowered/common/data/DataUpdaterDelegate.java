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

import com.google.common.collect.ImmutableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.data.persistence.DataContentUpdater;
import org.spongepowered.api.data.persistence.DataView;

public final class DataUpdaterDelegate implements DataContentUpdater {

    private static final Logger LOGGER = LogManager.getLogger();

    private final ImmutableList<DataContentUpdater> updaters;
    private final int from;
    private final int to;

    public DataUpdaterDelegate(ImmutableList<DataContentUpdater> updaters, int from, int to) {
        this.updaters = updaters;
        this.from = from;
        this.to = to;
    }

    @Override
    public int inputVersion() {
        return this.from;
    }

    @Override
    public int outputVersion() {
        return this.to;
    }

    @Override
    public DataView update(DataView content) {
        final DataView copied = content.copy(); // backup
        DataView updated = copied;
        for (DataContentUpdater updater : this.updaters) {
            try {
                updated = updater.update(updated);
            } catch (Exception e) {
                DataUpdaterDelegate.LOGGER.error("There was error attempting to update some data for the content updater: {}"
                                                 + "\nThe original data is being returned, possibly causing "
                                                 + "issues later on, \nbut the original data should not be lost. Please notify the developer "
                                                 + "of this exception with the stacktrace.", updater.getClass().getName(), e);
                return copied;
            }
        }
        return updated;
    }
}
