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
package org.spongepowered.common.data.manipulator.immutable.entity;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableActiveItemData;
import org.spongepowered.api.data.manipulator.mutable.entity.ActiveItemData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeActiveItemData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

public class ImmutableSpongeActiveItemData extends AbstractImmutableSingleData<ItemStackSnapshot, ImmutableActiveItemData, ActiveItemData>
        implements ImmutableActiveItemData {

    private final ImmutableValue<ItemStackSnapshot> snapshotValue;

    public ImmutableSpongeActiveItemData() {
        this(ItemStackSnapshot.NONE);
    }

    public ImmutableSpongeActiveItemData(ItemStackSnapshot itemStackSnapshot) {
        super(ImmutableActiveItemData.class, itemStackSnapshot, Keys.ACTIVE_ITEM);
        this.snapshotValue = new ImmutableSpongeValue<>(Keys.ACTIVE_ITEM, itemStackSnapshot);
    }

    @Override
    public ImmutableValue<ItemStackSnapshot> activeItem() {
        return this.snapshotValue;
    }

    @Override
    protected ImmutableValue<ItemStackSnapshot> getValueGetter() {
        return activeItem();
    }

    @Override
    public ActiveItemData asMutable() {
        return new SpongeActiveItemData(getValue());
    }

}
