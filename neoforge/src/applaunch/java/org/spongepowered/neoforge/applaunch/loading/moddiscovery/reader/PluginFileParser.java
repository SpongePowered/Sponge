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
package org.spongepowered.neoforge.applaunch.loading.moddiscovery.reader;

import cpw.mods.jarhandling.JarContents;
import cpw.mods.jarhandling.SecureJar;
import net.neoforged.fml.loading.moddiscovery.ModFile;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;
import net.neoforged.fml.loading.moddiscovery.ModJarMetadata;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.locating.IModFile;
import net.neoforged.neoforgespi.locating.InvalidModFileException;
import net.neoforged.neoforgespi.locating.ModFileDiscoveryAttributes;
import org.spongepowered.common.applaunch.AppLaunch;
import org.spongepowered.common.applaunch.metadata.PluginMetadataFixer;
import org.spongepowered.common.applaunch.plugin.PluginPlatformConstants;
import org.spongepowered.neoforge.applaunch.loading.metadata.PluginFileConfigurable;
import org.spongepowered.plugin.metadata.builtin.MetadataContainer;
import org.spongepowered.plugin.metadata.builtin.MetadataParser;

import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public final class PluginFileParser {

    @SuppressWarnings("UnstableApiUsage")
    private static IModFileInfo parsePluginMetadata(final IModFile modFile) throws InvalidModFileException {
        final Path metadataFile = modFile.findResource(PluginPlatformConstants.METADATA_FILE_LOCATION);

        try {
            final MetadataContainer container;
            try (final Reader reader = Files.newBufferedReader(metadataFile, StandardCharsets.UTF_8)) {
                container = MetadataParser.read(reader);
            }

            final PluginFileConfigurable config = new PluginFileConfigurable(PluginMetadataFixer.fixPluginIds(container));
            return new ModFileInfo((ModFile) modFile, config, (info) -> {}, List.of());
        } catch (final Exception e) {
            AppLaunch.logger().warn("Could not read metadata for plugin file '{}'", modFile, e);
            throw new InvalidModFileException("Could not read plugin metadata", modFile.getModFileInfo());
        }
    }

    public static IModFile newPluginInstance(final JarContents contents, final ModFileDiscoveryAttributes attributes) {
        AppLaunch.logger().debug("Considering plugin file candidate {}", contents.getPrimaryPath());

        final Optional<URI> metadataFile = contents.findFile(PluginPlatformConstants.METADATA_FILE_LOCATION);
        if (metadataFile.isEmpty()) {
            AppLaunch.logger().debug("Plugin file '{}' is missing a 'sponge_plugins.json' metadata file in META-INF", contents.getPrimaryPath());
            return null;
        }

        final ModJarMetadata mjm = new ModJarMetadata(contents);
        final IModFile modFile = IModFile.create(SecureJar.from(contents, mjm), PluginFileParser::parsePluginMetadata, attributes);
        mjm.setModFile(modFile);
        return modFile;
    }

    private PluginFileParser() {
    }
}
