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

import cpw.mods.jarhandling.JarMetadata;
import cpw.mods.jarhandling.SecureJar;
import net.minecraftforge.fml.loading.moddiscovery.AbstractModProvider;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.fml.loading.moddiscovery.ModFileParser;
import net.minecraftforge.fml.loading.moddiscovery.ModJarMetadata;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.forgespi.locating.IModProvider;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.common.applaunch.plugin.discovery.PluginDiscovery;
import org.spongepowered.forge.applaunch.mod.metadata.PluginFileConfigurable;
import org.spongepowered.forge.applaunch.plugin.discovery.PluginJarMetadata;
import org.spongepowered.forge.applaunch.plugin.discovery.SecureJarPluginResource;

import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public final class PluginFileParser {
    private static final String MODS_TOML   = "META-INF/mods.toml";
    private static final String MODULE_INFO = "module-info.class";

    private static Constructor<ModJarMetadata> modJarMetadataConstructor;

    static {
        try {
            PluginFileParser.modJarMetadataConstructor = ModJarMetadata.class.getDeclaredConstructor();
            PluginFileParser.modJarMetadataConstructor.setAccessible(true);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static IModFileInfo parsePluginFileInfo(final IModFile file, final PluginFileConfigurable config) {
        return new ModFileInfo((ModFile) file, config, (info) -> {}, List.of());
    }

    private static IModFileInfo parseModFileInfo(final IModFile file) {
        return ModFileParser.modsTomlParser(file);
    }

    private static IModFileInfo parseLibraryFileInfo(final IModFile file) {
        return DummyModProvider.INSTANCE.manifestParser(file);
    }

    private static ModJarMetadata newModJarMetadata() {
        try {
            return modJarMetadataConstructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean useModJarMetadata(final SecureJar jar) {
        final SecureJar.ModuleDataProvider data = jar.moduleDataProvider();
        if (data.findFile(PluginFileParser.MODULE_INFO).isPresent()) {
            return false;
        }
        return data.findFile(PluginFileParser.MODS_TOML).isPresent();
    }

    public static ModFile newJarPluginFile(final IModProvider provider, final PluginDiscovery.Candidate candidate) {
        final SecureJarPluginResource resource = (SecureJarPluginResource) candidate.resource();
        return PluginFileParser.newPluginFile(provider, candidate, resource.pluginJar(candidate.metadata().getFirst()));
    }

    public static ModFile newNonJarPluginFile(final IModProvider provider, final PluginDiscovery.Candidate candidate, final Path tempPath) {
        final SecureJar jar = SecureJar.from(j -> new PluginJarMetadata(j, candidate.metadata().getFirst()), tempPath);
        return  PluginFileParser.newPluginFile(provider, candidate, jar);
    }

    private static ModFile newPluginFile(final IModProvider provider, final PluginDiscovery.Candidate candidate, final SecureJar jar) {
        final PluginFileConfigurable config = new PluginFileConfigurable(candidate.resource(), candidate.metadata());
        return new ModFile(jar, provider, file -> parsePluginFileInfo(file, config), "MOD");
    }

    public static @Nullable ModFile newModFile(final IModProvider provider, SecureJarPluginResource resource) {
        final ModJarMetadata mjm = newModJarMetadata();
        final SecureJar jar = PluginFileParser.useModJarMetadata(resource.jar()) ? resource.modJar(mjm) : resource.jar();
        return PluginFileParser.newModFile(provider, mjm, jar);
    }

    public static @Nullable ModFile newModFile(final IModProvider provider, final Path... paths) {
        final ModJarMetadata mjm = newModJarMetadata();
        final SecureJar jar = SecureJar.from(j -> PluginFileParser.useModJarMetadata(j) ? mjm : JarMetadata.from(j, paths), paths);
        return PluginFileParser.newModFile(provider, mjm, jar);
    }

    private static @Nullable ModFile newModFile(final IModProvider provider, final ModJarMetadata mjm, final SecureJar jar) {
        final SecureJar.ModuleDataProvider data = jar.moduleDataProvider();
        final String type = data.getManifest().getMainAttributes().getValue("FMLModType");

        if (data.findFile(PluginFileParser.MODS_TOML).isPresent()) {
            ModFile modFile = new ModFile(jar, provider, PluginFileParser::parseModFileInfo, type == null ? "MOD" : type);
            mjm.setModFile(modFile);
            return modFile;
        }

        if (type == null) {
            return null;
        }

        return new ModFile(jar, provider, PluginFileParser::parseLibraryFileInfo, type);
    }

    public static ModFile newLibraryFile(final IModProvider provider, final boolean game, final SecureJar jar) {
        return new ModFile(jar, provider, PluginFileParser::parseLibraryFileInfo, game ? "GAMELIBRARY" : "LIBRARY");
    }

    private static class DummyModProvider extends AbstractModProvider {
        private static final DummyModProvider INSTANCE = new DummyModProvider();

        @Override
        public String name() {
            return "dummy";
        }

        @Override
        public IModFileInfo manifestParser(IModFile mod) {
            return super.manifestParser(mod);
        }
    }
}
