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
package org.spongepowered.common.data.provider.block.entity;

import org.spongepowered.api.data.Keys;
import org.spongepowered.common.accessor.world.level.block.entity.TheEndGatewayBlockEntityAccessor;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.SpongeTicks;
import org.spongepowered.common.util.VecHelper;

public final class EndGatewayData {

    private EndGatewayData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(TheEndGatewayBlockEntityAccessor.class)
                    .create(Keys.COOLDOWN)
                        .get(x -> new SpongeTicks(x.accessor$teleportCooldown()))
                        .setAnd((h, v) -> {
                            final int ticks = SpongeTicks.toSaturatedIntOrInfinite(v);
                            if (v.isInfinite() || ticks < 0) {
                                return false;
                            }
                            h.accessor$teleportCooldown(ticks);
                            return true;
                        })
                    .create(Keys.DO_EXACT_TELEPORT)
                        .get(TheEndGatewayBlockEntityAccessor::accessor$exactTeleport)
                        .set(TheEndGatewayBlockEntityAccessor::accessor$exactTeleport)
                    .create(Keys.END_GATEWAY_AGE)
                        .get(x -> new SpongeTicks(x.accessor$age()))
                        .setAnd((h, v) -> {
                            final int ticks = SpongeTicks.toSaturatedIntOrInfinite(v);
                            if (v.isInfinite() || ticks < 0) {
                                return false;
                            }
                            h.accessor$age(ticks);
                            return true;
                        })
                    .create(Keys.TARGET_POSITION)
                        .get(h -> VecHelper.toVector3i(h.accessor$exitPortal()))
                        .set((h, v) -> h.accessor$exitPortal(VecHelper.toBlockPos(v)));
    }
    // @formatter:on
}
