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
package org.spongepowered.forge.applaunch.discovery;

import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.loading.moddiscovery.ModFileParser;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.forgespi.locating.IModLocator;
import org.apache.commons.lang3.tuple.Pair;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSigner;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.jar.Manifest;

public class SpongeModLocator implements IModLocator {
    // this is run in an isolated classloader, we might have to inject dependencies in the static initializer to be able to interface with plugin-meta/plugin-spi

    @Override
    public List<IModFile> scanMods() {
        return Collections.emptyList();
    }

    @Override
    public String name() {
        return "spongeforge";
    }

    @Override
    public Path findPath(final IModFile modFile, final String... path) {
        return null;
    }

    @Override
    public void scanFile(final IModFile modFile, final Consumer<Path> pathConsumer) {

    }

    @Override
    public Optional<Manifest> findManifest(final Path file) {
        return this.findManifestAndSigners(file).getLeft();
    }

    @Override
    public Pair<Optional<Manifest>, Optional<CodeSigner[]>> findManifestAndSigners(final Path file) {
        return Pair.of(Optional.empty(), Optional.empty());
    }

    @Override
    public void initArguments(final Map<String, ?> arguments) {

    }

    @Override
    public boolean isValid(final IModFile modFile) {
        return false;
    }
}
