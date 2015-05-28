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

import com.google.common.collect.Maps;
import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.api.Platform;

import java.util.Map;

public abstract class AbstractPlatform implements Platform {

    private final MinecraftVersion minecraftVersion;
    private final String apiVersion;
    private final String version;

    public AbstractPlatform(MinecraftVersion minecraftVersion, String apiVersion, String version) {
        this.minecraftVersion = minecraftVersion;
        this.apiVersion = apiVersion;
        this.version = version;
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    @Override
    public String getApiVersion() {
        return this.apiVersion;
    }

    @Override
    public MinecraftVersion getMinecraftVersion() {
        return this.minecraftVersion;
    }

    @Override
    public String getName() {
        return "Sponge";
    }

    @Override
    public Map<String, Object> asMap() {
        final Map<String, Object> map = Maps.newHashMap();
        map.put("Name", this.getName());
        map.put("Type", this.getType());
        map.put("ExecutionType", this.getExecutionType());
        map.put("ApiVersion", this.getApiVersion());
        map.put("ImplementationVersion", this.getVersion());
        map.put("MinecraftVersion", this.getMinecraftVersion());
        return map;
    }

}
