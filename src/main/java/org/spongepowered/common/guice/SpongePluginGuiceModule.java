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
package org.spongepowered.common.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.config.ConfigDir;
import org.spongepowered.api.service.config.DefaultConfig;
import org.spongepowered.common.plugin.SpongePluginContainer;
import org.spongepowered.common.service.config.SpongeConfigService;

import java.io.File;
import java.nio.file.Path;

import javax.inject.Inject;

/**
 * Guice module that contains injections for a single plugin.
 */
public class SpongePluginGuiceModule extends AbstractModule {

    private final SpongePluginContainer container;
    private final Class<?> pluginClass;

    public SpongePluginGuiceModule(SpongePluginContainer container, Class<?> pluginClass) {
        this.container = container;
        this.pluginClass = pluginClass;
    }

    @Override
    protected void configure() {
        ConfigDir privateConfigDir = new ConfigDirAnnotation(false);
        DefaultConfig sharedConfigFile = new ConfigFileAnnotation(true);
        DefaultConfig privateConfigFile = new ConfigFileAnnotation(false);

        bind(this.pluginClass).in(Scopes.SINGLETON);
        bind(PluginContainer.class).toInstance(this.container);
        bind(Logger.class).toInstance(this.container.getLogger());

        // Plugin-private config directory (shared dir is in the global guice module)
        bind(Path.class).annotatedWith(privateConfigDir).toProvider(PrivateConfigDirProvider.class);
        bind(File.class).annotatedWith(privateConfigDir).toProvider(FilePrivateConfigDirProvider.class);
        bind(Path.class).annotatedWith(sharedConfigFile).toProvider(SharedConfigFileProvider.class); // Shared-directory config file
        bind(File.class).annotatedWith(sharedConfigFile).toProvider(FileSharedConfigFileProvider.class);
        bind(Path.class).annotatedWith(privateConfigFile).toProvider(PrivateConfigFileProvider.class); // Plugin-private directory config file
        bind(File.class).annotatedWith(privateConfigFile).toProvider(FilePrivateConfigFileProvider.class);

        bind(new TypeLiteral<ConfigurationLoader<CommentedConfigurationNode>>() {
        }).annotatedWith(sharedConfigFile)
                .toProvider(SharedHoconConfigProvider.class); // Loader for shared-directory config file
        bind(new TypeLiteral<ConfigurationLoader<CommentedConfigurationNode>>() {
        }).annotatedWith(privateConfigFile)
                .toProvider(PrivateHoconConfigProvider.class); // Loader for plugin-private directory config file
    }

    private static class PrivateConfigDirProvider implements Provider<Path> {

        private final PluginContainer container;

        @Inject
        private PrivateConfigDirProvider(PluginContainer container) {
            this.container = container;
        }

        @Override
        public Path get() {
            return SpongeConfigService.getPrivateRoot(this.container).getDirectory();
        }
    }

    private static class PrivateConfigFileProvider implements Provider<Path> {

        private final PluginContainer container;

        @Inject
        private PrivateConfigFileProvider(PluginContainer container) {
            this.container = container;
        }

        @Override
        public Path get() {
            return SpongeConfigService.getPrivateRoot(this.container).getConfigPath();
        }

    }

    private static class SharedConfigFileProvider implements Provider<Path> {

        private final PluginContainer container;

        @Inject
        private SharedConfigFileProvider(PluginContainer container) {
            this.container = container;
        }

        @Override
        public Path get() {
            return SpongeConfigService.getSharedRoot(this.container).getConfigPath();
        }

    }

    private static class SharedHoconConfigProvider implements Provider<ConfigurationLoader<CommentedConfigurationNode>> {

        private final PluginContainer container;

        @Inject
        private SharedHoconConfigProvider(PluginContainer container) {
            this.container = container;
        }

        @Override
        public ConfigurationLoader<CommentedConfigurationNode> get() {
            return SpongeConfigService.getSharedRoot(this.container).getConfig();
        }

    }

    private static class PrivateHoconConfigProvider implements Provider<ConfigurationLoader<CommentedConfigurationNode>> {

        private final PluginContainer container;

        @Inject
        private PrivateHoconConfigProvider(PluginContainer container) {
            this.container = container;
        }

        @Override
        public ConfigurationLoader<CommentedConfigurationNode> get() {
            return SpongeConfigService.getPrivateRoot(this.container).getConfig();
        }

    }

    // TODO: Support this without extra classes (basically it would be nice if Guice allowed something like an "alias" for File so we would only
    // need to add the conversion step Path -> File (Path.toFile()) once.

    private static class FilePrivateConfigDirProvider implements Provider<File> {

        private final Path configDir;

        @Inject
        private FilePrivateConfigDirProvider(@ConfigDir(sharedRoot = false) Path configDir) {
            this.configDir = configDir;
        }

        @Override
        public File get() {
            return this.configDir.toFile();
        }
    }

    private static class FilePrivateConfigFileProvider implements Provider<File> {

        private final Path configPath;

        @Inject
        private FilePrivateConfigFileProvider(@DefaultConfig(sharedRoot = false) Path configPath) {
            this.configPath = configPath;
        }

        @Override
        public File get() {
            return this.configPath.toFile();
        }

    }

    private static class FileSharedConfigFileProvider implements Provider<File> {

        private final Path configPath;

        @Inject
        private FileSharedConfigFileProvider(@DefaultConfig(sharedRoot = true) Path configPath) {
            this.configPath = configPath;
        }

        @Override
        public File get() {
            return this.configPath.toFile();
        }

    }

}
