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
package org.spongepowered.vanilla.installer;

import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;

public final class InstallerUtils {

    // From http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
    private static final char[] hexArray = "0123456789abcdef".toCharArray();

    public static String toHexString(final byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Downloads a file.
     *
     * @param url The file URL
     * @param path The local path
     * @throws IOException If there is a problem while downloading the file
     */
    public static void download(final Logger logger, final URL url, final Path path, final boolean requiresRequest) throws IOException {
        Files.createDirectories(path.getParent());

        final String name = path.getFileName().toString();

        logger.info("Downloading {}. This may take a while...", name);
        logger.info("URL -> <{}>", url);

        if (!requiresRequest) {
            try (final ReadableByteChannel in = Channels.newChannel(url.openStream()); final FileChannel out = FileChannel.open(path,
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                out.transferFrom(in, 0, Long.MAX_VALUE);
            }
        } else {
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "Sponge-Downloader");

            connection.connect();

            try (final ReadableByteChannel in = Channels.newChannel(connection.getInputStream()); final FileChannel out = FileChannel.open(path,
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                out.transferFrom(in, 0, Long.MAX_VALUE);
            }
        }
    }

    /**
     * Downloads a file and verify its digest.
     *
     * @param url The file URL
     * @param path The local path
     * @param expected The SHA-1 expected digest
     * @throws IOException If there is a problem while downloading the file
     */
    public static void downloadCheckHash(final Logger logger, final URL url, final Path path, final MessageDigest digest, final String expected,
        boolean requiresRequest) throws IOException {
        Files.createDirectories(path.getParent());

        final String name = path.getFileName().toString();

        logger.info("Downloading {}. This may take a while...", name);
        logger.info("URL -> <{}>", url);

        if (!requiresRequest) {
            // Pipe the download stream into the file and compute the hash
            try (final DigestInputStream stream = new DigestInputStream(url.openStream(), digest); final ReadableByteChannel in = Channels
                .newChannel(stream); final FileChannel out = FileChannel.open(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                out.transferFrom(in, 0, Long.MAX_VALUE);
            }
        } else {
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent", "Sponge-Downloader");

            connection.connect();

            // Pipe the download stream into the file and compute the hash
            try (final DigestInputStream stream = new DigestInputStream(connection.getInputStream(), digest); final ReadableByteChannel in = Channels
                .newChannel(stream); final FileChannel out = FileChannel.open(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                out.transferFrom(in, 0, Long.MAX_VALUE);
            }
        }

        final String fileSha1 = InstallerUtils.toHexString(digest.digest());

        if (LauncherCommandLine.checkMinecraftJarHash) {
            if (expected.equalsIgnoreCase(fileSha1)) {
                logger.info("Successfully downloaded {} and verified checksum!", name);
            } else {
                Files.delete(path);
                throw new IOException(String.format("Checksum verification failed: Expected '%s', got '%s'.", expected, fileSha1));
            }
        } else {
            logger.info("Skipping hash check as that is turned off...");
        }
    }

    private InstallerUtils() {
    }
}
