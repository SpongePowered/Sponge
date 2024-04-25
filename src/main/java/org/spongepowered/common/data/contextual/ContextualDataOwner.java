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
package org.spongepowered.common.data.contextual;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPerspective;
import org.spongepowered.plugin.PluginContainer;

import java.io.Closeable;
import java.util.Map;
import java.util.Set;

public abstract class ContextualDataOwner<T extends DataPerspective> implements Closeable {

    protected final T owner;

    protected final Map<DataPerspective, PerspectiveContainer<?, ?>> perspectives;
    private final Set<ContextualDataOwner<?>> links;

    ContextualDataOwner(final T owner) {
        this.owner = owner;

        this.perspectives = Maps.newHashMap();
        this.links = Sets.newHashSet();
    }

    public final @Nullable PerspectiveContainer<?, ?> dataPerception(final DataPerspective perspective) {
        return this.perspectives.get(perspective);
    }

    //TODO: Abstract method on ContextualData?
    public abstract PerspectiveContainer<?, ?> createDataPerception(final DataPerspective perspective);

    public final DataHolder.Mutable createDataPerception(final PluginContainer plugin, final DataPerspective perspective) {
        return new ContextualDataProvider(this.createDataPerception(perspective), plugin);
    }

    public final void linkContextualOwner(final ContextualDataOwner<?> owner) {
        this.links.add(owner);
    }

    public final void unlinkContextualOwner(final ContextualDataOwner<?> owner) {
        this.links.remove(owner);
    }

    public void perceiveAdded(final DataPerspective perspective) {
        for (final PerspectiveContainer<?, ?> container : this.perspectives.values()) {
            container.addPerspective(perspective);
        }
    }

    public void perceiveRemoved(final DataPerspective perspective) {
        for (final PerspectiveContainer<?, ?> container : this.perspectives.values()) {
            container.removePerspective(perspective);
        }
    }

    @Override
    public final void close() {
        this.perspectives.values().forEach(PerspectiveContainer::close);

        this.links.forEach(o -> o.perspectives.remove(this.owner));
    }
}
