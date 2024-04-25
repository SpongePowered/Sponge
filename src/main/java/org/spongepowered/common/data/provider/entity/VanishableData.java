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

import net.minecraft.world.entity.Entity;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.effect.VanishState;
import org.spongepowered.common.bridge.data.VanishableBridge;
import org.spongepowered.common.data.SpongeDataManager;
import org.spongepowered.common.data.provider.DataProviderRegistrator;
import org.spongepowered.common.util.Constants;

@SuppressWarnings("deprecation")
public final class VanishableData {

    private VanishableData() {
    }

    // @formatter:off
    @SuppressWarnings("unchecked")
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(VanishableBridge.class)
                    .create(Keys.IS_INVISIBLE)
                        .get(VanishableBridge::bridge$isInvisible)
                        .set(VanishableBridge::bridge$setInvisible)
                    .createContextual(Keys.VANISH_STATE)
                        .get(VanishableBridge::bridge$vanishState)
                        .setAnd((h, v) -> {
                            if (h instanceof Entity && ((Entity) h).level().isClientSide) {
                                return false;
                            }
                            h.bridge$vanishState(v);
                            return true;
                        })
                        .dataPerspectiveMerge(values -> {
                            VanishState current = VanishState.unvanished();
                            for (final VanishState value : values) {
                                if (!value.invisible()) {
                                    continue;
                                }
                                else if (!current.invisible()) {
                                    current = value;
                                    continue;
                                }

                                if (!value.affectsMonsterSpawning()) {
                                    current = current.affectMonsterSpawning(false);
                                }

                                if (value.untargetable()) {
                                    current = current.untargetable(true);
                                }

                                if (!value.createsSounds()) {
                                    current = current.createSounds(false);
                                }

                                if (!value.createsParticles()) {
                                    current = current.createParticles(false);
                                }

                                if (!value.triggerVibrations()) {
                                    current = current.triggerVibrations(false);
                                }
                            }

                            return current;
                        })
                        .dataPerspectiveApply((h, p, v) -> {
                            if (h instanceof Entity && ((Entity) h).level().isClientSide) {
                                return;
                            }
                            h.bridge$vanishState(v, p);
                        });
        final ResourceKey dataStoreKey = ResourceKey.sponge("invisibility");
        registrator.spongeDataStore(dataStoreKey, VanishableBridge.class, Keys.IS_INVISIBLE, Keys.VANISH_STATE);
        SpongeDataManager.INSTANCE.registerLegacySpongeData(Constants.Sponge.Entity.IS_INVISIBLE, dataStoreKey, Keys.IS_INVISIBLE);
        SpongeDataManager.INSTANCE.registerLegacySpongeData(Constants.Sponge.Entity.IS_VANISHED, dataStoreKey, Keys.VANISH_STATE);
    }
    // @formatter:on
}
