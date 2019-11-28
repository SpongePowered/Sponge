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
package org.spongepowered.common.data.processor.data.item;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableFireworkRocketData;
import org.spongepowered.api.data.manipulator.mutable.FireworkRocketData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.common.data.manipulator.mutable.SpongeFireworkRocketData;
import org.spongepowered.common.data.processor.common.AbstractItemSingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeBoundedValue;
import org.spongepowered.common.data.value.mutable.SpongeBoundedValue;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

public class ItemFireworkRocketDataProcessor
        extends AbstractItemSingleDataProcessor<Integer, MutableBoundedValue<Integer>, FireworkRocketData, ImmutableFireworkRocketData> {

    public ItemFireworkRocketDataProcessor() {
        super(stack -> stack.func_77973_b().equals(Items.field_151152_bP), Keys.FIREWORK_FLIGHT_MODIFIER);
    }

    @Override
    protected FireworkRocketData createManipulator() {
        return new SpongeFireworkRocketData();
    }

    @Override
    public boolean supports(EntityType entityType) {
        return entityType.equals(EntityTypes.FIREWORK);
    }

    @Override
    protected Optional<Integer> getVal(ItemStack itemStack) {
        CompoundNBT fireworks = itemStack.func_190925_c("Fireworks");
        if (fireworks.func_74764_b("Flight")) {
            return Optional.of((int) fireworks.func_74771_c("Flight"));
        }
        return Optional.empty();
    }

    @Override
    protected boolean set(ItemStack itemStack, Integer modifier) {
        CompoundNBT fireworks = itemStack.func_190925_c("Fireworks");
        fireworks.func_74774_a("Flight", modifier.byteValue());
        return true;
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (container instanceof ItemStack) {
            CompoundNBT fireworks = ((ItemStack) container).func_179543_a("Fireworks");
            if (fireworks != null) {
                fireworks.func_82580_o("Flight");
            }
            return DataTransactionResult.successNoData();
        }
        return DataTransactionResult.failNoData();
    }

    @Override
    protected MutableBoundedValue<Integer> constructValue(Integer value) {
        return new SpongeBoundedValue<>(Keys.FIREWORK_FLIGHT_MODIFIER, 0, Constants.Functional.intComparator(), 0, Integer.MAX_VALUE, value);
    }

    @Override
    protected ImmutableValue<Integer> constructImmutableValue(Integer value) {
        return new ImmutableSpongeBoundedValue<>(Keys.FIREWORK_FLIGHT_MODIFIER, value, 0, Constants.Functional.intComparator(), 0, Integer.MAX_VALUE);
    }

}
