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
package org.spongepowered.neoforge.applaunch.loading.metadata;

import net.neoforged.neoforgespi.language.IConfigurable;
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

        return switch (key[0]) {
            case "modId" -> Optional.of((T) this.metadata.id());
            case "version" -> Optional.of((T) this.metadata.version().toString());
            case "displayName" -> (Optional<T>) this.metadata.name();
            case "description" -> (Optional<T>) this.metadata.description();
            case "displayTest" -> Optional.of((T) "IGNORE_SERVER_VERSION");
            default -> Optional.empty();
        };
    }

    @Override
    public List<? extends IConfigurable> getConfigList(final String... key) {
        return Collections.emptyList();
    }
}
