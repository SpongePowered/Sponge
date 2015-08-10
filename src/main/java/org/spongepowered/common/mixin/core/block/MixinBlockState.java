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
package org.spongepowered.common.mixin.core.block;

import static org.spongepowered.api.data.DataQuery.of;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateBase;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.interfaces.block.IMixinBlock;

@NonnullByDefault
@Mixin(net.minecraft.block.state.BlockState.StateImplementation.class)
public abstract class MixinBlockState extends BlockStateBase implements BlockState {

    @Shadow
    @SuppressWarnings("rawtypes")
    private ImmutableMap properties;
    @Shadow private Block block;

    private ImmutableList<ImmutableDataManipulator<?, ?>> manipulators;

    @Override
    public BlockType getType() {
        return (BlockType) getBlock();
    }

    @Override
    public ImmutableList<ImmutableDataManipulator<?, ?>> getManipulators() {
        if (this.manipulators == null) {
            this.manipulators = ((IMixinBlock) this.block).getManipulators(this);
        }
        return this.manipulators;
    }


    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
            .set(of("BlockType"), this.getType().getId())
            .set(of("Data"), this.getManipulators());
    }
}
