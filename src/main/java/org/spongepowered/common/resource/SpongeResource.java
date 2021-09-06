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
package org.spongepowered.common.resource;

import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.resource.ResourcePath;

import java.io.IOException;
import java.io.InputStream;

public final class SpongeResource implements org.spongepowered.api.resource.Resource {

    private final ResourcePath path;
    private InputStream stream;

    public SpongeResource(final Resource resource) {
        this.path = new SpongeResourcePath((ResourceKey) (Object) resource.getLocation());
        this.stream = resource.getInputStream();
    }

    public SpongeResource(final ResourcePath path, final InputStream stream) {
        this.path = path;
        this.stream = stream;

    }

    @Override
    public ResourcePath path() {
        return this.path;
    }

    @Override
    public InputStream inputStream() {
        if (this.stream == null) {
            throw new IllegalStateException("Attempt made to access the data of a resource after it has been closed!");
        }
        return this.stream;
    }

    @Override
    public void close() throws IOException {
        try {
            this.stream.close();
        } finally {
            this.stream = null;
        }
    }
}
