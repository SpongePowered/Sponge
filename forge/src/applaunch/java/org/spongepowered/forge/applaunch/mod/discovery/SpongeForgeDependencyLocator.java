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
package org.spongepowered.forge.applaunch.mod.discovery;

import cpw.mods.jarhandling.SecureJar;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.forgespi.locating.IDependencyLocator;
import net.minecraftforge.forgespi.locating.IModFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.common.applaunch.AppLaunch;
import org.spongepowered.common.applaunch.plugin.PluginPlatform;
import org.spongepowered.forge.applaunch.mod.discovery.library.Log4JLogger;
import org.spongepowered.libs.LibraryManager;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SpongeForgeDependencyLocator extends AbstractModProvider implements IDependencyLocator {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public List<IModFile> scanMods(final Iterable<IModFile> loadedMods) {
        final List<IModFile> modFiles = new ArrayList<>();

        // Add Sponge-specific libraries
        if (FMLEnvironment.production) {
            final PluginPlatform platform = AppLaunch.pluginPlatform();
            final LibraryManager libraryManager = new LibraryManager(
                new Log4JLogger(LogManager.getLogger(LibraryManager.class)),
                platform.config().checkLibraryHashes(),
                Path.of(platform.tokens().replace(platform.config().librariesDirectory())),
                SpongeForgeModLocator.class.getResource("/sponge-libraries.json")
            );

            try {
                libraryManager.validate();
            } catch (final Exception ex) {
                throw new RuntimeException("Failed to download and validate Sponge libraries", ex);
            }
            libraryManager.finishedProcessing();

            for (final LibraryManager.Library library : libraryManager.getAll("main")) {
                final Path path = library.file();
                SpongeForgeDependencyLocator.LOGGER.debug("Proposing jar {} as a game library", path);
                modFiles.add(PluginFileParser.newLibraryFile(this, true, SecureJar.from(path)));
            }
        }

        return modFiles;
    }

    @Override
    public String name() {
        return "spongeforge";
    }
}
