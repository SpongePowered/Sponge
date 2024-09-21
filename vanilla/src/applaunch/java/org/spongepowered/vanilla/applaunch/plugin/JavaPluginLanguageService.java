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
package org.spongepowered.vanilla.applaunch.plugin;

import org.spongepowered.plugin.Environment;
import org.spongepowered.plugin.PluginCandidate;
import org.spongepowered.plugin.PluginResource;
import org.spongepowered.plugin.builtin.StandardPluginCandidate;
import org.spongepowered.plugin.builtin.StandardPluginLanguageService;
import org.spongepowered.plugin.builtin.jvm.JVMPluginResource;
import org.spongepowered.plugin.metadata.PluginMetadata;

import java.io.InputStream;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

public final class JavaPluginLanguageService extends StandardPluginLanguageService {
    private final static String NAME = "java_plain";

    @Override
    public String name() {
        return JavaPluginLanguageService.NAME;
    }

    @Override
    public String pluginLoader() {
        return "org.spongepowered.vanilla.launch.plugin.JavaPluginLoader";
    }

    @Override
    public List<PluginCandidate> createPluginCandidates(final Environment environment, final PluginResource resource) throws Exception {
        if (resource instanceof JVMPluginResource jvmResource && Files.exists(jvmResource.resourcesRoot().resolve("net/minecraft/server/MinecraftServer.class"))) {
            this.logger.debug("Container in path '{}' has been detected as Minecraft.", resource.path());

            final List<PluginCandidate> candidates = new LinkedList<>();
            try (final InputStream stream = JavaPluginLanguageService.class.getClassLoader().getResourceAsStream("META-INF/minecraft_sponge_plugins.json")) {
                for (final PluginMetadata metadata : loadMetadataContainer(environment, stream).metadata()) {
                    candidates.add(new StandardPluginCandidate(metadata, resource));
                }
            }

            return candidates;
        }

        return super.createPluginCandidates(environment, resource);
    }
}
