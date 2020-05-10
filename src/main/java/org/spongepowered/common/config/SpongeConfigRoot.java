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

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapperFactory;
import org.spongepowered.api.config.ConfigRoot;
import org.spongepowered.common.SpongeImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Root for sponge configurations.
 */
public class SpongeConfigRoot implements ConfigRoot {
    private final ObjectMapperFactory mapperFactory;
    private final String pluginName;
    private final Path baseDir;

    public SpongeConfigRoot(ObjectMapperFactory mapperFactory, String pluginName, Path baseDir) {
        this.mapperFactory = mapperFactory;
        this.pluginName = pluginName;
        this.baseDir = baseDir;
    }

    @Override
    public Path getConfigPath() {
        Path configFile = this.baseDir.resolve(this.pluginName + ".conf");
        try {
            Files.createDirectories(this.baseDir);
        } catch (IOException e) {
            SpongeImpl.getLogger().error("Failed to create plugin dir for {} at {}", this.pluginName, this.baseDir, e);
        }
        return configFile;
    }

    @Override
    public ConfigurationLoader<CommentedConfigurationNode> getConfig() {
        return HoconConfigurationLoader.builder()
                .setPath(getConfigPath())
                .setDefaultOptions(d -> d.withObjectMapperFactory(this.mapperFactory))
                .build();
    }

    @Override
    public Path getDirectory() {
        return this.baseDir;
    }
}
