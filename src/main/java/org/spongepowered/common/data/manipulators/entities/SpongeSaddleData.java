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
package org.spongepowered.common.data.manipulators.entities;

import static org.spongepowered.api.data.DataQuery.of;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.manipulators.entities.SaddleData;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.common.data.manipulators.AbstractSingleValueData;
import org.spongepowered.common.item.SpongeItemStackBuilder;

public class SpongeSaddleData extends AbstractSingleValueData<ItemStack, SaddleData> implements SaddleData {

    public SpongeSaddleData() {
        super(SaddleData.class, new SpongeItemStackBuilder().itemType(ItemTypes.STONE).quantity(1).build());
    }

    @Override
    public ItemStack getSaddle() {
        return this.getValue();
    }

    @Override
    public SaddleData setSaddle(ItemStack itemStack) {
        return this.setValue(itemStack);
    }

    @Override
    public int compareTo(SaddleData o) {
        return 0;
    }

    @Override
    public SaddleData copy() {
        return new SpongeSaddleData().setValue(new SpongeItemStackBuilder().fromItemStack(this.value).build());
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer().set(of("Saddle"), this.getValue());
    }
}
