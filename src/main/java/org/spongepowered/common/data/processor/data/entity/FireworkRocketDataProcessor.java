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

import net.minecraft.entity.item.FireworkRocketEntity;
import net.minecraft.item.ItemStack;
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
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.processor.common.FireworkUtils;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeBoundedValue;
import org.spongepowered.common.data.value.mutable.SpongeBoundedValue;
import org.spongepowered.common.mixin.core.entity.EntityAccessor;
import org.spongepowered.common.mixin.core.entity.item.EntityFireworkRocketAccessor;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

public class FireworkRocketDataProcessor extends
        AbstractEntitySingleDataProcessor<FireworkRocketEntity, Integer, MutableBoundedValue<Integer>, FireworkRocketData, ImmutableFireworkRocketData> {

    public FireworkRocketDataProcessor() {
        super(FireworkRocketEntity.class, Keys.FIREWORK_FLIGHT_MODIFIER);
    }

    @Override
    protected FireworkRocketData createManipulator() {
        return new SpongeFireworkRocketData();
    }

    @Override
    public boolean supports(final EntityType entityType) {
        return entityType.equals(EntityTypes.FIREWORK);
    }

    @Override
    protected Optional<Integer> getVal(final FireworkRocketEntity firework) {
        final ItemStack item = FireworkUtils.getItem(firework);
        final CompoundNBT fireworks = item.getOrCreateChildTag("Fireworks");
        if (fireworks.contains("Flight")) {
            return Optional.of((int) fireworks.getByte("Flight"));
        }
        return Optional.empty();

    }

    @Override
    protected boolean set(final FireworkRocketEntity firework, final Integer modifier) {
        final ItemStack item = FireworkUtils.getItem(firework);
        final CompoundNBT fireworks = item.getOrCreateChildTag("Fireworks");
        fireworks.putByte("Flight", modifier.byteValue());
        ((EntityFireworkRocketAccessor) firework).spongeImpl$setLifeTime(10 * modifier.byteValue() + ((EntityAccessor) firework).accessor$getRandom().nextInt(6) + ((EntityAccessor) firework).accessor$getRandom().nextInt(7));
        return true;
    }

    @Override
    public DataTransactionResult removeFrom(final ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected MutableBoundedValue<Integer> constructValue(final Integer value) {
        return new SpongeBoundedValue<>(Keys.FIREWORK_FLIGHT_MODIFIER, 0, Constants.Functional.intComparator(), 0, Integer.MAX_VALUE, value);
    }

    @Override
    protected ImmutableValue<Integer> constructImmutableValue(final Integer value) {
        return new ImmutableSpongeBoundedValue<>(Keys.FIREWORK_FLIGHT_MODIFIER, value, 0, Constants.Functional.intComparator(), 0, Integer.MAX_VALUE);
    }

}
