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

import com.google.common.collect.ImmutableMap;
import net.minecraftforge.fml.loading.FMLEnvironment;
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

import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.*;

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

        for (final PluginDiscovery.Candidate candidate : this.discovery.candidates()) {
            if (candidate.resource() instanceof SecureJarPluginResource resource) {
                candidate.readMetadata();

                // attempt to load as mod or plugin
                IModFile modFile;
                if (candidate.pluginFound()) {
                    modFile = PluginFileParser.newPluginFile(this, candidate);
                } else {
                    modFile = PluginFileParser.newModFile(this, resource);
                    if (modFile != null) {
                        candidate.setModFound();
                    }
                }

                if (modFile == null) {
                    // determine fallback type
                    switch (candidate.loading()) {
                        case IGNORED:
                            candidate.logResult();
                            continue;
                        case LIBRARY:
                            modFile = PluginFileParser.newLibraryFile(this, false, resource.jar());
                            break;
                        case GAME_LIBRARY:
                            modFile = PluginFileParser.newLibraryFile(this, true, resource.jar());
                            break;
                    }
                }

                mods.add(new ModFileOrException(modFile, null));
            }

            // TODO else: Forge can only loads jar mods, do we make a dummy jar?

            candidate.logResult();
        }

        this.discovery.logMetadataWarnings();

        if (!FMLEnvironment.production) {
            return mods;
        }

        try {
            URL rootJar = SpongeForgeModLocator.class.getProtectionDomain().getCodeSource().getLocation();
            FileSystem fs = FileSystems.getFileSystem(rootJar.toURI()); // FML has already opened a file system for this jar
            Files.list(fs.getPath("jars"))
                .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar"))
                .map(path -> {
                    try {
                        URI jij = new URI("jij:" + path.toAbsolutePath().toUri().getRawSchemeSpecificPart()).normalize();
                        final Map<String, ?> env = ImmutableMap.of("packagePath", path);
                        FileSystem jijFS = FileSystems.newFileSystem(jij, env);
                        return jijFS.getPath("/"); // root of the archive to load
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .map((path) -> PluginFileParser.newModFile(this, path))
                .filter(Objects::nonNull)
                .map((modFile -> new ModFileOrException(modFile, null)))
                .forEach(mods::add);
        } catch (Exception e) {
            LOGGER.error("Failed to scan embedded mod candidates", e);
        }

        return mods;
    }
}
