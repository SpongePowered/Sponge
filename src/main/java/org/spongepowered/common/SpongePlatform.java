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
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.plugin.PluginManager;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class SpongePlatform implements Platform {

    private final PluginContainer api;
    private final PluginContainer common;
    private final PluginContainer impl;
    private final PluginContainer minecraft;
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
    public SpongePlatform(PluginManager manager, MinecraftVersion minecraftVersion) {
        this(manager, manager.getPlugin(SpongeImplHooks.getImplementationId()).get(), minecraftVersion);
    }

    // For SpongeForge (implementation container isn't registered when SpongePlatform is initialized)
    protected SpongePlatform(PluginManager manager,  PluginContainer impl, MinecraftVersion minecraftVersion) {
        this.api = manager.getPlugin(Platform.API_ID).get();
        this.common = manager.getPlugin(SpongeImpl.ECOSYSTEM_ID).get();
        this.impl = checkNotNull(impl, "impl");
        this.minecraft = manager.getPlugin(SpongeImpl.GAME_ID).get();
        this.minecraftVersion = checkNotNull(minecraftVersion, "minecraftVersion");

        this.platformMap.put("Type", this.getType());
        this.platformMap.put("ApiName", this.api.getName());
        this.platformMap.put("ApiVersion", this.api.getVersion());
        this.platformMap.put("CommonName", this.common.getName());
        this.platformMap.put("CommonVersion", this.common.getVersion());
        this.platformMap.put("ImplementationName", this.impl.getName());
        this.platformMap.put("ImplementationVersion", this.impl.getVersion());
        this.platformMap.put("MinecraftVersion", this.getMinecraftVersion());
    }

    // For SpongeCommon we assume that we are always on the server
    // SpongeForge overrides this to return CLIENT when running in a client environment

    public PluginContainer getCommon() {
        return this.common;
    }

    @Override
    public Type getType() {
        return Type.SERVER;
    }

    @Override
    public Type getExecutionType() {
        return Type.SERVER;
    }

    @Override
    public PluginContainer getContainer(Component component) {
        switch (component) {
            case API:
                return this.api;
            case IMPLEMENTATION:
                return this.impl;
            case GAME:
                return this.minecraft;
            default:
                throw new AssertionError("Unknown platform component: " + component);
        }
    }

    @Override
    public final MinecraftVersion getMinecraftVersion() {
        return this.minecraftVersion;
    }

    @Override
    public final Map<String, Object> asMap() {
        return this.platformMap;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", getType())
                .add("executionType", getExecutionType())
                .add("api", this.api)
                .add("impl", this.impl)
                .add("minecraftVersion", getMinecraftVersion())
                .toString();
    }

}
