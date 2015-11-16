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
package org.spongepowered.common.data.processor.value.item;

import com.google.common.collect.ImmutableSet;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableSetValue;
import org.spongepowered.api.data.value.mutable.SetValue;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.processor.common.BreakablePlaceableUtils;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeSetValue;
import org.spongepowered.common.data.value.mutable.SpongeSetValue;

import java.util.Optional;
import java.util.Set;

public class PlaceableValueProcessor extends AbstractSpongeValueProcessor<ItemStack, Set<BlockType>, SetValue<BlockType>> {

    public PlaceableValueProcessor() {
        super(ItemStack.class, Keys.PLACEABLE_BLOCKS);
    }

    @Override
    public SetValue<BlockType> constructValue(Set<BlockType> defaultValue) {
        return new SpongeSetValue<>(Keys.PLACEABLE_BLOCKS, defaultValue);
    }
    
    @Override
    public ImmutableSetValue<BlockType> constructImmutableValue(Set<BlockType> defaultValue) {
        return new ImmutableSpongeSetValue<>(Keys.PLACEABLE_BLOCKS, defaultValue);
    }

    @Override
    public Optional<Set<BlockType>> getVal(ItemStack stack) {
        return BreakablePlaceableUtils.get(stack, NbtDataUtil.ITEM_PLACEABLE_BLOCKS);
    }

    @Override
    public boolean set(ItemStack stack, Set<BlockType> value) {
        return BreakablePlaceableUtils.set(stack, NbtDataUtil.ITEM_PLACEABLE_BLOCKS, value);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return offerToStore(container, ImmutableSet.<BlockType> of());
    }
}
