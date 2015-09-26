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

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.annotation.Nullable;

public class SpongeWorldResourcePack extends SpongeResourcePack {

    private final String path;
    private final URI uri;
    public static final String LEVEL_PACK_PROTOCOL = "level://";

    public SpongeWorldResourcePack(String levelUri, @Nullable String hash) {
        super(hash);
        this.path = levelUri.substring(LEVEL_PACK_PROTOCOL.length());
        try {
            this.uri = URI.create(LEVEL_PACK_PROTOCOL + URLEncoder.encode(this.path, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    public SpongeWorldResourcePack(URI levelUri, @Nullable String hash) {
        super(hash);
        String path = levelUri.toString().substring(LEVEL_PACK_PROTOCOL.length());
        try {
            this.path = URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
        this.uri = levelUri;
    }

    @Override
    public String getName() {
        return "resourceszip";
    }

    @Override
    public String getUrlString() {
        return LEVEL_PACK_PROTOCOL + this.path;
    }

    @Override
    public URI getUri() {
        return this.uri;
    }

}
