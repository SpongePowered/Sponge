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
package org.spongepowered.forge.applaunch.loading.moddiscovery;

import cpw.mods.jarhandling.SecureJar;
import cpw.mods.modlauncher.Environment;
import cpw.mods.modlauncher.Launcher;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.moddiscovery.AbstractJarFileModProvider;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.forgespi.locating.IModLocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.forge.applaunch.loading.moddiscovery.library.LibraryManager;
import org.spongepowered.forge.applaunch.loading.moddiscovery.library.LibraryModFileFactory;
import org.spongepowered.forge.applaunch.loading.moddiscovery.library.LibraryModFileInfoParser;
import org.spongepowered.forge.applaunch.plugin.ForgePluginPlatform;
import org.spongepowered.forge.applaunch.service.ForgeProductionBootstrap;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// works with ForgeProductionBootstrap to make this whole thing go
public final class ForgeBootstrap extends AbstractJarFileModProvider implements IModLocator {

    private static final Logger LOGGER = LogManager.getLogger();

    private LibraryManager libraryManager;

    private final Set<IModFile> modFiles = ConcurrentHashMap.newKeySet();

    @Override
    public List<ModFileOrException> scanMods() {
        final List<ModFileOrException> jars = new ArrayList<>();

        // Add Sponge-specific libraries
        if (FMLEnvironment.production) {
            try {
                this.libraryManager.validate();
            } catch (final Exception ex) {
                throw new RuntimeException("Failed to download and validate Sponge libraries", ex); // todo: more specific?
            }
            this.libraryManager.finishedProcessing();
            for (final LibraryManager.Library library : this.libraryManager.getAll().values()) {
                final Path path = library.getFile();
                ForgeBootstrap.LOGGER.debug("Adding jar {} to classpath as a library", path);
                final IModFile file = LibraryModFileFactory.INSTANCE.build(SecureJar.from(path), this, LibraryModFileInfoParser.INSTANCE);
                this.modFiles.add(file);
                jars.add(new ModFileOrException(file, null));
            }
        }

        return jars;
    }

    @Override
    public String name() {
        return "spongeforge";
    }

    @Override
    public boolean isValid(final IModFile modFile) {
        return this.modFiles.contains(modFile);
    }

    @Override
    public void initArguments(final Map<String, ?> arguments) {
        final Environment env = Launcher.INSTANCE.environment();
        ForgePluginPlatform.bootstrap(env);
        this.libraryManager = new LibraryManager(
            env.getProperty(ForgeProductionBootstrap.Keys.CHECK_LIBRARY_HASHES.get()).orElse(true),
            env.getProperty(ForgeProductionBootstrap.Keys.LIBRARIES_DIRECTORY.get())
                .orElseThrow(() -> new IllegalStateException("no libraries available")),
            ForgeBootstrap.class.getResource("libraries.json")
        );
    }
}
