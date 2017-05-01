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
package org.spongepowered.common.data.manipulator.immutable;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableRepresentedItemData;
import org.spongepowered.api.data.manipulator.mutable.RepresentedItemData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.common.data.manipulator.mutable.SpongeRepresentedItemData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

public class ImmutableSpongeRepresentedItemData extends AbstractImmutableSingleData<ItemStackSnapshot, ImmutableRepresentedItemData, RepresentedItemData> implements ImmutableRepresentedItemData {

    private final ImmutableValue<ItemStackSnapshot> immutableValue = new ImmutableSpongeValue<>(Keys.REPRESENTED_ITEM, ItemStackSnapshot.NONE, this.value);

    public ImmutableSpongeRepresentedItemData(ItemStackSnapshot itemStack) {
        super(ImmutableRepresentedItemData.class, itemStack, Keys.REPRESENTED_ITEM);
    }

    @Override
    public ImmutableValue<ItemStackSnapshot> item() {
        return this.immutableValue;
    }

    @Override
    protected ImmutableValue<?> getValueGetter() {
        return item();
    }

    @Override
    public RepresentedItemData asMutable() {
        return new SpongeRepresentedItemData(this.value);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
            .set(Keys.REPRESENTED_ITEM, this.value);
    }

}
