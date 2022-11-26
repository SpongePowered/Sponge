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
package org.spongepowered.common.data.provider.entity;

import net.minecraft.world.entity.animal.TropicalFish;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.TropicalFishShape;
import org.spongepowered.common.accessor.world.entity.animal.TropicalFishAccessor;
import org.spongepowered.common.data.provider.DataProviderRegistrator;

public final class TropicalFishData {

    private TropicalFishData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(TropicalFish.class)
                    // Fish variant is: size | pattern << 8 | bodyColor << 16 | patternColor << 24
                    .create(Keys.TROPICAL_FISH_SHAPE)
                        .get(h -> (TropicalFishShape) (Object) h.getVariant())
                        .set((h, v) -> h.setVariant((TropicalFish.Pattern) (Object) v))
                    .create(Keys.DYE_COLOR)
                        .get(h -> (DyeColor) (Object) h.getBaseColor())
                        .set((h, v) -> {
                            final net.minecraft.world.item.DyeColor patternColor = h.getPatternColor();
                            final TropicalFish.Pattern type = h.getVariant();
                            final int variant = TropicalFishAccessor.invoker$packVariant(type, patternColor, (net.minecraft.world.item.DyeColor) (Object) v);
                            ((TropicalFishAccessor) h).invoker$setPackedVariant(variant);
                        })
                    .create(Keys.PATTERN_COLOR)
                        .get(h -> (DyeColor) (Object) h.getPatternColor())
                        .set((h, v) -> {
                            final net.minecraft.world.item.DyeColor baseColor = h.getBaseColor();
                            final TropicalFish.Pattern type = h.getVariant();
                            final int variant = TropicalFishAccessor.invoker$packVariant(type, (net.minecraft.world.item.DyeColor) (Object) v, baseColor);
                            ((TropicalFishAccessor) h).invoker$setPackedVariant(variant);
                        });
    }
    // @formatter:on

}
