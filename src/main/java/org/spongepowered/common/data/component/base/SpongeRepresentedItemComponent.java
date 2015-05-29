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
package org.spongepowered.common.data.component.base;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.component.base.RepresentedItemComponent;
import org.spongepowered.api.data.token.Tokens;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackComparators;
import org.spongepowered.common.data.component.AbstractSingleValueComponent;
import org.spongepowered.common.item.SpongeItemStackBuilder;

public class SpongeRepresentedItemComponent extends AbstractSingleValueComponent<ItemStack, RepresentedItemComponent> implements RepresentedItemComponent {

    public SpongeRepresentedItemComponent() {
        super(RepresentedItemComponent.class, new SpongeItemStackBuilder().itemType(ItemTypes.STONE).quantity(1).build());
    }

    @Override
    public RepresentedItemComponent reset() {
        return setValue(new SpongeItemStackBuilder().itemType(ItemTypes.STONE).quantity(1).build());
    }

    @Override
    public RepresentedItemComponent copy() {
        return new SpongeRepresentedItemComponent().setValue(new SpongeItemStackBuilder().fromItemStack(this.getValue()).build());
    }

    @Override
    public int compareTo(RepresentedItemComponent o) {
        return ItemStackComparators.ALL.compare(o.getValue(), this.getValue());
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer().set(Tokens.REPRESENTED_ITEM.getQuery(), this.getValue());
    }
}
