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
package org.spongepowered.common.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.common.applaunch.config.core.ConfigHandle;
import org.spongepowered.common.applaunch.config.core.SpongeConfigs;
import org.spongepowered.common.bridge.world.level.storage.PrimaryLevelDataBridge;
import org.spongepowered.common.config.customdata.CustomDataConfig;
import org.spongepowered.common.config.inheritable.BaseConfig;
import org.spongepowered.common.config.inheritable.GlobalConfig;
import org.spongepowered.common.config.inheritable.InheritableConfigHandle;
import org.spongepowered.common.config.inheritable.WorldConfig;
import org.spongepowered.common.config.tracker.TrackerConfig;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import net.minecraft.world.level.Level;

/**
 * SpongeCommon configurations that need to interact with game state
 */
public final class SpongeGameConfigs {

    static final Logger LOGGER = LogManager.getLogger();

    private static final Lock initLock = new ReentrantLock();
    private static ConfigHandle<TrackerConfig> trackerConfigAdapter;
    private static ConfigHandle<CustomDataConfig> customDataConfigAdapter;
    private static volatile InheritableConfigHandle<GlobalConfig> global;

    private SpongeGameConfigs() {
    }

    public static ConfigHandle<CustomDataConfig> getCustomData() {
        if (SpongeGameConfigs.customDataConfigAdapter == null) {
            SpongeGameConfigs.customDataConfigAdapter = SpongeConfigs.create(CustomDataConfig.class, null, CustomDataConfig.FILE_NAME);
        }
        return SpongeGameConfigs.customDataConfigAdapter;
    }

    public static ConfigHandle<TrackerConfig> getTracker() {
        if (SpongeGameConfigs.trackerConfigAdapter == null) {
            SpongeGameConfigs.trackerConfigAdapter = SpongeConfigs.create(TrackerConfig.class, null, TrackerConfig.FILE_NAME);
        }
        return SpongeGameConfigs.trackerConfigAdapter;
    }

    public static InheritableConfigHandle<WorldConfig> getForWorld(final org.spongepowered.api.world.World<?, ?> spongeWorld) {
        return SpongeGameConfigs.getForWorld((net.minecraft.world.level.Level) spongeWorld);
    }

    public static InheritableConfigHandle<WorldConfig> getForWorld(final net.minecraft.world.level.Level mcWorld) {
        return ((PrimaryLevelDataBridge) mcWorld.getLevelData()).bridge$configAdapter();
    }

    public static boolean doesWorldConfigExist(final ResourceKey world) {
        final Path configPath = SpongeConfigs.getDirectory().resolve(Paths.get("worlds", world.namespace(), world.value() + ".conf"));
        return Files.exists(configPath);
    }

    public static InheritableConfigHandle<WorldConfig> createWorld(final @Nullable ResourceKey dimensionTypeKey, final ResourceKey world) {
        // Path format: config/sponge/worlds/<world-namespace>/<world-value>.conf
        final Path configPath = SpongeConfigs.getDirectory().resolve(Paths.get("worlds", world.namespace(), world.value() + ".conf"));
        if (dimensionTypeKey != null) {
            // Legacy config path: config/sponge/worlds/<dim-namespace>/<dim-value>/<world-name>/world.conf
            final String legacyName = SpongeGameConfigs.getLegacyWorldName(world);
            if (legacyName != null) {
                final Path legacyPath = SpongeConfigs.getDirectory().resolve(Paths.get("worlds", dimensionTypeKey.namespace(),
                        SpongeGameConfigs.getLegacyValue(dimensionTypeKey), legacyName, "world.conf"));
                if (legacyPath.toFile().isFile() && !configPath.toFile().isFile()) {
                    try {
                        Files.createDirectories(configPath.getParent());
                        Files.move(legacyPath, configPath);
                        final Path legacyParent = legacyPath.getParent();
                        try (final DirectoryStream<Path> str = Files.newDirectoryStream(legacyParent)) {
                            if (!str.iterator().hasNext()) {
                                Files.delete(legacyParent);
                            }
                        }
                    } catch (final IOException ex) {
                        SpongeGameConfigs.LOGGER.error("Unable to migrate config for world {} from legacy location {}", world, legacyPath, ex);
                    }
                }
            }
        }
        try {
            final InheritableConfigHandle<WorldConfig> config = new InheritableConfigHandle<>(WorldConfig.class, BaseConfig::transformation, SpongeConfigs.createLoader(configPath),
                    SpongeGameConfigs.getGlobalInheritable());
            config.load();
            return config;
        } catch (final IOException ex) {
            SpongeGameConfigs.LOGGER.error("Unable to load configuration for world {}. Sponge will use a "
                    + "fallback configuration with default values that will not save.", world, ex);
            return SpongeGameConfigs.createDetached();
        }
    }

    @SuppressWarnings("EqualsBetweenInconvertibleTypes") // ResourceKey is ResourceLocation
    private static String getLegacyValue(final ResourceKey dimensionType) {
        if (dimensionType.equals(Level.NETHER.location())) {
            return "nether";
        } else {
            return dimensionType.value();
        }
    }

    @SuppressWarnings("EqualsBetweenInconvertibleTypes") // ResourceKey is ResourceLocation
    private static @Nullable String getLegacyWorldName(final ResourceKey world) {
        if (world.equals(Level.OVERWORLD.location())) {
            return "world";
        } else if (world.equals(Level.END.location())) {
            return "DIM1";
        } else if (world.equals(Level.NETHER.location())) {
            return "DIM-1";
        }
        return null;
    }

    public static InheritableConfigHandle<GlobalConfig> getGlobalInheritable() {
        if (SpongeGameConfigs.global == null) {
            SpongeGameConfigs.initLock.lock();
            try {
                if (SpongeGameConfigs.global == null) {
                    try {
                        SpongeGameConfigs.global = new InheritableConfigHandle<>(GlobalConfig.class,
                                BaseConfig::transformation,
                                SpongeConfigs.createLoader(SpongeConfigs.getDirectory().resolve(GlobalConfig.FILE_NAME)), null);
                        SpongeGameConfigs.global.load();
                    } catch (final IOException e) {
                        SpongeGameConfigs.LOGGER.error("Unable to load global world configuration in {}. Sponge will run with default settings",
                                            GlobalConfig.FILE_NAME, e);
                        SpongeGameConfigs.global = new InheritableConfigHandle<>(GlobalConfig.class, null);
                    }
                }
            } finally {
                SpongeGameConfigs.initLock.unlock();
            }
        }
        return SpongeGameConfigs.global;
    }

    public static InheritableConfigHandle<WorldConfig> createDetached() {
        return new InheritableConfigHandle<>(WorldConfig.class, SpongeGameConfigs.getGlobalInheritable());
    }
}
