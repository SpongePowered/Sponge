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

import com.google.common.base.MoreObjects;
import org.spongepowered.api.resourcepack.ResourcePack;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

public abstract class SpongeResourcePack implements ResourcePack {

    private final Optional<String> hash;
    private final String id = UUID.randomUUID().toString();

    public static final int HASH_SIZE = 40;

    public SpongeResourcePack(@Nullable String hash) {
        this.hash = Optional.ofNullable(hash);
    }

    public abstract String getUrlString();

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public Optional<String> getHash() {
        return this.hash;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", this.getId()).add("uri", this.getUri()).toString();
    }

    public static SpongeResourcePack create(String uri, String hash) throws URISyntaxException {
        if (uri.startsWith(SpongeWorldResourcePack.LEVEL_PACK_PROTOCOL)) {
            return new SpongeWorldResourcePack(uri, hash);
        }
        if (hash != null && hash.length() != HASH_SIZE) {
            hash = null;
        }
        return new SpongeURIResourcePack(uri, hash);
    }

    public static SpongeResourcePack create(URI uri, String hash) {
        if (uri.toString().startsWith(SpongeWorldResourcePack.LEVEL_PACK_PROTOCOL)) {
            return new SpongeWorldResourcePack(uri, hash);
        }
        return new SpongeURIResourcePack(uri, hash);
    }

}
