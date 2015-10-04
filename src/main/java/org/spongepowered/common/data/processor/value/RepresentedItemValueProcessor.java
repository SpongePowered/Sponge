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
package org.spongepowered.common.data.processor.value;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;

public class RepresentedItemValueProcessor extends AbstractSpongeValueProcessor<ItemStackSnapshot, Value<ItemStackSnapshot>> {

    public RepresentedItemValueProcessor() {
        super(Keys.REPRESENTED_ITEM);
    }

    @Override
    protected Value<ItemStackSnapshot> constructValue(ItemStackSnapshot defaultValue) {
        return new SpongeValue<ItemStackSnapshot>(Keys.REPRESENTED_ITEM, defaultValue);
    }

    @Override
    public Optional<ItemStackSnapshot> getValueFromContainer(ValueContainer<?> container) {
        if (container instanceof EntityItemFrame) {
            final ItemStack itemStack = ((EntityItemFrame) container).getDisplayedItem();
            if (itemStack != null) {
                return Optional.of(((org.spongepowered.api.item.inventory.ItemStack) itemStack).createSnapshot());
            }
        } else if (container instanceof EntityItem) {
            return Optional.of(((org.spongepowered.api.item.inventory.ItemStack) ((EntityItem) container).getEntityItem()).createSnapshot());
        }
        return Optional.empty();
    }

    @Override
    public boolean supports(ValueContainer<?> container) {
        return container instanceof EntityItem || container instanceof EntityItemFrame;
    }

    @Override
    public DataTransactionResult offerToStore(ValueContainer<?> container, ItemStackSnapshot value) {
        final ImmutableValue<ItemStackSnapshot> newValue = constructValue(value).asImmutable();
        if (container instanceof EntityItemFrame) {
            final DataTransactionBuilder builder = DataTransactionBuilder.builder();
            if (((EntityItemFrame) container).getDisplayedItem() != null) {
                builder.replace(constructValue(getValueFromContainer(container).get()).asImmutable());
            }
            try {
                final ItemStack itemStack = (ItemStack) value.createStack();
                ((EntityItemFrame) container).setDisplayedItem(itemStack);
                builder.success(newValue);
                return builder.result(DataTransactionResult.Type.SUCCESS).build();
            } catch (Exception e) {
                return builder.result(DataTransactionResult.Type.ERROR).build();
            }
        } else if (container instanceof EntityItem) {
            final ImmutableValue<ItemStackSnapshot> old = constructValue(getValueFromContainer(container).get()).asImmutable();
            final ItemStack itemStack = (ItemStack) value.createStack();
            try {
                ((EntityItem) container).setEntityItemStack(itemStack);
            } catch (Exception e) {
                return DataTransactionBuilder.errorResult(newValue);
            }
            return DataTransactionBuilder.successReplaceResult(newValue, old);
        }
        return DataTransactionBuilder.failResult(newValue);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (container instanceof EntityItemFrame) {
            if (((EntityItemFrame) container).getDisplayedItem() != null) {
                final ImmutableValue<ItemStackSnapshot> value = constructValue(getValueFromContainer(container).get()).asImmutable();
                ((EntityItemFrame) container).setDisplayedItem(null);
                return DataTransactionBuilder.builder().replace(value).result(DataTransactionResult.Type.SUCCESS).build();
            }
        }
        return DataTransactionBuilder.failNoData();
    }
}
