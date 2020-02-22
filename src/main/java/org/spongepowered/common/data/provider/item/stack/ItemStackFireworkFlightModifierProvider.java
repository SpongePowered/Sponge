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
package org.spongepowered.common.data.provider.item.stack;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.item.ItemStackBoundedDataProvider;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

public class ItemStackFireworkFlightModifierProvider extends ItemStackBoundedDataProvider<Integer> {

    public ItemStackFireworkFlightModifierProvider() {
        super(Keys.FIREWORK_FLIGHT_MODIFIER);
    }

    @Override
    protected boolean supports(Item item) {
        return item == Items.FIREWORK_ROCKET;
    }

    @Override
    protected Optional<Integer> getFrom(ItemStack dataHolder) {
        @Nullable final CompoundNBT fireworks = dataHolder.getChildTag(Constants.Item.Fireworks.FIREWORKS);
        if (fireworks != null && fireworks.contains(Constants.Item.Fireworks.FLIGHT)) {
            return Optional.of((int) fireworks.getByte(Constants.Item.Fireworks.FLIGHT));
        }
        return Optional.empty();
    }

    @Override
    protected boolean set(ItemStack dataHolder, Integer value) {
        final CompoundNBT fireworks = dataHolder.getOrCreateChildTag(Constants.Item.Fireworks.FIREWORKS);
        fireworks.putByte(Constants.Item.Fireworks.FLIGHT, value.byteValue());
        return true;
    }

    @Override
    protected boolean delete(ItemStack dataHolder) {
        @Nullable final CompoundNBT fireworks = dataHolder.getChildTag(Constants.Item.Fireworks.FIREWORKS);
        if (fireworks != null) {
            fireworks.remove(Constants.Item.Fireworks.FLIGHT);
        }
        return true;
    }
}
