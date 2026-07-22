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
package org.spongepowered.common.applaunch.plugin;

import com.google.common.collect.ImmutableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.common.applaunch.config.LaunchConfig;
import org.spongepowered.common.applaunch.config.TokenReplacement;
import org.spongepowered.common.applaunch.plugin.discovery.PluginDiscovery;
import org.spongepowered.common.applaunch.test.TestEnvironment;
import org.spongepowered.plugin.Environment;
import org.spongepowered.plugin.blackboard.Blackboard;
import org.spongepowered.plugin.blackboard.Keys;
import org.spongepowered.plugin.builtin.StandardEnvironment;
import org.spongepowered.plugin.builtin.jvm.JVMKeys;
import org.spongepowered.plugin.builtin.jvm.JVMPluginResource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public abstract class PluginPlatform implements JVMPluginResource.Factory {
    public static final Logger LOGGER = LogManager.getLogger("plugin");

    private final Environment environment;
    private final LaunchConfig config;
    private final TokenReplacement tokens;
    private final List<Path> pluginDirectories;

    public PluginPlatform() {
        this.environment = new StandardEnvironment(PluginPlatform.LOGGER);

        this.logger().info("SpongePowered PLUGIN Subsystem Version={} Source={}", PluginPlatformConstants.SPI_VERSION, this.getCodeSource());

        final Path baseDirectory = this.baseDirectory();
        final Path modsDirectory = this.modsDirectory();

        this.tokens = new TokenReplacement();
        this.tokens.register("BASE_DIR", baseDirectory);
        this.tokens.register("CONFIG_DIR", this.configDirectory());
        this.tokens.register("MODS_DIR", modsDirectory);

        final Path additionalPluginsDirectory;
        try {
            this.config = LaunchConfig.load(baseDirectory, false);
            additionalPluginsDirectory = Path.of(this.tokens.replace(this.config.additionalPluginsDirectory()));
            Files.createDirectories(additionalPluginsDirectory);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        this.pluginDirectories = List.of(modsDirectory, additionalPluginsDirectory);

        ImmutableList.Builder<String> metadataFilePaths = ImmutableList.builder();
        metadataFilePaths.add(PluginPlatformConstants.METADATA_FILE_PATH);
        if (TestEnvironment.isActive()) {
            metadataFilePaths.add(PluginPlatformConstants.TEST_METADATA_FILE_PATH);
        }

        final Blackboard blackboard = this.environment.blackboard();
        blackboard.set(Keys.VERSION, PluginPlatformConstants.SPI_VERSION);
        blackboard.set(Keys.BASE_DIRECTORY, baseDirectory);
        blackboard.set(Keys.PLUGIN_DIRECTORIES, this.pluginDirectories);
        blackboard.set(Keys.METADATA_FILE_PATHS, metadataFilePaths.build());
        blackboard.set(JVMKeys.ENVIRONMENT_LOCATOR_VARIABLE_NAME, PluginPlatformConstants.ENVIRONMENT_LOCATOR_VARIABLE_NAME);
        blackboard.set(JVMKeys.JVM_PLUGIN_RESOURCE_FACTORY, this);
    }

    public final Logger logger() {
        return PluginPlatform.LOGGER;
    }

    protected String getCodeSource() {
        try {
            return this.getClass().getProtectionDomain().getCodeSource().getLocation().toString();
        } catch (final Throwable th) {
            return "Unknown";
        }
    }

    public abstract boolean vanilla();

    public abstract Path baseDirectory();

    public abstract Path configDirectory();

    public abstract Path modsDirectory();

    public abstract PluginDiscovery discovery();

    /**
     * Adds a callback that will be called once, when the platform loader is about to close.
     * When the platform loader is closed, no new class can be loaded, so this is the last thing we can do.
     */
    public abstract void addLoaderCloseCallback(AutoCloseable closeable);

    public final List<Path> pluginDirectories() {
        return this.pluginDirectories;
    }

    public final LaunchConfig config() {
        return this.config;
    }

    public final TokenReplacement tokens() {
        return this.tokens;
    }

    public final Environment environment() {
        return this.environment;
    }
}
