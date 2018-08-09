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
package org.spongepowered.common.data.builder;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Locale;
import java.util.Optional;

import javax.annotation.Nullable;

public class CatalogKeyBuilder implements CatalogKey.Builder {

    @Nullable private String nameSpace;
    @Nullable private String value;

    @Override
    public CatalogKey.Builder namespace(String nameSpace) {
        checkArgument(!checkNotNull(nameSpace).isEmpty());
        this.nameSpace = nameSpace.toLowerCase(Locale.ENGLISH);
        return this;
    }

    @Override
    public CatalogKey.Builder namespace(PluginContainer container) {
        checkArgument(!checkNotNull(container).getId().isEmpty());
        this.nameSpace = container.getId();
        return this;
    }

    @Override
    public CatalogKey.Builder namespace(Object pluginInstance) {
        checkNotNull(pluginInstance, "plugin");
        final Optional<PluginContainer> pluginContainer = Sponge.getPluginManager().fromInstance(pluginInstance);
        checkArgument(pluginContainer.isPresent(), "Not a plugin");
        this.nameSpace = pluginContainer.get().getId();
        return this;
    }

    @Override
    public CatalogKey.Builder value(String value) {
        checkArgument(!checkNotNull(value).isEmpty());
        this.value = value;
        return this;
    }

    @Override
    public CatalogKey build() throws IllegalStateException {
        checkState(this.nameSpace != null);
        checkState(this.value != null);
        return (CatalogKey) (Object) new ResourceLocation(this.nameSpace, this.value);
    }

    @SuppressWarnings("deprecation")
    @Override
    public CatalogKey.Builder from(CatalogKey value) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cannot create new CatalogKeys from existing ones!");
    }

    @Override
    public CatalogKey.Builder reset() {
        this.nameSpace = null;
        this.value = null;
        return this;
    }
}
