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
package org.spongepowered.server.plugin;

import static org.spongepowered.server.launch.VanillaLaunch.Environment.DEVELOPMENT;

import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.plugin.meta.McModInfo;
import org.spongepowered.plugin.meta.PluginMetadata;
import org.spongepowered.server.launch.LaunchException;
import org.spongepowered.server.launch.VanillaLaunch;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public final class MetadataContainer {

    private final ImmutableMap<String, PluginMetadata> metadata;

    private MetadataContainer(ImmutableMap<String, PluginMetadata> metadata) {
        this.metadata = metadata;
    }

    public boolean isEmpty() {
        return this.metadata.isEmpty();
    }

    public PluginMetadata get(String id, String name) {
        PluginMetadata meta = this.metadata.get(id);
        if (meta == null) {
            if (VanillaLaunch.ENVIRONMENT != DEVELOPMENT) {
                throw new RuntimeException("Unable to find metadata for " + id);
            }

            meta = new PluginMetadata(id);
            meta.setName(name);
        }

        return meta;
    }

    PluginContainer createContainer(String id, String name, Optional<Path> source) {
        return new MetaPluginContainer(get(id, name), source);
    }

    public static MetadataContainer load() {
        return load("");
    }

    public static MetadataContainer load(String path) {
        List<PluginMetadata> meta;

        path = path + '/' + McModInfo.STANDARD_FILENAME;
        try (InputStream in = MetadataContainer.class.getResourceAsStream(path)) {
            if (in == null) {
                if (VanillaLaunch.ENVIRONMENT != DEVELOPMENT) {
                    throw new LaunchException("Unable to find metadata file at " + path);
                }

                return new MetadataContainer(ImmutableMap.of());
            }

            meta = McModInfo.DEFAULT.read(in);
        } catch (IOException e) {
            throw new LaunchException("Failed to load metadata", e);
        }

        ImmutableMap.Builder<String, PluginMetadata> builder = ImmutableMap.builder();

        for (PluginMetadata m : meta) {
            builder.put(m.getId(), m);
        }

        return new MetadataContainer(builder.build());
    }

}
