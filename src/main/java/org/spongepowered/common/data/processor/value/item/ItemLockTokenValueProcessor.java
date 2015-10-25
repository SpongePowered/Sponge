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

import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Optional;

public class ItemLockTokenValueProcessor extends AbstractSpongeValueProcessor<ItemStack, String, Value<String>> {

    public ItemLockTokenValueProcessor() {
        super(ItemStack.class, Keys.LOCK_TOKEN);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        DataTransactionBuilder builder = DataTransactionBuilder.builder();
        Optional<String> previousLock = getValueFromContainer(container);
        
        if (container instanceof ItemStack) {
            if (previousLock.isPresent()) {
                ImmutableValue<String> immutableLock = new ImmutableSpongeValue<>(Keys.LOCK_TOKEN, previousLock.get());
                builder.replace(immutableLock);
            }
            if (!((ItemStack) container).hasTagCompound()) {
                return builder.result(DataTransactionResult.Type.SUCCESS).build();
            }
            
            try {
                NBTTagCompound mainCompound = NbtDataUtil.getItemCompound((ItemStack) container).get();
                NBTTagCompound tileCompound = mainCompound.getCompoundTag(NbtDataUtil.BLOCK_ENTITY_TAG);
                tileCompound.removeTag(NbtDataUtil.ITEM_LOCK);
                
                return builder.result(DataTransactionResult.Type.SUCCESS).build();
            } catch (Exception e) {
                return builder.result(DataTransactionResult.Type.ERROR).build();
            }
        }
        
        return DataTransactionBuilder.failNoData();
    }

    @Override
    protected Value<String> constructValue(String defaultValue) {
        return new SpongeValue<>(Keys.LOCK_TOKEN, defaultValue);
    }

    @Override
    protected boolean set(ItemStack container, String value) {
        NBTTagCompound mainCompound = NbtDataUtil.getOrCreateCompound(container);
        NBTTagCompound tileCompound = NbtDataUtil.getOrCreateSubCompound(mainCompound, NbtDataUtil.BLOCK_ENTITY_TAG);
        tileCompound.setString(NbtDataUtil.ITEM_LOCK, value);
        return true;
    }

    @Override
    protected Optional<String> getVal(ItemStack container) {
        if (container.hasTagCompound()) {
            NBTTagCompound mainCompound = container.getTagCompound();
            
            if (mainCompound.hasKey(NbtDataUtil.BLOCK_ENTITY_TAG)) {
                NBTTagCompound tileCompound = mainCompound.getCompoundTag(NbtDataUtil.BLOCK_ENTITY_TAG);
                
                return Optional.of(tileCompound.getString(NbtDataUtil.ITEM_LOCK));
            }
        }
        return Optional.empty();
    }

    @Override
    protected ImmutableValue<String> constructImmutableValue(String value) {
        return constructValue(value).asImmutable();
    }

}
