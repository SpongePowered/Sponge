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
package org.spongepowered.common;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.plugin.PluginContainer;

import java.util.HashMap;
import java.util.Map;

@Singleton
public final class SpongePlatform implements Platform {

    private final PluginContainer apiPlugin;
    private final PluginContainer platformPlugin;
    private final PluginContainer minecraftPlugin;
    private final MinecraftVersion minecraftVersion;

    protected final Map<String, Object> platformMap = new HashMap<String, Object>() {

        private static final long serialVersionUID = 7022397614988467398L;

        @Override
        public Object put(String key, Object value) {
            checkArgument(!this.containsKey(key), "Cannot set the value of the existing key %s", key);
            return super.put(key, value);
        }
    };

    @Inject
    public SpongePlatform(MinecraftVersion minecraftVersion) {
        this.minecraftVersion = checkNotNull(minecraftVersion, "minecraftVersion");

        this.apiPlugin = Launch.instance().apiPlugin();
        this.platformPlugin = Launch.instance().platformPlugin();
        this.minecraftPlugin = Launch.instance().minecraftPlugin();

        final PluginContainer common = Launch.instance().commonPlugin();
        this.platformMap.put("Type", this.type());
        this.platformMap.put("ApiName", this.apiPlugin.metadata().name());
        this.platformMap.put("ApiVersion", this.apiPlugin.metadata().version());
        this.platformMap.put("CommonName", common.metadata().name());
        this.platformMap.put("CommonVersion", common.metadata().version());
        this.platformMap.put("ImplementationName", this.platformPlugin.metadata().name());
        this.platformMap.put("ImplementationVersion", this.platformPlugin.metadata().version());
        this.platformMap.put("MinecraftVersion", this.minecraftVersion());
    }

    @Override
    public Type type() {
        return !Launch.instance().dedicatedServer() ? Type.CLIENT : Type.SERVER;
    }

    @Override
    public Type executionType() {
        if (Sponge.isServerAvailable() && Sponge.server().onMainThread()) {
            return Type.SERVER;
        }
        if (Sponge.isClientAvailable() && Sponge.client().onMainThread()) {
            return Type.CLIENT;
        }

        return Type.UNKNOWN;
    }

    @Override
    public PluginContainer container(Component component) {
        switch (component) {
            case API:
                return this.apiPlugin;
            case IMPLEMENTATION:
                return this.platformPlugin;
            case GAME:
                return this.minecraftPlugin;
            default:
                throw new AssertionError("Unknown platform component: " + component);
        }
    }

    @Override
    public final MinecraftVersion minecraftVersion() {
        return this.minecraftVersion;
    }

    @Override
    public final Map<String, Object> asMap() {
        return this.platformMap;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", this.type())
                .add("executionType", this.executionType())
                .add("api", this.apiPlugin.metadata().id())
                .add("implementation", this.platformPlugin.metadata().id())
                .add("minecraftVersion", this.minecraftVersion())
                .toString();
    }
}
