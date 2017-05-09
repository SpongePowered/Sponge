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
package org.spongepowered.common.mixin.optimization.block.state;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateBase;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockStateContainer.StateImplementation.class)
public abstract class MixinStateImplementation extends BlockStateBase {

    @Shadow @Final private Block block;
    @Shadow @Final private ImmutableMap<IProperty<?>, Comparable<?>> properties;
    @Shadow private ImmutableTable<IProperty<?>, Comparable<?>, IBlockState> propertyValueTable;

    /**
     * @author gabizou - April 8th, 2016
     *
     * @reason This is done to improve the performance of the method by
     *     removing an unnecessary hash lookup. The same logic can be
     *     computed with 1 hash lookup instead of 2.
     *
     * <p>Normally, all lookups are with proper properties anyways, the
     * only corner cases that they fail will result in null block states.
     * Throwing exceptions is an option, however, for the sake of speed,
     * these methods are otherwise patched for speed. If issues arise,
     * disabling this mixin is easily done in the sponge global config.</p>
     *
     * <p>This is partially contributed code from Aikar in PaperSpigot.</p>
     *
     * @param property The property to use
     * @param value The value keyed to the property
     * @param <T> The type of property
     * @param <V> The type of value
     * @return The block state, if not already this block state
     */
    @Overwrite
    @Final
    @Override
    public <T extends Comparable<T>, V extends T> IBlockState withProperty(IProperty<T> property, V value) {
        // Sponge - eliminate the hash lookups and validation lookups
        if (this.properties.get(property) == value) {
            return this;
        } else {
            final IBlockState blockState = this.propertyValueTable.get(property, value);
            if (blockState == null) {
                throw new IllegalArgumentException("No mapping found for the blockstate: "
                        + Block.REGISTRY.getNameForObject(this.block) + " of property: " + property.getName() + " and value: " + value);
            }
            return blockState;
        }
    }

}
