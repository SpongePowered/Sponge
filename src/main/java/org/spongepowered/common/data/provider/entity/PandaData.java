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

import net.minecraft.world.entity.animal.Panda;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.PandaGene;
import org.spongepowered.common.accessor.world.entity.animal.PandaAccessor;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.SpongeTicks;

public final class PandaData {

    private PandaData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(Panda.class)
                    .create(Keys.HIDDEN_GENE)
                        .get(h -> ((PandaGene) (Object) h.getHiddenGene()))
                        .set((h, v) -> h.setHiddenGene((Panda.Gene) (Object) v))
                    .create(Keys.IS_EATING)
                        .get(Panda::isEating)
                        .set(Panda::eat)
                    .create(Keys.IS_FRIGHTENED)
                        .get(Panda::isScared)
                    .create(Keys.IS_LYING_ON_BACK)
                        .get(Panda::isOnBack)
                        .set(Panda::setOnBack)
                    .create(Keys.IS_ROLLING_AROUND)
                        .get(Panda::isRolling)
                        .set(Panda::roll)
                    .create(Keys.IS_SNEEZING)
                        .get(Panda::isSneezing)
                        .set(Panda::sneeze)
                    .create(Keys.IS_UNHAPPY)
                        .get(h -> h.getUnhappyCounter() > 0)
                    .create(Keys.KNOWN_GENE)
                        .get(h -> ((PandaGene) (Object) h.getMainGene()))
                        .set((h, v) -> h.setMainGene((Panda.Gene) (Object) v))
                    .create(Keys.SNEEZING_TIME)
                        .get(x -> new SpongeTicks(x.getSneezeCounter()))
                        .setAnd((h, v) -> {
                            if (v.isInfinite()) {
                                return false;
                            }
                            h.setSneezeCounter(SpongeTicks.toSaturatedIntOrInfinite(v));
                            return true;
                        })
                    .create(Keys.UNHAPPY_TIME)
                        .get(x -> new SpongeTicks(x.getUnhappyCounter()))
                        .setAnd((h, v) -> {
                            if (v.isInfinite()) {
                                return false;
                            }
                            h.setUnhappyCounter(SpongeTicks.toSaturatedIntOrInfinite(v));
                            return true;
                        })
                .asMutable(PandaAccessor.class)
                    .create(Keys.EATING_TIME)
                        .get(x -> new SpongeTicks(x.invoker$getEatCounter()))
                        .setAnd((h, v) -> {
                            if (v.isInfinite()) {
                                return false;
                            }
                            h.invoker$setEatCounter(SpongeTicks.toSaturatedIntOrInfinite(v));
                            return true;
                        });
    }
    // @formatter:on
}
