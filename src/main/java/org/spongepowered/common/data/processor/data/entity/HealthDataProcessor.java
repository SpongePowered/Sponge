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
package org.spongepowered.common.data.processor.data.entity;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.common.data.util.ComparatorUtil.doubleComparator;
import static org.spongepowered.common.data.util.DataUtil.getData;

import com.google.common.base.Optional;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableHealthData;
import org.spongepowered.api.data.manipulator.mutable.entity.HealthData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeHealthData;
import org.spongepowered.common.data.processor.common.AbstractSpongeDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeBoundedValue;

public class HealthDataProcessor extends AbstractSpongeDataProcessor<HealthData, ImmutableHealthData> {

    @Override
    public boolean supports(DataHolder dataHolder) {
        return dataHolder instanceof EntityLivingBase;
    }

    @SuppressWarnings("unused")
    @Override
    public Optional<HealthData> from(DataHolder dataHolder) {
        if (dataHolder instanceof EntityLivingBase) {
            final SpongeHealthData healthData = new SpongeHealthData();
            final double health = ((EntityLivingBase) dataHolder).getHealth();
            final double maxHealth = ((EntityLivingBase) dataHolder).getMaxHealth();
            return Optional.<HealthData>of(healthData.setHealth(health).setMaxHealth(maxHealth));
        } else if (dataHolder instanceof ItemStack) {
            final ItemStack itemStack = ((ItemStack) dataHolder);
            return Optional.absent(); // Pending decision on whether we should store custom data onto itemstacks
        } else {
            return Optional.absent();
        }

    }

    @Override
    public Optional<HealthData> fill(DataHolder dataHolder, HealthData manipulator, MergeFunction overlap) {
        if (dataHolder instanceof EntityLivingBase) {
            final HealthData merged = overlap.merge(checkNotNull(manipulator).copy(), from(dataHolder).get());
            manipulator.set(Keys.MAX_HEALTH, merged.maxHealth().get())
                .set(Keys.HEALTH, merged.health().get());
            return Optional.of(manipulator);
        }
        return Optional.absent();
    }

    @Override
    public Optional<HealthData> fill(DataContainer container, HealthData healthData) {
        healthData.set(Keys.MAX_HEALTH, getData(container, Keys.MAX_HEALTH));
        healthData.set(Keys.HEALTH, getData(container, Keys.HEALTH));
        return Optional.of(healthData);
    }

    @Override
    public DataTransactionResult set(DataHolder dataHolder, HealthData manipulator, MergeFunction function) {
        if (dataHolder instanceof EntityLivingBase) {
            DataTransactionBuilder builder = DataTransactionBuilder.builder();
            final double prevMaxHealth = ((EntityLivingBase) dataHolder).getMaxHealth();
            final double prevHealth = ((EntityLivingBase) dataHolder).getHealth();
            final EntityLivingBase livingBase = ((EntityLivingBase) dataHolder);
            final HealthData newData = checkNotNull(function).merge(from(dataHolder).orNull(), manipulator);
            final float newMaxHealth = newData.maxHealth().get().floatValue();
            final float newHealth = newData.health().get().floatValue();

            try {
                builder.replace(
                    new ImmutableSpongeBoundedValue<Double>(Keys.HEALTH, prevHealth, prevMaxHealth, doubleComparator(), 0D, prevMaxHealth),
                    new ImmutableSpongeBoundedValue<Double>(Keys.MAX_HEALTH, prevMaxHealth, prevMaxHealth, doubleComparator(), 1D,
                                                            (double) Float.MAX_VALUE));
                livingBase.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(newMaxHealth);
                livingBase.setHealth(newHealth);
                builder.success(
                    new ImmutableSpongeBoundedValue<Double>(Keys.MAX_HEALTH, (double) newMaxHealth, (double) newMaxHealth, doubleComparator(), 1D,
                                                            (double) Float.MAX_VALUE),
                    new ImmutableSpongeBoundedValue<Double>(Keys.HEALTH, (double) newHealth, (double) newHealth, doubleComparator(), 0D,
                                                            (double) Float.MAX_VALUE))
                    .result(DataTransactionResult.Type.SUCCESS);
                return builder.build();
            } catch (Exception e) {
                livingBase.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(prevMaxHealth);
                livingBase.setHealth((float) prevHealth);
                builder.reject(
                    new ImmutableSpongeBoundedValue<Double>(Keys.MAX_HEALTH, (double) newMaxHealth, (double) newMaxHealth, doubleComparator(), 1D,
                                                            (double) Float.MAX_VALUE),
                    new ImmutableSpongeBoundedValue<Double>(Keys.HEALTH, (double) newHealth, (double) newHealth, doubleComparator(), 0D,
                                                            (double) Float.MAX_VALUE))
                    .result(DataTransactionResult.Type.ERROR);
                return builder.build();
            }
        }
        return DataTransactionBuilder.failResult(manipulator.getValues());
    }

    @Override
    public Optional<ImmutableHealthData> with(Key<? extends BaseValue<?>> key, Object value, ImmutableHealthData immutable) {
        return Optional.absent();
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionBuilder.builder().result(DataTransactionResult.Type.FAILURE).build();
    }

    @Override
    public Optional<HealthData> createFrom(DataHolder dataHolder) {
        if (dataHolder instanceof EntityLivingBase) {
            final double maxHealth = ((EntityLivingBase) dataHolder).getMaxHealth();
            final double health = ((EntityLivingBase) dataHolder).getHealth();
            return Optional.<HealthData>of(new SpongeHealthData(health, maxHealth));
        }
        return Optional.absent();
    }

    @Override
    public boolean supports(EntityType entityType) {
        return Living.class.isAssignableFrom(entityType.getEntityClass());
    }

}
