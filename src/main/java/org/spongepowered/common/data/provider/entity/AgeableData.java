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

import net.minecraft.world.entity.AgeableMob;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.SpongeTicks;

public final class AgeableData {

    private AgeableData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(AgeableMob.class)
                    .create(Keys.BABY_TICKS)
                        .get(h -> h.getAge() < 0 ? new SpongeTicks(-h.getAge()) : null)
                        .setAnd((h, v) -> {
                            final int ticks = SpongeTicks.toSaturatedIntOrInfinite(v);
                            if (v.isInfinite() || ticks < 0) {
                                return false;
                            }
                            h.setAge(-ticks);
                            return true;
                        })
                    .create(Keys.BREEDING_COOLDOWN)
                        .get(h -> h.getAge() >= 0 ? new SpongeTicks(h.getAge()) : null)
                        .setAnd((h, v) -> {
                            final int ticks = SpongeTicks.toSaturatedIntOrInfinite(v);
                            if (v.isInfinite() || ticks < 0) {
                                return false;
                            }
                            h.setAge(ticks);
                            return true;
                        })
                    .create(Keys.CAN_BREED)
                        .get(h -> h.getAge() == 0)
                        .setAnd((h, v) -> {
                            if (h.getAge() < 0) {
                                return false;
                            }
                            h.setAge(v ? 0 : 6000);
                            return true;
                        })
                    .create(Keys.IS_ADULT)
                        .get(h -> !h.isBaby())
                        .set((h, v) -> h.setAge(v ? Constants.Entity.Ageable.ADULT : Constants.Entity.Ageable.CHILD));
    }
    // @formatter:on
}
