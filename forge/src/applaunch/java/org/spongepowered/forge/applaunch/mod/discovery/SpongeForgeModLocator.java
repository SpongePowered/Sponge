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

import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.forgespi.locating.IModLocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.spongepowered.common.applaunch.AppLaunch;
import org.spongepowered.common.applaunch.plugin.discovery.PluginDiscovery;
import org.spongepowered.forge.applaunch.plugin.ForgePluginPlatform;
import org.spongepowered.forge.applaunch.plugin.discovery.ForgePluginDiscovery;
import org.spongepowered.forge.applaunch.plugin.discovery.SecureJarPluginResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarOutputStream;

public final class SpongeForgeModLocator extends AbstractModProvider implements IModLocator {
    private static final Logger LOGGER = LogManager.getLogger();

    private @MonotonicNonNull ForgePluginDiscovery discovery;

    @Override
    public String name() {
        return "spongeforge";
    }

    @Override
    public void initArguments(Map<String, ?> arguments) {
        this.discovery = AppLaunch.bootstrap(ForgePluginPlatform::new).discovery();
    }

    @Override
    public List<ModFileOrException> scanMods() {
        this.discovery.discoverPluginResources();

        final List<ModFileOrException> mods = new ArrayList<>();

        Path virtualDir = null;
        int virtualJarCount = 0;

        for (final PluginDiscovery.Candidate candidate : this.discovery.candidates()) {
            IModFile modFile;
            if (candidate.resource() instanceof SecureJarPluginResource resource) {
                candidate.readMetadata();

                // attempt to load as mod or plugin
                if (candidate.pluginFound()) {
                    modFile = PluginFileParser.newJarPluginFile(this, candidate);
                } else {
                    modFile = PluginFileParser.newModFile(this, resource);
                    if (modFile != null) {
                        candidate.setModFound();
                    }
                }

                if (modFile == null) {
                    // fallback
                    if (!candidate.gameResource()) {
                        candidate.logResult();
                        continue;
                    }
                    modFile = PluginFileParser.newGameLibraryFile(this, resource.jar());
                }
            } else {
                candidate.readMetadata();
                if (!candidate.pluginFound()) {
                    candidate.logResult();
                    continue;
                }

                // Forge requires each mod to have an existing file, so we generate one
                final Path path;
                try {
                    if (virtualDir == null) {
                        virtualDir = Files.createTempDirectory("sponge_resources_");
                    }
                    path = virtualDir.resolve(virtualJarCount++ + ".jar");

                    // an empty jar is enough
                    new JarOutputStream(Files.newOutputStream(path)).close();
                } catch (IOException e) {
                    LOGGER.error("Failed to create virtual jar for resource {}", candidate.resource(), e);
                    continue;
                }

                LOGGER.debug("Using temporary jar {} for resource {}", path, candidate.resource());
                modFile = PluginFileParser.newNonJarPluginFile(this, candidate, path);
            }

            mods.add(new ModFileOrException(modFile, null));
            candidate.logResult();
        }

        this.discovery.logMetadataWarnings();

        return mods;
    }
}
