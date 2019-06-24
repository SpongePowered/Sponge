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
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableSkinData;
import org.spongepowered.api.data.manipulator.mutable.entity.SkinData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeSkinData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.util.Constants;

import java.util.UUID;

public class SpongeSkinData extends AbstractSingleData<UUID, SkinData, ImmutableSkinData> implements SkinData {

    public SpongeSkinData() {
        this(new UUID(0, 0));
    }

    public SpongeSkinData(UUID skinUuid) {
        super(SkinData.class, skinUuid, Keys.SKIN_UNIQUE_ID);
    }

    @Override
    public SkinData copy() {
        return new SpongeSkinData(getValue());
    }

    @Override
    public ImmutableSkinData asImmutable() {
        return new ImmutableSpongeSkinData(getValue());
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
            .set(Constants.GameProfile.SKIN_UUID, getValue());
    }

    @Override
    public Value<UUID> skinUniqueId() {
        return new SpongeValue<>(Keys.SKIN_UNIQUE_ID, getValue());
    }

    @Override
    protected Value<?> getValueGetter() {
        return skinUniqueId();
    }

}
