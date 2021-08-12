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

import com.electronwill.nightconfig.core.file.FileConfig;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.fml.loading.moddiscovery.NightConfigWrapper;
import net.minecraftforge.forgespi.language.IConfigurable;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.forgespi.locating.IModLocator;
import net.minecraftforge.forgespi.locating.ModFileFactory;
import org.spongepowered.common.applaunch.AppLaunch;
import org.spongepowered.forge.applaunch.loading.metadata.PluginFileConfigurable;
import org.spongepowered.plugin.metadata.PluginMetadata;
import org.spongepowered.plugin.metadata.PluginMetadataContainer;
import org.spongepowered.plugin.metadata.util.PluginMetadataHelper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

public final class ModFileParsers {

    private static Constructor<ModFileInfo> modFileInfoConstructor;
    private static Field modFileInfoField;

    static {
        try {
            ModFileParsers.modFileInfoConstructor = ModFileInfo.class.getDeclaredConstructor(ModFile.class, IConfigurable.class);
            ModFileParsers.modFileInfoField = NightConfigWrapper.class.getDeclaredField("file");
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static IModFileInfo dummySpongeModParser(final String fileName, final IModFile iModFile) {
        final ModFile modFile = (ModFile)iModFile;
        AppLaunch.logger().debug("Considering sponge platform candidate {}", modFile.getFilePath());
        final Path modsjson = modFile.getLocator().findPath(modFile, fileName + ".toml");
        if (!Files.exists(modsjson)) {
            AppLaunch.logger().warn("Sponge platform file '{}' is missing the '{}' file", modFile, fileName + ".toml");
            return null;
        } else {
            final FileConfig fileConfig = FileConfig.builder(modsjson).build();
            fileConfig.load();
            fileConfig.close();
            final NightConfigWrapper configWrapper = new NightConfigWrapper(fileConfig);
            try {
                return ModFileParsers.generateModFileMetadata(modFile, configWrapper);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static IModFileInfo pluginMetadataParser(final String fileName, final IModFile iModFile) {
        final ModFile modFile = (ModFile)iModFile;
        AppLaunch.logger().debug("Considering plugin file candidate {}", modFile.getFilePath());
        final Path metadataFile = modFile.getLocator().findPath(modFile, "META-INF/" + fileName + ".json");
        if (Files.notExists(metadataFile)) {
            AppLaunch.logger().debug("Plugin file '{}' is missing a 'plugins.json' metadata file in META-INF", modFile);
            return null;
        }
        try {
            final Collection<PluginMetadata> metadata = PluginMetadataHelper.builder().build().read(metadataFile);
            final PluginMetadataContainer container = new PluginMetadataContainer(metadata);
            final PluginFileConfigurable configurable = new PluginFileConfigurable(container);

            return ModFileParsers.generateModFileMetadata(modFile, configurable);
        } catch (final Exception e) {
            AppLaunch.logger().warn("Could not read metadata for plugin file '{}'", modFile, e);
            return null;
        }
    }

    private static ModFileInfo generateModFileMetadata(final ModFile file, final IConfigurable configurable) throws Exception {
        ModFileParsers.modFileInfoConstructor.setAccessible(true);
        final ModFileInfo modFileInfo = ModFileParsers.modFileInfoConstructor.newInstance(file, configurable);
        ModFileParsers.modFileInfoConstructor.setAccessible(false);
        if (configurable instanceof NightConfigWrapper) {
            ModFileParsers.modFileInfoField.setAccessible(true);
            ModFileParsers.modFileInfoField.set(configurable, modFileInfo);
            ModFileParsers.modFileInfoField.setAccessible(false);
        }

        return modFileInfo;
    }

    public static ModFile newPluginInstance(final Path path, final IModLocator locator, final String fileName) {
        return (ModFile) ModFileFactory.FACTORY.build(path, locator, file -> ModFileParsers.pluginMetadataParser(fileName, file));
    }

    private ModFileParsers() {
    }
}
