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
package org.spongepowered.common.data.utils;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.data.DataQuery.of;
import static org.spongepowered.common.data.DataTransactionBuilder.builder;
import static org.spongepowered.common.data.DataTransactionBuilder.fail;

import com.google.common.base.Optional;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulators.PotionEffectData;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.api.service.persistence.SerializationService;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.SpongeDataUtil;
import org.spongepowered.common.data.manipulators.SpongePotionEffectData;
import org.spongepowered.common.potion.SpongePotionBuilder;

import java.util.Collection;
import java.util.List;

@SuppressWarnings("unchecked")
public class SpongePotionDataBuilder implements SpongeDataUtil<PotionEffectData> {

    private static final DataQuery POTION_QUERY = of("Potions");

    @Override
    public Optional<PotionEffectData> fillData(DataHolder holder, PotionEffectData manipulator, DataPriority priority) {
        if (holder instanceof EntityLivingBase) { // todo priority
            for (PotionEffect entry : (Collection<PotionEffect>) ((EntityLivingBase) holder).getActivePotionEffects()) {
                manipulator.add(new SpongePotionBuilder().from(((org.spongepowered.api.potion.PotionEffect) entry)).build());
            }
            return Optional.of(manipulator);
        }
        return Optional.absent();
    }

    @Override
    public DataTransactionResult setData(DataHolder dataHolder, PotionEffectData manipulator, DataPriority priority) {
        if (dataHolder instanceof EntityLivingBase) {
            switch (checkNotNull(priority)) {
                case DATA_MANIPULATOR:
                    ((EntityLivingBase) dataHolder).clearActivePotions();
                    for (final org.spongepowered.api.potion.PotionEffect effect : checkNotNull(manipulator).getPotionEffects()) {
                        ((EntityLivingBase) dataHolder).addPotionEffect(((PotionEffect) effect));
                    }
                    break;
                case DATA_HOLDER:
                    final PotionEffectData rejectedData = create();
                    for (final org.spongepowered.api.potion.PotionEffect effect : checkNotNull(manipulator).getPotionEffects()) {
                        if (((EntityLivingBase) dataHolder).isPotionActive(((PotionEffect) effect).getPotionID())) {
                            rejectedData.add(effect);
                        } else {
                            ((EntityLivingBase) dataHolder).addPotionEffect(((PotionEffect) effect));
                        }
                    }
                    return builder().reject(rejectedData).result(DataTransactionResult.Type.SUCCESS).build();
                default:
                    break;
            }


        }
        return fail(manipulator);
    }

    @Override
    public boolean remove(DataHolder dataHolder) {
        if (dataHolder instanceof EntityLivingBase) {
            ((EntityLivingBase) dataHolder).clearActivePotions();
            return true;
        } // todo handle itemstacks
        return false;
    }

    @Override
    public Optional<PotionEffectData> build(DataView container) throws InvalidDataException {
        if (!checkNotNull(container).contains(POTION_QUERY)) {
            throw new InvalidDataException("Missing data for Potions!");
        } else {
            final PotionEffectData data = create();
            final SerializationService serializationService = Sponge.getGame().getServiceManager().provide(SerializationService.class).get();
            List<org.spongepowered.api.potion.PotionEffect> effectList = container.getSerializableList(POTION_QUERY,
                    org.spongepowered.api.potion.PotionEffect.class, serializationService).get();
            for (org.spongepowered.api.potion.PotionEffect effect : effectList) {
                data.add(effect);
            }
            return Optional.of(data);
        }
    }

    @Override
    public PotionEffectData create() {
        return new SpongePotionEffectData();
    }

    @Override
    public Optional<PotionEffectData> createFrom(DataHolder dataHolder) {
        if (dataHolder instanceof EntityLivingBase) {
            final PotionEffectData data = create();
            for (PotionEffect entry : (Collection<PotionEffect>) ((EntityLivingBase) dataHolder).getActivePotionEffects()) {
                data.add(new SpongePotionBuilder().from(((org.spongepowered.api.potion.PotionEffect) entry)).build());
            }
            return Optional.of(data);
        }
        return Optional.absent();
    }
}
