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
package org.spongepowered.common.network.status;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import org.spongepowered.api.network.status.Favicon;
import org.spongepowered.common.util.Preconditions;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.StringJoiner;

import javax.imageio.ImageIO;

public class SpongeFavicon implements Favicon {

    private final String encoded;
    private final BufferedImage decoded;

    public SpongeFavicon(BufferedImage decoded) throws IOException {
        this.decoded = java.util.Objects.requireNonNull(decoded, "decoded");
        this.encoded = SpongeFavicon.encode(decoded);
    }

    public SpongeFavicon(String encoded) throws IOException {
        this.encoded = java.util.Objects.requireNonNull(encoded, "encoded");
        this.decoded = SpongeFavicon.decode(encoded);
    }

    public String getEncoded() {
        return this.encoded;
    }

    @Override
    public BufferedImage image() {
        return this.decoded;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SpongeFavicon)) {
            return false;
        }

        SpongeFavicon that = (SpongeFavicon) o;
        return Objects.equals(this.encoded, that.encoded);

    }

    @Override
    public int hashCode() {
        return this.encoded.hashCode();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SpongeFavicon.class.getSimpleName() + "[", "]")
                .add("image=" + this.decoded)
                .toString();
    }

    private static final String FAVICON_PREFIX = "data:image/png;base64,";

    private static String encode(BufferedImage favicon) throws IOException {
        Preconditions.checkArgument(favicon.getWidth() == 64, "favicon must be 64 pixels wide");
        Preconditions.checkArgument(favicon.getHeight() == 64, "favicon must be 64 pixels high");

        ByteBuf buf = Unpooled.buffer();
        try {
            ImageIO.write(favicon, "PNG", new ByteBufOutputStream(buf));
            ByteBuf base64 = Base64.encode(buf);
            try {
                return SpongeFavicon.FAVICON_PREFIX + base64.toString(StandardCharsets.UTF_8);
            } finally {
                base64.release();
            }
        } finally {
            buf.release();
        }
    }

    private static BufferedImage decode(String encoded) throws IOException {
        Preconditions.checkArgument(encoded.startsWith(SpongeFavicon.FAVICON_PREFIX), "Unknown favicon format");
        ByteBuf base64 = Unpooled.copiedBuffer(encoded.substring(SpongeFavicon.FAVICON_PREFIX.length()), StandardCharsets.UTF_8);
        try {
            ByteBuf buf = Base64.decode(base64);
            try {
                BufferedImage result = ImageIO.read(new ByteBufInputStream(buf));
                Preconditions.checkState(result.getWidth() == 64, "favicon must be 64 pixels wide");
                Preconditions.checkState(result.getHeight() == 64, "favicon must be 64 pixels high");
                return result;
            } finally {
                buf.release();
            }
        } finally {
            base64.release();
        }
    }

    public static final class FactoryImpl implements Favicon.Factory {

        @Override
        public Favicon load(final String raw) throws IOException {
            return new SpongeFavicon(raw);
        }

        @Override
        public Favicon load(final Path path) throws IOException {
            try (InputStream in = Files.newInputStream(path)) {
                return this.load(in);
            }
        }

        @Override
        public Favicon load(final URL url) throws IOException {
            return this.load(ImageIO.read(url));
        }

        @Override
        public Favicon load(final InputStream in) throws IOException {
            return this.load(ImageIO.read(in));
        }

        @Override
        public Favicon load(final BufferedImage image) throws IOException {
            return new SpongeFavicon(image);
        }
    }
}
