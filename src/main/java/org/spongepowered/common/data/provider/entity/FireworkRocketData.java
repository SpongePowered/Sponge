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

import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.accessor.world.entity.EntityAccessor;
import org.spongepowered.common.accessor.world.entity.projectile.FireworkRocketEntityAccessor;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.FireworkUtil;
import org.spongepowered.common.util.SpongeTicks;

public final class FireworkRocketData {

    private FireworkRocketData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(FireworkRocketEntity.class)
                    .create(Keys.FIREWORK_EFFECTS)
                        .get(h -> FireworkUtil.getFireworkEffects(h).orElse(null))
                        .set(FireworkUtil::setFireworkEffects)
                        .resetOnDelete(ImmutableList.of())
                    .create(Keys.FIREWORK_FLIGHT_MODIFIER)
                        .get(h -> {
                            final ItemStack item = FireworkUtil.getItem(h);
                            final CompoundTag fireworks = item.getOrCreateTagElement(Constants.Item.Fireworks.FIREWORKS);
                            if (fireworks.contains(Constants.Item.Fireworks.FLIGHT)) {
                                return new SpongeTicks(fireworks.getByte(Constants.Item.Fireworks.FLIGHT));
                            }
                            return null;
                        })
                        .setAnd((h, v) -> {
                            final int ticks = SpongeTicks.toSaturatedIntOrInfinite(v);
                            if (v.isInfinite() || ticks < 0 || ticks > Byte.MAX_VALUE) {
                                return false;
                            }
                            final ItemStack item = FireworkUtil.getItem(h);
                            final CompoundTag fireworks = item.getOrCreateTagElement(Constants.Item.Fireworks.FIREWORKS);
                            fireworks.putByte(Constants.Item.Fireworks.FLIGHT, (byte) ticks);
                            ((FireworkRocketEntityAccessor) h).accessor$lifetime(10 * ticks + ((EntityAccessor) h).accessor$random().nextInt(6) + ((EntityAccessor) h).accessor$random().nextInt(7));
                            return true;
                        });
    }
    // @formatter:on
}
