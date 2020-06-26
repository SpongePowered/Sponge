/*
 * This file is part of plugin-spi, licensed under the MIT License (MIT).
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
package org.spongepowered.common.launch.plugin.config;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.json.JSONConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.common.launch.plugin.config.section.PluginSection;
import org.spongepowered.plugin.PluginEnvironment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@ConfigSerializable
public final class PluginMetadataConfiguration {

    private static final ObjectMapper<PluginMetadataConfiguration> MAPPER;

    static {
        try {
            MAPPER = ObjectMapper.forClass(PluginMetadataConfiguration.class);
        } catch (ObjectMappingException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Setting(value = "plugins")
    private final List<PluginSection> pluginSections = new ArrayList<>();

    public static PluginMetadataConfiguration loadFrom(final PluginEnvironment pluginEnvironment, final String fileName, final InputStream stream)
        throws IOException,
        ObjectMappingException {
        final PluginMetadataConfiguration configuration;

        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            final JSONConfigurationLoader loader = JSONConfigurationLoader.builder().setSource(() -> reader).build();
            final ConfigurationNode node = loader.load();
            configuration = PluginMetadataConfiguration.MAPPER.bindToNew().populate(node);

            // Plugin validation checks
            final Iterator<PluginSection> iter = configuration.pluginSections.iterator();
            while (iter.hasNext()) {
                final PluginSection pluginSection = iter.next();

                if (pluginSection.getId() == null) {
                    // TODO Use regex to check for malformed plugin id, this will do for the moment
                    pluginEnvironment.getLogger().error("Plugin specified with no id in '{}'. This plugin will be skipped...", fileName);
                    iter.remove();
                    continue;
                }

                if (pluginSection.getVersion() == null) {
                    // TODO Enforce sane versioning...maybe
                    pluginEnvironment.getLogger().error("Plugin '{}' has no version specified. This plugin will be skipped...", pluginSection.getId());
                    iter.remove();
                    continue;
                }

                if (pluginSection.getMainClass() == null) {
                    // TODO Validate class format
                    pluginEnvironment.getLogger().error("Plugin '{}' has no main class specified. This plugin will be skipped...", pluginSection.getId());
                    iter.remove();
                }
            }
        }

        return configuration;
    }

    public List<PluginSection> getPluginSections() {
        return this.pluginSections;
    }
}
