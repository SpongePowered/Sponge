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

import com.google.common.base.MoreObjects;
import com.google.common.reflect.TypeToken;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.data.ChangeDataHolderEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.common.SpongeImpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

public final class SpongeKey<V extends BaseValue<?>> implements Key<V> {

    private static final Set<String> loggedPlugins = new HashSet<>();

    private final TypeToken<V> valueToken;
    private final String id;
    private final String name;
    private final DataQuery query;
    private final TypeToken<?> elementToken;
    private final PluginContainer parent;
    @Nullable private List<KeyBasedDataListener<?>> listeners;

    SpongeKey(SpongeKeyBuilder<?, V> builder) {
        this.valueToken = builder.valueToken;
        this.name = builder.name;
        this.query = builder.query;
        this.elementToken = this.valueToken.resolveType(BaseValue.class.getTypeParameters()[0]);
        this.parent = getCurrentContainer();
        final String id = builder.id;
        if (id.indexOf(':') == -1) {
            this.id = this.parent.getId() + ':' + id;
        } else {
            this.id = id;
            if (loggedPlugins.add(this.parent.getId())) {
                SpongeImpl.getLogger().warn(this.parent.getId() + ": It is no longer required to include the plugin id when specifying a "
                        + "Key id through Key.Builder#id. This is deprecated and may be removed later. The plugin id will be retrieved from the "
                        + "current PluginContainer in the cause stack. Key: " + this);
            }
        }
    }

    private static PluginContainer getCurrentContainer() {
        return Sponge.getCauseStackManager().getCurrentCause().first(PluginContainer.class)
            .orElse(SpongeImpl.getMinecraftPlugin());
    }

    @Override
    public TypeToken<V> getValueToken() {
        return this.valueToken;
    }

    @Override
    public TypeToken<?> getElementToken() {
        return this.elementToken;
    }

    @Override
    public DataQuery getQuery() {
        return this.query;
    }

    @Override
    public <E extends DataHolder> void registerEvent(Class<E> holderFilter, EventListener<ChangeDataHolderEvent.ValueChange> listener) {
        if (this.listeners == null) {
            this.listeners = new ArrayList<>();
        }
        this.listeners.add(new KeyBasedDataListener<>(holderFilter, this, listener, getCurrentContainer()));
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void registerListeners() {
        if (this.listeners != null) {
            for (KeyBasedDataListener<?> listener : this.listeners) {
                Sponge.getEventManager().registerListener(listener.getOwner(), ChangeDataHolderEvent.ValueChange.class, listener);
            }
            // Clean up
            this.listeners.clear();
            this.listeners = null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final SpongeKey<?> spongeKey = (SpongeKey<?>) o;
        return Objects.equals(this.valueToken, spongeKey.valueToken) &&
               Objects.equals(this.id, spongeKey.id) &&
               Objects.equals(this.name, spongeKey.name) &&
               Objects.equals(this.query, spongeKey.query) &&
               Objects.equals(this.elementToken, spongeKey.elementToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.valueToken, this.id, this.name, this.query, this.elementToken);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("name", this.name)
            .add("id", this.id)
            .add("valueToken", this.valueToken)
            .add("elementToken", this.elementToken)
            .add("query", this.query)
            .toString();
    }

    public PluginContainer getParent() {
        return this.parent;
    }
}
