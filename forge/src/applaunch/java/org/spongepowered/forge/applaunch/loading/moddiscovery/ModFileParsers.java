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
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.fml.loading.moddiscovery.ModJarMetadata;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.forgespi.locating.IModLocator;
import net.minecraftforge.forgespi.locating.ModFileFactory;
import org.spongepowered.common.applaunch.AppLaunch;
import org.spongepowered.common.applaunch.plugin.PluginPlatformConstants;
import org.spongepowered.forge.applaunch.loading.metadata.PluginFileConfigurable;
import org.spongepowered.forge.applaunch.loading.metadata.PluginMetadataUtils;
import org.spongepowered.plugin.metadata.builtin.MetadataContainer;
import org.spongepowered.plugin.metadata.builtin.MetadataParser;

import java.io.Reader;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class ModFileParsers {
    private static Constructor<ModJarMetadata> modJarMetadataConstructor;

    static {
        try {
            ModFileParsers.modJarMetadataConstructor = ModJarMetadata.class.getDeclaredConstructor();
            ModFileParsers.modJarMetadataConstructor.setAccessible(true);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static IModFileInfo parsePluginMetadata(final IModFile iModFile) {
        final ModFile modFile = (ModFile) iModFile;
        AppLaunch.logger().debug("Considering plugin file candidate {}", modFile.getFilePath());

        final Path metadataFile = modFile.findResource(PluginPlatformConstants.METADATA_FILE_LOCATION);
        if (Files.notExists(metadataFile)) {
            AppLaunch.logger().debug("Plugin file '{}' is missing a 'sponge_plugins.json' metadata file in META-INF", modFile);
            return null;
        }

        try {
            final MetadataContainer container;
            try (final Reader reader = Files.newBufferedReader(metadataFile, StandardCharsets.UTF_8)) {
                container = MetadataParser.read(reader);
            }

            final PluginFileConfigurable config = new PluginFileConfigurable(PluginMetadataUtils.fixPluginIds(container));
            return new ModFileInfo(modFile, config, (info) -> {}, List.of());
        } catch (final Exception e) {
            AppLaunch.logger().warn("Could not read metadata for plugin file '{}'", modFile, e);
            return null;
        }
    }

    private static ModJarMetadata newModJarMetadata() {
        try {
            return modJarMetadataConstructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ModFile newPluginInstance(final IModLocator locator, final Path... path) {
        ModJarMetadata mjm = newModJarMetadata();
        ModFile modFile = (ModFile) ModFileFactory.FACTORY.build(SecureJar.from(jar -> mjm, path), locator, ModFileParsers::parsePluginMetadata);
        mjm.setModFile(modFile);
        return modFile;
    }

    private ModFileParsers() {
    }
}
