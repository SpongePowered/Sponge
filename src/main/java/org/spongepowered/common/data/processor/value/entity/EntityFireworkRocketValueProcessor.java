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
package org.spongepowered.common.data.processor.value.entity;

import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.processor.common.FireworkUtils;
import org.spongepowered.common.data.util.ComparatorUtil;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeBoundedValue;
import org.spongepowered.common.data.value.mutable.SpongeBoundedValue;
import org.spongepowered.common.interfaces.entity.IMixinEntityFireworkRocket;

import java.util.Optional;

public class EntityFireworkRocketValueProcessor extends AbstractSpongeValueProcessor<EntityFireworkRocket, Integer, MutableBoundedValue<Integer>> {

    public EntityFireworkRocketValueProcessor() {
        super(EntityFireworkRocket.class, Keys.FIREWORK_FLIGHT_MODIFIER);
    }

    @Override
    protected MutableBoundedValue<Integer> constructValue(Integer value) {
        return new SpongeBoundedValue<>(Keys.FIREWORK_FLIGHT_MODIFIER, 0, ComparatorUtil.intComparator(), 0, Integer.MAX_VALUE, value);
    }

    @Override
    protected boolean set(EntityFireworkRocket container, Integer value) {
        NBTTagCompound fireworks = FireworkUtils.getItem(container).getSubCompound("Fireworks", true);
        fireworks.setByte("Flight", value.byteValue());
        ((IMixinEntityFireworkRocket) container).setModifier(value.byteValue());
        return true;
    }

    @Override
    protected Optional<Integer> getVal(EntityFireworkRocket container) {
        NBTTagCompound tag = NbtDataUtil.getOrCreateCompound(FireworkUtils.getItem(container));
        if(tag.hasKey("Fireworks") && tag.getCompoundTag("Fireworks").hasKey("Flight")) {
            return Optional.of((int) tag.getCompoundTag("Fireworks").getByte("Flight"));
        }
        return Optional.empty();
    }

    @Override
    protected ImmutableValue<Integer> constructImmutableValue(Integer value) {
        return new ImmutableSpongeBoundedValue<>(Keys.FIREWORK_FLIGHT_MODIFIER, value, 0, ComparatorUtil.intComparator(), 0, Integer.MAX_VALUE);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionBuilder.failNoData();
    }
}
