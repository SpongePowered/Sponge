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
package org.spongepowered.common.registry.type.world.dimension;

import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.world.Dimension;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.type.DimensionConfig;

import java.nio.file.Path;

public final class GlobalDimensionType implements DimensionType {

    private final CatalogKey key;
    private final Class<? extends net.minecraft.world.dimension.Dimension> dimensionClass;
    private Path configPath;
    private SpongeConfig<DimensionConfig> config;
    private volatile Context context;

    public GlobalDimensionType(CatalogKey key, Class<? extends net.minecraft.world.dimension.Dimension> dimensionClass) {
        this.key = key;
        this.dimensionClass = dimensionClass;
        this.configPath = SpongeImpl.getSpongeConfigDir().resolve("worlds").resolve(this.key.getNamespace()).resolve(this.key.getValue());
        this.config = new SpongeConfig<>(SpongeConfig.Type.DIMENSION, this.configPath.resolve("dimension.conf"), SpongeImpl.ECOSYSTEM_ID, SpongeImpl.getGlobalConfig());
        this.context = new Context(Context.DIMENSION_KEY, this.key.toString().replace(":", "."));
    }

    @Override
    public CatalogKey getKey() {
        return this.key;
    }

    @Override
    public Class<? extends Dimension> getDimensionClass() {
        return (Class<? extends Dimension>) this.dimensionClass;
    }

    public Path getConfigPath() {
        return this.configPath;
    }

    public SpongeConfig<DimensionConfig> getConfig() {
        return this.config;
    }

    public Context getContext() {
        return this.context;
    }
}
