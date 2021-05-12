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
package org.spongepowered.common.applaunch.config.core;

import com.google.common.collect.ImmutableSet;
import io.leangen.geantyref.GenericTypeReflector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.common.applaunch.config.common.CommonConfig;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.meta.NodeResolver;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;
import org.spongepowered.plugin.Blackboard;
import org.spongepowered.plugin.PluginEnvironment;
import org.spongepowered.plugin.PluginKeys;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Common utility methods for sponge configurations and necessary helpers for early init.
 */
public final class SpongeConfigs {

    public static final Blackboard.Key<Boolean> IS_VANILLA_PLATFORM = Blackboard.Key.of("is_vanilla", Boolean.class);

    public static final String GLOBAL_NAME = "global.conf";
    public static final String METRICS_NAME = "metrics.conf";

    static final String HEADER = "\n"
            + "# If you need help with the configuration or have any questions related to Sponge,\n"
            + "# join us on Discord or drop by our forums and leave a post.\n"
            + "\n"
            + "# Discord: https://discord.gg/sponge\n"
            + "# Forums: https://forums.spongepowered.org/\n";

    public static final ObjectMapper.Factory OBJECT_MAPPERS = ObjectMapper.factoryBuilder()
            .addNodeResolver(NodeResolver.onlyWithSetting())
            .build();

    public static final ConfigurationOptions OPTIONS = ConfigurationOptions.defaults()
            .header(SpongeConfigs.HEADER)
            .serializers(collection -> collection.register(TokenHoldingString.SERIALIZER)
                    .register(type -> {
                        final Class<?> erasure = GenericTypeReflector.erase(type);
                        return erasure.isAnnotationPresent(ConfigSerializable.class) || Config.class.isAssignableFrom(erasure);
                    }, SpongeConfigs.OBJECT_MAPPERS.asTypeSerializer()));

    static final Logger LOGGER = LogManager.getLogger();

    public static final Lock initLock = new ReentrantLock();
    private static @MonotonicNonNull PluginEnvironment environment;
    private static Path configDir;

    private static ConfigHandle<CommonConfig> sponge;

    public static void initialize(final PluginEnvironment environment) {
        if (SpongeConfigs.environment != null) {
            throw new IllegalArgumentException("Cannot initialize SpongeConfigs twice!");
        }
        SpongeConfigs.environment = environment;
    }

    public static PluginEnvironment getPluginEnvironment() {
        if (SpongeConfigs.environment == null) {
            throw new IllegalStateException("SpongeConfigs has not yet been initialized with a PluginEnvironment");
        }
        return SpongeConfigs.environment;
    }

    public static Path getDirectory() {
        if (SpongeConfigs.configDir == null) {
            SpongeConfigs.configDir = SpongeConfigs.getPluginEnvironment().blackboard()
                    .get(PluginKeys.BASE_DIRECTORY)
                    .orElseThrow(() -> new IllegalStateException("No base directory was set"))
                    .resolve("config")
                    .resolve("sponge");
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
            SpongeConfigs.initLock.lock();
            try {
                if (SpongeConfigs.sponge == null) {
                    // Load global config first so we can migrate over old settings
                    SpongeConfigs.splitFiles();
                    // Then load the actual configuration based on the new file
                    SpongeConfigs.sponge = SpongeConfigs.create(CommonConfig.class, CommonConfig::transformation, CommonConfig.FILE_NAME);
                }
            } finally {
                SpongeConfigs.initLock.unlock();
            }
        }
        return SpongeConfigs.sponge;
    }


    // Config-internal
    // everything below here should (mostly) not be directly accessed

    public static HoconConfigurationLoader createLoader(final Path path) throws IOException {
        return SpongeConfigs.createLoader(path, SpongeConfigs.OPTIONS);
    }

    public static HoconConfigurationLoader createLoader(final Path path, final ConfigurationOptions options) throws IOException {
        Files.createDirectories(path.getParent());

        return HoconConfigurationLoader.builder()
            .path(path)
            .defaultOptions(options)
            .build();
    }

