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
package org.spongepowered.common.resourcepack;

import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.Nullable;

public final class SpongeURIResourcePack extends SpongeResourcePack {

    private final URI uri;
    private final String name;

    public SpongeURIResourcePack(URI uri, @Nullable String hash) {
        super(hash);
        this.uri = uri;
        this.name = getName0();
    }

    public SpongeURIResourcePack(String uri, @Nullable String hash) throws URISyntaxException {
        this(new URI(uri), hash);
    }

    private String getName0() {
        String name = this.uri.getPath();
        name = name.substring(name.lastIndexOf("/") + 1);
        return name.replaceAll("\\W", "");
    }

    @Override
    public String getUrlString() {
        return this.uri.toString();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public URI getUri() {
        return this.uri;
    }

}
