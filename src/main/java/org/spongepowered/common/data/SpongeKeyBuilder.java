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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.util.SpongeCatalogBuilder;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

public final class SpongeKeyBuilder<E, V extends BaseValue<E>> extends SpongeCatalogBuilder<Key<V>, Key.Builder<E, V>> implements Key.Builder<E, V> {

    private static final Set<String> loggedPlugins = new HashSet<>();

    @Nullable TypeToken<V> valueToken;
    @Nullable Translation name;
    @Nullable DataQuery query;

    @SuppressWarnings("unchecked")
    @Override
    public <T, B extends BaseValue<T>> Key.Builder<T, B> type(TypeToken<B> token) {
        this.valueToken = (TypeToken<V>) checkNotNull(token, "Value Token cannot be null!");
        return (Key.Builder<T, B>) this;
    }

    @Override
    public Key.Builder<E, V> query(DataQuery query) {
        checkArgument(!query.getParts().isEmpty(), "DataQuery cannot be null!");
        this.query = query;
        return this;
    }

    @Override
    public Key.Builder<E, V> id(String id) {
        final int index = id.indexOf(':');
        // Strip the plugin id
        if (index != -1) {
            final String pluginId = id.substring(0, index);
            if (loggedPlugins.add(pluginId)) {
                SpongeImpl.getLogger().warn(pluginId + ": It is no longer required to include the plugin id when specifying a "
                        + "Key id through Key.Builder#id. This is deprecated and may be removed later. The plugin id will be retrieved from the "
                        + "current PluginContainer in the cause stack. Key: " + id);
            }
            id = id.substring(index + 1);
        }
        return super.id(id);
    }

    @Override
    protected Key<V> build(PluginContainer plugin, String id, Translation name) {
        checkState(this.valueToken != null, "Value token must be set!");
        checkState(this.query != null, "DataQuery must be set!");
        return new SpongeKey<>(plugin.getId() + ':' + id, name, this.valueToken, this.query, plugin);
    }

    @Override
    public Key.Builder<E, V> reset() {
        super.reset();
        this.valueToken = null;
        this.id = null;
        this.name = null;
        this.query = null;
        return this;
    }
}
