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

import net.minecraft.entity.passive.horse.HorseEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.HorseColor;
import org.spongepowered.api.data.type.HorseStyle;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.entity.passive.horse.HorseEntityAccessor;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.data.type.SpongeHorseColor;
import org.spongepowered.common.data.type.SpongeHorseStyle;
import org.spongepowered.common.registry.MappedRegistry;

public final class HorseData {

    private HorseData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(HorseEntity.class)
                    .create(Keys.HORSE_COLOR)
                        .get(h -> {
                            final MappedRegistry<HorseColor, Integer> registry = SpongeCommon.getRegistry().getCatalogRegistry().getRegistry(HorseColor.class);
                            return registry.getReverseMapping(HorseData.getHorseColor(h));
                        })
                        .set((h, v) -> {
                            final int style = HorseData.getHorseStyle(h);
                            ((HorseEntityAccessor) h).invoker$setTypeVariant(((SpongeHorseColor) v).getMetadata() | style);
                        })
                    .create(Keys.HORSE_STYLE)
                        .get(h -> {
                            final MappedRegistry<HorseStyle, Integer> registry = SpongeCommon.getRegistry().getCatalogRegistry().getRegistry(HorseStyle.class);
                            return registry.getReverseMapping(HorseData.getHorseStyle(h));
                        })
                        .set((h, v) -> {
                            final int color = HorseData.getHorseColor(h);
                            ((HorseEntityAccessor) h).invoker$setTypeVariant((color | ((SpongeHorseStyle) v).getBitMask()));
                        });
    }
    // @formatter:on

    private static int getHorseColor(final HorseEntity holder) {
        return ((HorseEntityAccessor) holder).invoker$getTypeVariant() & 0xFF;
    }

    private static int getHorseStyle(final HorseEntity holder) {
        return (((HorseEntityAccessor) holder).invoker$getTypeVariant() & 0xFF00) >> 8;
    }
}
