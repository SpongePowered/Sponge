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
package org.spongepowered.vanilla.applaunch.plugin;

import cpw.mods.jarhandling.SecureJar;
import org.spongepowered.plugin.PluginResource;

import java.util.Locale;

public enum ResourceType {
    SERVICE, // service layer
    LANGUAGE, // plugin layer
    PLUGIN; // game layer

    public static final String PROPERTY_NAME = "Resource-Type";

    public static ResourceType of(final PluginResource resource) {
        return ResourceType.fromName(resource.property(PROPERTY_NAME).orElse(null));
    }

    public static ResourceType of(final SecureJar jar) {
        return ResourceType.fromName(jar.moduleDataProvider().getManifest().getMainAttributes().getValue(PROPERTY_NAME));
    }

    public static ResourceType fromName(final String name) {
        return name == null ? ResourceType.PLUGIN : ResourceType.valueOf(name.toUpperCase(Locale.ROOT));
    }
}
