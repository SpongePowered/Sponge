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

import net.minecraft.entity.passive.fish.TropicalFishEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.common.data.provider.DataProviderRegistrator;

public final class TropicalFishData {

    private TropicalFishData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(TropicalFishEntity.class)
                    // Fish variant is: size | pattern << 8 | bodyColor << 16 | patternColor << 24
                    .create(Keys.DYE_COLOR)
                        .get(h -> (DyeColor) (Object) net.minecraft.item.DyeColor.byId((h.getVariant() >> 16) & 0xFF))
                        .set((h, v) -> {
                            final int dyeId = ((net.minecraft.item.DyeColor) (Object) h).getId() << 16;
                            h.setVariant(h.getVariant() & 0xFF00FFFF | dyeId << 16);
                        })
                    .create(Keys.PATTERN_COLOR)
                        .get(h -> (DyeColor) (Object) net.minecraft.item.DyeColor.byId((h.getVariant() >> 24) & 0xFF))
                        .set((h, v) -> {
                            final int dyeId = ((net.minecraft.item.DyeColor) (Object) h).getId() << 24;
                            h.setVariant(h.getVariant() & 0x00FFFFFF | dyeId << 24);
                        });
    }
    // @formatter:on
}
