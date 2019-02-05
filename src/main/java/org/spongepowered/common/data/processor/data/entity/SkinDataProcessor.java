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

import com.google.common.collect.ImmutableMap;
import com.mojang.authlib.properties.Property;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableSkinData;
import org.spongepowered.api.data.manipulator.mutable.SkinData;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeSkinData;
import org.spongepowered.common.data.processor.common.AbstractMultiDataSingleTargetProcessor;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.interfaces.entity.IMixinSkinnable;

import java.util.Map;
import java.util.Optional;

public class SkinDataProcessor extends
        AbstractMultiDataSingleTargetProcessor<IMixinSkinnable, SkinData, ImmutableSkinData> {

    public SkinDataProcessor() {
        super(IMixinSkinnable.class);
    }

    public static final ProfileProperty EMPTY_SKIN = (ProfileProperty) new Property(ProfileProperty.TEXTURES, "");

    @Override
    protected SkinData createManipulator() {
        return new SpongeSkinData();
    }


    @Override
    protected boolean doesDataExist(IMixinSkinnable dataHolder) {
        return true;
    }

    @Override
    protected boolean set(IMixinSkinnable dataHolder, Map<Key<?>, Object> keyValues) {
        ProfileProperty skin = (ProfileProperty) keyValues.get(Keys.SKIN);
        boolean updateTabList = (Boolean) keyValues.get(Keys.UPDATE_GAME_PROFILE);

        dataHolder.setSkin(skin, updateTabList);
        return true;
    }

    @Override
    protected Map<Key<?>, ?> getValues(IMixinSkinnable dataHolder) {
        return ImmutableMap.of(Keys.SKIN, dataHolder.getSkin(),
                Keys.UPDATE_GAME_PROFILE, dataHolder.shouldUpdateGameProfile());
    }

    @Override
    public Optional<SkinData> fill(DataContainer container, SkinData skinData) {
        ProfileProperty skin = container.getSerializable(DataQueries.SKIN, ProfileProperty.class).get();
        boolean updateTabList = container.getBoolean(DataQueries.UPDATE_TAB_LIST).get();

        return Optional.of(skinData.set(Keys.SKIN,  skin)
                .set(Keys.UPDATE_GAME_PROFILE, updateTabList));
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder) {
        return DataTransactionResult.failNoData();
    }
}
