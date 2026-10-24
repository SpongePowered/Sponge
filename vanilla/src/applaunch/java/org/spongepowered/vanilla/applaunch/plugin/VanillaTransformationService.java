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
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.IModuleLayerManager;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.fusesource.jansi.AnsiConsole;
import org.spongepowered.asm.launch.MixinLaunchPlugin;
import org.spongepowered.common.applaunch.AppLaunch;
import org.spongepowered.plugin.PluginResource;
import org.spongepowered.vanilla.applaunch.plugin.resource.SecureJarPluginResource;
import org.spongepowered.vanilla.applaunch.transformation.VanillaAccessWidenerTransformer;
import org.spongepowered.vanilla.applaunch.transformation.VanillaSuperclassChangeTransformer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class VanillaTransformationService implements ITransformationService {
    private @MonotonicNonNull VanillaPluginPlatform pluginPlatform;

    @Override
    public String name() {
        return "spongevanilla";
    }

    @Override
    public void onLoad(final IEnvironment env, final Set<String> otherServices) {
        AnsiConsole.systemInstall();

        final Path baseDirectory = env.getProperty(IEnvironment.Keys.GAMEDIR.get()).orElse(Paths.get("."));
        try {
            this.pluginPlatform = new VanillaPluginPlatform(baseDirectory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        AppLaunch.setPluginPlatform(this.pluginPlatform);

        this.pluginPlatform.logger().info("SpongePowered PLUGIN Subsystem Version={} Source={}", this.pluginPlatform.version(), this.getCodeSource());
    }

    @Override
    public void initialize(final IEnvironment environment) {
        this.pluginPlatform.initializeLanguageServices();
    }

    @Override
    public List<Resource> beginScanning(final IEnvironment environment) {
        this.pluginPlatform.discoverLocatorServices();
        this.pluginPlatform.getLocatorServices().forEach((k, v) -> this.pluginPlatform.logger().info("Plugin resource locator '{}' found.", k));

        this.pluginPlatform.locatePluginResources();

        final List<SecureJar> languageResources = new ArrayList<>();

        for (final Set<? extends PluginResource> resources : this.pluginPlatform.getResources().values()) {
            for (final PluginResource resource : resources) {
                if (resource instanceof SecureJarPluginResource secureJarResource) {
                    if (ResourceType.of(resource) == ResourceType.LANGUAGE) {
                        languageResources.add(secureJarResource.jar());
                    }
                }
            }
        }

        return List.of(new Resource(IModuleLayerManager.Layer.PLUGIN, languageResources));
    }

    @Override
    public List<Resource> completeScan(final IModuleLayerManager layerManager) {
        this.pluginPlatform.discoverLanguageServices();
        this.pluginPlatform.getLanguageServices().forEach((k, v) -> this.pluginPlatform.logger().info("Plugin language loader '{}' found.", k));

        this.pluginPlatform.createPluginCandidates();

        final IEnvironment env = Launcher.INSTANCE.environment();
        final ILaunchPluginService mixin = env.findLaunchPlugin(MixinLaunchPlugin.NAME).orElse(null);

        final List<SecureJar> gameResources = new ArrayList<>();

        for (final Set<? extends PluginResource> resources : this.pluginPlatform.getResources().values()) {
            for (final PluginResource resource : resources) {
                if (resource instanceof SecureJarPluginResource secureJarResource) {
                    // Build jar metadata from first candidate, or fallback to standard
                    secureJarResource.init();

                    if (ResourceType.of(resource) == ResourceType.PLUGIN) {
                        gameResources.add(secureJarResource.jar());
                    }
                }

                // Offer jar to the Mixin service
                if (mixin != null) {
                    mixin.offerResource(VanillaTransformationService.mixinRoot(resource), resource.path().getFileName().toString());
                }

                // Log warning about plugin using Mixin
                if (mixin != null && resource.property(org.spongepowered.asm.util.Constants.ManifestAttributes.MIXINCONFIGS).isPresent()) {
                    if (!VanillaTransformationService.isSponge(resource)) {
                        this.pluginPlatform.logger().warn("Plugin from {} uses Mixins to modify the Minecraft Server. If something breaks, remove it before reporting the problem to Sponge!", resource.path());
                    }
                }
            }
        }

        return List.of(new Resource(IModuleLayerManager.Layer.GAME, gameResources));
    }

    /**
     * Mixin reads {@code MixinConfigs} from {@code META-INF/MANIFEST.MF} under the offered path. A resource made of
     * directories, as a dev workspace is, keeps that manifest in whichever of them holds the resources, so the union
     * root is offered: Mixin reads it through NIO.
     */
    private static Path mixinRoot(final PluginResource resource) {
        if (resource instanceof SecureJarPluginResource secureJarResource && Files.isDirectory(resource.path())) {
            return secureJarResource.jar().getRootPath();
        }
        return resource.path();
    }

    private static boolean isSponge(final PluginResource resource) {
        return resource instanceof SecureJarPluginResource secureJarResource && secureJarResource.jar().name().equals("spongevanilla");
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<ITransformer> transformers() {
        return List.of(new VanillaAccessWidenerTransformer(this.pluginPlatform), new VanillaSuperclassChangeTransformer(this.pluginPlatform));
    }

    private String getCodeSource() {
        try {
            return this.getClass().getProtectionDomain().getCodeSource().getLocation().toString();
        } catch (final Throwable th) {
            return "Unknown";
        }
    }
}
