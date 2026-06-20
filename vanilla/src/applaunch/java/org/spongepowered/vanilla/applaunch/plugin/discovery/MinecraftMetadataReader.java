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
package org.spongepowered.vanilla.applaunch.plugin.discovery;

import org.spongepowered.plugin.Environment;
import org.spongepowered.plugin.builtin.jvm.JVMPluginResource;
import org.spongepowered.plugin.discovery.PluginMetadataReader;
import org.spongepowered.plugin.discovery.PluginResource;
import org.spongepowered.plugin.discovery.PluginResourceLocator;
import org.spongepowered.plugin.metadata.PluginMetadata;
import org.spongepowered.plugin.metadata.builtin.MetadataParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

public final class MinecraftMetadataReader implements PluginMetadataReader {

    @Override
    public String name() {
        return "minecraft";
    }

    @Override
    public Collection<? extends PluginMetadata> readPluginMetadata(final Environment environment, final PluginResource resource, final List<PluginResourceLocator> locators) throws Exception {
        if (resource instanceof JVMPluginResource && resource.locateResource("net/minecraft/server/MinecraftServer.class").isPresent()) {
            environment.logger().debug("Container in path '{}' has been detected as Minecraft.", resource.path());

            final InputStream input = MinecraftMetadataReader.class.getClassLoader().getResourceAsStream("META-INF/minecraft_sponge_plugins.json");
            if (input == null) {
                throw new IOException("Unable to locate Minecraft plugins metadata file");
            }

            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                return MetadataParser.read(reader).plugins();
            }
        }
        return List.of();
    }
}
