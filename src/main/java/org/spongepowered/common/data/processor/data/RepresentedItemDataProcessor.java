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
package org.spongepowered.common.data.processor.data;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableRepresentedItemData;
import org.spongepowered.api.data.manipulator.mutable.RepresentedItemData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.common.data.manipulator.mutable.SpongeRepresentedItemData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

import java.util.Optional;

public class RepresentedItemDataProcessor extends AbstractEntitySingleDataProcessor<Entity, ItemStackSnapshot, Value<ItemStackSnapshot>, RepresentedItemData, ImmutableRepresentedItemData> {

    public RepresentedItemDataProcessor() {
        super(Entity.class, Keys.REPRESENTED_ITEM);
    }

    @Override
    protected RepresentedItemData createManipulator() {
        return new SpongeRepresentedItemData();
    }

    @Override
    protected boolean set(Entity entity, ItemStackSnapshot value) {
        if (entity instanceof EntityItemFrame) {
            final ItemStack itemStack = (ItemStack) value.createStack();
            ((EntityItemFrame) entity).setDisplayedItem(itemStack);
        } else if (entity instanceof EntityItem) {
            final ItemStack itemStack = (ItemStack) value.createStack();
            ((EntityItem) entity).setEntityItemStack(itemStack);
        }
        return false;
    }

    @Override
    protected Optional<ItemStackSnapshot> getVal(Entity entity) {
        if (entity instanceof EntityItemFrame) {
            final ItemStack itemStack = ((EntityItemFrame) entity).getDisplayedItem();
            if (itemStack != null) {
                return Optional.of(((org.spongepowered.api.item.inventory.ItemStack) itemStack).createSnapshot());
            }
        } else if (entity instanceof EntityItem) {
            return Optional.of(((org.spongepowered.api.item.inventory.ItemStack) ((EntityItem) entity).getEntityItem()).createSnapshot());
        }
        return Optional.empty();
    }

    @Override
    protected ImmutableValue<ItemStackSnapshot> constructImmutableValue(ItemStackSnapshot value) {
        return new ImmutableSpongeValue<ItemStackSnapshot>(Keys.REPRESENTED_ITEM, value);
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        if (dataHolder instanceof EntityItemFrame) {
            if (((EntityItemFrame) dataHolder).getDisplayedItem() != null) {
                final ImmutableValue<ItemStackSnapshot> old = constructImmutableValue(getVal((EntityItemFrame) dataHolder).get());
                ((EntityItemFrame) dataHolder).setDisplayedItem(null);
                return DataTransactionBuilder.builder().replace(old).result(DataTransactionResult.Type.SUCCESS).build();
            } else {
                return DataTransactionBuilder.successNoData();
            }
        }
        return DataTransactionBuilder.failNoData();
    }
}
