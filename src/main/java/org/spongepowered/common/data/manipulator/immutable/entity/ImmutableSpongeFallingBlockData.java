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
package org.spongepowered.common.data.manipulator.immutable.entity;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFallingBlockData;
import org.spongepowered.api.data.manipulator.mutable.entity.FallingBlockData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeFallingBlockData;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

public class ImmutableSpongeFallingBlockData extends AbstractImmutableData<ImmutableFallingBlockData, FallingBlockData> implements ImmutableFallingBlockData  {

    private final double fallDamagePerBlock;
    private final double maxFallDamage;
    private final BlockState blockState;
    private final boolean canPlaceAsBlock;
    private final boolean canDropAsItem;
    private final int fallTime;
    private boolean canHurtEntities;

    private final ImmutableSpongeValue<Double> fallDamagePerBlockValue;
    private final ImmutableSpongeValue<Double> maxFallDamageValue;
    private final ImmutableSpongeValue<BlockState> blockStateValue;
    private final ImmutableSpongeValue<Boolean> canPlaceAsBlockValue;
    private final ImmutableSpongeValue<Boolean> canDropAsItemValue;
    private final ImmutableSpongeValue<Integer> fallTimeValue;
    private final ImmutableSpongeValue<Boolean> canHurtEntitiesValue;

    public ImmutableSpongeFallingBlockData(double fallDamagePerBlock, double maxFallDamage, BlockState blockState, boolean canPlaceAsBlock,
            boolean canDropAsItem, int fallTime, boolean canHurtEntities) {
        super(ImmutableFallingBlockData.class);
        this.fallDamagePerBlock = fallDamagePerBlock;
        this.maxFallDamage = maxFallDamage;
        this.blockState = blockState;
        this.canPlaceAsBlock = canPlaceAsBlock;
        this.canDropAsItem = canDropAsItem;
        this.fallTime = fallTime;
        this.canHurtEntities = canHurtEntities;

        this.fallDamagePerBlockValue = new ImmutableSpongeValue<>(Keys.FALL_DAMAGE_PER_BLOCK, Constants.Entity.FallingBlock
                .DEFAULT_FALL_DAMAGE_PER_BLOCK, this.fallDamagePerBlock);
        this.maxFallDamageValue = new ImmutableSpongeValue<>(Keys.MAX_FALL_DAMAGE, Constants.Entity.FallingBlock.DEFAULT_MAX_FALL_DAMAGE, this
                .maxFallDamage);
        this.blockStateValue = new ImmutableSpongeValue<>(Keys.FALLING_BLOCK_STATE, Constants.Catalog.DEFAULT_FALLING_BLOCK_BLOCKSTATE, this
                .blockState);
        this.canPlaceAsBlockValue = new ImmutableSpongeValue<>(Keys.CAN_PLACE_AS_BLOCK, Constants.Entity.FallingBlock
                .DEFAULT_CAN_PLACE_AS_BLOCK, this.canPlaceAsBlock);
        this.canDropAsItemValue = new ImmutableSpongeValue<>(Keys.CAN_DROP_AS_ITEM, Constants.Entity.FallingBlock.DEFAULT_CAN_DROP_AS_ITEM,
                this.canDropAsItem);
        this.fallTimeValue = new ImmutableSpongeValue<>(Keys.FALL_TIME, Constants.Entity.FallingBlock.DEFAULT_FALL_TIME,
                this.fallTime);
        this.canHurtEntitiesValue = new ImmutableSpongeValue<>(Keys.FALLING_BLOCK_CAN_HURT_ENTITIES, Constants.Entity.FallingBlock
                .DEFAULT_CAN_HURT_ENTITIES, this.canHurtEntities);

        registerGetters();
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(Keys.FALL_DAMAGE_PER_BLOCK, ImmutableSpongeFallingBlockData.this::getFallDamagePerBlock);
        registerKeyValue(Keys.FALL_DAMAGE_PER_BLOCK, ImmutableSpongeFallingBlockData.this::fallDamagePerBlock);

        registerFieldGetter(Keys.MAX_FALL_DAMAGE, ImmutableSpongeFallingBlockData.this::getMaxFallDamage);
        registerKeyValue(Keys.MAX_FALL_DAMAGE, ImmutableSpongeFallingBlockData.this::maxFallDamage);

        registerFieldGetter(Keys.FALLING_BLOCK_STATE, ImmutableSpongeFallingBlockData.this::getBlockState);
        registerKeyValue(Keys.FALLING_BLOCK_STATE, ImmutableSpongeFallingBlockData.this::blockState);

        registerFieldGetter(Keys.CAN_PLACE_AS_BLOCK, ImmutableSpongeFallingBlockData.this::getCanPlaceAsBlock);
        registerKeyValue(Keys.CAN_PLACE_AS_BLOCK, ImmutableSpongeFallingBlockData.this::canPlaceAsBlock);

        registerFieldGetter(Keys.CAN_DROP_AS_ITEM, ImmutableSpongeFallingBlockData.this::getCanDropAsItem);
        registerKeyValue(Keys.CAN_DROP_AS_ITEM, ImmutableSpongeFallingBlockData.this::canDropAsItem);

        registerFieldGetter(Keys.FALL_TIME, ImmutableSpongeFallingBlockData.this::getFallTime);
        registerKeyValue(Keys.FALL_TIME, ImmutableSpongeFallingBlockData.this::fallTime);

        registerFieldGetter(Keys.FALLING_BLOCK_CAN_HURT_ENTITIES, ImmutableSpongeFallingBlockData.this::getCanHurtEntities);
        registerKeyValue(Keys.FALLING_BLOCK_CAN_HURT_ENTITIES, ImmutableSpongeFallingBlockData.this::canHurtEntities);
    }

