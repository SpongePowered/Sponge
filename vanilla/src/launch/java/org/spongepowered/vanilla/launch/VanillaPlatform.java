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
package org.spongepowered.vanilla.launch;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.common.SpongePlatform;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.plugin.PluginContainer;

import java.util.HashMap;
import java.util.Map;

@Singleton
public final class VanillaPlatform extends SpongePlatform {

    @Inject
    public VanillaPlatform(final MinecraftVersion minecraftVersion) {
        super(minecraftVersion);

        final PluginContainer apiPlugin = Launch.instance().apiPlugin();
        final PluginContainer platformPlugin = Launch.instance().platformPlugin();

        final PluginContainer common = Launch.instance().commonPlugin();
        this.platformMap.put("Type", this.type());
        this.platformMap.put("ApiName", apiPlugin.metadata().name());
        this.platformMap.put("ApiVersion", apiPlugin.metadata().version());
        this.platformMap.put("CommonName", common.metadata().name());
        this.platformMap.put("CommonVersion", common.metadata().version());
        this.platformMap.put("ImplementationName", platformPlugin.metadata().name());
        this.platformMap.put("ImplementationVersion", platformPlugin.metadata().version());
        this.platformMap.put("MinecraftVersion", this.minecraftVersion());
    }

    @Override
    public final Map<String, Object> asMap() {
        return this.platformMap;
    }
}
