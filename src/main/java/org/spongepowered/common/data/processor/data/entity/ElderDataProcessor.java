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

import net.minecraft.entity.monster.EntityElderGuardian;
import net.minecraft.entity.monster.EntityGuardian;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeElderData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.util.DataConstants;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;

@SuppressWarnings("deprecation")
public class ElderDataProcessor
    extends AbstractEntitySingleDataProcessor<EntityGuardian,
    Boolean,
    Value<Boolean>,
    org.spongepowered.api.data.manipulator.mutable.entity.ElderData,
    org.spongepowered.api.data.manipulator.immutable.entity.ImmutableElderData> {

    public ElderDataProcessor() {
        super(EntityGuardian.class, Keys.ELDER_GUARDIAN);
    }

    @Override
    protected org.spongepowered.api.data.manipulator.mutable.entity.ElderData createManipulator() {
        return new SpongeElderData(false);
    }

    @Override
    protected boolean set(EntityGuardian entity, Boolean value) {
        throw new UnsupportedOperationException("See EntityTypes.ELDER_GUARDIAN");
    }

    @Override
    protected Optional<Boolean> getVal(EntityGuardian entity) {
        return Optional.of(entity instanceof EntityElderGuardian);
    }

    @Override
    protected Value<Boolean> constructValue(Boolean actualValue) {
        return new SpongeValue<>(this.key, DataConstants.ELDER_GUARDIAN_DEFAULT, actualValue);
    }

    @Override
    protected ImmutableValue<Boolean> constructImmutableValue(Boolean value) {
        return ImmutableSpongeValue.cachedOf(this.key, DataConstants.ELDER_GUARDIAN_DEFAULT, value);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

}
