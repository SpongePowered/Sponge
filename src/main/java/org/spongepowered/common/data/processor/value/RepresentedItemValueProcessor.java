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

import net.minecraft.entity.Entity;
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
import org.spongepowered.common.data.util.EntityDataUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;

public class RepresentedItemValueProcessor extends AbstractSpongeValueProcessor<Entity, ItemStackSnapshot, Value<ItemStackSnapshot>> {

    public RepresentedItemValueProcessor() {
        super(Entity.class, Keys.REPRESENTED_ITEM);
    }

    @Override
    protected Value<ItemStackSnapshot> constructValue(ItemStackSnapshot defaultValue) {
        return new SpongeValue<>(Keys.REPRESENTED_ITEM, defaultValue);
    }

    @Override
    protected boolean set(Entity container, ItemStackSnapshot value) {
        if (container instanceof EntityItemFrame) {
            ((EntityItemFrame) container).setDisplayedItem((ItemStack) value.createStack());
            return true;
        } else if (container instanceof EntityItem) {
            ((EntityItem) container).setEntityItemStack((ItemStack) value.createStack());
            return true;
        }
        return false;
    }

    @Override
    protected Optional<ItemStackSnapshot> getVal(Entity container) {
        return EntityDataUtil.getRepresentedItemFrom(container);
    }

    @Override
    protected ImmutableValue<ItemStackSnapshot> constructImmutableValue(ItemStackSnapshot value) {
        return new ImmutableSpongeValue<>(Keys.REPRESENTED_ITEM, value);
    }

    @Override
    public boolean supports(ValueContainer<?> container) {
        return container instanceof EntityItem || container instanceof EntityItemFrame;
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
