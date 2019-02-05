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
package org.spongepowered.common.data.manipulator.mutable.item;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableBlockItemData;
import org.spongepowered.api.data.manipulator.mutable.BlockItemData;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeBlockItemData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.common.data.util.DataConstants;
import org.spongepowered.common.data.value.SpongeMutableValue;

public class SpongeBlockItemData extends AbstractSingleData<BlockState, BlockItemData, ImmutableBlockItemData> implements BlockItemData {

    public SpongeBlockItemData(BlockState value) {
        super(BlockItemData.class, value, Keys.ITEM_BLOCKSTATE);
    }

    public SpongeBlockItemData() {
        this(DataConstants.Catalog.DEFAULT_BLOCK_STATE);
    }

    @Override
    public BlockItemData copy() {
        return new SpongeBlockItemData(this.getValue());
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
            .set(Keys.ITEM_BLOCKSTATE, this.getValue());
    }

    @Override
    public Value.Mutable<BlockState> state() {
        return new SpongeMutableValue<>(Keys.ITEM_BLOCKSTATE, this.getValue());
    }

    @Override
    protected Value.Mutable<?> getValueGetter() {
        return state();
    }

    @Override
    public ImmutableBlockItemData asImmutable() {
        return new ImmutableSpongeBlockItemData(this.getValue());
    }

}
