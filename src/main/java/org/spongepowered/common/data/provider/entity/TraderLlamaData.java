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

import net.minecraft.world.entity.Mob;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.common.accessor.world.entity.MobAccessor;
import org.spongepowered.common.accessor.world.entity.animal.horse.TraderLlamaAccessor;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.SpongeTicks;

public final class TraderLlamaData {

    private TraderLlamaData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(TraderLlamaAccessor.class)
                    .create(Keys.DESPAWN_DELAY)
                        .get(h -> ((Mob) h).isPersistenceRequired()
                                ? Ticks.infinite()
                                : Ticks.of(h.accessor$despawnDelay()))
                        .setAnd((h, v) -> {
                            if (v.isInfinite()) {
                                ((Mob) v).setPersistenceRequired();
                                return true;
                            }
                            if (v.ticks() < 0) {
                                return false;
                            }
                            ((MobAccessor) h).accessor$persistenceRequired(false);
                            h.accessor$despawnDelay(SpongeTicks.toSaturatedIntOrInfinite(v));
                            return true;
                        });
    }
    // @formatter:on
}
