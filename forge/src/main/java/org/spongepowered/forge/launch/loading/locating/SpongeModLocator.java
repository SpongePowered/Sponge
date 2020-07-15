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
package org.spongepowered.forge.launch.loading.locating;

import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.forgespi.locating.IModLocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.forge.launch.loading.SpongeForgeLoader;
import org.spongepowered.plugin.PluginCandidate;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.PluginLanguageService;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.jar.Manifest;

public class SpongeModLocator implements IModLocator {

    private final static String NAME = "sponge plugin locator";
    private static final Logger log = LogManager.getLogger();

    @Override
    public List<IModFile> scanMods() {
        discoverPluginResources();

        final Map<PluginLanguageService<PluginContainer>, List<PluginCandidate>> pluginCandidates = createPluginCandidates();

        final List<IModFile> mods = new ArrayList<>();

        for (final Map.Entry<PluginLanguageService<PluginContainer>, List<PluginCandidate>> languageCandidates : pluginCandidates.entrySet()) {
            final PluginLanguageService<PluginContainer> languageService = languageCandidates.getKey();
            final Collection<PluginCandidate> candidates = languageCandidates.getValue();

            for (final PluginCandidate candidate : candidates) {
                mods.add(new SpongeModFile(candidate, this, languageService));
            }
        }

        return mods;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Path findPath(final IModFile modFile, final String... path) {
        final SpongeModFile pluginFile = (SpongeModFile) modFile;

        Path resolved = pluginFile.getPlugin().getFile();
        for (final String s : path) {
            resolved = resolved.resolve(s);
        }
        return resolved;
    }

    @Override
    public void scanFile(final IModFile modFile, final Consumer<Path> pathConsumer) {
    }

    @Override
    public Optional<Manifest> findManifest(final Path file) {
        return Optional.empty();
    }

    @Override
    public void initArguments(final Map<String, ?> arguments) {
    }

    @Override
    public boolean isValid(final IModFile modFile) {
        return true;
    }

    private static void discoverPluginResources() {
        for (final Map.Entry<String, PluginLanguageService<PluginContainer>> languageEntry : SpongeForgeLoader.languageServices.entrySet()) {
            final PluginLanguageService<PluginContainer> languageService = languageEntry.getValue();
            final List<Path> pluginFiles = languageService.discoverPluginResources(SpongeForgeLoader.pluginEnvironment);
            if (pluginFiles.size() > 0) {
                SpongeForgeLoader.pluginFiles.put(languageEntry.getKey(), pluginFiles);
            }
        }
    }

    private static Map<PluginLanguageService<PluginContainer>, List<PluginCandidate>> createPluginCandidates() {
        final Map<PluginLanguageService<PluginContainer>, List<PluginCandidate>> pluginCandidates = new HashMap<>();

        for (final Map.Entry<String, PluginLanguageService<PluginContainer>> languageEntry : SpongeForgeLoader.languageServices.entrySet()) {
            final PluginLanguageService<PluginContainer> languageService = languageEntry.getValue();
            final List<PluginCandidate> candidates = languageService.createPluginCandidates(SpongeForgeLoader.pluginEnvironment);
            if (candidates.size() > 0) {
                pluginCandidates.put(languageService, candidates);
            }
        }

        return pluginCandidates;
    }

}
