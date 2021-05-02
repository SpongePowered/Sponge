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

import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.TropicalFishShape;
import org.spongepowered.common.accessor.world.entity.animal.TropicalFishAccessor;
import org.spongepowered.common.data.provider.DataProviderRegistrator;

import net.minecraft.world.entity.animal.TropicalFish;

public final class TropicalFishData {

    private TropicalFishData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(TropicalFish.class)
                    // Fish variant is: size | pattern << 8 | bodyColor << 16 | patternColor << 24
                    .create(Keys.TROPICAL_FISH_SHAPE)
                        .get(h -> (TropicalFishShape) (Object) TropicalFishData.getType(h))
                        .set((h, v) -> {
                            final net.minecraft.world.item.DyeColor baseColor = TropicalFishData.getBaseColor(h);
                            final net.minecraft.world.item.DyeColor patternColor = TropicalFishData.getPatternColor(h);
                            h.setVariant(((TropicalFishAccessor.invoker$calculateVariant(((TropicalFish.Pattern) (Object) v), patternColor, baseColor))));
                        })
                    .create(Keys.DYE_COLOR)
                        .get(h -> (DyeColor) (Object) TropicalFishData.getBaseColor(h))
                        .set((h, v) -> {
                            final net.minecraft.world.item.DyeColor patternColor = TropicalFishData.getPatternColor(h);
                            final TropicalFish.Pattern type = TropicalFishData.getType(h);
                            h.setVariant(((TropicalFishAccessor.invoker$calculateVariant(type, patternColor, (net.minecraft.world.item.DyeColor) (Object) v))));
                        })
                    .create(Keys.PATTERN_COLOR)
                        .get(h -> (DyeColor) (Object) TropicalFishData.getPatternColor(h))
                        .set((h, v) -> {
                            final net.minecraft.world.item.DyeColor baseColor = TropicalFishData.getBaseColor(h);
                            final TropicalFish.Pattern type = TropicalFishData.getType(h);
                            h.setVariant(((TropicalFishAccessor.invoker$calculateVariant(type, (net.minecraft.world.item.DyeColor) (Object) v, baseColor))));
                        });
    }
    // @formatter:on

    private static net.minecraft.world.item.DyeColor getBaseColor(final TropicalFish fishy) {
        return net.minecraft.world.item.DyeColor.byId((fishy.getVariant() & 16711680) >> 16);
    }

    private static net.minecraft.world.item.DyeColor getPatternColor(final TropicalFish fishy) {
        return net.minecraft.world.item.DyeColor.byId((fishy.getVariant() & -16777216) >> 24);
    }

    private static int getSize(final TropicalFish fishy) {
        return Math.min(fishy.getVariant() & 255, 1);
    }

    private static int getPattern(final TropicalFish fishy) {
        return Math.min(fishy.getVariant() & '\uff00' >> 8, 5);
    }

    private static TropicalFish.Pattern getType(final TropicalFish fishy) {
        return TropicalFish.Pattern.values()[TropicalFishData.getSize(fishy) + 6 * TropicalFishData.getPattern(fishy)];
    }

}
