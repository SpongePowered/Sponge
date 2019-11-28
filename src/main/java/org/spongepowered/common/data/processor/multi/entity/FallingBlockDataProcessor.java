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
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFallingBlockData;
import org.spongepowered.api.data.manipulator.mutable.entity.FallingBlockData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeFallingBlockData;
import org.spongepowered.common.data.processor.common.AbstractEntityDataProcessor;
import org.spongepowered.common.data.processor.common.AbstractMultiDataSingleTargetProcessor;
import org.spongepowered.common.mixin.core.entity.item.EntityFallingBlockAccessor;

import java.util.Map;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.FallingBlockEntity;

public class FallingBlockDataProcessor extends
    AbstractMultiDataSingleTargetProcessor<EntityFallingBlockAccessor, FallingBlockData, ImmutableFallingBlockData> {

    public FallingBlockDataProcessor() {
        super(EntityFallingBlockAccessor.class);
    }

    @Override
    protected boolean doesDataExist(final EntityFallingBlockAccessor entity) {
        return true;
    }

    @Override
    protected boolean set(final EntityFallingBlockAccessor entity, final Map<Key<?>, Object> keyValues) {
        entity.accessor$setFallHurtAmount(((Double) keyValues.get(Keys.FALL_DAMAGE_PER_BLOCK)).floatValue());
        entity.accessor$setFallHurtMax(((Double) keyValues.get(Keys.MAX_FALL_DAMAGE)).intValue());
        entity.accessor$setFallBlockState((BlockState) keyValues.get(Keys.FALLING_BLOCK_STATE));
        entity.accessor$setDontSetAsBlock(!(Boolean) keyValues.get(Keys.CAN_PLACE_AS_BLOCK));
        ((FallingBlockEntity) entity).shouldDropItem = (Boolean) keyValues.get(Keys.CAN_DROP_AS_ITEM);
        entity.accessor$setFallTime((Integer) keyValues.get(Keys.FALL_TIME));
        entity.accessor$setHurtEntities((Boolean) keyValues.get(Keys.FALLING_BLOCK_CAN_HURT_ENTITIES));
        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(final EntityFallingBlockAccessor entity) {
        return ImmutableMap.<Key<?>, Object> builder()
                .put(Keys.FALL_DAMAGE_PER_BLOCK, (double)entity.accessor$getFallHurtAmount())
                .put(Keys.MAX_FALL_DAMAGE, (double)entity.accessor$getFallHurtMax())
                .put(Keys.FALLING_BLOCK_STATE, entity.accessor$getFallBlockState())
                .put(Keys.CAN_PLACE_AS_BLOCK, !entity.accessor$getDontSetAsBlock())
                .put(Keys.CAN_DROP_AS_ITEM, ((FallingBlockEntity) entity).shouldDropItem)
                .put(Keys.FALL_TIME, entity.accessor$getFallTime())
                .put(Keys.FALLING_BLOCK_CAN_HURT_ENTITIES, entity.accessor$getHurtEntities())
                .build();
    }

    @Override
    protected FallingBlockData createManipulator() {
        return new SpongeFallingBlockData();
    }

    @Override
    public Optional<FallingBlockData> fill(final DataContainer container, final FallingBlockData fallingBlockData) {
        fallingBlockData.set(Keys.FALL_DAMAGE_PER_BLOCK, getData(container, Keys.FALL_DAMAGE_PER_BLOCK));
        fallingBlockData.set(Keys.MAX_FALL_DAMAGE, getData(container, Keys.MAX_FALL_DAMAGE));
        fallingBlockData.set(Keys.FALLING_BLOCK_STATE, getData(container, Keys.FALLING_BLOCK_STATE));
        fallingBlockData.set(Keys.CAN_PLACE_AS_BLOCK, getData(container, Keys.CAN_PLACE_AS_BLOCK));
        fallingBlockData.set(Keys.CAN_DROP_AS_ITEM, getData(container, Keys.CAN_DROP_AS_ITEM));
        fallingBlockData.set(Keys.FALL_TIME, getData(container, Keys.FALL_TIME));
        fallingBlockData.set(Keys.FALLING_BLOCK_CAN_HURT_ENTITIES, getData(container, Keys.FALLING_BLOCK_CAN_HURT_ENTITIES));
        return Optional.of(fallingBlockData);
    }

    @Override
    public DataTransactionResult remove(final DataHolder dataHolder) {
        return DataTransactionResult.failNoData();
    }
}
