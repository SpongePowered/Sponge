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
import net.neoforged.fml.loading.moddiscovery.ModFile;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;
import net.neoforged.fml.loading.moddiscovery.ModJarMetadata;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.locating.IModFile;
import net.neoforged.neoforgespi.locating.IModFileReader;
import net.neoforged.neoforgespi.locating.InvalidModFileException;
import net.neoforged.neoforgespi.locating.ModFileDiscoveryAttributes;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.common.applaunch.AppLaunch;
import org.spongepowered.common.applaunch.plugin.discovery.PluginDiscovery;
import org.spongepowered.neoforge.applaunch.mod.metadata.PluginFileConfigurable;
import org.spongepowered.neoforge.applaunch.plugin.discovery.JarContentsPluginResource;
import org.spongepowered.plugin.discovery.PluginResource;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class PluginFileReader implements IModFileReader {
    @Override
    public @Nullable IModFile read(final JarContents jar, final ModFileDiscoveryAttributes attributes) {
        final PluginResource resource = new JarContentsPluginResource(jar);
        final PluginDiscovery.Candidate candidate = AppLaunch.pluginPlatform().discovery().candidate(resource);
        candidate.readMetadata();
        if (!candidate.pluginFound()) {
            return null;
        }
        return PluginFileReader.newPluginFile(jar, candidate, attributes);
    }

    @Override
    public String toString() {
        return "sponge manifest";
    }

    static IModFile newPluginFile(final JarContents jar, final PluginDiscovery.Candidate candidate, final ModFileDiscoveryAttributes attributes) {
        final PluginFileConfigurable config = new PluginFileConfigurable(candidate.resource(), candidate.metadata());
        final ModJarMetadata mjm = new ModJarMetadata();
        final IModFile modFile = new ModFile(jar, mjm, file -> newModFileInfo(file, config), attributes);
        mjm.setModFile(modFile);
        return modFile;
    }

    private static IModFileInfo newModFileInfo(final IModFile file, final PluginFileConfigurable config) throws InvalidModFileException {
        return new ModFileInfo((ModFile) file, config, (info) -> {}, List.of());
    }
}
