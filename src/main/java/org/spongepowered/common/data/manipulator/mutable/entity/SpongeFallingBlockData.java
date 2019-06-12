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
package org.spongepowered.common.data.manipulator.mutable.entity;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableFallingBlockData;
import org.spongepowered.api.data.manipulator.mutable.entity.FallingBlockData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeFallingBlockData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.data.value.mutable.SpongeValue;

public class SpongeFallingBlockData extends AbstractData<FallingBlockData, ImmutableFallingBlockData> implements FallingBlockData {

    private double fallDamagePerBlock;
    private double maxFallDamage;
    private BlockState blockState;
    private boolean canPlaceAsBlock;
    private boolean canDropAsItem;
    private int fallTime;
    private boolean canHurtEntities;

    public SpongeFallingBlockData(double fallDamagePerBlock, double maxFallDamage, BlockState blockState, boolean canPlaceAsBlock, boolean
            canDropAsItem, int fallTime, boolean canHurtEntities) {
        super(FallingBlockData.class);
        this.fallDamagePerBlock = fallDamagePerBlock;
        this.maxFallDamage = maxFallDamage;
        this.blockState = blockState;
        this.canPlaceAsBlock = canPlaceAsBlock;
        this.canDropAsItem = canDropAsItem;
        this.fallTime = fallTime;
        this.canHurtEntities = canHurtEntities;
        registerGettersAndSetters();
    }

    public SpongeFallingBlockData() {
        this(Constants.Entity.FallingBlock.DEFAULT_FALL_DAMAGE_PER_BLOCK, Constants.Entity.FallingBlock.DEFAULT_MAX_FALL_DAMAGE, Constants.Catalog
                        .DEFAULT_FALLING_BLOCK_BLOCKSTATE, Constants.Entity.FallingBlock.DEFAULT_CAN_PLACE_AS_BLOCK, Constants.Entity.FallingBlock
                        .DEFAULT_CAN_DROP_AS_ITEM, Constants.Entity.FallingBlock.DEFAULT_FALL_TIME,
                Constants.Entity.FallingBlock.DEFAULT_CAN_HURT_ENTITIES);
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(Keys.FALL_DAMAGE_PER_BLOCK, SpongeFallingBlockData.this::getFallDamagePerBlock);
        registerFieldSetter(Keys.FALL_DAMAGE_PER_BLOCK, SpongeFallingBlockData.this::setFallDamagePerBlock);
        registerKeyValue(Keys.FALL_DAMAGE_PER_BLOCK, SpongeFallingBlockData.this::fallDamagePerBlock);

        registerFieldGetter(Keys.MAX_FALL_DAMAGE, SpongeFallingBlockData.this::getMaxFallDamage);
        registerFieldSetter(Keys.MAX_FALL_DAMAGE, SpongeFallingBlockData.this::setMaxFallDamage);
        registerKeyValue(Keys.MAX_FALL_DAMAGE, SpongeFallingBlockData.this::maxFallDamage);

        registerFieldGetter(Keys.FALLING_BLOCK_STATE, SpongeFallingBlockData.this::getBlockState);
        registerFieldSetter(Keys.FALLING_BLOCK_STATE, SpongeFallingBlockData.this::setBlockState);
        registerKeyValue(Keys.FALLING_BLOCK_STATE, SpongeFallingBlockData.this::blockState);

        registerFieldGetter(Keys.CAN_PLACE_AS_BLOCK, SpongeFallingBlockData.this::getCanPlaceAsBlock);
        registerFieldSetter(Keys.CAN_PLACE_AS_BLOCK, SpongeFallingBlockData.this::setCanPlaceAsBlock);
        registerKeyValue(Keys.CAN_PLACE_AS_BLOCK, SpongeFallingBlockData.this::canPlaceAsBlock);

        registerFieldGetter(Keys.CAN_DROP_AS_ITEM, SpongeFallingBlockData.this::getCanDropAsItem);
        registerFieldSetter(Keys.CAN_DROP_AS_ITEM, SpongeFallingBlockData.this::setCanDropAsItem);
        registerKeyValue(Keys.CAN_DROP_AS_ITEM, SpongeFallingBlockData.this::canDropAsItem);

        registerFieldGetter(Keys.FALL_TIME, SpongeFallingBlockData.this::getFallTime);
        registerFieldSetter(Keys.FALL_TIME, SpongeFallingBlockData.this::setFallTime);
        registerKeyValue(Keys.FALL_TIME, SpongeFallingBlockData.this::fallTime);

        registerFieldGetter(Keys.FALLING_BLOCK_CAN_HURT_ENTITIES, SpongeFallingBlockData.this::getCanHurtEntities);
        registerFieldSetter(Keys.FALLING_BLOCK_CAN_HURT_ENTITIES, SpongeFallingBlockData.this::setCanHurtEntities);
        registerKeyValue(Keys.FALLING_BLOCK_CAN_HURT_ENTITIES, SpongeFallingBlockData.this::canHurtEntities);
    }

