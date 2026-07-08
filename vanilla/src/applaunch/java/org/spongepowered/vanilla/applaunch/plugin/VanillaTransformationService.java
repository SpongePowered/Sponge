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

import cpw.mods.jarhandling.SecureJar;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.IModuleLayerManager;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.fusesource.jansi.AnsiConsole;
import org.spongepowered.asm.launch.MixinLaunchPlugin;
import org.spongepowered.common.applaunch.AppLaunch;
import org.spongepowered.common.applaunch.plugin.discovery.PluginDiscovery;
import org.spongepowered.plugin.discovery.PluginResource;
import org.spongepowered.vanilla.applaunch.plugin.discovery.SecureJarPluginResource;
import org.spongepowered.vanilla.applaunch.transformation.VanillaAccessWidenerTransformer;
import org.spongepowered.vanilla.applaunch.transformation.VanillaSuperclassChangeTransformer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class VanillaTransformationService implements ITransformationService {
    private @MonotonicNonNull VanillaPluginPlatform platform;

    @Override
    public String name() {
        return "spongevanilla";
    }

    @Override
    public void onLoad(final IEnvironment env, final Set<String> otherServices) {
        AnsiConsole.systemInstall();
        this.platform = AppLaunch.bootstrap(VanillaPluginPlatform::new);
    }

    @Override
    public void initialize(final IEnvironment environment) {}

    @Override
    public List<Resource> beginScanning(final IEnvironment env) {
        final ILaunchPluginService mixin = env.findLaunchPlugin(MixinLaunchPlugin.NAME).orElse(null);

        this.platform.discovery().discoverPluginResources();

        final List<SecureJar> libs = new ArrayList<>();
        final List<SecureJar> gameLibs = new ArrayList<>();

        for (final PluginDiscovery.Candidate candidate : this.platform.discovery().candidates()) {
            final PluginResource resource = candidate.resource();
            candidate.readMetadata();

            switch (candidate.loading()) {
                case IGNORED:
                    break;
                case LIBRARY:
                    if (resource instanceof SecureJarPluginResource jarResource) {
                        libs.add(jarResource.jar());
                    }
                    break;
                case GAME_LIBRARY:
                    if (resource instanceof SecureJarPluginResource jarResource) {
                        gameLibs.add(candidate.pluginFound() ? jarResource.pluginJar(candidate.metadata().getFirst()) : jarResource.jar());

                        if (mixin != null) {
                            final Path path = jarResource.jar().getPrimaryPath();
                            mixin.offerResource(path, path.getFileName().toString());

                            if (resource.property(org.spongepowered.asm.util.Constants.ManifestAttributes.MIXINCONFIGS).isPresent()) {
                                if (candidate.metadata().stream().noneMatch(m -> "spongevanilla".equals(m.id()))) {
                                    this.platform.logger().warn("Plugin from {} uses Mixins to modify the Minecraft Server. If something breaks, remove it before reporting the problem to Sponge!", path);
                                }
                            }
                        }
                    }
                    break;
            }

            candidate.logResult();
        }

        this.platform.discovery().logMetadataWarnings();

        return List.of(new Resource(IModuleLayerManager.Layer.PLUGIN, libs), new Resource(IModuleLayerManager.Layer.GAME, gameLibs));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<ITransformer> transformers() {
        return List.of(new VanillaAccessWidenerTransformer(this.platform.discovery()), new VanillaSuperclassChangeTransformer(this.platform.discovery()));
    }
}
