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
package org.spongepowered.vanilla.applaunch.plugin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.plugin.PluginKeys;
import org.spongepowered.plugin.PluginResource;
import org.spongepowered.plugin.jvm.locator.JVMPluginResource;
import org.spongepowered.vanilla.applaunch.Main;
import org.spongepowered.vanilla.applaunch.service.AccessWidenerLaunchService;
import org.spongepowered.vanilla.installer.Constants;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class VanillaPlatformService implements ITransformationService {

    private static final String NAME = "vanilla_platform";

    private static final VanillaPluginEngine pluginEngine = Main.getInstance().getPluginEngine();

    @Override
    public @NonNull String name() {
        return VanillaPlatformService.NAME;
    }

    @Override
    public void initialize(final IEnvironment environment) {
        Main.getInstance().getPluginEngine().initialize();
    }

    @Override
    public void beginScanning(final IEnvironment environment) {
        //NOOP
    }

    @Override
    public List<Map.Entry<String, Path>> runScan(final IEnvironment environment) {
        VanillaPlatformService.pluginEngine.locatePluginResources();
        VanillaPlatformService.pluginEngine.createPluginCandidates();
        final ILaunchPluginService accessWidener = environment.findLaunchPlugin(AccessWidenerLaunchService.NAME)
                .orElse(null);


        final List<Map.Entry<String, Path>> launchResources = new ArrayList<>();

        for (final Map.Entry<String, List<PluginResource>> resourcesEntry : VanillaPlatformService.pluginEngine.getResources().entrySet()) {
            final List<PluginResource> resources = resourcesEntry.getValue();
            for (final PluginResource resource : resources) {

                // Handle Access Transformers
                if (accessWidener != null && resource instanceof JVMPluginResource) {
                    ((JVMPluginResource) resource).getManifest().ifPresent(manifest -> {
                        final String atFiles = manifest.getMainAttributes().getValue(Constants.ManifestAttributes.ACCESS_WIDENER);
                        if (atFiles != null) {
                            for (final String atFile : atFiles.split(",")) {
                                if (!atFile.endsWith(".accesswidener")) {
                                    continue;
                                }
                                accessWidener.offerResource(resource.getFileSystem().getPath(atFile), atFile);
                            }
                        }
                    });
                }

                final Map.Entry<String, Path> entry = Maps.immutableEntry(resource.getPath().getFileName().toString(), resource.getPath());
                launchResources.add(entry);
            }
        }

        return launchResources;
    }

    @Override
    public void onLoad(final IEnvironment env, final Set<String> otherServices) {
        final VanillaPluginEngine pluginEngine = VanillaPlatformService.pluginEngine;

        pluginEngine.getPluginEnvironment().getLogger().info("SpongePowered PLUGIN Subsystem Version={} Source={}",
            pluginEngine.getPluginEnvironment().getBlackboard().get(PluginKeys.VERSION).get(), this.getCodeSource());

        pluginEngine.discoverLocatorServices();
        pluginEngine.getLocatorServices().forEach((k, v) -> pluginEngine.getPluginEnvironment()
                .getLogger().info("Plugin resource locator '{}' found.", k));
        pluginEngine.discoverLanguageServices();
        pluginEngine.getLanguageServices().forEach((k, v) -> pluginEngine.getPluginEnvironment()
                .getLogger().info("Plugin language loader '{}' found.", k));
    }

    @Override
    public @NonNull List<ITransformer> transformers() {
        return ImmutableList.of();
    }

    private String getCodeSource() {
        try {
            return this.getClass().getProtectionDomain().getCodeSource().getLocation().toString();
        } catch (final Throwable th) {
            return "Unknown";
        }
    }
}
