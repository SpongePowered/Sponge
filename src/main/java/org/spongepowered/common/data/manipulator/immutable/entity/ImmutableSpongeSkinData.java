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
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeSkinData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

import java.util.UUID;

public class ImmutableSpongeSkinData extends AbstractImmutableSingleData<UUID, ImmutableSkinData, SkinData> implements ImmutableSkinData {

    private final ImmutableSpongeValue<UUID> skinValue;

    public ImmutableSpongeSkinData() {
        this(new UUID(0, 0));
    }

    public ImmutableSpongeSkinData(UUID value) {
        super(ImmutableSkinData.class, value, Keys.SKIN_UNIQUE_ID);
        this.skinValue = new ImmutableSpongeValue<>(Keys.SKIN_UNIQUE_ID, value);
    }

    @Override
    public SkinData asMutable() {
        return new SpongeSkinData(this.value);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
            .set(Keys.SKIN_UNIQUE_ID.getQuery(), this.value.toString());
    }

    @Override
    public ImmutableValue<UUID> skinUniqueId() {
        return this.skinValue;
    }

    @Override
    protected ImmutableValue<?> getValueGetter() {
        return skinUniqueId();
    }

}
