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

import net.kyori.adventure.bossbar.BossBar;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.WitherEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.accessor.entity.boss.WitherEntityAccessor;
import org.spongepowered.common.data.provider.DataProviderRegistrator;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class WitherData {

    private WitherData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(WitherEntity.class)
                    .create(Keys.WITHER_TARGETS)
                        .get(h -> Stream.of(h.getWatchedTargetId(0), h.getWatchedTargetId(1), h.getWatchedTargetId(2))
                                .map(id -> h.getEntityWorld().getEntityByID(id))
                                // TODO filter null?                .filter(Objects::nonNull)
                                .map(org.spongepowered.api.entity.Entity.class::cast)
                                .collect(Collectors.toList())
                        )
                        .set((h, v) -> {
                            for (int i = 0; i < v.size(); i++) {
                                if (i > 2) { // only 3 heads
                                    break;
                                }
                                final Entity target = (Entity) v.get(i);
                                h.updateWatchedTargetId(i, target == null ? 0 : target.getEntityId());
                            }
                        })
                .asMutable(WitherEntityAccessor.class)
                    .create(Keys.BOSS_BAR)
                        .get(h -> (BossBar) h.accessor$bossEvent());
    }
    // @formatter:on
}
