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
package org.spongepowered.common.data.processor.data.tileentity;

import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFlowerPot;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableRepresentedItemData;
import org.spongepowered.api.data.manipulator.mutable.RepresentedItemData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.common.data.manipulator.mutable.SpongeRepresentedItemData;
import org.spongepowered.common.data.processor.common.AbstractTileEntitySingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.mixin.core.block.BlockFlowerPotAccessor;

import java.util.Optional;

public class FlowerPotDataProcessor extends
        AbstractTileEntitySingleDataProcessor<TileEntityFlowerPot, ItemStackSnapshot, Value<ItemStackSnapshot>, RepresentedItemData, ImmutableRepresentedItemData> {

    public FlowerPotDataProcessor() {
        super(TileEntityFlowerPot.class, Keys.REPRESENTED_ITEM);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (!(container instanceof TileEntityFlowerPot)) {
            return DataTransactionResult.failNoData();
        }
        TileEntityFlowerPot flowerPot = (TileEntityFlowerPot) container;
        Optional<ItemStackSnapshot> old = getVal(flowerPot);
        if (!old.isPresent()) {
            return DataTransactionResult.successNoData();
        }
        flowerPot.setItemStack(ItemStack.EMPTY);
        flowerPot.markDirty();
        return DataTransactionResult.successRemove(constructImmutableValue(old.get()));
    }

    @Override
    protected boolean set(TileEntityFlowerPot flowerPot, ItemStackSnapshot stackSnapshot) {
        if (stackSnapshot == ItemStackSnapshot.NONE) {
            flowerPot.setItemStack(ItemStack.EMPTY);
        } else {
            ItemStack stack = (ItemStack) stackSnapshot.createStack();
            if (!((BlockFlowerPotAccessor) Blocks.FLOWER_POT).accessor$canItemBePotted(stack)) {
                return false;
            }
            flowerPot.setItemStack(stack);
        }
        flowerPot.markDirty();
        flowerPot.getWorld().notifyBlockUpdate(flowerPot.getPos(), flowerPot.getWorld().getBlockState(flowerPot.getPos()), flowerPot.getWorld()
                .getBlockState(flowerPot.getPos()), 3);
        return true;
    }

    @Override
    protected Optional<ItemStackSnapshot> getVal(TileEntityFlowerPot flowerPot) {
        if (flowerPot.getFlowerPotItem() == null) {
            return Optional.empty();
        }
        ItemStack stack = new ItemStack(flowerPot.getFlowerPotItem(), 1, flowerPot.getFlowerPotData());
        return Optional.of(((org.spongepowered.api.item.inventory.ItemStack) stack).createSnapshot());
    }

    @Override
    protected Value<ItemStackSnapshot> constructValue(ItemStackSnapshot value) {
        return new SpongeValue<>(Keys.REPRESENTED_ITEM, ItemStackSnapshot.NONE, value);
    }

    @Override
    protected ImmutableValue<ItemStackSnapshot> constructImmutableValue(ItemStackSnapshot value) {
        return new ImmutableSpongeValue<>(Keys.REPRESENTED_ITEM, ItemStackSnapshot.NONE, value);
    }

    @Override
    protected RepresentedItemData createManipulator() {
        return new SpongeRepresentedItemData();
    }

}
