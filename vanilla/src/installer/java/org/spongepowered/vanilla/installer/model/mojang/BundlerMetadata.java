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
package org.spongepowered.vanilla.installer.model.mojang;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Bundler metadata
 *
 * @param version The bundler format version used by this jar
 * @param libraries Libraries packed in the jar as dependencies for the server
 * @param server A bundle element describing the server itself
 * @param mainClass The declared main class of the bundler jar
 */
public record BundlerMetadata(
    FormatVersion version,
    Set<BundleElement> libraries,
    BundleElement server,
    @Nullable String mainClass
) {
    private static final String MAIN_CLASS = "META-INF/main-class";

    public BundlerMetadata {
        libraries = Set.copyOf(libraries);
    }

    /**
     * Attempt to read bundler metadata from a jar.
     *
     * <p>If the jar is not a Minecraft bundler jar, an empty {@link Optional} will
     * be returned.</p>
     *
     * @param jar the jar to read
     * @return parsed metadata
     * @throws IOException if an error occurs while trying to read from the jar
     */
    public static Optional<BundlerMetadata> read(final Path jar) throws IOException {
        try (final JarFile file = new JarFile(jar.toFile())) {
            return BundlerMetadata.read(file);
        }
    }

    /**
     * Attempt to read bundler metadata from a jar.
     *
     * <p>If the jar is not a Minecraft bundler jar, an empty {@link Optional} will
     * be returned.</p>
     *
     * @param file the jar to read
     * @return parsed metadata
     * @throws IOException if an error occurs while trying to read from the jar
     */
    public static Optional<BundlerMetadata> read(final JarFile file) throws IOException {
        final Manifest manifest = file.getManifest();
        final @Nullable String formatVersion = manifest.getMainAttributes().getValue(FormatVersion.MANIFEST_ATTRIBUTE);
        if (formatVersion == null) {
            return Optional.empty();
        }

        final FormatVersion parsed = FormatVersion.parse(formatVersion);

        // load information:
        // server jar
        final BundleElement serverJar;
        try (final Stream<BundleElement> stream = BundlerMetadata.readIndex(file, "versions")) {
            serverJar = stream.findFirst()
                .orElse(null);
        }

        if (serverJar == null) {
            throw new IllegalArgumentException("Missing server jar from versions list");
        }

        // libraries list
        final Set<BundleElement> libraries;
        try (final Stream<BundleElement> elements = BundlerMetadata.readIndex(file, "libraries")) {
            libraries = elements.collect(Collectors.toUnmodifiableSet());
        }

        // main class
        final JarEntry mainClassEntry = file.getJarEntry(BundlerMetadata.MAIN_CLASS);
        if (mainClassEntry == null) {
            throw new IllegalArgumentException("Missing main class entry in bundle");
        }

        final String mainClass;
        try (final BufferedReader read = new BufferedReader(new InputStreamReader(file.getInputStream(mainClassEntry), StandardCharsets.UTF_8))) {
            mainClass = read.readLine();
        }

        return Optional.of(new BundlerMetadata(parsed, libraries, serverJar, mainClass));
    }

    static Stream<BundleElement> readIndex(final JarFile jar, final String index) throws IOException {
        final @Nullable JarEntry entry = jar.getJarEntry("META-INF/" + index + ".list");
        if (entry == null) {
            return Stream.empty();
        }

        return BundlerMetadata.readIndex(index, jar.getInputStream(entry));
    }

    public static Stream<BundleElement> readIndex(final String indexName, final InputStream in) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        return reader.lines()
            .map(x -> x.split("\t"))
            .map(line -> new BundleElement(line[0], line[1], "META-INF/" + indexName + "/" + line[2]))
            .onClose(() -> {
                try {
                    reader.close();
                } catch (final IOException ex) {
                    throw new RuntimeException(ex);
                }
            });

    }

}
