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
package org.spongepowered.common.data.persistence.datastore;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.DataContentUpdater;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.data.DataUpdaterDelegate;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class SpongeDataStore extends VanillaDataStore {

    private ResourceKey key;
    private int version;
    private DataContentUpdater[] updaters;

    public SpongeDataStore(ResourceKey key, final Map<Key<?>, Tuple<BiConsumer<DataView, ?>, Function<DataView, Optional<?>>>> queriesByKey,
            final Collection<Type> tokens, int version, DataContentUpdater[] updaters) {
        super(queriesByKey, tokens);
        this.key = key;
        this.version = version;
        this.updaters = updaters;
    }

    public ResourceKey getDataStoreKey() {
        return this.key;
    }

    public int getVersion() {
        return version;
    }

    public Optional<DataContentUpdater> getUpdaterFor(Integer fromVersion) {
        int toVersion = this.version;
        ImmutableList.Builder<DataContentUpdater> builder = ImmutableList.builder();
        int version = fromVersion;
        for (DataContentUpdater updater : this.updaters) {
            if (updater.inputVersion() == version) {
                if (updater.outputVersion() > toVersion) {
                    continue;
                }
                version = updater.outputVersion();
                builder.add(updater);
            }
        }
        if (version < toVersion || version > toVersion) { // There wasn't a registered updater for the version being requested
            final String message = "Failed to update content for datastore: " + this.key.asString()
                    + "\nUpdating from " + fromVersion + " to " + toVersion + " is impossible."
                    + "\nPlease notify the plugin author of this error.";
            SpongeCommon.logger().warn(message);
            return Optional.empty();
        }
        final ImmutableList<DataContentUpdater> updatersList = builder.build();
        if (updatersList.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new DataUpdaterDelegate(updatersList, fromVersion, toVersion));
    }
}
