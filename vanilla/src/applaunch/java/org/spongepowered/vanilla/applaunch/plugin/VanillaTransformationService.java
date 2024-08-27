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
import cpw.mods.jarhandling.SecureJar;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.IModuleLayerManager;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.fusesource.jansi.AnsiConsole;
import org.spongepowered.asm.launch.MixinLaunchPlugin;
import org.spongepowered.common.applaunch.AppLaunch;
import org.spongepowered.common.applaunch.plugin.PluginPlatformConstants;
import org.spongepowered.plugin.PluginResource;
import org.spongepowered.plugin.builtin.StandardEnvironment;
import org.spongepowered.transformers.modlauncher.AccessWidenerTransformationService;
import org.spongepowered.transformers.modlauncher.SuperclassChanger;
import org.spongepowered.vanilla.applaunch.Constants;
import org.spongepowered.vanilla.applaunch.plugin.locator.SecureJarPluginResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public final class VanillaTransformationService implements ITransformationService {
    private @MonotonicNonNull VanillaPluginPlatform pluginPlatform;

    @Override
    public @NonNull String name() {
        return "spongevanilla";
    }

    @Override
    public void onLoad(final IEnvironment env, final Set<String> otherServices) {
        AnsiConsole.systemInstall();

        this.pluginPlatform = new VanillaPluginPlatform(new StandardEnvironment());
        AppLaunch.setPluginPlatform(this.pluginPlatform);

        final String implementationVersion = StandardEnvironment.class.getPackage().getImplementationVersion();
        final Path baseDirectory = env.getProperty(IEnvironment.Keys.GAMEDIR.get()).orElse(Paths.get("."));

        this.pluginPlatform.setVersion(implementationVersion == null ? "dev" : implementationVersion);
        this.pluginPlatform.setBaseDirectory(baseDirectory);

        this.pluginPlatform.logger().info("SpongePowered PLUGIN Subsystem Version={} Source={}", this.pluginPlatform.version(), this.getCodeSource());

        final Path modsDirectory = baseDirectory.resolve("mods");
        if (Files.notExists(modsDirectory)) {
            try {
                Files.createDirectories(modsDirectory);
            } catch (IOException ignored) {}
        }

        final Path pluginsDirectory = baseDirectory.resolve("plugins");
        final List<Path> pluginDirectories = new ArrayList<>();
        pluginDirectories.add(modsDirectory);
        if (Files.exists(pluginsDirectory)) {
            pluginDirectories.add(pluginsDirectory);
        }

        this.pluginPlatform.setPluginDirectories(pluginDirectories);
        this.pluginPlatform.setMetadataFilePath(PluginPlatformConstants.METADATA_FILE_LOCATION);
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

        for (final Set<PluginResource> resources : this.pluginPlatform.getResources().values()) {
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
    public List<Resource> completeScan(IModuleLayerManager layerManager) {
        this.pluginPlatform.discoverLanguageServices();
        this.pluginPlatform.getLanguageServices().forEach((k, v) -> this.pluginPlatform.logger().info("Plugin language loader '{}' found.", k));

        this.pluginPlatform.createPluginCandidates();

        IEnvironment env = Launcher.INSTANCE.environment();
        final AccessWidenerTransformationService accessWidener = env.getProperty(AccessWidenerTransformationService.INSTANCE.get()).orElse(null);
        final SuperclassChanger superclassChanger = env.getProperty(SuperclassChanger.INSTANCE.get()).orElse(null);
        final ILaunchPluginService mixin = env.findLaunchPlugin(MixinLaunchPlugin.NAME).orElse(null);

        final List<SecureJar> gameResources = new ArrayList<>();

        for (final Set<PluginResource> resources : this.pluginPlatform.getResources().values()) {
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
                    mixin.offerResource(resource.path(), resource.path().getFileName().toString());
                }

                // Offer jar to the AW service
                if (accessWidener != null) {
                    final Optional<String> awFiles = resource.property(Constants.ManifestAttributes.ACCESS_WIDENER);
                    if (awFiles.isPresent()) {
                        for (final String awFile : awFiles.get().split(",")) {
                            if (!awFile.endsWith(AccessWidenerTransformationService.ACCESS_WIDENER_EXTENSION)) {
                                continue;
                            }
                            try {
                                accessWidener.offerResource(resource.locateResource(awFile).get().toURL(), awFile);
                            } catch (final Exception ex) {
                                this.pluginPlatform.logger().warn("Failed to read declared access widener {}, from {}:", awFile, resource.locator());
                            }
                        }
                    }
                }

                // Offer jar to the SuperclassChanger service
                if (superclassChanger != null) {
                    final Optional<String> superclassChangeFiles = resource.property(Constants.ManifestAttributes.SUPERCLASS_CHANGE);
                    if (superclassChangeFiles.isPresent()) {
                        for (final String superclassChangeFile : superclassChangeFiles.get().split(",")) {
                            if (!superclassChangeFile.endsWith(SuperclassChanger.SUPER_CLASS_EXTENSION)) {
                                continue;
                            }
                            try {
                                superclassChanger.offerResource(resource.locateResource(superclassChangeFile).get().toURL(), superclassChangeFile);
                            } catch (final Exception ex) {
                                this.pluginPlatform.logger().warn("Failed to read declared superclass changer {}, from {}:", superclassChangeFile, resource.locator());
                            }
                        }
                    }
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

    private static boolean isSponge(final PluginResource resource) {
        return resource instanceof SecureJarPluginResource secureJarResource && secureJarResource.jar().name().equals("spongevanilla");
    }

    @Override
    @SuppressWarnings("rawtypes")
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
