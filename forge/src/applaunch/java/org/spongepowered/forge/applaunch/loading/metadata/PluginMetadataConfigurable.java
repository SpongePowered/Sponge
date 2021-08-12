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
package org.spongepowered.forge.applaunch.loading.metadata;

import net.minecraftforge.forgespi.language.IConfigurable;
import org.spongepowered.plugin.metadata.PluginMetadata;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

// ModInfo
public final class PluginMetadataConfigurable implements IConfigurable {

    private final PluginMetadata metadata;

    public PluginMetadataConfigurable(final PluginMetadata metadata) {
        this.metadata = metadata;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<T> getConfigElement(final String... key) {
        if (key.length != 1) {
            return Optional.empty();
        }
        final String query = key[0];
        if ("modId".equals(query)) {
            return Optional.of((T) this.metadata.id());
        }
        if ("version".equals(query)) {
            return Optional.of((T) this.metadata.version());
        }
        if ("displayName".equals(query)) {
            return (Optional<T>) this.metadata.name();
        }
        if ("description".equals(query)) {
            return (Optional<T>) this.metadata.description();
        }
        return Optional.empty();
    }

    @Override
    public List<? extends IConfigurable> getConfigList(final String... key) {
        return Collections.emptyList();
    }
}
