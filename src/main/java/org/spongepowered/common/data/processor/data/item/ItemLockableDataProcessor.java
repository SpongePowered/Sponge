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
package org.spongepowered.common.data.processor.data.item;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.LockCode;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableLockableData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.LockableData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeLockableData;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

public final class ItemLockableDataProcessor extends AbstractItemSingleDataProcessor<String, Value<String>, LockableData, ImmutableLockableData> {

    public ItemLockableDataProcessor() {
        super(stack -> {
            final Item item = stack.func_77973_b();
            if (!(item instanceof BlockItem)) {
                return false;
            }
            final Block block = ((BlockItem) item).func_179223_d();
            if (!(block instanceof ITileEntityProvider)) {
                return false;
            }
            final TileEntity tile = ((ITileEntityProvider) block).func_149915_a(null, item.func_77647_b(stack.func_77952_i()));
            return tile instanceof LockableTileEntity;
        } , Keys.LOCK_TOKEN);
    }

    @Override
    public DataTransactionResult removeFrom(final ValueContainer<?> container) {
        if (supports(container)) {
            set((ItemStack) container, "");
            return DataTransactionResult.successNoData();
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    protected boolean set(final ItemStack stack, final String value) {
        if (value.isEmpty()) {
            if (stack.func_77942_o() && stack.func_77978_p().func_150297_b(Constants.Item.BLOCK_ENTITY_TAG, Constants.NBT.TAG_COMPOUND)) {
                stack.func_77978_p().func_74775_l(Constants.Item.BLOCK_ENTITY_TAG).func_82580_o(Constants.Item.LOCK);
            }
            return true;
        }
        final LockCode code = new LockCode(value);
        code.func_180157_a(stack.func_190925_c(Constants.Item.BLOCK_ENTITY_TAG));
        return true;
    }

    @Override
    protected Optional<String> getVal(final ItemStack container) {
        if (container.func_77978_p() == null) {
            return Optional.of("");
        }
        final CompoundNBT tileCompound = container.func_77978_p().func_74775_l(Constants.Item.BLOCK_ENTITY_TAG);
        final LockCode code = LockCode.func_180158_b(tileCompound);
        if (code.func_180160_a()) {
            return Optional.empty();
        }
        return Optional.of(code.func_180159_b());
    }

    @Override
    protected Value<String> constructValue(final String actualValue) {
        return new SpongeValue<String>(Keys.LOCK_TOKEN, "", actualValue);
    }

    @Override
    protected ImmutableValue<String> constructImmutableValue(final String value) {
        return new ImmutableSpongeValue<String>(Keys.LOCK_TOKEN, "", value);
    }

    @Override
    protected LockableData createManipulator() {
        return new SpongeLockableData();
    }

}
