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

import net.minecraft.item.ItemStack;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.common.data.provider.GenericMutableBoundedDataProvider;

import java.util.Optional;

public class ItemStackDurabilityProvider extends GenericMutableBoundedDataProvider<ItemStack, Integer> {

    public ItemStackDurabilityProvider() {
        super(Keys.ITEM_DURABILITY);
    }

    @Override
    protected boolean supports(ItemStack dataHolder) {
        return dataHolder.getItem().isDamageable();
    }

    @Override
    protected BoundedValue<Integer> constructValue(ItemStack dataHolder, Integer element) {
        final int maxDamage = dataHolder.getItem().getMaxDamage();
        return BoundedValue.immutableOf(this.getKey(), element, 0, maxDamage);
    }

    @Override
    protected Optional<Integer> getFrom(ItemStack dataHolder) {
        final int maxDamage = dataHolder.getItem().getMaxDamage();
        return Optional.of(maxDamage - dataHolder.getDamage());
    }

    @Override
    protected boolean set(ItemStack dataHolder, Integer value) {
        final int maxDamage = dataHolder.getItem().getMaxDamage();
        final int damage = maxDamage - value;
        if (damage > maxDamage || damage < 0) {
            return false;
        }
        dataHolder.setDamage(damage);
        return true;
    }
}
