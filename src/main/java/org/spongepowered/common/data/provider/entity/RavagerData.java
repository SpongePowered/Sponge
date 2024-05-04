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
import org.spongepowered.common.accessor.world.entity.monster.RavagerAccessor;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.SpongeTicks;

public final class RavagerData {

    private RavagerData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(RavagerAccessor.class)
                    .create(Keys.ATTACK_TIME)
                        .get(x -> new SpongeTicks(x.accessor$attackTick()))
                        .setAnd((h, v) -> {
                            if (v.isInfinite()) {
                                return false;
                            }
                            h.accessor$attackTick(SpongeTicks.toSaturatedIntOrInfinite(v));
                            return true;
                        })
                    .create(Keys.IS_IMMOBILIZED)
                        .get(h -> h.accessor$attackTick() > 0 || h.accessor$stunnedTick() > 0 || h.accessor$roarTick() > 0)
                    .create(Keys.IS_ROARING)
                        .get(h -> h.accessor$roarTick() > 0)
                    .create(Keys.IS_STUNNED)
                        .get(h -> h.accessor$stunnedTick() > 0)
                    .create(Keys.ROARING_TIME)
                        .get(x -> new SpongeTicks(x.accessor$roarTick()))
                        .setAnd((h, v) -> {
                            if (v.isInfinite()) {
                                return false;
                            }
                            h.accessor$roarTick(SpongeTicks.toSaturatedIntOrInfinite(v));
                            return true;
                        })
                    .create(Keys.STUNNED_TIME)
                        .get(x -> new SpongeTicks(x.accessor$stunnedTick()))
                        .setAnd((h, v) -> {
                            if (v.isInfinite()) {
                                return false;
                            }
                            h.accessor$stunnedTick(SpongeTicks.toSaturatedIntOrInfinite(v));
                            return true;
                        });
    }
    // @formatter:on
}
