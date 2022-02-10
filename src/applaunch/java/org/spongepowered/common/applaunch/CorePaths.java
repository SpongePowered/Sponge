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
package org.spongepowered.common.applaunch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Core directory structure of the Sponge platform
 *
 * <base_directory>
 *  - plugins
 *  - plugin-configs
 *  - sponge
 *    - configs
 *      - worlds
 *    - libraries
 */
public final class CorePaths {

    private final Path baseDirectory;
    private final Path spongeDirectory;
    private final Path pluginsDirectory;
    private final Path pluginConfigsDirectory;
    private final Path spongeConfigsDirectory;
    private final Path spongeConfigWorldsDirectory;
    private final Path spongeLibrariesDirectory;

    // TODO Determine how much we want to do for CLI args here...unsure if I want to allow any of these (besides the base directory) to be changed

    public CorePaths(final Path baseDirectory) {
        this.baseDirectory = baseDirectory;
        this.pluginsDirectory = baseDirectory.resolve("plugins");
        this.pluginConfigsDirectory = baseDirectory.resolve("plugin-configs");
        this.spongeDirectory = baseDirectory.resolve("sponge");
        this.spongeConfigsDirectory = this.spongeDirectory.resolve("configs");
        this.spongeConfigWorldsDirectory = this.spongeConfigsDirectory.resolve("worlds");
        this.spongeLibrariesDirectory = this.spongeDirectory.resolve("libraries");

        // Now create all the directories immediately
        this.createDirectory(this.baseDirectory);
        this.createDirectory(this.pluginsDirectory);
        this.createDirectory(this.pluginConfigsDirectory);
        this.createDirectory(this.spongeDirectory);
        this.createDirectory(this.spongeConfigsDirectory);
        this.createDirectory(this.spongeConfigWorldsDirectory);
        this.createDirectory(this.spongeLibrariesDirectory);
    }

    public Path baseDirectory() {
        return this.baseDirectory;
    }

    public Path pluginsDirectory() {
        return this.pluginsDirectory;
    }

    public Path pluginConfigsDirectory() {
        return this.pluginConfigsDirectory;
    }

    public Path spongeDirectory() {
        return this.spongeDirectory;
    }

    public Path spongeConfigsDirectory() {
        return this.spongeConfigsDirectory;
    }

    public Path spongeConfigWorldsDirectory() {
        return this.spongeConfigWorldsDirectory;
    }

    public Path spongeLibrariesDirectory() {
        return this.spongeLibrariesDirectory;
    }

    private void createDirectory(final Path path) {
        if (Files.notExists(path)) {
            try {
                Files.createDirectories(path);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
