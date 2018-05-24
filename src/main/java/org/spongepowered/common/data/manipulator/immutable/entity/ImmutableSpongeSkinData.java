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
package org.spongepowered.common.data.manipulator.immutable.entity;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableSkinData;
import org.spongepowered.api.data.manipulator.mutable.entity.SkinData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeSkinData;
import org.spongepowered.common.data.processor.data.entity.SkinDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

public class ImmutableSpongeSkinData extends AbstractImmutableData<ImmutableSkinData, SkinData> implements ImmutableSkinData {

    private final ImmutableValue<ProfileProperty> skinValue;
    private final ImmutableValue<Boolean> updateTabListValue;

    public ImmutableSpongeSkinData() {
        this(SkinDataProcessor.EMPTY_SKIN, false);
    }

    public ImmutableSpongeSkinData(ProfileProperty skin, boolean updateTabList) {
        super(ImmutableSkinData.class);
        this.skinValue = new ImmutableSpongeValue<>(Keys.SKIN, SkinDataProcessor.EMPTY_SKIN, skin);
        this.updateTabListValue = ImmutableSpongeValue.<Boolean>cachedOf(Keys.UPDATE_GAME_PROFILE, false, (boolean) updateTabList);
    }

    @Override
    public SkinData asMutable() {
        return new SpongeSkinData(this.skinValue.get(), this.updateTabListValue.get());
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(Keys.SKIN, () -> this.skinValue);
        registerKeyValue(Keys.SKIN, this::skin);

        registerFieldGetter(Keys.UPDATE_GAME_PROFILE, () -> this.updateTabListValue);
        registerKeyValue(Keys.UPDATE_GAME_PROFILE, this::updateGameProfile);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.SKIN.getQuery(), this.skinValue.get())
                .set(Keys.UPDATE_GAME_PROFILE.getQuery(), this.updateTabListValue.get());
    }

    @Override
    public ImmutableValue<ProfileProperty> skin() {
        return this.skinValue;
    }

    @Override
    public ImmutableValue<Boolean> updateGameProfile() {
        return this.updateTabListValue;
    }
}
