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
package org.spongepowered.common.data.utils.entities;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.data.DataQuery.of;
import static org.spongepowered.common.data.DataTransactionBuilder.builder;
import static org.spongepowered.common.data.DataTransactionBuilder.fail;

import com.google.common.base.Optional;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.entity.HealthData;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.SpongeDataProcessor;
import org.spongepowered.common.data.manipulators.entities.SpongeHealthData;

public class SpongeHealthProcessor implements SpongeDataProcessor<HealthData> {

    private static final DataQuery HEALTH_QUERY = of("Health");
    private static final DataQuery MAX_HEALTH_QUERY = of("MaxHealth");

    @Override
    public Optional<HealthData> fillData(DataHolder holder, HealthData manipulator, DataPriority priority) {
        // todo for now, we only focus on health to and from entities.
        if (!(holder instanceof EntityLivingBase)) {
            return Optional.absent();
        }
        switch (checkNotNull(priority)) {
            case DATA_HOLDER:
            case PRE_MERGE:
                manipulator.setMaxHealth(((EntityLivingBase) holder).getMaxHealth())
                        .setHealth(((EntityLivingBase) holder).getHealth());
                return Optional.of(manipulator);
            default :
                return Optional.of(manipulator);
        }
    }

    @Override
    public DataTransactionResult setData(DataHolder dataHolder, HealthData manipulator, DataPriority priority) {
        if (!(dataHolder instanceof EntityLivingBase)) {
            return fail(manipulator);
        }
        switch (checkNotNull(priority)) {
            case DATA_HOLDER:
            case PRE_MERGE:
                return builder().reject(manipulator).result(DataTransactionResult.Type.SUCCESS).build();
            case DATA_MANIPULATOR:
            case POST_MERGE:
                final float oldMaxHealth = ((EntityLivingBase) dataHolder).getMaxHealth();
                final float oldHealth = ((EntityLivingBase) dataHolder).getHealth();
                final HealthData oldData = create().setMaxHealth(oldMaxHealth).setHealth(oldHealth);
                try {
                    final float newMaxHealth = (float) manipulator.getMaxHealth();
                    final float newHealth = (float) manipulator.getHealth();
                    ((EntityLivingBase) dataHolder).getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(newMaxHealth);
                    ((EntityLivingBase) dataHolder).setHealth(newHealth);
                } catch (Exception e) {
                    // Usually we don't have this type of problem, but at times, health is too high
                    // and we have to consider there may have been an issue setting these types of things.
                    ((EntityLivingBase) dataHolder).getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(oldMaxHealth);
                    ((EntityLivingBase) dataHolder).setHealth(oldHealth);
                    return builder().reject(manipulator).result(DataTransactionResult.Type.ERROR).build();
                }
                return builder().replace(oldData).result(DataTransactionResult.Type.SUCCESS).build();
            default:
                return builder().reject(manipulator).result(DataTransactionResult.Type.UNDEFINED).build();
        }
    }

    @Override
    public boolean remove(DataHolder dataHolder) {
        if (!(dataHolder instanceof EntityLivingBase)) {
            return false;
        }
        //TODO discuss the possibility of "resetting" health to a default state based on the difficulty level
        // and the possibility of resetting based on "entity type".
        ((EntityLivingBase) dataHolder).setHealth(((EntityLivingBase) dataHolder).getMaxHealth());
        return true;
    }

    @Override
    public Optional<HealthData> build(DataView container) throws InvalidDataException {
        if (!checkNotNull(container).contains(HEALTH_QUERY) || !container.contains(MAX_HEALTH_QUERY)) {
            throw new InvalidDataException("Not enough data to construct a HealthData");
        }
        final double health = container.getDouble(HEALTH_QUERY).get();
        final double maxHealth = container.getDouble(MAX_HEALTH_QUERY).get();
        return Optional.of(create().setMaxHealth(maxHealth).setHealth(health));
    }

    @Override
    public HealthData create() {
        return new SpongeHealthData();
    }

    @Override
    public Optional<HealthData> createFrom(DataHolder dataHolder) {
        if (!(dataHolder instanceof EntityLivingBase)) {
            return Optional.absent();
        }
        final double maxHealth = ((EntityLivingBase) dataHolder).getMaxHealth();
        final double health = ((EntityLivingBase) dataHolder).getHealth();
        return Optional.of(create().setMaxHealth(maxHealth).setHealth(health));
    }

    @Override
    public Optional<HealthData> getFrom(DataHolder holder) {
        return Optional.absent();
    }
}
