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
package org.spongepowered.common.data.manipulators.tiles;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.data.DataQuery.of;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.spongepowered.api.data.AbstractDataManipulator;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.manipulators.tileentities.SignData;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.common.data.SpongeDataUtil;
import org.spongepowered.common.data.SpongeManipulatorRegistry;
import org.spongepowered.common.data.manipulators.SpongeAbstractData;

import java.util.List;

public class SpongeSignData extends SpongeAbstractData<SignData> implements SignData {

    private final Text[] lines = new Text[] { Texts.of(),  Texts.of(), Texts.of(), Texts.of() };

    public SpongeSignData() {
        super(SignData.class);
    }

    @Override
    public int compareTo(SignData o) {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        DataContainer container = new MemoryDataContainer();
        List<String> jsonLines = Lists.newArrayList();
        for (Text line : lines) {
            jsonLines.add(Texts.toJson(line));
        }
        container.set(of("Lines"), jsonLines);
        return container;
    }

    @Override
    public Text[] getLines() {
        return this.lines;
    }

    @Override
    public void setLines(Text... lines) {
        for (int i = 0; i < 4; i++) {
            if (lines.length >= i) {
                this.lines[i] = checkNotNull(lines[i]);
            }
        }
    }

    @Override
    public Text getLine(int index) throws IndexOutOfBoundsException {
        return this.lines[index];
    }

    @Override
    public void setLine(int index, Text text) throws IndexOutOfBoundsException {
        this.lines[index] = checkNotNull(text);
    }

}
