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
package org.spongepowered.common.data.manipulator.immutable;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableDisplayNameData;
import org.spongepowered.api.data.manipulator.mutable.DisplayNameData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.common.data.manipulator.mutable.SpongeDisplayNameData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

public class ImmutableSpongeDisplayNameData extends AbstractImmutableSingleData<Text, ImmutableDisplayNameData, DisplayNameData> implements ImmutableDisplayNameData {

    private final ImmutableSpongeValue<Text> nameValue;

    public ImmutableSpongeDisplayNameData() {
        this(Text.of());
    }

    public ImmutableSpongeDisplayNameData(Text value) {
        super(ImmutableDisplayNameData.class, value, Keys.DISPLAY_NAME);
        this.nameValue = new ImmutableSpongeValue<>(this.usedKey, Text.of(), value);
    }

    @Override
    protected ImmutableValue<?> getValueGetter() {
        return this.displayName();
    }

    @Override
    public DisplayNameData asMutable() {
        return new SpongeDisplayNameData(this.getValue());
    }

    @Override
    public ImmutableValue<Text> displayName() {
        return this.nameValue;
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.DISPLAY_NAME.getQuery(), TextSerializers.JSON.serialize(this.getValue()));
    }

}
