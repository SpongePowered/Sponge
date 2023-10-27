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

import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableSkinSignatureData;
import org.spongepowered.api.data.manipulator.mutable.entity.SkinSignatureData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeSkinSignatureData;
import org.spongepowered.common.data.processor.common.AbstractEntitySingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.entity.living.human.EntityHuman;

import java.util.Optional;

public class SkinSignatureDataProcessor extends
        AbstractEntitySingleDataProcessor<EntityHuman, String, Value<String>, SkinSignatureData, ImmutableSkinSignatureData> {

    public SkinSignatureDataProcessor() {
        super(EntityHuman.class, Keys.SKIN_SIGNATURE);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (!(container instanceof EntityHuman)) {
            return DataTransactionResult.failNoData();
        }
        return ((EntityHuman) container).removeSkinSignature();
    }

    @Override
    protected Value<String> constructValue(String actualValue) {
        return new SpongeValue<>(Keys.SKIN_SIGNATURE, actualValue);
    }

    @Override
    protected boolean set(EntityHuman entity, String value) {
        return entity.setSkinSignature(value);
    }

    @Override
    protected Optional<String> getVal(EntityHuman entity) {
        return Optional.ofNullable(entity.getSkinSignature());
    }

    @Override
    protected ImmutableValue<String> constructImmutableValue(String value) {
        return new ImmutableSpongeValue<String>(Keys.SKIN_SIGNATURE, value);
    }

    @Override
    protected SkinSignatureData createManipulator() {
        return new SpongeSkinSignatureData();
    }

}
