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
package org.spongepowered.common.data.component.tileentity;

import com.google.common.collect.Lists;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.component.tileentity.SignComponent;
import org.spongepowered.api.data.token.Tokens;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.common.data.component.AbstractListComponent;

import java.util.List;

@NonnullByDefault
public class SpongeSignComponent extends AbstractListComponent<Text, SignComponent> implements SignComponent {

    private final List<Text> lines;

    public SpongeSignComponent() {
        this(Lists.newArrayList(Texts.of(), Texts.of(), Texts.of(), Texts.of()));
    }

    public SpongeSignComponent(List<Text> lines) {
        super(SignComponent.class);
        this.lines = lines;
    }

    @Override
    public List<Text> getLines() {
        return this.lines;
    }

    @Override
    public Text getLine(int index) throws IndexOutOfBoundsException {
        return this.lines.get(index);
    }

    @Override
    public SignComponent setLine(int index, Text text) throws IndexOutOfBoundsException {
        this.lines.set(index, text);
        return this;
    }

    @Override
    public SignComponent reset() {
        for (int i = 0; i < this.lines.size(); i++) {
            this.lines.set(i, Texts.of());
        }
        return this;
    }

    @Override
    public SignComponent copy() {
        return new SpongeSignComponent(Lists.newArrayList(this.lines));
    }

    @Override
    public int compareTo(SignComponent o) {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        List<String> jsonLines = Lists.newArrayListWithExpectedSize(4);
        for (Text line : this.lines) {
            jsonLines.add(Texts.toJson(line));
        }
        return new MemoryDataContainer().set(Tokens.SIGN_TEXT.getQuery(), jsonLines);
    }
}
