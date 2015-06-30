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
import org.spongepowered.api.text.Texts;
import org.spongepowered.common.data.manipulator.immutable.ImmutableSpongeDisplayNameData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.util.GetterFunction;
import org.spongepowered.common.util.SetterFunction;

public class SpongeDisplayNameData extends AbstractData<DisplayNameData, ImmutableDisplayNameData> implements DisplayNameData {

    private Text displayName;
    private boolean displays = false;

    public SpongeDisplayNameData() {
        this(Texts.of(), false);
    }

    public SpongeDisplayNameData(Text displayName) {
        this(displayName, true);
    }

    public SpongeDisplayNameData(Text displayName, boolean renders) {
        super(DisplayNameData.class);
        this.displayName = checkNotNull(displayName);
        this.displays = renders;
        registerStuff();
    }

    @Override
    public Value<Text> displayName() {
        return new SpongeValue<Text>(Keys.DISPLAY_NAME, Texts.of(), this.displayName);
    }

    @Override
    public Value<Boolean> customNameVisible() {
        return new SpongeValue<Boolean>(Keys.SHOWS_DISPLAY_NAME, false, this.displays);
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
        return displayName;
    }

    public SpongeDisplayNameData setDisplayName(Text displayName) {
        this.displayName = checkNotNull(displayName);
        return this;
    }

    public boolean isDisplays() {
        return displays;
    }

    public SpongeDisplayNameData setDisplays(boolean displays) {
        this.displays = displays;
        return this;
    }

    private void registerStuff() {
        registerFieldGetter(Keys.DISPLAY_NAME, new GetterFunction<Object>() {
            @Override
            public Object get() {
                return getDisplayName();
            }
        });
        registerFieldSetter(Keys.DISPLAY_NAME, new SetterFunction<Object>() {
            @Override
            public void set(Object value) {
                setDisplayName((Text) value);
            }
        });
        registerKeyValue(Keys.DISPLAY_NAME, new GetterFunction<Value<?>>() {
            @Override
            public Value<?> get() {
                return displayName();
            }
        });

        registerFieldGetter(Keys.SHOWS_DISPLAY_NAME, new GetterFunction<Object>() {
            @Override
            public Object get() {
                return isDisplays();
            }
        });
        registerFieldSetter(Keys.SHOWS_DISPLAY_NAME, new SetterFunction<Object>() {
            @Override
            public void set(Object value) {
                setDisplays((Boolean) value);
            }
        });
        registerKeyValue(Keys.SHOWS_DISPLAY_NAME, new GetterFunction<Value<?>>() {
            @Override
            public Value<?> get() {
                return customNameVisible();
            }
        });


    }
}
