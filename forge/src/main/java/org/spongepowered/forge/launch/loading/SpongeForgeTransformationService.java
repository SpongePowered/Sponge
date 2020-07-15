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
package org.spongepowered.forge.launch.loading;

import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.PluginEnvironment;
import org.spongepowered.plugin.PluginKeys;
import org.spongepowered.plugin.PluginLanguageService;

import javax.annotation.Nonnull;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;

public class SpongeForgeTransformationService implements ITransformationService {

    private static final String NAME = "sponge transform";
    private static final Logger log = LogManager.getLogger();

    @Nonnull
    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void initialize(final IEnvironment environment) {
        for (final Map.Entry<String, PluginLanguageService<PluginContainer>> entry : SpongeForgeLoader.languageServices.entrySet()) {
            entry.getValue().initialize(SpongeForgeLoader.pluginEnvironment);
        }
    }

    @Override
    public void beginScanning(final IEnvironment environment) {
    }

    @Override
    public void onLoad(final IEnvironment env, final Set<String> otherServices) {
        // Populate SpongeForge.pluginEnvironment
        // TODO: improve
        final String implementationVersion = PluginEnvironment.class.getPackage().getImplementationVersion();
        SpongeForgeLoader.pluginEnvironment.getBlackboard().getOrCreate(PluginKeys.VERSION, () -> implementationVersion == null ? "dev" : implementationVersion);
        SpongeForgeLoader.pluginEnvironment.getBlackboard().getOrCreate(PluginKeys.BASE_DIRECTORY, () -> Paths.get(""));
        SpongeForgeLoader.pluginEnvironment.getBlackboard().getOrCreate(PluginKeys.PLUGIN_DIRECTORIES, () -> Arrays.asList(Paths.get("mods")));

        log.info("SpongePowered PLUGIN Subsystem Version={} Service=FML", SpongeForgeLoader.pluginEnvironment.getBlackboard().get(PluginKeys.VERSION).get());
        this.discoverLanguageServices();
    }

    @Nonnull
    @Override
    public List<ITransformer> transformers() {
        return Collections.emptyList();
    }

    private void discoverLanguageServices() {
        final ServiceLoader<PluginLanguageService> serviceLoader = ServiceLoader.load(PluginLanguageService.class, SpongeForgeTransformationService.class.getClassLoader());

        for (final Iterator<PluginLanguageService<PluginContainer>> iter = (Iterator<PluginLanguageService<PluginContainer>>) (Object) serviceLoader.iterator(); iter.hasNext(); ) {
            final PluginLanguageService<PluginContainer> next;

            try {
                next = iter.next();
            } catch (final ServiceConfigurationError e) {
                log.error("Error encountered initializing plugin loader!", e);
                continue;
            }

            log.info("Plugin language loader '{}' found.", next.getName());
            SpongeForgeLoader.languageServices.put(next.getName(), next);
        }
    }

}
