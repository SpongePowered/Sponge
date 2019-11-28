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

import com.google.common.base.Preconditions;
import net.minecraft.init.Blocks;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableMinecartBlockData;
import org.spongepowered.api.data.manipulator.mutable.entity.MinecartBlockData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeMinecartBlockData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

public class ImmutableSpongeMinecartBlockData extends AbstractImmutableData<ImmutableMinecartBlockData, MinecartBlockData> implements ImmutableMinecartBlockData {

    private final BlockState block;
    private final int offset;

    private final ImmutableValue<BlockState> blockValue;
    private final ImmutableValue<Integer> offsetValue;

    public ImmutableSpongeMinecartBlockData() {
        this((BlockState) Blocks.field_150350_a.func_176223_P(), 6);
    }

    public ImmutableSpongeMinecartBlockData(BlockState block, int offset) {
        super(ImmutableMinecartBlockData.class);
        this.block = Preconditions.checkNotNull(block);
        this.offset = offset;
        this.blockValue = new ImmutableSpongeValue<>(Keys.REPRESENTED_BLOCK, (BlockState) Blocks.field_150350_a.func_176223_P(), block);
        this.offsetValue = new ImmutableSpongeValue<>(Keys.OFFSET, 6, offset);
        registerGetters();
    }

    @Override
    public ImmutableValue<BlockState> block() {
        return this.blockValue;
    }

    @Override
    public ImmutableValue<Integer> offset() {
        return this.offsetValue;
    }

    @Override
    public MinecartBlockData asMutable() {
        return new SpongeMinecartBlockData(this.block, this.offset);
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

    public int getOffset() {
        return this.offset;
    }

    @Override
    protected void registerGetters() {
        registerKeyValue(Keys.REPRESENTED_BLOCK, ImmutableSpongeMinecartBlockData.this::block);
        registerKeyValue(Keys.OFFSET, ImmutableSpongeMinecartBlockData.this::offset);

        registerFieldGetter(Keys.REPRESENTED_BLOCK, ImmutableSpongeMinecartBlockData.this::getBlock);
        registerFieldGetter(Keys.OFFSET, ImmutableSpongeMinecartBlockData.this::getOffset);
    }
}
