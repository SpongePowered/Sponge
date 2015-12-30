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
package org.spongepowered.common.data.manipulator.mutable;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableDisplayNameData;
import org.spongepowered.api.data.manipulator.mutable.DisplayNameData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeDisplayNameData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.util.DataConstants;
import org.spongepowered.common.data.util.ImplementationRequiredForTest;
import org.spongepowered.common.data.value.mutable.SpongeValue;

@ImplementationRequiredForTest
public class SpongeDisplayNameData extends AbstractData<DisplayNameData, ImmutableDisplayNameData> implements DisplayNameData {

    private Text displayName;
    private boolean displays = false;

    public SpongeDisplayNameData() {
        this(Text.of(), false);
    }

    public SpongeDisplayNameData(Text displayName) {
        this(displayName, true);
    }

    public SpongeDisplayNameData(Text displayName, boolean renders) {
        super(DisplayNameData.class);
        this.displayName = checkNotNull(displayName);
        this.displays = renders;
        registerGettersAndSetters();
    }

    @Override
    public Value<Text> displayName() {
        return new SpongeValue<>(Keys.DISPLAY_NAME, Text.of(), this.displayName);
    }

    @Override
    public Value<Boolean> customNameVisible() {
        return new SpongeValue<>(Keys.SHOWS_DISPLAY_NAME, false, this.displays);
    }

    @Override
    public DisplayNameData copy() {
        return new SpongeDisplayNameData(this.displayName).setDisplays(this.displays);
    }

    @Override
    public ImmutableDisplayNameData asImmutable() {
        return new ImmutableSpongeDisplayNameData(this.displayName, this.displays);
    }

    @Override
    public int compareTo(DisplayNameData o) {
        return ComparisonChain.start()
                .compare(TextSerializers.JSON.serialize(o.get(Keys.DISPLAY_NAME).get()),
                        TextSerializers.JSON.serialize(this.displayName))
                .compare(o.get(Keys.SHOWS_DISPLAY_NAME).get(), this.displays)
                .result();
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
            .set(Keys.DISPLAY_NAME.getQuery(), TextSerializers.JSON.serialize(this.displayName))
            .set(Keys.SHOWS_DISPLAY_NAME, this.displays);
    }

    public Text getDisplayName() {
        return this.displayName;
    }

    public SpongeDisplayNameData setDisplayName(Text displayName) {
        this.displayName = checkNotNull(displayName);
        return this;
    }

    public boolean isDisplays() {
        return this.displays;
    }

    public SpongeDisplayNameData setDisplays(boolean displays) {
        this.displays = displays;
        return this;
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(Keys.DISPLAY_NAME, SpongeDisplayNameData.this::getDisplayName);
        registerFieldSetter(Keys.DISPLAY_NAME, this::setDisplayName);
        registerKeyValue(Keys.DISPLAY_NAME, SpongeDisplayNameData.this::displayName);

        registerFieldGetter(Keys.SHOWS_DISPLAY_NAME, SpongeDisplayNameData.this::isDisplays);
        registerFieldSetter(Keys.SHOWS_DISPLAY_NAME, this::setDisplays);
        registerKeyValue(Keys.SHOWS_DISPLAY_NAME, SpongeDisplayNameData.this::customNameVisible);


    }
}
