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
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.common.SpongeCommon;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public abstract class SpongeResourcePack implements ResourcePack {

    private final String hash;
    private final String id = UUID.randomUUID().toString();

    public static final int HASH_SIZE = 40;

    public SpongeResourcePack(@Nullable String hash) {
        this.hash = hash;
    }

    public abstract String getUrlString();

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public Optional<String> hash() {
        return Optional.of(this.hash);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", this.id()).add("uri", this.uri()).toString();
    }

    public static SpongeResourcePack create(String uri, String hash) throws URISyntaxException {
        if (uri.startsWith(SpongeWorldResourcePack.LEVEL_PACK_PROTOCOL)) {
            return new SpongeWorldResourcePack(uri, hash);
        }
        if (hash != null && hash.length() != SpongeResourcePack.HASH_SIZE) {
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

    public static final class Factory implements ResourcePack.Factory {
        @Override
        public ResourcePack fromUri(final URI uri) throws FileNotFoundException {
            Objects.requireNonNull(uri);
            try {
                Hasher hasher = Hashing.sha1().newHasher();
                try (InputStream in = org.spongepowered.common.resourcepack.SpongeResourcePack.Factory.openStream(uri)) {
                    byte[] buf = new byte[256];
                    while (true) {
                        int read = in.read(buf);
                        if (read <= 0) {
                            break;
                        }
                        hasher.putBytes(buf, 0, read);
                    }
                }
                return SpongeResourcePack.create(uri, hasher.hash().toString());
            } catch (final IOException e) {
                FileNotFoundException ex = new FileNotFoundException(e.toString());
                ex.initCause(e);
                throw ex;
            }
        }

        private static InputStream openStream(final URI uri) throws IOException {
            if (uri.toString().startsWith(SpongeWorldResourcePack.LEVEL_PACK_PROTOCOL)) {
                return Files.newInputStream(SpongeCommon.gameDirectory().resolve(uri.toString().substring(SpongeWorldResourcePack.LEVEL_PACK_PROTOCOL.length
                        ())));
            }
            return uri.toURL().openStream();
        }

        @Override
        public ResourcePack fromUriUnchecked(final URI uri) {
            return SpongeResourcePack.create(Objects.requireNonNull(uri), null);
        }
    }
}
