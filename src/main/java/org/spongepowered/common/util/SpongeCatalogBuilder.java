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
package org.spongepowered.common.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.translation.FixedTranslation;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.util.CatalogBuilder;
import org.spongepowered.api.util.ResettableBuilder;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.annotation.Nullable;

@SuppressWarnings("unchecked")
@NonnullByDefault
public abstract class SpongeCatalogBuilder<C extends CatalogType, B extends ResettableBuilder<C, B>>
        implements CatalogBuilder<C, B> {

    @Nullable protected Translation name;
    protected CatalogKey key;

    @Override
    public B name(String name) {
        checkNotNull(name, "name");
        checkState(!name.isEmpty(), "The name may not be empty");
        this.name = new FixedTranslation(name);
        return (B) this;
    }

    public Translation getName() {
        if (this.name != null) {
            return this.name;
        }
        return new FixedTranslation(this.key.getValue());
    }

    @Override
    public B name(Translation name) {
        checkNotNull(name, "name");
        checkState(!name.getId().isEmpty(), "The translation id may not be empty");
        this.name = name;
        return (B) this;
    }

    @Override
    public B key(CatalogKey key) {
        checkNotNull(key, "key");
        checkArgument(!key.getNamespace().isEmpty(), "The key namespace may not be empty.");
        checkArgument(!key.getValue().isEmpty(), "The key value may not be empty.");
        this.key = key;
        return (B) this;
    }

    @Override
    public B id(String id) {
        checkNotNull(id, "id");
        checkArgument(!id.isEmpty(), "The id may not be empty.");
        final PluginContainer pluginContainer = Sponge.getCauseStackManager().getCurrentCause().first(PluginContainer.class)
                .orElseThrow(() -> new IllegalStateException("Unable to find PluginContainer in cause stack."));
        return key(CatalogKey.of(pluginContainer.getId(), id));
    }

    @Override
    public C build() {
        checkNotNull(this.key, "The key must be set.");
        Translation name = this.name;
        if (name == null) {
            name = new FixedTranslation(this.key.getValue());
        }
        return build(this.key, name);
    }

    protected abstract C build(CatalogKey key, Translation name);

    @Override
    public B reset() {
        this.name = null;
        this.key = null;
        return (B) this;
    }
}
