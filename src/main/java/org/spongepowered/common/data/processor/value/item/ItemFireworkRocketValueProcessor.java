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
package org.spongepowered.common.data.processor.value.item;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.util.ComparatorUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeBoundedValue;
import org.spongepowered.common.data.value.mutable.SpongeBoundedValue;

import java.util.Optional;

public class ItemFireworkRocketValueProcessor extends AbstractSpongeValueProcessor<ItemStack, Integer, MutableBoundedValue<Integer>> {

    public ItemFireworkRocketValueProcessor() {
        super(ItemStack.class, Keys.FIREWORK_FLIGHT_MODIFIER);
    }

    @Override
    protected boolean supports(ItemStack container) {
        return container.getItem() == Items.fireworks;
    }

    @Override
    protected MutableBoundedValue<Integer> constructValue(Integer value) {
        return new SpongeBoundedValue<>(Keys.FIREWORK_FLIGHT_MODIFIER, 0, ComparatorUtil.intComparator(), 0, Integer.MAX_VALUE, value);
    }

    @Override
    protected boolean set(ItemStack container, Integer value) {
        NBTTagCompound fireworks = container.getSubCompound("Fireworks", true);
        fireworks.setByte("Flight", value.byteValue());
        return true;
    }

    @Override
    protected Optional<Integer> getVal(ItemStack container) {
        return Optional.of((int) container.getSubCompound("Fireworks", true).getByte("Flight"));
    }

    @Override
    protected ImmutableValue<Integer> constructImmutableValue(Integer value) {
        return new ImmutableSpongeBoundedValue<>(Keys.FIREWORK_FLIGHT_MODIFIER, value, 0, ComparatorUtil.intComparator(), 0, Integer.MAX_VALUE);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if(container instanceof ItemStack) {
            NBTTagCompound fireworks = ((ItemStack) container).getSubCompound("Fireworks", false);
            if(fireworks != null) {
                fireworks.removeTag("Flight");
            }
            return DataTransactionBuilder.successNoData();
        }
        return DataTransactionBuilder.failNoData();
    }
}
