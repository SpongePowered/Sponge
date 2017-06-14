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
package org.spongepowered.common.config;

import com.google.inject.Injector;
import com.google.inject.Singleton;
import ninja.leaping.configurate.objectmapping.DefaultObjectMapperFactory;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import ninja.leaping.configurate.objectmapping.ObjectMapperFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigManager;
import org.spongepowered.api.config.ConfigRoot;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.plugin.PluginContainerExtension;

import java.util.Optional;

/**
 * Implementation of service to manage configurations.
 */
@Singleton
public class SpongeConfigManager implements ConfigManager {

    @Override
    public ConfigRoot getSharedConfig(Object instance) {
        return getSharedRoot(containerFromInstance(instance));
    }

    @Override
    public ConfigRoot getPluginConfig(Object instance) {
        return getPrivateRoot(containerFromInstance(instance));
    }

    private PluginContainer containerFromInstance(Object instance) {
        Optional<PluginContainer> container = Sponge.getPluginManager().fromInstance(instance);
        if (container.isPresent()) {
            return container.get();
        }
        throw new IllegalArgumentException("No container available for instance " + instance + ", is this actually a plugin?");
    }

    public static ConfigRoot getSharedRoot(PluginContainer container) {
        final String name = container.getId().toLowerCase();
        return new SpongeConfigRoot(getMapperFactory(container), name, SpongeImpl.getPluginConfigDir());
    }

    public static ConfigRoot getPrivateRoot(PluginContainer container) {
        final String name = container.getId().toLowerCase();
        return new SpongeConfigRoot(getMapperFactory(container), name, SpongeImpl.getPluginConfigDir().resolve(name));
    }

    private static ObjectMapperFactory getMapperFactory(PluginContainer container) {
        if (container instanceof PluginContainerExtension) {
            Injector injector = ((PluginContainerExtension) container).getInjector();
            if (injector != null) {
                return injector.getInstance(GuiceObjectMapperFactory.class);
            }
        }
        return DefaultObjectMapperFactory.getInstance();
    }

}
