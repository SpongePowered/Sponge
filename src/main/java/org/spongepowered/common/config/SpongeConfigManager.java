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
import org.spongepowered.api.config.ConfigManager;
import org.spongepowered.api.config.ConfigRoot;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.plugin.PluginContainerExtension;

/**
 * Implementation of service to manage configurations.
 */
@Singleton
public final class SpongeConfigManager implements ConfigManager {

    @Override
    public ConfigRoot getSharedConfig(PluginContainer container) {
        return new SpongeConfigRoot(getMapperFactory(container), container.getId().toLowerCase(), SpongeImpl.getPluginConfigDir());
    }

    @Override
    public ConfigRoot getPluginConfig(PluginContainer container) {
        return new SpongeConfigRoot(getMapperFactory(container), container.getId().toLowerCase(), SpongeImpl.getPluginConfigDir().resolve(container.getId().toLowerCase()));
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
