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
package org.spongepowered.common.registry.type;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import net.minecraft.tileentity.BannerPattern;
import org.spongepowered.api.data.type.BannerPatternShape;
import org.spongepowered.api.data.type.BannerPatternShapes;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.registry.util.AdditionalRegistration;
import org.spongepowered.api.registry.util.RegisterCatalog;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class BannerPatternShapeRegistryModule implements CatalogRegistryModule<BannerPatternShape> {

    @RegisterCatalog(BannerPatternShapes.class)
    private final Map<String, BannerPatternShape> bannerPatternShapeMappings = Maps.newHashMap();

    @Override
    public Optional<BannerPatternShape> getById(String id) {
        return Optional.ofNullable(this.bannerPatternShapeMappings.get(checkNotNull(id).toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<BannerPatternShape> getAll() {
        return ImmutableList.copyOf(this.bannerPatternShapeMappings.values());
    }

    @Override
    public void registerDefaults() {
        for (BannerPattern pattern : BannerPattern.values()) {
            this.bannerPatternShapeMappings.put(pattern.name().toLowerCase(Locale.ENGLISH), (BannerPatternShape) (Object) pattern);
            this.bannerPatternShapeMappings.put(pattern.getHashname().toLowerCase(Locale.ENGLISH), (BannerPatternShape) (Object) pattern);
        }
    }

    @AdditionalRegistration
    public void registerAdditional() {
        for (BannerPattern pattern : BannerPattern.values()) {
            if (!this.bannerPatternShapeMappings.containsKey(pattern.name())) {
                this.bannerPatternShapeMappings.put(pattern.name().toLowerCase(Locale.ENGLISH), (BannerPatternShape) (Object) pattern);
                this.bannerPatternShapeMappings.put(pattern.getHashname().toLowerCase(Locale.ENGLISH), (BannerPatternShape) (Object) pattern);
            }
        }
    }

}
