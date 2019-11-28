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
package org.spongepowered.common.data.manipulator.mutable;

import net.minecraft.item.Items;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableRepresentedItemData;
import org.spongepowered.api.data.manipulator.mutable.RepresentedItemData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeRepresentedItemData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.common.data.value.mutable.SpongeValue;

public class SpongeRepresentedItemData extends AbstractSingleData<ItemStackSnapshot, RepresentedItemData, ImmutableRepresentedItemData> implements RepresentedItemData {

    public SpongeRepresentedItemData() {
        this(((ItemStack) new net.minecraft.item.ItemStack(Items.field_151055_y, 1)).createSnapshot());
    }

    public SpongeRepresentedItemData(ItemStackSnapshot itemStack) {
        super(RepresentedItemData.class, itemStack, Keys.REPRESENTED_ITEM);
    }

    @Override
    public Value<ItemStackSnapshot> item() {
        return new SpongeValue<>(Keys.REPRESENTED_ITEM, ItemStackSnapshot.NONE, this.getValue());
    }

    @Override
    public RepresentedItemData copy() {
        return new SpongeRepresentedItemData(this.getValue());
    }

    @Override
    public ImmutableRepresentedItemData asImmutable() {
        return new ImmutableSpongeRepresentedItemData(this.getValue());
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew()
            .set(Keys.REPRESENTED_ITEM, this.getValue());
    }

    @Override
    protected Value<?> getValueGetter() {
        return item();
    }
}
