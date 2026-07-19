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
package org.spongepowered.forge.launch.plugin;

import com.google.common.collect.MapMaker;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.forgespi.locating.IModFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.forge.applaunch.plugin.metadata.PluginMetadataConverter;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.metadata.PluginMetadata;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ForgePluginContainer implements PluginContainer {
    private final ModContainer modContainer;
    private final IModFile modFile;

    private Logger logger;
    private PluginMetadata pluginMetadata;

    private ForgePluginContainer(final ModContainer modContainer) {
        this.modContainer = modContainer;
        this.modFile = modContainer.getModInfo().getOwningFile().getFile();
    }

    @Override
    public PluginMetadata metadata() {
        if (this.pluginMetadata == null) {
            this.pluginMetadata = PluginMetadataConverter.modToPlugin(this.modContainer.getModInfo());
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
        final Path p = this.modFile.findResource(Objects.requireNonNull(relative, "relative"));
        return Files.exists(p) ? Optional.of(p.toUri()) : Optional.empty();
    }

    @SuppressWarnings("removal")
    @Override
    public Object instance() {
        return this.modContainer.getMod();
    }

    private static final Map<ModContainer, ForgePluginContainer> mods = new MapMaker().weakKeys().makeMap();

    public static Optional<PluginContainer> of(final ModContainer modContainer) {
        if (modContainer instanceof PluginModContainer plugin) {
            return plugin.container();
        }
        return Optional.of(ForgePluginContainer.mods.computeIfAbsent(modContainer, ForgePluginContainer::new));
    }
}
