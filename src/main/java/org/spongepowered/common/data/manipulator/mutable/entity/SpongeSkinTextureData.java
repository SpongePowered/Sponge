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
package org.spongepowered.common.data.manipulator.mutable.entity;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableSkinTextureData;
import org.spongepowered.api.data.manipulator.mutable.entity.SkinTextureData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeSkinTextureData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.util.Constants;

public class SpongeSkinTextureData extends AbstractSingleData<String, SkinTextureData, ImmutableSkinTextureData> implements SkinTextureData {

    public SpongeSkinTextureData() {
        super(SkinTextureData.class, "", Keys.SKIN_TEXTURE);
    }

    public SpongeSkinTextureData(String texture) {
        super(SkinTextureData.class, texture, Keys.SKIN_TEXTURE);
    }

    @Override
    public SkinTextureData copy() {
        return new SpongeSkinTextureData(getValue());
    }

    @Override
    public ImmutableSkinTextureData asImmutable() {
        return new ImmutableSpongeSkinTextureData(getValue());
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
            .set(Constants.GameProfile.SKIN_TEXTURE, getValue());
    }

    @Override
    public Value<String> skinTexture() {
        return new SpongeValue<>(Keys.SKIN_TEXTURE, getValue());
    }

    @Override
    protected Value<?> getValueGetter() {
        return skinTexture();
    }

}
