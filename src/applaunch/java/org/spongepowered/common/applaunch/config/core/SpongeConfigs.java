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

import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.spongepowered.common.applaunch.config.common.CommonConfig;
import org.spongepowered.common.applaunch.config.inheritable.GlobalConfig;
import org.spongepowered.common.applaunch.config.inheritable.WorldConfig;
import org.spongepowered.configurate.objectmapping.meta.NodeResolver;
import org.spongepowered.plugin.Blackboard;
import org.spongepowered.plugin.PluginEnvironment;
import org.spongepowered.plugin.PluginKeys;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Common utility methods for sponge configurations and necessary helpers for early init.
 */
public class SpongeConfigs {

    public static final Blackboard.Key<Boolean> IS_VANILLA_PLATFORM = Blackboard.Key.of("is_vanilla", Boolean.class);

    public static final String METRICS_NAME = "metrics.conf";

    static final String HEADER = "\n"
            + "# If you need help with the configuration or have any questions related to Sponge,\n"
            + "# join us on Discord or drop by our forums and leave a post.\n"
            + "\n"
            + "# Discord: https://discord.gg/sponge\n"
            + "# Forums: https://forums.spongepowered.org/\n";

    static final ObjectMapper.Factory OBJECT_MAPPERS = ObjectMapper.factoryBuilder()
            .addNodeResolver(NodeResolver.onlyWithSetting())
            .build();

    static final ConfigurationOptions OPTIONS = ConfigurationOptions.defaults()
            .header(HEADER)
            .shouldCopyDefaults(true)
            .implicitInitialization(true)
            .serializers(collection -> collection.register(TokenHoldingString.SERIALIZER)
                    .registerAnnotatedObjects(OBJECT_MAPPERS));

    static final Logger LOGGER = LogManager.getLogger();

    private static final Object initLock = new Object();
    private static @MonotonicNonNull PluginEnvironment environment;
    private static Path configDir;

    private static ConfigHandle<CommonConfig> sponge;
    private static InheritableConfigHandle<GlobalConfig> global;

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
            SpongeConfigs.configDir = getPluginEnvironment().getBlackboard()
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
            .path(path)
            .defaultOptions(OPTIONS)
            .build();
    }

    public static <T extends Config> ConfigHandle<T> create(final T instance, final String fileName) {
        final HoconConfigurationLoader loader = createLoader(SpongeConfigs.getDirectory().resolve(fileName));
        try {
            final ConfigHandle<T> handle = new ConfigHandle<>(instance, loader);
            handle.load();
            return handle;
        } catch (final ConfigurateException ex) {
            LOGGER.error("Unable to load configuration {}. Sponge will operate in "
                            + "fallback mode, with default configuration options and will not write to the invalid file", fileName, ex);
            return new ConfigHandle<>(instance);
        }
    }

    @Deprecated // Only world-specific configurations should be accessed, see SpongeGameConfigs
    public static InheritableConfigHandle<GlobalConfig> getGlobalInheritable() {
        if (global == null) {
            synchronized (initLock) {
                if (global == null) {
                    try {
                        global = new InheritableConfigHandle<>(new GlobalConfig(),
                                createLoader(getDirectory().resolve(GlobalConfig.FILE_NAME)), null);
                        global.load();
                    } catch (final ConfigurateException e) {
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

}
