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
package org.spongepowered.common.data.processor.data.entity;

import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.processor.common.FireworkUtils;
import org.spongepowered.common.data.util.ComparatorUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeBoundedValue;
import org.spongepowered.common.data.value.mutable.SpongeBoundedValue;
import org.spongepowered.common.interfaces.entity.IMixinEntityFireworkRocket;

import java.util.Optional;

public class FireworkRocketDataProcessor extends
        AbstractEntitySingleDataProcessor<EntityFireworkRocket, Integer, MutableBoundedValue<Integer>, FireworkRocketData, ImmutableFireworkRocketData> {

    public FireworkRocketDataProcessor() {
        super(EntityFireworkRocket.class, Keys.FIREWORK_FLIGHT_MODIFIER);
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
    protected Optional<Integer> getVal(EntityFireworkRocket firework) {
        ItemStack item = FireworkUtils.getItem(firework);
        NBTTagCompound fireworks = item.getOrCreateSubCompound("Fireworks");
        if (fireworks.hasKey("Flight")) {
            return Optional.of((int) fireworks.getByte("Flight"));
        }
        return Optional.empty();

    }

    @Override
    protected boolean set(EntityFireworkRocket firework, Integer modifier) {
        ItemStack item = FireworkUtils.getItem(firework);
        NBTTagCompound fireworks = item.getOrCreateSubCompound("Fireworks");
        fireworks.setByte("Flight", modifier.byteValue());
        ((IMixinEntityFireworkRocket) firework).setModifier(modifier.byteValue());
        return true;
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected MutableBoundedValue<Integer> constructValue(Integer value) {
        return new SpongeBoundedValue<>(Keys.FIREWORK_FLIGHT_MODIFIER, 0, value, ComparatorUtil.intComparator(), 0, Integer.MAX_VALUE);
    }

    @Override
    protected ImmutableValue<Integer> constructImmutableValue(Integer value) {
        return new ImmutableSpongeBoundedValue<>(Keys.FIREWORK_FLIGHT_MODIFIER, value, 0, ComparatorUtil.intComparator(), 0, Integer.MAX_VALUE);
    }

}
