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
package org.spongepowered.common.service.config;

import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.plugin.PluginManager;
import org.spongepowered.api.service.config.ConfigRoot;
import org.spongepowered.api.service.config.ConfigService;

import java.io.File;
import java.util.Optional;

import javax.inject.Inject;

/**
 * Implementation of service to manage configurations.
 */
public class SpongeConfigService implements ConfigService {
    private static final File CONFIG_ROOT = new File("config");
    private final PluginManager manager;

    @Inject
    public SpongeConfigService(PluginManager manager) {
        this.manager = manager;
    }

    @Override
    public ConfigRoot getSharedConfig(Object instance) {
        return getSharedRoot(containerFromInstance(instance));
    }

    @Override
    public ConfigRoot getPluginConfig(Object instance) {
        return getPrivateRoot(containerFromInstance(instance));
    }

    private PluginContainer containerFromInstance(Object instance) {
        Optional<PluginContainer> container = this.manager.fromInstance(instance);
        if (container.isPresent()) {
            return container.get();
        } else {
            throw new IllegalArgumentException("No container available for instance " + instance + ", is this actually a plugin?");
        }
    }

    public static ConfigRoot getSharedRoot(PluginContainer container) {
        final String name = container.getId().toLowerCase();
        return new SpongeConfigRoot(name, CONFIG_ROOT);

    }

    public static ConfigRoot getPrivateRoot(PluginContainer container) {
        final String name = container.getId().toLowerCase();
        return new SpongeConfigRoot(name, new File(CONFIG_ROOT, name));
    }
}
