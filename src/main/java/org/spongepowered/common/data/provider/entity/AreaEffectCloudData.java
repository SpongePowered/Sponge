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

import net.minecraft.entity.AreaEffectCloudEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.util.Color;
import org.spongepowered.common.accessor.entity.AreaEffectCloudEntityAccessor;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.data.util.PotionEffectHelper;
import org.spongepowered.common.util.MissingImplementationException;
import org.spongepowered.common.util.SpongeTicks;

public final class AreaEffectCloudData {

    private AreaEffectCloudData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(AreaEffectCloudEntity.class)
                    .create(Keys.COLOR)
                        .get(h -> Color.ofRgb(h.getColor()))
                        .set((h, v) -> h.setColor(v.getRgb()))
                    .create(Keys.DURATION)
                        .get(x -> new SpongeTicks(x.getDuration()))
                        .setAnd((h, v) -> {
                            final int ticks = (int) v.getTicks();
                            if (ticks < 0) {
                                return false;
                            }
                            h.setDuration(ticks);
                            return true;
                        })
                    .create(Keys.PARTICLE_EFFECT)
                        .get(h -> {
                            throw new MissingImplementationException("AreaEffectCloudData", "PARTICLE_EFFECT::getter");
                        })
                        .set((h, v) -> {
                            throw new MissingImplementationException("AreaEffectCloudData", "PARTICLE_EFFECT::setter");
                        })
                    .create(Keys.RADIUS)
                        .get(h -> (double) h.getRadius())
                        .set((h, v) -> h.setRadius(v.floatValue()))
                    .create(Keys.RADIUS_ON_USE)
                        .get(h -> (double) ((AreaEffectCloudEntityAccessor) h).accessor$getRadiusOnUse())
                        .set((h, v) -> h.setRadiusOnUse(v.floatValue()))
                    .create(Keys.RADIUS_PER_TICK)
                        .get(h -> (double) ((AreaEffectCloudEntityAccessor) h).accessor$getRadiusPerTick())
                        .set((h, v) -> h.setRadiusPerTick(v.floatValue()))
                    .create(Keys.WAIT_TIME)
                        .get(h -> new SpongeTicks(((AreaEffectCloudEntityAccessor) h).accessor$getWaitTime()))
                        .set((h, v) -> h.setWaitTime((int) v.getTicks()))
                .asMutable(AreaEffectCloudEntityAccessor.class)
                    .create(Keys.DURATION_ON_USE)
                        .get(h -> new SpongeTicks(h.accessor$getDurationOnUse()))
                        .set((h, v) -> h.accessor$setDurationOnUse((int) v.getTicks()))
                    .create(Keys.POTION_EFFECTS)
                        .get(h -> PotionEffectHelper.copyAsPotionEffects(h.accessor$getEffects()))
                        .set((h, v) -> h.accessor$setEffects(PotionEffectHelper.copyAsEffectInstances(v)))
                    .create(Keys.REAPPLICATION_DELAY)
                        .get(h -> new SpongeTicks(h.accessor$getReapplicationDelay()))
                        .set((h, v) -> h.accessor$setReapplicationDelay((int) v.getTicks()));
    }
    // @formatter:on
}