    public double getFallDamagePerBlock() {
        return this.fallDamagePerBlock;
    }

    @Override
    public ImmutableValue<Double> fallDamagePerBlock() {
        return this.fallDamagePerBlockValue;
    }

    public double getMaxFallDamage() {
        return this.maxFallDamage;
    }

    @Override
    public ImmutableValue<Double> maxFallDamage() {
        return this.maxFallDamageValue;
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    @Override
    public ImmutableValue<BlockState> blockState() {
        return this.blockStateValue;
    }

    public boolean getCanPlaceAsBlock() {
        return this.canPlaceAsBlock;
    }

    @Override
    public ImmutableValue<Boolean> canPlaceAsBlock() {
        return this.canPlaceAsBlockValue;
    }

    public boolean getCanDropAsItem() {
        return this.canDropAsItem;
    }

    @Override
    public ImmutableValue<Boolean> canDropAsItem() {
        return this.canDropAsItemValue;
    }

    public int getFallTime() {
        return this.fallTime;
    }

    @Override
    public ImmutableValue<Integer> fallTime() {
        return this.fallTimeValue;
    }

    public boolean getCanHurtEntities() {
        return this.canHurtEntities;
    }

    @Override
    public ImmutableValue<Boolean> canHurtEntities() {
        return this.canHurtEntitiesValue;
    }

    @Override
    public FallingBlockData asMutable() {
        return new SpongeFallingBlockData(this.fallDamagePerBlock, this.maxFallDamage, this.blockState, this.canPlaceAsBlock, this.canDropAsItem,
                this.fallTime, this.canHurtEntities);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.FALL_DAMAGE_PER_BLOCK.getQuery(), this.fallDamagePerBlock)
                .set(Keys.MAX_FALL_DAMAGE.getQuery(), this.maxFallDamage)
                .set(Keys.FALLING_BLOCK_STATE.getQuery(), this.blockState)
                .set(Keys.CAN_PLACE_AS_BLOCK.getQuery(), this.canPlaceAsBlock)
                .set(Keys.CAN_DROP_AS_ITEM.getQuery(), this.canDropAsItem)
                .set(Keys.FALL_TIME.getQuery(), this.fallTime)
                .set(Keys.FALLING_BLOCK_CAN_HURT_ENTITIES, this.canHurtEntities);
    }
}
