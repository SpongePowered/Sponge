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

import com.google.common.collect.Streams;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.item.alchemy.PotionContents;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.util.Color;
import org.spongepowered.common.accessor.world.entity.AreaEffectCloudAccessor;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.effect.particle.SpongeParticleHelper;
import org.spongepowered.common.util.PotionEffectUtil;
import org.spongepowered.common.util.SpongeTicks;

import java.util.Optional;

public final class AreaEffectCloudData {

    private AreaEffectCloudData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(AreaEffectCloud.class)
                    .create(Keys.COLOR)
                        .get(h -> Color.ofRgb(((AreaEffectCloudAccessor) h).accessor$potionContents().getColor()))
                        .set((h, v) -> {
                            var contents = ((AreaEffectCloudAccessor) h).accessor$potionContents();
                            h.setPotionContents(new PotionContents(contents.potion(), Optional.of(v.rgb()), contents.customEffects()));
                        })
                    .create(Keys.DURATION)
                        .get(x -> new SpongeTicks(x.getDuration()))
                        .setAnd((h, v) -> {
                            final int ticks = SpongeTicks.toSaturatedIntOrInfinite(v);
                            if (v.isInfinite() || ticks < 0) {
                                return false;
                            }
                            h.setDuration(ticks);
                            return true;
                        })
                    .create(Keys.PARTICLE_EFFECT)
                        .get(h -> SpongeParticleHelper.spongeParticleOptions(h.getParticle()))
                        .set((h, v) -> h.setParticle(SpongeParticleHelper.vanillaParticleOptions(v)))
                    .create(Keys.RADIUS)
                        .get(h -> (double) h.getRadius())
                        .set((h, v) -> h.setRadius(v.floatValue()))
                    .create(Keys.RADIUS_ON_USE)
                        .get(h -> (double) ((AreaEffectCloudAccessor) h).accessor$radiusOnUse())
                        .set((h, v) -> h.setRadiusOnUse(v.floatValue()))
                    .create(Keys.RADIUS_PER_TICK)
                        .get(h -> (double) ((AreaEffectCloudAccessor) h).accessor$radiusPerTick())
                        .set((h, v) -> h.setRadiusPerTick(v.floatValue()))
                    .create(Keys.WAIT_TIME)
                        .get(h -> new SpongeTicks(((AreaEffectCloudAccessor) h).accessor$waitTime()))
                        .setAnd((h, v) -> {
                            if (v.isInfinite()) {
                                return false;
                            }
                            h.setWaitTime(SpongeTicks.toSaturatedIntOrInfinite(v));
                            return true;
                        })
                .asMutable(AreaEffectCloudAccessor.class)
                    .create(Keys.DURATION_ON_USE)
                        .get(h -> new SpongeTicks(h.accessor$durationOnUse()))
                        .setAnd((h, v) -> {
                            if (v.isInfinite()) {
                                return false;
                            }
                            h.accessor$durationOnUse(SpongeTicks.toSaturatedIntOrInfinite(v));
                            return true;
                        })
                    .create(Keys.POTION_EFFECTS)
                        .get(h -> PotionEffectUtil.copyAsPotionEffects(Streams.stream(h.accessor$potionContents().getAllEffects()).toList()))
                        .set((h, v) -> {
                            final PotionContents contents = h.accessor$potionContents();
                            ((AreaEffectCloud) h).setPotionContents(new PotionContents(contents.potion(), contents.customColor(), PotionEffectUtil.copyAsEffectInstances(v)));
                        })
                    .create(Keys.REAPPLICATION_DELAY)
                        .get(h -> new SpongeTicks(h.accessor$reapplicationDelay()))
                        .setAnd((h, v) -> {
                            if (v.isInfinite()) {
                                return false;
                            }
                            h.accessor$reapplicationDelay(SpongeTicks.toSaturatedIntOrInfinite(v));
                            return true;
                        });
    }
    // @formatter:on
}
