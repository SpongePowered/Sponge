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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.spongepowered.api.resourcepack.ResourcePack;
import org.spongepowered.api.resourcepack.ResourcePackFactory;
import org.spongepowered.common.SpongeImpl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;

public final class SpongeResourcePackFactory implements ResourcePackFactory {

    @Override
    public ResourcePack fromUri(URI uri) throws FileNotFoundException {
        checkNotNull(uri, "uri");
        try {
            Hasher hasher = Hashing.sha1().newHasher();
            try (InputStream in = openStream(uri)) {
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
        } catch (IOException e) {
            FileNotFoundException ex = new FileNotFoundException(e.toString());
            ex.initCause(e);
            throw ex;
        }
    }

    private static InputStream openStream(URI uri) throws IOException {
        if (uri.toString().startsWith(SpongeWorldResourcePack.LEVEL_PACK_PROTOCOL)) {
            return Files.newInputStream(SpongeImpl.getGameDir().resolve(uri.toString().substring(SpongeWorldResourcePack.LEVEL_PACK_PROTOCOL.length
                    ())));
        }
        return uri.toURL().openStream();
    }

    @Override
    public ResourcePack fromUriUnchecked(URI uri) {
        return SpongeResourcePack.create(checkNotNull(uri, "uri"), null);
    }

}
