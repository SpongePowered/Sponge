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

import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableLockableData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.LockableData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeLockableData;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Optional;

public class ItemLockableDataProcessor extends AbstractItemSingleDataProcessor<String, Value<String>, LockableData, ImmutableLockableData> {

    public ItemLockableDataProcessor() {
        super(stack -> {
            Item item = stack.getItem();
            return item.equals(Item.getItemFromBlock(Blocks.chest))
                || item.equals(Item.getItemFromBlock(Blocks.furnace))
                || item.equals(Item.getItemFromBlock(Blocks.lit_furnace))
                || item.equals(Item.getItemFromBlock(Blocks.dispenser))
                || item.equals(Item.getItemFromBlock(Blocks.dropper))
                || item.equals(Item.getItemFromBlock(Blocks.hopper))
                || item.equals(Item.getItemFromBlock(Blocks.brewing_stand))
                || item.equals(Item.getItemFromBlock(Blocks.beacon));
        }, Keys.LOCK_TOKEN);
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        if (dataHolder instanceof ItemStack) {
            DataTransactionBuilder builder = DataTransactionBuilder.builder();
            Optional<LockableData> oldData = from(dataHolder);

            if (oldData.isPresent()) {
                builder.replace(oldData.get().getValues());
            }
            if (!((ItemStack) dataHolder).hasTagCompound()) {
                return builder.result(DataTransactionResult.Type.SUCCESS).build();
            }
            try {
                final NBTTagCompound mainCompound = NbtDataUtil.getItemCompound(((ItemStack) dataHolder)).get();
                
                if (mainCompound.hasKey(NbtDataUtil.BLOCK_ENTITY_TAG)) {
                    NBTTagCompound tileCompound = mainCompound.getCompoundTag(NbtDataUtil.BLOCK_ENTITY_TAG);
                    tileCompound.removeTag(NbtDataUtil.ITEM_LOCK);
                }   
                
                return builder.result(DataTransactionResult.Type.SUCCESS).build();
            } catch (Exception e) {
                return builder.result(DataTransactionResult.Type.ERROR).build();
            }
        }
        
        return DataTransactionBuilder.failNoData();
    }

    @Override
    protected boolean set(ItemStack itemStack, String value) {
        NBTTagCompound mainCompound = NbtDataUtil.getOrCreateCompound(itemStack);
        NBTTagCompound tileCompound = NbtDataUtil.getOrCreateSubCompound(mainCompound, NbtDataUtil.BLOCK_ENTITY_TAG);

        tileCompound.setString(NbtDataUtil.ITEM_LOCK, value);
        return true;
    }

    @Override
    protected Optional<String> getVal(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            return Optional.empty();
        }
        NBTTagCompound mainCompound = stack.getTagCompound();

        if (!mainCompound.hasKey(NbtDataUtil.BLOCK_ENTITY_TAG, NbtDataUtil.TAG_COMPOUND)) {
            return Optional.empty();
        }
        NBTTagCompound tileCompound = mainCompound.getCompoundTag(NbtDataUtil.BLOCK_ENTITY_TAG);
        String lock = tileCompound.getString(NbtDataUtil.ITEM_LOCK);

        return Optional.of(lock);
    }

    @Override
    protected ImmutableValue<String> constructImmutableValue(String value) {
        return new ImmutableSpongeValue<String>(Keys.LOCK_TOKEN, "", value);
    }

    @Override
    protected LockableData createManipulator() {
        return new SpongeLockableData();
    }

}
