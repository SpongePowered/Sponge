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
package org.spongepowered.common.data.processor.data.block;

import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.BlockStoneSlabNew;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableSlabData;
import org.spongepowered.api.data.manipulator.mutable.block.SlabData;
import org.spongepowered.api.data.type.SlabType;
import org.spongepowered.api.data.type.SlabTypes;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeSlabData;
import org.spongepowered.common.data.processor.common.AbstractCatalogDataProcessor;
import org.spongepowered.common.data.value.mutable.SpongeValue;

public class SlabDataProcessor extends AbstractCatalogDataProcessor<SlabType, Value<SlabType>, SlabData, ImmutableSlabData> {

    public SlabDataProcessor() {
        super(Keys.SLAB_TYPE, input -> input.getItem() == ItemTypes.STONE_SLAB || input.getItem() == ItemTypes.STONE_SLAB2);
    }

    @Override
    protected int setToMeta(SlabType value) {
        return -1; // not used
    }

    @Override
    protected SlabType getFromMeta(int meta) {
        return (SlabType) (Object) BlockStoneSlab.EnumType.func_176625_a(meta);
    }

    @Override
    public SlabData createManipulator() {
        return new SpongeSlabData();
    }

    @Override
    protected boolean set(ItemStack stack, SlabType value) {
        // TODO - the API needs to be refactored, as it's no longer possible to change the type of an ItemStack
        Object oValue = value;
        if (stack.getItem() == ItemTypes.STONE_SLAB) {
            if (oValue instanceof BlockStoneSlab.EnumType) {
                stack.func_77964_b(((BlockStoneSlab.EnumType) oValue).func_176624_a());
                return true;
            }
        } else if (stack.getItem() == ItemTypes.STONE_SLAB2) {
            if (oValue instanceof BlockStoneSlabNew.EnumType) {
                stack.func_77964_b(((BlockStoneSlabNew.EnumType) oValue).func_176915_a());
                return true;
            }
        }
        return false;
    }

    @Override
    protected SlabType getDefaultValue() {
        return SlabTypes.COBBLESTONE;
    }

    @Override
    protected Value<SlabType> constructValue(SlabType actualValue) {
        return new SpongeValue<>(this.key, getDefaultValue(), actualValue);
    }

}
