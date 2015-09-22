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

import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.api.Platform;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractPlatform implements Platform {

    private final MinecraftVersion minecraftVersion;
    private final String apiVersion;
    private final String version;

    protected final Map<String, Object> platformMap = new HashMap<String, Object>() {

        private static final long serialVersionUID = 4481663796339419546L;

        @Override
        public Object put(String key, Object value) {
            checkArgument(!this.containsKey(key), "Cannot set the value of the existing key " + key);
            return super.put(key, value);
        }
    };

    public AbstractPlatform(MinecraftVersion minecraftVersion, String apiVersion, String version) {
        this.minecraftVersion = minecraftVersion;
        this.apiVersion = apiVersion;
        this.version = version;

        this.platformMap.put("Name", this.getName());
        this.platformMap.put("Type", this.getType());
        this.platformMap.put("ExecutionType", this.getExecutionType());
        this.platformMap.put("ApiVersion", this.getApiVersion());
        this.platformMap.put("ImplementationVersion", this.getVersion());
        this.platformMap.put("MinecraftVersion", this.getMinecraftVersion());
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
    public final Map<String, Object> asMap() {
        return this.platformMap;
    }

}
