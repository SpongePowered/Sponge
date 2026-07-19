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

import net.neoforged.fml.jarcontents.JarContents;
import net.neoforged.fml.loading.moddiscovery.readers.JarModsDotTomlModFileReader;
import net.neoforged.neoforgespi.ILaunchContext;
import net.neoforged.neoforgespi.locating.IDiscoveryPipeline;
import net.neoforged.neoforgespi.locating.IModFile;
import net.neoforged.neoforgespi.locating.IModFileCandidateLocator;
import net.neoforged.neoforgespi.locating.ModFileDiscoveryAttributes;
import org.spongepowered.common.applaunch.AppLaunch;
import org.spongepowered.common.applaunch.plugin.discovery.PluginDiscovery;
import org.spongepowered.neoforge.applaunch.plugin.NeoPluginPlatform;
import org.spongepowered.neoforge.applaunch.plugin.discovery.JarContentsPluginResource;
import org.spongepowered.neoforge.applaunch.plugin.discovery.NeoPluginDiscovery;

import java.nio.file.Path;

public final class SpongeNeoModLocator implements IModFileCandidateLocator {
    private final NeoPluginDiscovery discovery;

    public SpongeNeoModLocator() {
        this.discovery = AppLaunch.bootstrap(NeoPluginPlatform::new).discovery();
        this.discovery.discoverPluginResources();
    }

    @Override
    public void findCandidates(ILaunchContext context, IDiscoveryPipeline pipeline) {
        final ModFileDiscoveryAttributes attributes = ModFileDiscoveryAttributes.DEFAULT.withLocator(this);
        int virtualJarCount = 0;

        for (final PluginDiscovery.Candidate candidate : this.discovery.candidates()) {
            if (candidate.resource() instanceof JarContentsPluginResource resource) {
                // skip duplicates
                if (resource.paths().stream().anyMatch(context::isLocated)) {
                    candidate.setModFound();
                    candidate.logResult();
                    continue;
                }
                resource.paths().forEach(context::addLocated);

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
            } else {
                candidate.readMetadata();
                if (!candidate.pluginFound()) {
                    candidate.logResult();
                    continue;
                }

                // This jar is only used as an identifier and does not need to actually exist
                final JarContents jar = JarContents.empty(Path.of("sponge_resource_" + virtualJarCount++));

                pipeline.addModFile(PluginFileReader.newPluginFile(jar, candidate, attributes));
            }

            candidate.logResult();
        }

        this.discovery.logMetadataWarnings();
    }

    @Override
    public String toString() {
        return "spongeneo";
    }
}
