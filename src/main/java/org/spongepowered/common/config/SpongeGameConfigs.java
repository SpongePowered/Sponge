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

import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.util.file.DeleteFileVisitor;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.world.level.storage.ServerLevelDataBridge;
import org.spongepowered.common.config.inheritable.BaseConfig;
import org.spongepowered.common.config.inheritable.GlobalConfig;
import org.spongepowered.common.config.inheritable.InheritableConfigHandle;
import org.spongepowered.common.config.inheritable.WorldConfig;
import org.spongepowered.common.config.tracker.TrackerConfig;
import org.spongepowered.common.launch.config.core.ConfigHandle;
import org.spongepowered.common.launch.config.core.SpongeConfigs;
import org.spongepowered.common.world.server.SpongeServerLevelData;
import org.spongepowered.configurate.ConfigurationOptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * SpongeCommon configurations that need to interact with game state
 */
public final class SpongeGameConfigs {

    static final Logger LOGGER = LogManager.getLogger();

    private static final Lock initLock = new ReentrantLock();
    private static ConfigHandle<TrackerConfig> trackerConfigAdapter;
    private static volatile InheritableConfigHandle<GlobalConfig> global;

    private static final ConfigurationOptions OPTIONS = SpongeConfigs.OPTIONS.serializers(c -> c.registerAll(SpongeAdventure.CONFIGURATE.serializers()));

    private SpongeGameConfigs() {
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

    public static InheritableConfigHandle<WorldConfig> getForWorld(final net.minecraft.world.level.Level level) {
        final SpongeServerLevelData spongeData = ((ServerLevelDataBridge) level.getLevelData()).bridge$spongeData();
        if (spongeData.configAdapter() == null) {
            final ResourceKey worldKey = ((ServerWorld) level).key();
            SpongeGameConfigs.LOGGER.warn("Level data ({}) has no Sponge config but is used by world {} ({}). World config will be reloaded.", level.getLevelData().getClass().getSimpleName(), worldKey, level.getClass().getSimpleName());

            // A custom level data has been set by a mod, so the key is likely missing too.
            if (spongeData.key() == null) {
                spongeData.setKey(worldKey);
            }

            spongeData.setConfigAdapter(SpongeGameConfigs.load(level.dimensionType(), spongeData.key()));
        }
        return spongeData.configAdapter();
    }

    public static boolean doesWorldConfigExist(final ResourceKey world) {
        final Path configPath = SpongeConfigs.getDirectory().resolve(Paths.get("worlds", world.namespace(), world.value() + ".conf"));
        return Files.exists(configPath);
    }

    public static InheritableConfigHandle<WorldConfig> load(final DimensionType dimensionType, final ResourceKey world) {
        // Path format: config/sponge/worlds/<world-namespace>/<world-value>.conf
        final Path configFile = SpongeConfigs.getDirectory().resolve(Paths.get("worlds", world.namespace(), world.value() + ".conf"));

        // Legacy config path: config/sponge/worlds/<dim-namespace>/<dim-value>/<world-name>/world.conf
        final String legacyDimAndName = SpongeGameConfigs.getLegacyDimensionAndName(world);
        if (legacyDimAndName != null) {
            final Path legacyFile = SpongeConfigs.getDirectory().resolve("worlds/minecraft/" + legacyDimAndName + "/world.conf");
            if (Files.isRegularFile(legacyFile) && !Files.exists(configFile)) {
                try {
                    Files.createDirectories(configFile.getParent());
                    Files.move(legacyFile, configFile);
                    Files.walkFileTree(legacyFile.getParent(), DeleteFileVisitor.INSTANCE);
                } catch (final IOException ex) {
                    SpongeGameConfigs.LOGGER.error("Unable to migrate config for world {} from legacy location {}", world, legacyFile, ex);
                }
            }
        }

        try {
            final InheritableConfigHandle<WorldConfig> config = new InheritableConfigHandle<>(WorldConfig.class, BaseConfig::transformation, SpongeConfigs.createLoader(configFile, SpongeGameConfigs.OPTIONS),
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
    private static @Nullable String getLegacyDimensionAndName(final ResourceKey world) {
        if (world.equals(Level.OVERWORLD.location())) {
            return "overworld/world";
        } else if (world.equals(Level.END.location())) {
            return "the_end/DIM1";
        } else if (world.equals(Level.NETHER.location())) {
            return "nether/DIM-1";
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
                                SpongeConfigs.createLoader(SpongeConfigs.getDirectory().resolve(GlobalConfig.FILE_NAME), SpongeGameConfigs.OPTIONS), null);
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