    public static <T extends Config> ConfigHandle<T> create(final Class<T> instance, final @Nullable Supplier<ConfigurationTransformation> versionModifier, final String fileName) {
        try {
            final HoconConfigurationLoader loader = SpongeConfigs.createLoader(SpongeConfigs.getDirectory().resolve(fileName));
            final ConfigHandle<T> handle = new ConfigHandle<>(instance, versionModifier, loader);
            handle.load();
            return handle;
        } catch (final IOException ex) {
            SpongeConfigs.LOGGER.error("Unable to load configuration {}. Sponge will operate in "
                            + "fallback mode, with default configuration options and will not write to the invalid file", fileName, ex);
            return new ConfigHandle<>(instance);
        }
    }

    // Do the migration to split configuration files into separate files

    private static final NodePath PATH_PREFIX = NodePath.path("sponge");

    // Paths moved to sponge.conf
    private static final Set<NodePath> MIGRATE_SPONGE_PATHS = Stream.of(
            NodePath.path("world", "auto-player-save-interval"),
            NodePath.path("world", "leaf-decay"),
            NodePath.path("world", "game-profile-query-task-interval"),
            NodePath.path("world", "invalid-lookup-uuids"),
            NodePath.path("general"),
            NodePath.path("sql"),
            NodePath.path("commands"),
            NodePath.path("permission"),
            NodePath.path("modules"),
            NodePath.path("ip-sets"),
            NodePath.path("bungeecord"),
            NodePath.path("exploits"),
            NodePath.path("optimizations"),
            NodePath.path("cause-tracker"),
            NodePath.path("teleport-helper"),
            NodePath.path("broken-mods"),
            NodePath.path("service-registration"),
            NodePath.path("debug"),
            NodePath.path("timings"))
        .map(SpongeConfigs.PATH_PREFIX::plus)
        .collect(ImmutableSet.toImmutableSet());

    // Paths moved to metrics.conf
    private static final Set<NodePath> MIGRATE_METRICS_PATHS = ImmutableSet.of(
            NodePath.path("sponge", "metrics"));

    private static void splitFiles() {
        final Path commonFile = SpongeConfigs.getDirectory().resolve(CommonConfig.FILE_NAME);
        final Path metricsFile = SpongeConfigs.getDirectory().resolve(SpongeConfigs.METRICS_NAME);
        final Path oldGlobalFile = SpongeConfigs.getDirectory().resolve(SpongeConfigs.GLOBAL_NAME);

        // Is this migration unnecessary?
        if (!Files.exists(oldGlobalFile) || Files.exists(commonFile) || Files.exists(metricsFile)) {
            return;
        }


        try {
            final ConfigurationTransformation xform = ConfigurationTransformation.chain(
                    new FileMovingConfigurationTransformation(SpongeConfigs.MIGRATE_SPONGE_PATHS, SpongeConfigs.createLoader(commonFile), true),
                    new FileMovingConfigurationTransformation(SpongeConfigs.MIGRATE_METRICS_PATHS, SpongeConfigs.createLoader(metricsFile), true));
            final ConfigurationLoader<CommentedConfigurationNode> globalLoader = SpongeConfigs.createLoader(oldGlobalFile);

            Files.copy(oldGlobalFile, oldGlobalFile.resolveSibling(SpongeConfigs.GLOBAL_NAME + ".old-backup"));
            final CommentedConfigurationNode source = globalLoader.load();
            xform.apply(source);
            globalLoader.save(source);
            SpongeConfigs.LOGGER.info("Migrated Sponge configuration to 1.15+ split-file layout");
        } catch (final IOException ex) {
            SpongeConfigs.LOGGER.error("An error occurred while trying to migrate to a split-file configuration layout", ex);
        }

    }

    private SpongeConfigs() {
    }

}
