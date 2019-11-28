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
package org.spongepowered.common.data.processor.multi.entity;

import static org.spongepowered.common.data.util.DataUtil.getData;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.Entity;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableIgniteableData;
import org.spongepowered.api.data.manipulator.mutable.entity.IgniteableData;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeIgniteableData;
import org.spongepowered.common.data.processor.common.AbstractEntityDataProcessor;
import org.spongepowered.common.mixin.core.entity.EntityAccessor;

import java.util.Map;
import java.util.Optional;

public class IgniteableDataProcessor extends AbstractEntityDataProcessor<Entity, IgniteableData, ImmutableIgniteableData> {

    public IgniteableDataProcessor() {
        super(Entity.class);
    }

    @Override
    public Optional<IgniteableData> fill(final DataContainer container, final IgniteableData igniteableData) {
        igniteableData.set(Keys.FIRE_TICKS, getData(container, Keys.FIRE_TICKS));
        igniteableData.set(Keys.FIRE_DAMAGE_DELAY, getData(container, Keys.FIRE_DAMAGE_DELAY));
        return Optional.of(igniteableData);
    }

    @Override
    public DataTransactionResult remove(final DataHolder dataHolder) {
        if (dataHolder instanceof Entity) {
            if (((EntityAccessor) dataHolder).accessor$getFire() > 0) {
                final DataTransactionResult.Builder builder = DataTransactionResult.builder();
                builder.replace(from(dataHolder).get().getValues());
                ((Entity) dataHolder).extinguish();
                return builder.result(DataTransactionResult.Type.SUCCESS).build();
            }
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    protected IgniteableData createManipulator() {
        return new SpongeIgniteableData();
    }

    @Override
    protected boolean doesDataExist(final Entity entity) {
        return ((EntityAccessor) entity).accessor$getFire() > 0;
    }

    @Override
    protected boolean set(final Entity entity, final Map<Key<?>, Object> keyValues) {
        ((EntityAccessor) entity).accessor$setFire((Integer) keyValues.get(Keys.FIRE_TICKS));
        // TODO - this needs to be a property
        //entity.fireResistance = (Integer) keyValues.get(Keys.FIRE_DAMAGE_DELAY);
        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(final Entity entity) {
        final int fireTicks = ((EntityAccessor) entity).accessor$getFire();
        final int fireDamageDelay = ((EntityAccessor) entity).accessor$getFireImmuneTicks();
        return ImmutableMap.<Key<?>, Object>of(Keys.FIRE_TICKS, fireTicks,
                                               Keys.FIRE_DAMAGE_DELAY, fireDamageDelay);
    }

}
