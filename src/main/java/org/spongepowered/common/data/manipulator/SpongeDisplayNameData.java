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
package org.spongepowered.common.data.manipulator;

import static org.spongepowered.api.data.DataQuery.of;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.manipulator.DisplayNameData;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;

import java.util.Locale;

public class SpongeDisplayNameData extends AbstractSingleValueData<Text, DisplayNameData> implements DisplayNameData {

    public static final DataQuery DISPLAY_NAME = of("DisplayName");
    public static final DataQuery VISIBLE = of("Visible");
    private boolean visible = true;

    public SpongeDisplayNameData() {
        super(DisplayNameData.class, Texts.of());
    }

    @Override
    public Text getDisplayName() {
        return this.getValue();
    }

    @Override
    public DisplayNameData setDisplayName(Text displayName) {
        return setValue(displayName);
    }

    @Override
    public boolean isCustomNameVisible() {
        return this.visible;
    }

    @Override
    public DisplayNameData setCustomNameVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    @Override
    public DisplayNameData copy() {
        return new SpongeDisplayNameData().setValue(this.getValue()).setCustomNameVisible(this.visible);
    }

    @Override
    public int compareTo(DisplayNameData o) {
        return 0; // TODO
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(DISPLAY_NAME, Texts.toJson(this.getValue(), Locale.ENGLISH))
                .set(VISIBLE, this.visible);
    }
}
