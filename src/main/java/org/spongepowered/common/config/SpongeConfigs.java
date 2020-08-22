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

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.dimension.DimensionType;
import org.spongepowered.api.world.dimension.DimensionTypes;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.storage.WorldInfoBridge;
import org.spongepowered.common.config.customdata.CustomDataConfig;
import org.spongepowered.common.config.common.CommonConfig;
import org.spongepowered.common.config.inheritable.GlobalConfig;
import org.spongepowered.common.config.inheritable.WorldConfig;
import org.spongepowered.common.config.tracker.TrackerConfig;
import org.spongepowered.common.util.IpSet;
import org.spongepowered.common.world.server.SpongeWorldManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class SpongeConfigs {

    static final String HEADER = "\n"
            + "# If you need help with the configuration or have any questions related to Sponge,\n"
            + "# join us on Discord or drop by our forums and leave a post.\n"
            + "\n"
            + "# Discord: https://discord.gg/sponge\n"
            + "# Forums: https://forums.spongepowered.org/\n";

    static final ConfigurationOptions OPTIONS = ConfigurationOptions.defaults()
            .setHeader(HEADER)
            .setShouldCopyDefaults(true)
            .setSerializers(TypeSerializers.getDefaultSerializers().newChild()
                    .registerType(TypeToken.of(IpSet.class), new IpSet.IpSetSerializer())
            );

    static final Logger LOGGER = LogManager.getLogger();

    private static final Object initLock = new Object();
    private static Path configDir;

    private static ConfigHandle<CommonConfig> sponge;
    private static ConfigHandle<TrackerConfig> trackerConfigAdapter;
    private static ConfigHandle<CustomDataConfig> customDataConfigAdapter;
    private static InheritableConfigHandle<GlobalConfig> global;

    public static Path getDirectory() {
        if (SpongeConfigs.configDir == null) {
            final Path baseDir = Paths.get(Objects.requireNonNull(System.getProperty("org.spongepowered.common.baseDir"),
                    "No base directory was passed to the Sponge configuration"));
            SpongeConfigs.configDir = baseDir
                    .resolve("config")
                    .resolve(SpongeCommon.ECOSYSTEM_ID);
        }
        return SpongeConfigs.configDir;
    }

    /**
     * Get global configuration, containing options that cannot be overridden per-world.
     *
     * @return global config
     */
    public static ConfigHandle<CommonConfig> getCommon() {
        if (SpongeConfigs.sponge == null) {
            synchronized (SpongeConfigs.initLock) {
                if (SpongeConfigs.sponge == null) {
                    // Load global config first so we can migrate over old settings
                    SpongeConfigs.getGlobalInheritable();
                    // Then load the actual configuration based on the new file
                    SpongeConfigs.sponge = create(new CommonConfig(), CommonConfig.FILE_NAME);
                }
            }
        }
        return sponge;
    }

    public static CompletableFuture<CommentedConfigurationNode> savePluginsInMetricsConfig(final Map<String, Tristate> entries) {
        return SpongeConfigs.getCommon().updateSetting("metrics.plugin-states", entries,
                        new TypeToken<Map<String, Tristate>>() { private static final long serialVersionUID = -1L;});
    }

    public static ConfigHandle<CustomDataConfig> getCustomData() {
        if (SpongeConfigs.customDataConfigAdapter == null) {
            SpongeConfigs.customDataConfigAdapter = SpongeConfigs.create(new CustomDataConfig(), CustomDataConfig.FILE_NAME);
        }
        return customDataConfigAdapter;
    }

    public static ConfigHandle<TrackerConfig> getTracker() {
        if (SpongeConfigs.trackerConfigAdapter == null) {
            SpongeConfigs.trackerConfigAdapter = SpongeConfigs.create(new TrackerConfig(), TrackerConfig.FILE_NAME);
        }
        return SpongeConfigs.trackerConfigAdapter;
    }


    public static InheritableConfigHandle<WorldConfig> getForWorld(final org.spongepowered.api.world.World<?> spongeWorld) {
        return getForWorld((net.minecraft.world.World) spongeWorld);
    }

    public static InheritableConfigHandle<WorldConfig> getForWorld(final net.minecraft.world.World mcWorld) {
        return ((WorldInfoBridge) mcWorld.getWorldInfo()).bridge$getConfigAdapter();
    }

    // Config-internal
    // everything below here should (mostly) not be directly accessed
    // unless performing specialized initialization (mostly world loads)

    public static HoconConfigurationLoader createLoader(final Path path) {
        // use File for slightly better performance on directory creation
        // Files.exists uses an exception for this :(
        final File parentFile = path.getParent().toFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }

        return HoconConfigurationLoader.builder()
            .setPath(path)
            .setDefaultOptions(OPTIONS)
            .build();
    }

    public static <T extends Config> ConfigHandle<T> create(final T instance, final String fileName) {
        // TODO(zml): Rather than rethrowing exceptions:
        // - gather all errors on load
        // - as soon as the first error has occurred, disable saving for the rest of the server runtime
        // - once server has loaded, list every error
        // - warn that configs will not save
        // What happens when a config fails to load? Will using default settings cause anything to be damaged?
        final HoconConfigurationLoader loader = createLoader(SpongeConfigs.getDirectory().resolve(fileName));
        try {
            final ConfigHandle<T> handle = new ConfigHandle<>(instance, loader);
            handle.load();
            return handle;
        } catch (final IOException | ObjectMappingException ex) {
            LOGGER.error("Unable to load configuration {}. Sponge will operate in "
                            + "fallback mode, with default configuration options and will not write to the invalid file", fileName, ex);
            return new ConfigHandle<>(instance);
        }
    }

    private static InheritableConfigHandle<GlobalConfig> getGlobalInheritable() {
        if (global == null) {
            synchronized (initLock) {
                if (global == null) {
                    try {
                        global = new InheritableConfigHandle<>(new GlobalConfig(),
                                createLoader(getDirectory().resolve(GlobalConfig.FILE_NAME)), null);
                        global.load();
                    } catch (IOException | ObjectMappingException e) {
                        LOGGER.error("Unable to load global world configuration in {}. Sponge will run with default settings", GlobalConfig.FILE_NAME, e);
                        global = new InheritableConfigHandle<>(new GlobalConfig(), null);
                    }
                }
            }
        }
        return global;
    }

    public static InheritableConfigHandle<WorldConfig> createDetached() {
        return new InheritableConfigHandle<>(new WorldConfig(), SpongeConfigs.getGlobalInheritable());
    }

    public static InheritableConfigHandle<WorldConfig> createWorld(final @Nullable DimensionType legacyType, final ResourceKey world) {
        // Path format: config/sponge/worlds/<world-namespace>/<world-value>.conf
        final Path configPath = getDirectory().resolve(Paths.get("worlds", world.getNamespace(), world.getValue() + ".conf"));
        if (legacyType != null) {
            // Legacy config path: config/sponge/worlds/<dim-namespace>/<dim-value>/<world-name>/world.conf
            final String legacyName = getLegacyWorldName(world);
            if (legacyName != null) {
                final Path legacyPath = getDirectory().resolve(Paths.get("worlds", legacyType.getKey().getNamespace(),
                        getLegacyValue(legacyType.getKey()), legacyName, "world.conf"));
                if (legacyPath.toFile().isFile() && !configPath.toFile().isFile()) {
                    try {
                        Files.createDirectories(configPath.getParent());
                        Files.move(legacyPath, configPath);
                        final Path legacyParent = legacyPath.getParent();
                        try (DirectoryStream<Path> str = Files.newDirectoryStream(legacyParent)) {
                            if (!str.iterator().hasNext()) {
                                Files.delete(legacyParent);
                            }
                        }
                    } catch (final IOException ex) {
                        LOGGER.error("Unable to migrate config for world {} from legacy location {}", world, legacyPath, ex);
                    }
                }
            }
        }
        try {
            final InheritableConfigHandle<WorldConfig> config = new InheritableConfigHandle<>(new WorldConfig(), SpongeConfigs.createLoader(configPath),
                    SpongeConfigs.getGlobalInheritable());
            config.load();
            return config;
        } catch (final IOException | ObjectMappingException ex) {
            LOGGER.error("Unable to load configuration for world {}. Sponge will use a "
                    + "fallback configuration with default values that will not save.", world, ex);
            return createDetached();
        }
    }

    private static String getLegacyValue(final ResourceKey dimensionType) {
        if (dimensionType.equals(DimensionTypes.THE_NETHER.get().getKey())) {
            return "nether";
        } else {
            return dimensionType.getValue();
        }
    }

    private static @Nullable String getLegacyWorldName(final ResourceKey world) {
        if (world.equals(SpongeWorldManager.VANILLA_OVERWORLD)) {
            return "world";
        } else if (world.equals(SpongeWorldManager.VANILLA_THE_END)) {
            return "DIM1";
        } else if (world.equals(SpongeWorldManager.VANILLA_THE_NETHER)) {
            return "DIM-1";
        }
        return null;
    }

}
