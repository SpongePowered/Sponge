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

import net.minecraft.entity.passive.PandaEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.PandaGene;
import org.spongepowered.common.accessor.entity.passive.PandaEntityAccessor;
import org.spongepowered.common.data.provider.DataProviderRegistrator;

public final class PandaData {

    private PandaData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(PandaEntity.class)
                    .create(Keys.HIDDEN_GENE)
                        .get(h -> ((PandaGene) (Object) h.getHiddenGene()))
                        .set((h, v) -> h.setHiddenGene((PandaEntity.Type) (Object) v))
                    .create(Keys.IS_EATING)
                        .get(PandaEntity::func_213578_dZ)
                        .set(PandaEntity::func_213534_t)
                    .create(Keys.IS_FRIGHTENED)
                        .get(PandaEntity::func_213566_eo)
                    .create(Keys.IS_LYING_ON_BACK)
                        .get(PandaEntity::func_213567_dY)
                        .set(PandaEntity::func_213542_s)
                    .create(Keys.IS_ROLLING_AROUND)
                        .get(PandaEntity::func_213564_eh)
                        .set(PandaEntity::func_213576_v)
                    .create(Keys.IS_SNEEZING)
                        .get(PandaEntity::func_213539_dW)
                        .set(PandaEntity::func_213581_u)
                    .create(Keys.IS_UNHAPPY)
                        .get(h -> h.getUnhappyCounter() > 0)
                    .create(Keys.KNOWN_GENE)
                        .get(h -> ((PandaGene) (Object) h.getMainGene()))
                        .set((h, v) -> h.setMainGene((PandaEntity.Type) (Object) v))
                    .create(Keys.SNEEZING_TIME)
                        .get(PandaEntity::getSneezeCounter)
                        .set(PandaEntity::setSneezeCounter)
                    .create(Keys.UNHAPPY_TIME)
                        .get(PandaEntity::getUnhappyCounter)
                        .set(PandaEntity::setUnhappyCounter)
                .asMutable(PandaEntityAccessor.class)
                    .create(Keys.EATING_TIME)
                        .get(PandaEntityAccessor::accessor$getEatCounter)
                        .set(PandaEntityAccessor::accessor$setEatCounter);
    }
    // @formatter:on
}
