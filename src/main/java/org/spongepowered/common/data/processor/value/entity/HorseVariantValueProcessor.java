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

import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityDonkey;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityMule;
import net.minecraft.entity.passive.EntitySkeletonHorse;
import net.minecraft.entity.passive.EntityZombieHorse;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;

@SuppressWarnings("deprecation")
public class HorseVariantValueProcessor extends AbstractSpongeValueProcessor<AbstractHorse, org.spongepowered.api.data.type.HorseVariant, Value<org.spongepowered.api.data.type.HorseVariant>> {

    public HorseVariantValueProcessor() {
        super(AbstractHorse.class, Keys.HORSE_VARIANT);
    }

    @Override
    protected Value<org.spongepowered.api.data.type.HorseVariant> constructValue(org.spongepowered.api.data.type.HorseVariant defaultValue) {
        return new SpongeValue<>(Keys.HORSE_VARIANT, defaultValue);
    }

    @Override
    protected boolean set(AbstractHorse container, org.spongepowered.api.data.type.HorseVariant value) {
        throw new UnsupportedOperationException("HorseData is deprecated - horse types are now separate entities!");
    }

    @Override
    protected Optional<org.spongepowered.api.data.type.HorseVariant> getVal(AbstractHorse container) {
        if (container instanceof EntityHorse) {
            return Optional.of(org.spongepowered.api.data.type.HorseVariants.HORSE);
        } else if (container instanceof EntityDonkey) {
            return Optional.of(org.spongepowered.api.data.type.HorseVariants.DONKEY);
        } else if (container instanceof EntityMule) {
            return Optional.of(org.spongepowered.api.data.type.HorseVariants.MULE);
        } else if (container instanceof EntityZombieHorse) {
            return Optional.of(org.spongepowered.api.data.type.HorseVariants.UNDEAD_HORSE);
        } else if (container instanceof EntitySkeletonHorse) {
            return Optional.of(org.spongepowered.api.data.type.HorseVariants.SKELETON_HORSE);
        }
        return Optional.empty();
    }

    @Override
    protected ImmutableValue<org.spongepowered.api.data.type.HorseVariant> constructImmutableValue(org.spongepowered.api.data.type.HorseVariant value) {
        return ImmutableSpongeValue.cachedOf(Keys.HORSE_VARIANT, org.spongepowered.api.data.type.HorseVariants.HORSE, value);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
