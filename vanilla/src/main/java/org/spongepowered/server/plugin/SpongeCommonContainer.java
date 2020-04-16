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

import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.plugin.meta.PluginMetadata;
import org.spongepowered.server.launch.plugin.PluginSource;

public class SpongeCommonContainer {


    public static void register(MetadataContainer metadata) {
        SpongeImpl.setSpongePlugin(create(metadata));
    }

    private static PluginContainer create(MetadataContainer metadata) {
        PluginMetadata meta = metadata.get(SpongeImpl.ECOSYSTEM_ID, SpongeImpl.ECOSYSTEM_NAME);
        return new MetaPluginContainer(meta, PluginSource.find(SpongeImpl.class));
    }
}
