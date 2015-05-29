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
package org.spongepowered.common.data.component.base;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.component.base.DisplayNameComponent;
import org.spongepowered.api.data.token.Tokens;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.common.data.component.AbstractSingleValueComponent;

import java.util.Locale;

public class SpongeDisplayNameComponent extends AbstractSingleValueComponent<Text, DisplayNameComponent> implements DisplayNameComponent {

    private boolean visible = true;

    public SpongeDisplayNameComponent() {
        super(DisplayNameComponent.class, Texts.of());
    }

    @Override
    public boolean isCustomNameVisible() {
        return this.visible;
    }

    @Override
    public DisplayNameComponent setCustomNameVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    @Override
    public DisplayNameComponent copy() {
        return new SpongeDisplayNameComponent().setValue(this.getValue()).setCustomNameVisible(this.visible);
    }

    @Override
    public DisplayNameComponent reset() {
        return setValue(Texts.of()).setCustomNameVisible(true);
    }

    @Override
    public int compareTo(DisplayNameComponent o) {
        return o.getValue().toString().compareTo(getValue().toString());
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(Tokens.DISPLAY_NAME.getQuery(), Texts.toJson(this.getValue(), Locale.ENGLISH))
                .set(Tokens.SHOW_DISPLAY_NAME.getQuery(), this.visible);
    }
}
