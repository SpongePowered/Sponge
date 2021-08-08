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
package org.spongepowered.forge.applaunch.loading.moddiscovery.locator;

import cpw.mods.modlauncher.api.LamdbaExceptionUtils;
import net.minecraftforge.fml.loading.LogMarkers;
import net.minecraftforge.fml.loading.ModDirTransformerDiscoverer;
import net.minecraftforge.fml.loading.StringUtils;
import net.minecraftforge.fml.loading.moddiscovery.AbstractJarFileLocator;
import net.minecraftforge.forgespi.locating.IModFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.common.applaunch.AppLaunch;
import org.spongepowered.forge.applaunch.loading.moddiscovery.ModFileParsers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class PluginsFolderLocator extends AbstractJarFileLocator {
    private static final Logger LOGGER = LogManager.getLogger();

    public List<IModFile> scanMods() {
        final List<Path> pluginDirectories = AppLaunch.pluginPlatform().pluginDirectories();

        final List<IModFile> modFiles = new ArrayList<>();

        for (final Path pluginDirectory : pluginDirectories) {
            PluginsFolderLocator.LOGGER.debug(LogMarkers.SCAN, "Scanning plugins directory '{}' for plugins", pluginDirectory);
            modFiles.addAll(this.scanForModsIn(pluginDirectory));
        }

        return modFiles;
    }

    private List<IModFile> scanForModsIn(final Path pluginsDirectory) {
        List<Path> excluded = ModDirTransformerDiscoverer.allExcluded();
        return LamdbaExceptionUtils.uncheck(() ->
                Files.list(pluginsDirectory)).filter((p) ->
                !excluded.contains(p)).sorted(Comparator.comparing((path) ->
                StringUtils.toLowerCase(path.getFileName().toString()))).filter((p) ->
                StringUtils.toLowerCase(p.getFileName().toString()).endsWith(".jar")).map((p) ->
                ModFileParsers.newPluginInstance(p, this, "plugins")).peek((f) ->
                this.modJars.compute(f, (mf, fs) -> this.createFileSystem(mf))).collect(Collectors.toList());
    }

    public String name() {
        return "plugins directory";
    }

    public void initArguments(final Map<String, ?> arguments) {
    }
}
