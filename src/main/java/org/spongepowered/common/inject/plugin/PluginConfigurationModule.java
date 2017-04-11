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
package org.spongepowered.common.inject.plugin;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.common.config.SpongeConfigManager;
import org.spongepowered.common.inject.config.ConfigDirAnnotation;
import org.spongepowered.common.inject.config.DefaultConfigAnnotation;
import org.spongepowered.common.inject.provider.PathAsFileProvider;

import java.io.File;
import java.nio.file.Path;

import javax.inject.Inject;

/**
 * A module which provides bindings for configuration annotations.
 */
class PluginConfigurationModule extends AbstractModule {

    private static final TypeLiteral<ConfigurationLoader<CommentedConfigurationNode>> COMMENTED_CONFIGURATION_NODE_LOADER = new TypeLiteral<ConfigurationLoader<CommentedConfigurationNode>>() {};
    /**
     * Provides a non-shared (private) directory.
     *
     * {@literal @}ConfigDir(sharedRoot = false) File configDir;
     */
    private static final PathAsFileProvider NON_SHARED_CONFIG_DIR = new PathAsFileProvider() {
        @Inject
        void init(@ConfigDir(sharedRoot = false) Provider<Path> path) {
            this.path = path;
        }
    };
    /**
     * Provides a configuration file within a non-shared (private) directory.
     *
     * {@literal @}DefaultConfig(sharedRoot = false) File configFile;
     */
    private static final PathAsFileProvider NON_SHARED_CONFIG_FILE = new PathAsFileProvider() {
        @Inject
        void init(@DefaultConfig(sharedRoot = false) Provider<Path> path) {
            this.path = path;
        }
    };
    /**
     * Provides a configuration file within a shared directory.
     *
     * {@literal @}DefaultConfig(sharedRoot = true) File configFile;
     */
    private static final PathAsFileProvider SHARED_CONFIG_FILE = new PathAsFileProvider() {
        @Inject
        void init(@DefaultConfig(sharedRoot = true) Provider<Path> path) {
            this.path = path;
        }
    };
    private final PluginContainer container;

    PluginConfigurationModule(final PluginContainer container) {
        this.container = container;
    }

    @Override
    protected void configure() {
        this.bind(Path.class).annotatedWith(ConfigDirAnnotation.NON_SHARED).toProvider(() -> SpongeConfigManager.getPrivateRoot(this.container).getDirectory());
        this.bind(File.class).annotatedWith(ConfigDirAnnotation.NON_SHARED).toProvider(NON_SHARED_CONFIG_DIR);
        // Plugin-private directory config file
        this.bind(Path.class).annotatedWith(DefaultConfigAnnotation.NON_SHARED).toProvider(() -> SpongeConfigManager.getPrivateRoot(this.container).getConfigPath());
        this.bind(File.class).annotatedWith(DefaultConfigAnnotation.NON_SHARED).toProvider(NON_SHARED_CONFIG_FILE);
        // Shared-directory config file
        this.bind(Path.class).annotatedWith(DefaultConfigAnnotation.SHARED).toProvider(() -> SpongeConfigManager.getSharedRoot(this.container).getConfigPath());
        this.bind(File.class).annotatedWith(DefaultConfigAnnotation.SHARED).toProvider(SHARED_CONFIG_FILE);
        // Loader for shared-directory config file
        this.bind(COMMENTED_CONFIGURATION_NODE_LOADER).annotatedWith(DefaultConfigAnnotation.SHARED).toProvider(() -> SpongeConfigManager.getSharedRoot(this.container).getConfig());
        // Loader for plugin-private directory config file
        this.bind(COMMENTED_CONFIGURATION_NODE_LOADER).annotatedWith(DefaultConfigAnnotation.NON_SHARED).toProvider(() -> SpongeConfigManager.getPrivateRoot(this.container).getConfig());
    }
}
