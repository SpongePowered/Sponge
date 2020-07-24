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
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.MissingImplementationException;

public final class PandaData {

    private PandaData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(PandaEntity.class)
                    .create(Keys.EATING_TIME)
                        .get(h -> {
                            throw new MissingImplementationException("PandaData", "EATING_TIME::getter");
                        })
                        .set((h, v) -> {
                            throw new MissingImplementationException("PandaData", "EATING_TIME::setter");
                        })
                    .create(Keys.HIDDEN_GENE)
                        .get(h -> ((PandaGene) (Object) h.getHiddenGene()))
                        .set((h, v) -> h.setHiddenGene((PandaEntity.Type) (Object) v))
                    .create(Keys.IS_EATING)
                        .get(h -> {
                            throw new MissingImplementationException("PandaData", "IS_EATING::getter");
                        })
                        .set((h, v) -> {
                            throw new MissingImplementationException("PandaData", "IS_EATING::setter");
                        })
                    .create(Keys.IS_FRIGHTENED)
                        .get(h -> {
                            throw new MissingImplementationException("PandaData", "IS_FRIGHTENED::getter");
                        })
                        .set((h, v) -> {
                            throw new MissingImplementationException("PandaData", "IS_FRIGHTENED::setter");
                        })
                    .create(Keys.IS_LYING_ON_BACK)
                        .get(PandaEntity::func_213567_dY)
                        .set(PandaEntity::func_213542_s)
                    .create(Keys.IS_ROLLING_AROUND)
                        .get(PandaEntity::func_213564_eh)
                        .set(PandaEntity::func_213576_v)
                    .create(Keys.IS_SNEEZING)
                        .get(h -> {
                            throw new MissingImplementationException("PandaData", "IS_SNEEZING::getter");
                        })
                        .set((h, v) -> {
                            throw new MissingImplementationException("PandaData", "IS_SNEEZING::setter");
                        })
                    .create(Keys.IS_UNHAPPY)
                        .get(h -> {
                            throw new MissingImplementationException("PandaData", "IS_UNHAPPY::getter");
                        })
                        .set((h, v) -> {
                            throw new MissingImplementationException("PandaData", "IS_UNHAPPY::setter");
                        })
                    .create(Keys.KNOWN_GENE)
                        .get(h -> ((PandaGene) (Object) h.getMainGene()))
                        .set((h, v) -> h.setMainGene((PandaEntity.Type) (Object) v))
                    .create(Keys.SNEEZING_TIME)
                        .get(PandaEntity::func_213585_ee)
                        .set(PandaEntity::func_213562_s)
                    .create(Keys.UNHAPPY_TIME)
                        .get(h -> {
                            throw new MissingImplementationException("PandaData", "UNHAPPY_TIME::getter");
                        })
                        .set((h, v) -> {
                            throw new MissingImplementationException("PandaData", "UNHAPPY_TIME::setter");
                        });
    }
    // @formatter:on
}
