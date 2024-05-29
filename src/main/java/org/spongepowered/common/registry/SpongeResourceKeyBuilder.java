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


import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.common.util.Preconditions;

import java.util.Objects;

public final class SpongeResourceKeyBuilder implements ResourceKey.Builder {

    private String namespace;
    private String value;

    @Override
    public ResourceKey.Builder namespace(String namespace) {
        Objects.requireNonNull(namespace, "Namespace cannot be null");
        this.namespace = namespace;
        return this;
    }

    @Override
    public ResourceKey.Builder value(String value) {
        Objects.requireNonNull(value, "Value cannot be null");
        this.value = value;
        return this;
    }

    @Override
    public ResourceKey build() throws IllegalStateException {
        Preconditions.checkState(this.namespace != null, "Namespace cannot be empty");
        Preconditions.checkState(this.value != null, "Value cannot be empty");

        try {
            final ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath(this.namespace, this.value);
            return (ResourceKey) (Object) resourceLocation;
        } catch (ResourceLocationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public ResourceKey.Builder reset() {
        this.namespace = null;
        this.value = null;
        return this;
    }
}
