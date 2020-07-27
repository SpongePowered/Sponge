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

import net.minecraft.entity.item.FireworkRocketEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.accessor.entity.EntityAccessor;
import org.spongepowered.common.accessor.entity.item.FireworkRocketEntityAccessor;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.data.provider.util.FireworkUtils;
import org.spongepowered.common.util.Constants;

public final class FireworkRocketData {

    private FireworkRocketData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(FireworkRocketEntity.class)
                    .create(Keys.FIREWORK_EFFECTS)
                        .get(h -> FireworkUtils.getFireworkEffects(h).orElse(null))
                        .set(FireworkUtils::setFireworkEffects)
                    .create(Keys.FIREWORK_FLIGHT_MODIFIER)
                        .get(h -> {
                            final ItemStack item = FireworkUtils.getItem(h);
                            final CompoundNBT fireworks = item.getOrCreateChildTag(Constants.Item.Fireworks.FIREWORKS);
                            if (fireworks.contains(Constants.Item.Fireworks.FLIGHT)) {
                                return (int) fireworks.getByte(Constants.Item.Fireworks.FLIGHT);
                            }
                            return null;
                        })
                        .set((h, v) -> {
                            final ItemStack item = FireworkUtils.getItem(h);
                            final CompoundNBT fireworks = item.getOrCreateChildTag(Constants.Item.Fireworks.FIREWORKS);
                            fireworks.putByte(Constants.Item.Fireworks.FLIGHT, v.byteValue());
                            ((FireworkRocketEntityAccessor) h).accessor$setLifeTime(10 * v.byteValue() + ((EntityAccessor) h).accessor$getRand().nextInt(6) + ((EntityAccessor) h).accessor$getRand().nextInt(7));
                        });
    }
    // @formatter:on
}
