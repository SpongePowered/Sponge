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
package org.spongepowered.neoforge.launch.plugin;

import com.google.common.collect.MapMaker;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.moddiscovery.ModInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.neoforge.launch.NeoLaunch;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.metadata.PluginMetadata;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class NeoPluginContainer implements PluginContainer {
    private final ModContainer modContainer;

    private Logger logger;
    private PluginMetadata pluginMetadata;

    NeoPluginContainer(final ModContainer modContainer) {
        this.modContainer = modContainer;
    }

    public ModContainer getModContainer() {
        return this.modContainer;
    }

    @Override
    public PluginMetadata metadata() {
        if (this.pluginMetadata == null) {
            this.pluginMetadata = ((NeoLaunch) Launch.instance()).metadataForMod((ModInfo) this.modContainer.getModInfo());
        }
        return this.pluginMetadata;
    }

    @Override
    public Logger logger() {
        if (this.logger == null) {
            this.logger = LogManager.getLogger(this.modContainer.getModId());
        }
        return this.logger;
    }

    @Override
    public Optional<URI> locateResource(String relative) {
        final Path p = this.modContainer.getModInfo().getOwningFile().getFile().findResource(Objects.requireNonNull(relative, "relative"));
        return Files.exists(p) ? Optional.of(p.toUri()) : Optional.empty();
    }

    @Override
    public Object instance() {
        // TODO NeoForge: remove from API?
        return null;
    }

    private static final Map<ModContainer, NeoPluginContainer> containers = new MapMaker().weakKeys().makeMap();

    public static NeoPluginContainer of(final ModContainer modContainer) {
        return containers.computeIfAbsent(modContainer, NeoPluginContainer::new);
    }
}
