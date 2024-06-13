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
import cpw.mods.modlauncher.api.*;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.fusesource.jansi.AnsiConsole;
import org.spongepowered.asm.launch.MixinLaunchPluginLegacy;
import org.spongepowered.common.applaunch.AppLaunch;
import org.spongepowered.common.applaunch.plugin.PluginPlatformConstants;
import org.spongepowered.plugin.PluginResource;
import org.spongepowered.plugin.builtin.StandardEnvironment;
import org.spongepowered.plugin.builtin.jvm.locator.JVMPluginResource;
import org.spongepowered.transformers.modlauncher.AccessWidenerTransformationService;
import org.spongepowered.transformers.modlauncher.SuperclassChanger;
import org.spongepowered.vanilla.applaunch.Constants;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class VanillaPlatformService implements ITransformationService {

    private static final String NAME = "vanilla_platform";

    private @MonotonicNonNull VanillaPluginPlatform pluginPlatform;

    @Override
    public @NonNull String name() {
        return VanillaPlatformService.NAME;
    }

    @Override
    public void initialize(final IEnvironment environment) {
        this.pluginPlatform.initialize();
    }

    @Override
    public List<Resource> beginScanning(final IEnvironment environment) {
        this.pluginPlatform.locatePluginResources();
        this.pluginPlatform.createPluginCandidates();
        final AccessWidenerTransformationService accessWidener = environment.getProperty(AccessWidenerTransformationService.INSTANCE.get()).orElse(null);
        final SuperclassChanger superclassChanger = environment.getProperty(SuperclassChanger.INSTANCE.get()).orElse(null);
        final ILaunchPluginService mixin = environment.findLaunchPlugin(MixinLaunchPluginLegacy.NAME).orElse(null);

        final List<SecureJar> gameResources = new ArrayList<>();

        for (final Map.Entry<String, Set<PluginResource>> resourcesEntry : this.pluginPlatform.getResources().entrySet()) {
            final Set<PluginResource> resources = resourcesEntry.getValue();
            for (final PluginResource resource : resources) {

                // Handle Access Transformers
                if ((accessWidener != null || mixin != null || superclassChanger != null) && resource instanceof JVMPluginResource) {
                    if (mixin != null) {
                        // Offer jar to the Mixin service
                        mixin.offerResource(resource.path(), resource.path().getFileName().toString());
                    }

                    // Offer jar to the AW service
                    ((JVMPluginResource) resource).manifest().ifPresent(manifest -> {
                        if (accessWidener != null) {
                            final String atFiles = manifest.getMainAttributes().getValue(Constants.ManifestAttributes.ACCESS_WIDENER);
                            if (atFiles != null) {
                                for (final String atFile : atFiles.split(",")) {
                                    if (!atFile.endsWith(AccessWidenerTransformationService.ACCESS_WIDENER_EXTENSION)) {
                                        continue;
                                    }
                                    try {
                                        accessWidener.offerResource(
                                                ((JVMPluginResource) resource).fileSystem().getPath(atFile).toUri().toURL(),
                                                atFile
                                        );
                                    } catch (final MalformedURLException ex) {
                                        this.pluginPlatform.logger().warn(
                                                "Failed to read declared access widener {}, from {}:",
                                                atFile,
                                                resource.locator()
                                        );
                                    }
                                }
                            }
                        }
                        if (mixin != null && manifest.getMainAttributes().getValue(org.spongepowered.asm.util.Constants.ManifestAttributes.MIXINCONFIGS) != null) {
                            if (!VanillaPlatformService.isSponge((JVMPluginResource) resource)) {
                                this.pluginPlatform.logger().warn(
                                        "Plugin from {} uses Mixins to modify the Minecraft Server. If something breaks, remove it before reporting the "
                                                + "problem to Sponge!", resource.path()
                                );
                            }
                        }
                        if (superclassChanger != null) {
                            final String superclassChangeFiles = manifest.getMainAttributes().getValue(Constants.ManifestAttributes.SUPERCLASS_CHANGE);
                            if (superclassChangeFiles != null) {
                                for (final String superclassChangeFile : superclassChangeFiles.split(",")) {
                                    if (!superclassChangeFile.endsWith(SuperclassChanger.SUPER_CLASS_EXTENSION)) {
                                        continue;
                                    }
                                    try {
                                        superclassChanger.offerResource(
                                                ((JVMPluginResource) resource).fileSystem().getPath(superclassChangeFile).toUri().toURL(),
                                                superclassChangeFile
                                        );
                                    } catch (final MalformedURLException ex) {
                                        this.pluginPlatform.logger().warn(
                                                "Failed to read declared superclass changer {}, from {}:",
                                                superclassChangeFile,
                                                resource.locator()
                                        );
                                    }
                                }
                            }
                        }
                    });

                    // TODO custom jar metadata ?
                    gameResources.add(SecureJar.from(resource.path()));
                }
            }
        }

        return List.of(new Resource(IModuleLayerManager.Layer.GAME, gameResources));
    }

    private static boolean isSponge(final JVMPluginResource resource) {
        try {
            return resource.path().toUri().equals(VanillaPlatformService.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (final URISyntaxException ex) {
            return false;
        }
    }

    @Override
    public void onLoad(final IEnvironment env, final Set<String> otherServices) {
        AnsiConsole.systemInstall();

        this.pluginPlatform = new VanillaPluginPlatform(new StandardEnvironment());
        AppLaunch.setPluginPlatform(this.pluginPlatform);

        final String implementationVersion = StandardEnvironment.class.getPackage().getImplementationVersion();
        final Path baseDirectory = Launcher.INSTANCE.environment().getProperty(IEnvironment.Keys.GAMEDIR.get()).orElse(Paths.get("."));

        this.pluginPlatform.setVersion(implementationVersion == null ? "dev" : implementationVersion);
        this.pluginPlatform.setBaseDirectory(baseDirectory);

        this.pluginPlatform.logger().info("SpongePowered PLUGIN Subsystem Version={} Source={}",
                this.pluginPlatform.version(), this.getCodeSource());

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

        this.pluginPlatform.discoverLocatorServices();
        this.pluginPlatform.getLocatorServices().forEach((k, v) -> this.pluginPlatform.logger().info("Plugin resource locator '{}' found.", k));
        this.pluginPlatform.discoverLanguageServices();
        this.pluginPlatform.getLanguageServices().forEach((k, v) -> this.pluginPlatform.logger().info("Plugin language loader '{}' found.", k));
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
