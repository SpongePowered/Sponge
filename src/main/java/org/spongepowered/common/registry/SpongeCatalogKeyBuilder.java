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
package org.spongepowered.common.registry;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.plugin.PluginContainer;

public final class SpongeCatalogKeyBuilder implements CatalogKey.Builder {

    @Nullable private String namespace;
    private String value;

    @Override
    public CatalogKey.Builder namespace(String namespace) {
        checkNotNull(namespace, "Namespace cannot be null");
        this.namespace = namespace;
        return this;
    }

    @Override
    public CatalogKey.Builder namespace(PluginContainer container) {
        checkNotNull(container, "PluginContainer cannot be null");
        this.namespace = container.getId();
        return this;
    }

    @Override
    public CatalogKey.Builder value(String value) {
        checkNotNull(value, "Value cannot be null");
        this.value = value;
        return this;
    }

    @Override
    public CatalogKey build() throws IllegalStateException {
        checkState(this.value != null, "Value cannot be empty");
        if (this.namespace != null) {
            try {
                final ResourceLocation resourceLocation = new ResourceLocation(this.namespace, this.value);
                return (CatalogKey) (Object) resourceLocation;
            } catch (ResourceLocationException e) {
                throw new IllegalStateException(e);
            }
        }

        try {
            final ResourceLocation resourceLocation = new ResourceLocation(this.value);
            return (CatalogKey) (Object) resourceLocation;
        } catch (ResourceLocationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public CatalogKey.Builder reset() {
        this.namespace = null;
        this.value = null;
        return this;
    }
}
