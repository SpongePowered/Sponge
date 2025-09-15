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

import org.spongepowered.common.applaunch.config.LaunchConfig;
import org.spongepowered.common.applaunch.config.TokenReplacement;
import org.spongepowered.libs.LibraryManager;
import org.spongepowered.vanilla.installer.library.TinyLogger;

import java.io.IOException;
import java.nio.file.Path;

public final class Installer {
    private final Path directory;
    private final LibraryManager libraryManager;
    private final LaunchConfig config;

    public Installer(final Path directory) throws IOException {
        this.directory = directory;
        this.config = LaunchConfig.load(directory, true);

        final TokenReplacement tokens = new TokenReplacement();
        tokens.register("BASE_DIR", directory);

        this.libraryManager = new LibraryManager(
            TinyLogger.INSTANCE,
            this.config.checkLibraryHashes(),
            Path.of(tokens.replace(this.config.librariesDirectory())),
            this.getClass().getResource("/sponge-libraries.json")
        );
    }

    public Path getDirectory() {
        return this.directory;
    }

    public LibraryManager getLibraryManager() {
        return this.libraryManager;
    }

    public LaunchConfig getConfig() {
        return this.config;
    }
}
