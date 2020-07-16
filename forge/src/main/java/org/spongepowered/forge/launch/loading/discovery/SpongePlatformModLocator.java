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
package org.spongepowered.forge.launch.loading.discovery;

import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.forgespi.locating.IModLocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.jar.Manifest;

/**
 * The {@link IModLocator mod locator} to find the Sponge platform implementation
 * plugin descriptors to be feed into FML.
 *
 * @author Jamie Mansfield
 */
public class SpongePlatformModLocator implements IModLocator {

    private final static String NAME = "sponge platform locator";

    private static final Logger log = LogManager.getLogger();

    @Override
    public List<IModFile> scanMods() {
        final List<IModFile> mods = new ArrayList<>();
        return mods;
    }

    @Override
    public String name() {
        return NAME;
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
        return Optional.empty();
    }

    @Override
    public void initArguments(final Map<String, ?> arguments) {
    }

    @Override
    public boolean isValid(final IModFile modFile) {
        return true;
    }

}
