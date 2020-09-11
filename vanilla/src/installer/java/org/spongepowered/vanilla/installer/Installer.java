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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Installer {

    private static Installer INSTANCE;

    private final Logger logger;
    private final Path directory;
    private final LibraryManager libraryManager;
    private final Gson gson;
    private final LauncherConfig config;

    public static Installer getInstance() {
        return Installer.INSTANCE;
    }

    public Installer(final Logger logger, final Path directory) throws IOException {
        Installer.INSTANCE = this;

        this.logger = logger;
        this.directory = directory;
        final Path launcherConfigFile = this.directory.resolve("launcher.conf");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        if (Files.notExists(launcherConfigFile)) {
            this.config = new LauncherConfig();
            this.saveConfig(launcherConfigFile);
        } else {
            this.config = this.loadConfig(launcherConfigFile);
        }
        this.libraryManager = new LibraryManager(this, Paths.get(this.config.librariesDirectory.replace("${BASE_DIRECTORY}",
            directory.toAbsolutePath().toString())));
    }

    private LauncherConfig loadConfig(final Path configFile) throws IOException {
        try (final JsonReader reader = new JsonReader(new InputStreamReader(Files.newInputStream(configFile)))) {
            return this.gson.fromJson(reader, LauncherConfig.class);
        }
    }

    private void saveConfig(final Path configFile) throws IOException {
        try (final Writer writer = Files.newBufferedWriter(configFile, StandardCharsets.UTF_8)) {
            this.gson.toJson(this.config, writer);
        }
    }

    public Logger getLogger() {
        return this.logger;
    }

    public Path getDirectory() {
        return this.directory;
    }

    public LibraryManager getLibraryManager() {
        return this.libraryManager;
    }

    public LauncherConfig getLauncherConfig() {
        return this.config;
    }
}
