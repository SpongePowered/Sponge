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

import com.google.common.base.Preconditions;
import net.minecraft.block.Blocks;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableMinecartBlockData;
import org.spongepowered.api.data.manipulator.mutable.entity.MinecartBlockData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeMinecartBlockData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.mutable.SpongeValue;

public class SpongeMinecartBlockData extends AbstractData<MinecartBlockData, ImmutableMinecartBlockData> implements MinecartBlockData {

    private BlockState block;
    private int offset;

    public SpongeMinecartBlockData() {
        this((BlockState) Blocks.AIR.getDefaultState(), 6);
    }

    public SpongeMinecartBlockData(BlockState block, int offset) {
        super(MinecartBlockData.class);

        this.block = Preconditions.checkNotNull(block);
        this.offset = offset;

        registerGettersAndSetters();
    }

    @Override
    public Value<BlockState> block() {
        return new SpongeValue<>(Keys.REPRESENTED_BLOCK, (BlockState) Blocks.AIR.getDefaultState(), this.block);
    }

    @Override
    public Value<Integer> offset() {
        return new SpongeValue<>(Keys.OFFSET, 6, this.offset);
    }

    @Override
    public MinecartBlockData copy() {
        return new SpongeMinecartBlockData(this.block, this.offset);
    }

    @Override
    public ImmutableMinecartBlockData asImmutable() {
        return new ImmutableSpongeMinecartBlockData(this.block, this.offset);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.REPRESENTED_BLOCK, this.block)
                .set(Keys.OFFSET, this.offset);
    }

    public BlockState getBlock() {
        return this.block;
    }

    public void setBlock(BlockState block) {
        this.block = Preconditions.checkNotNull(block);
    }

    public int getOffset() {
        return this.offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    protected void registerGettersAndSetters() {
        registerKeyValue(Keys.REPRESENTED_BLOCK, SpongeMinecartBlockData.this::block);
        registerKeyValue(Keys.OFFSET, SpongeMinecartBlockData.this::offset);

        registerFieldGetter(Keys.REPRESENTED_BLOCK, SpongeMinecartBlockData.this::getBlock);
        registerFieldGetter(Keys.OFFSET, SpongeMinecartBlockData.this::getOffset);

        registerFieldSetter(Keys.REPRESENTED_BLOCK, SpongeMinecartBlockData.this::setBlock);
        registerFieldSetter(Keys.OFFSET, SpongeMinecartBlockData.this::setOffset);
    }

}
