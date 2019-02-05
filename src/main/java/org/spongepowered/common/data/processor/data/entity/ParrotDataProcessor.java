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

import net.minecraft.entity.passive.EntityParrot;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableParrotData;
import org.spongepowered.api.data.manipulator.mutable.ParrotData;
import org.spongepowered.api.data.type.ParrotVariant;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeParrotData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.value.SpongeImmutableValue;
import org.spongepowered.common.data.value.SpongeMutableValue;
import org.spongepowered.common.entity.SpongeParrotVariant;
import org.spongepowered.common.registry.type.entity.ParrotVariantRegistryModule;

import java.util.Optional;

public class ParrotDataProcessor extends
        AbstractEntitySingleDataProcessor<EntityParrot, ParrotVariant, ParrotData, ImmutableParrotData> {

    public ParrotDataProcessor() {
        super(EntityParrot.class, Keys.PARROT_VARIANT);
    }

    @Override
    protected boolean set(EntityParrot dataHolder, ParrotVariant value) {
        dataHolder.setVariant(((SpongeParrotVariant)value).type);
        return true;
    }

    @Override
    protected Optional<ParrotVariant> getVal(EntityParrot dataHolder) {
        return Optional.of(ParrotVariantRegistryModule.PARROT_VARIANT_IDMAP.get(dataHolder.getVariant()));
    }

    @Override
    protected Value.Immutable<ParrotVariant> constructImmutableValue(ParrotVariant value) {
        return SpongeImmutableValue.cachedOf(this.key, value);
    }

    @Override
    protected Value.Mutable<ParrotVariant> constructMutableValue(ParrotVariant actualValue) {
        return new SpongeMutableValue<>(this.key, actualValue);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }

    @Override
    protected ParrotData createManipulator() {
        return new SpongeParrotData();
    }

}
