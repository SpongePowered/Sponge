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

import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityDonkey;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityMule;
import net.minecraft.entity.passive.EntitySkeletonHorse;
import net.minecraft.entity.passive.EntityZombieHorse;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HorseVariant;
import org.spongepowered.api.data.type.HorseVariants;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.processor.common.HorseUtils;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.entity.SpongeHorseVariant;

import java.util.Optional;

public class HorseVariantValueProcessor extends AbstractSpongeValueProcessor<AbstractHorse, HorseVariant, Value<HorseVariant>> {

    public HorseVariantValueProcessor() {
        super(AbstractHorse.class, Keys.HORSE_VARIANT);
    }

    @Override
    protected Value<HorseVariant> constructValue(HorseVariant defaultValue) {
        return new SpongeValue<>(Keys.HORSE_VARIANT, defaultValue);
    }

    @Override
    protected boolean set(AbstractHorse container, HorseVariant value) {
        throw new UnsupportedOperationException("HorseData is deprecated - horse types are now separate entities!");
    }

    @Override
    protected Optional<HorseVariant> getVal(AbstractHorse container) {
        if (container instanceof EntityHorse) {
            return Optional.of(HorseVariants.HORSE);
        } else if (container instanceof EntityDonkey) {
            return Optional.of(HorseVariants.DONKEY);
        } else if (container instanceof EntityMule) {
            return Optional.of(HorseVariants.MULE);
        } else if (container instanceof EntityZombieHorse) {
            return Optional.of(HorseVariants.UNDEAD_HORSE);
        } else if (container instanceof EntitySkeletonHorse) {
            return Optional.of(HorseVariants.SKELETON_HORSE);
        }
        return Optional.empty();
    }

    @Override
    protected ImmutableValue<HorseVariant> constructImmutableValue(HorseVariant value) {
        return ImmutableSpongeValue.cachedOf(Keys.HORSE_VARIANT, HorseVariants.HORSE, value);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
