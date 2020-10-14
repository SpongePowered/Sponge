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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Singleton;
import org.spongepowered.api.CatalogType;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.api.config.ConfigManager;
import org.spongepowered.api.config.ConfigRoot;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.reference.WatchServiceListener;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.plugin.PluginContainer;

import java.io.IOException;

import javax.inject.Inject;

/**
 * Implementation of service to manage configurations.
 */
@Singleton
public final class PluginConfigManager implements ConfigManager {

    private final TypeSerializerCollection serializers;
    private final WatchServiceListener listener;

    @Inject
    PluginConfigManager(final CatalogTypeTypeSerializer catalogSerializer, final DataSerializableTypeSerializer dataSerializableSerializer) throws IOException {
        // TODO: Move this onto the async scheduler, rather than shared FJ pool?
        this.listener = WatchServiceListener.builder()
                .threadFactory(new ThreadFactoryBuilder().setDaemon(true).setNameFormat("SpongeCommon-WatchService-%d").build())
                .build();

        this.serializers = TypeSerializerCollection.defaults().childBuilder()
                .register(CatalogType.class, catalogSerializer)
                // We have a separate type serializer for CatalogTypes, so we explicitly discount them here.
                // See https://github.com/SpongePowered/SpongeCommon/issues/1348
                .register(DataSerializableTypeSerializer.predicate(), dataSerializableSerializer)
                // TODO: register DataTranslators?
                .registerAll(SpongeAdventure.CONFIGURATE.serializers())
                .build();
    }

    @Override
    public ConfigRoot getSharedConfig(final PluginContainer container) {
        return new PluginConfigRoot(this.serializers, container.getMetadata().getId().toLowerCase(),
                                    SpongeCommon.getPluginConfigDirectory());
    }

    @Override
    public ConfigRoot getPluginConfig(final PluginContainer container) {
        return new PluginConfigRoot(this.serializers, container.getMetadata().getId().toLowerCase(),
                                    SpongeCommon.getPluginConfigDirectory().resolve(container.getMetadata().getId().toLowerCase()));
    }

    @Override
    public TypeSerializerCollection getSerializers() {
        return this.serializers;
    }

    @Override
    public WatchServiceListener getWatchServiceListener() {
        return this.listener;
    }

    public static ConfigurationOptions getOptions(final TypeSerializerCollection serializers) {
        return ConfigurationOptions.defaults()
                .implicitInitialization(true)
                .shouldCopyDefaults(true)
                .serializers(serializers);
    }

    public void close() throws IOException {
        this.listener.close();
    }
}
