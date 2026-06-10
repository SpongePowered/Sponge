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
package org.spongepowered.neoforge.applaunch.mod.discovery;

import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.moddiscovery.readers.JarModsDotTomlModFileReader;
import net.neoforged.neoforgespi.ILaunchContext;
import net.neoforged.neoforgespi.locating.IDiscoveryPipeline;
import net.neoforged.neoforgespi.locating.IModFile;
import net.neoforged.neoforgespi.locating.IModFileCandidateLocator;
import net.neoforged.neoforgespi.locating.IncompatibleFileReporting;
import net.neoforged.neoforgespi.locating.ModFileDiscoveryAttributes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.common.applaunch.AppLaunch;
import org.spongepowered.common.applaunch.plugin.discovery.PluginDiscovery;
import org.spongepowered.neoforge.applaunch.plugin.NeoPluginPlatform;
import org.spongepowered.neoforge.applaunch.plugin.discovery.JarContentsPluginResource;
import org.spongepowered.neoforge.applaunch.plugin.discovery.NeoPluginDiscovery;

import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public final class SpongeNeoModLocator implements IModFileCandidateLocator {
    private static final Logger LOGGER = LogManager.getLogger();

    private final NeoPluginDiscovery discovery;

    public SpongeNeoModLocator() {
        this.discovery = AppLaunch.bootstrap(NeoPluginPlatform::new).discovery();
        this.discovery.discoverPluginResources();
    }

    @Override
    public void findCandidates(ILaunchContext context, IDiscoveryPipeline pipeline) {
        final ModFileDiscoveryAttributes attributes = ModFileDiscoveryAttributes.DEFAULT.withLocator(this);

        for (final PluginDiscovery.Candidate candidate : this.discovery.candidates()) {
            if (candidate.resource() instanceof JarContentsPluginResource resource) {
                // skip duplicates
                if (!context.addLocated(resource.path())) {
                    candidate.setModFound();
                    candidate.logResult();
                    continue;
                }

                // attempt to load as mod or plugin
                IModFile modFile = null;
                try {
                    modFile = pipeline.readModFile(resource.jar(), attributes);
                } catch (Exception ignored) {}

                if (modFile == null) {
                    // determine fallback type
                    IModFile.Type type = null;
                    switch (candidate.loading()) {
                        case IGNORED:
                            candidate.logResult();
                            continue;
                        case LIBRARY:
                            type = IModFile.Type.LIBRARY;
                            break;
                        case GAME_LIBRARY:
                            type = IModFile.Type.GAMELIBRARY;
                            break;
                    }
                    modFile = IModFile.create(resource.jar(), JarModsDotTomlModFileReader::manifestParser, type, attributes);
                } else if (!candidate.pluginFound()) {
                    candidate.setModFound();
                }

                pipeline.addModFile(modFile);
            }

            // TODO else: Neo can only loads jar mods, do we make a dummy jar?

            candidate.logResult();
        }

        this.discovery.logMetadataWarnings();

        if (!FMLEnvironment.isProduction()) {
            return;
        }

        // Official JiJ cannot be used in a service jar. We have to reimplement it.
        // FML 10 only supports files directly on disk, so we must extract them.
        try {
            final URL rootJar = SpongeNeoModLocator.class.getProtectionDomain().getCodeSource().getLocation();
            final Path cacheDir = FMLPaths.JIJ_CACHEDIR.get();

            try (final FileSystem fs = FileSystems.newFileSystem(new URI("jar:" + rootJar), Map.of())) {
                final Path jarsDir = fs.getPath("jars");

                // Difference with the official JiJ: our checksums are precalculated, to avoid unnecessary IO.
                for (final String line : Files.readAllLines(jarsDir.resolve("checksums.txt"))) {
                    final int i = line.indexOf(' ');
                    final String digest = line.substring(0, i), name = line.substring(i + 1);
                    final Path target = cacheDir.resolve(digest).resolve(name);

                    // FML does not verify the cached file checksum, so neither do we.
                    // The checksum is only there to distinguish different files.
                    if (!Files.exists(target)) {
                        Files.createDirectories(target.getParent());
                        Files.copy(jarsDir.resolve(name), target);
                    }

                    pipeline.addPath(target, attributes, IncompatibleFileReporting.WARN_ALWAYS);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to register embedded mod candidates", e);
        }
    }

    @Override
    public String toString() {
        return "spongeneo";
    }
}
