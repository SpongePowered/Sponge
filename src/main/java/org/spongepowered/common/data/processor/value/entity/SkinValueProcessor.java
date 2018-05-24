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

import com.mojang.authlib.properties.Property;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.processor.data.entity.SkinDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.interfaces.entity.IMixinSkinnable;

import java.util.Optional;

public class SkinValueProcessor extends AbstractSpongeValueProcessor<IMixinSkinnable, ProfileProperty, Value<ProfileProperty>> {

    public SkinValueProcessor() {
        super(IMixinSkinnable.class, Keys.SKIN);
    }

    @Override
    protected Value<ProfileProperty> constructValue(ProfileProperty actualValue) {
        return new SpongeValue<>(Keys.SKIN, SkinDataProcessor.EMPTY_SKIN,  actualValue);
    }

    @Override
    protected boolean set(IMixinSkinnable container, ProfileProperty value) {
        return container.setSkin(value);
    }

    @Override
    protected Optional<ProfileProperty> getVal(IMixinSkinnable container) {
        return Optional.of(container.getSkin());
    }

    @Override
    protected ImmutableValue<ProfileProperty> constructImmutableValue(ProfileProperty value) {
        return new ImmutableSpongeValue<>(Keys.SKIN, SkinDataProcessor.EMPTY_SKIN, value);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
