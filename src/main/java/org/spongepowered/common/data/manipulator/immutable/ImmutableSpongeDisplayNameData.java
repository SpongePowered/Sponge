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

import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableDisplayNameData;
import org.spongepowered.api.data.manipulator.mutable.DisplayNameData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.SpongeDisplayNameData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

public class ImmutableSpongeDisplayNameData extends AbstractImmutableData<ImmutableDisplayNameData, DisplayNameData> implements ImmutableDisplayNameData {

    private final Text displayName;
    private final boolean displays;

    private final ImmutableValue<Text> nameValue;
    private final ImmutableValue<Boolean> displaysValue;

    public ImmutableSpongeDisplayNameData(Text displayName, boolean displays) {
        super(ImmutableDisplayNameData.class);
        this.displayName = displayName;
        this.displays = displays;

        this.nameValue = new ImmutableSpongeValue<>(Keys.DISPLAY_NAME, this.displayName);
        this.displaysValue = ImmutableSpongeValue.cachedOf(Keys.SHOWS_DISPLAY_NAME, false, this.displays);

        registerGetters();
    }

    @Override
    public ImmutableValue<Text> displayName() {
        return this.nameValue;
    }

    @Override
    public ImmutableValue<Boolean> customNameVisible() {
        return this.displaysValue;
    }

    @Override
    public DisplayNameData asMutable() {
        return new SpongeDisplayNameData(this.displayName, this.displays);
    }

    @Override
    public int compareTo(ImmutableDisplayNameData o) {
        return ComparisonChain.start()
                .compare(Texts.json().to(o.get(Keys.DISPLAY_NAME).get()), Texts.json().to(this.displayName))
                .compare(o.get(Keys.SHOWS_DISPLAY_NAME).get(), this.displays)
                .result();
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
            .set(Keys.DISPLAY_NAME.getQuery(), Texts.json().to(this.displayName))
            .set(Keys.SHOWS_DISPLAY_NAME, this.displays);
    }

    public Text getDisplayName() {
        return this.displayName;
    }

    public boolean isDisplays() {
        return this.displays;
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(Keys.DISPLAY_NAME, ImmutableSpongeDisplayNameData.this::getDisplayName);
        registerKeyValue(Keys.DISPLAY_NAME, ImmutableSpongeDisplayNameData.this::displayName);

        registerFieldGetter(Keys.SHOWS_DISPLAY_NAME, ImmutableSpongeDisplayNameData.this::isDisplays);
        registerKeyValue(Keys.SHOWS_DISPLAY_NAME, ImmutableSpongeDisplayNameData.this::customNameVisible);
    }
}
