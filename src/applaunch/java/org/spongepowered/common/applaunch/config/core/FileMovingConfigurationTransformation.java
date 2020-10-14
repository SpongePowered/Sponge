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
package org.spongepowered.common.applaunch.config.core;

import com.google.common.collect.ImmutableSet;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.transformation.NodePath;

import java.util.Set;

/**
 * A transformation that moves a set of entries from the loaded file into another file.
 */
public class FileMovingConfigurationTransformation implements ConfigurationTransformation {
    private final Set<NodePath> paths;
    private final ConfigurationLoader<?> destinationLoader;
    private final boolean override;

    public FileMovingConfigurationTransformation(final Set<NodePath> paths, final ConfigurationLoader<?> destinationLoader, final boolean override) {
        this.paths = ImmutableSet.copyOf(paths);
        this.destinationLoader = destinationLoader;
        this.override = override;
    }

    @Override
    public void apply(final @NonNull ConfigurationNode oldConfig) throws ConfigurateException {
        final ConfigurationNode newConfig = this.destinationLoader.load();
        boolean acted = false;
        for (final NodePath path : this.paths) {
            final ConfigurationNode source = oldConfig.node(path);
            if (!source.virtual()) {
                acted = true;
                if (this.override) {
                    newConfig.node(path).from(source);
                } else {
                    newConfig.node(path).mergeFrom(source);
                }
                source.raw(null);
            }
        }
        if (acted) {
            this.destinationLoader.save(newConfig);
        }
    }
}