    public void setFallDamagePerBlock(double value) {
        this.fallDamagePerBlock = value;
    }

    public double getFallDamagePerBlock() {
        return this.fallDamagePerBlock;
    }

    @Override
    public Value<Double> fallDamagePerBlock() {
        return new SpongeValue<>(Keys.FALL_DAMAGE_PER_BLOCK, Constants.Entity.FallingBlock.DEFAULT_FALL_DAMAGE_PER_BLOCK, this.fallDamagePerBlock);
    }

    public void setMaxFallDamage(double value) {
        this.maxFallDamage = value;
    }

    public double getMaxFallDamage() {
        return this.maxFallDamage;
    }

    @Override
    public Value<Double> maxFallDamage() {
        return new SpongeValue<>(Keys.MAX_FALL_DAMAGE, Constants.Entity.FallingBlock.DEFAULT_MAX_FALL_DAMAGE, this.maxFallDamage);
    }

    public void setBlockState(BlockState value) {
        this.blockState = value;
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    @Override
    public Value<BlockState> blockState() {
        return new SpongeValue<>(Keys.FALLING_BLOCK_STATE, Constants.Catalog.DEFAULT_FALLING_BLOCK_BLOCKSTATE, this.blockState);
    }

    public void setCanPlaceAsBlock(boolean value) {
        this.canPlaceAsBlock = value;
    }

    public boolean getCanPlaceAsBlock() {
        return this.canPlaceAsBlock;
    }

    @Override
    public Value<Boolean> canPlaceAsBlock() {
        return new SpongeValue<>(Keys.CAN_PLACE_AS_BLOCK, Constants.Entity.FallingBlock.DEFAULT_CAN_PLACE_AS_BLOCK, this.canPlaceAsBlock);
    }

    public void setCanDropAsItem(boolean value) {
        this.canDropAsItem = value;
    }

    public boolean getCanDropAsItem() {
        return this.canDropAsItem;
    }

    @Override
    public Value<Boolean> canDropAsItem() {
        return new SpongeValue<>(Keys.CAN_DROP_AS_ITEM, Constants.Entity.FallingBlock.DEFAULT_CAN_DROP_AS_ITEM, this.canDropAsItem);
    }

    public void setFallTime(int value) {
        this.fallTime = value;
    }

    public int getFallTime() {
        return this.fallTime;
    }

    @Override
    public Value<Integer> fallTime() {
        return new SpongeValue<>(Keys.FALL_TIME, Constants.Entity.FallingBlock.DEFAULT_FALL_TIME, this.fallTime);
    }

    public void setCanHurtEntities(boolean value) {
        this.canHurtEntities = value;
    }

    public boolean getCanHurtEntities() {
        return this.canHurtEntities;
    }

    @Override
    public Value<Boolean> canHurtEntities() {
        return new SpongeValue<>(Keys.FALLING_BLOCK_CAN_HURT_ENTITIES, Constants.Entity.FallingBlock.DEFAULT_CAN_HURT_ENTITIES, this.canHurtEntities);
    }

    @Override
    public FallingBlockData copy() {
        return new SpongeFallingBlockData(this.fallDamagePerBlock, this.maxFallDamage, this.blockState, this.canPlaceAsBlock, this.canDropAsItem,
                this.fallTime, this.canHurtEntities);
    }

    @Override
    public ImmutableFallingBlockData asImmutable() {
        return new ImmutableSpongeFallingBlockData(this.fallDamagePerBlock, this.maxFallDamage, this.blockState, this.canPlaceAsBlock, this
                .canDropAsItem, this.fallTime, this.canHurtEntities);
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
