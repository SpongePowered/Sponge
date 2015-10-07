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
package org.spongepowered.common.data.manipulator.immutable.tileentity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableSignData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.data.value.immutable.ImmutableListValue;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeSignData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeListValue;

import java.util.List;
import java.util.stream.Collectors;

public class ImmutableSpongeSignData extends AbstractImmutableData<ImmutableSignData, SignData> implements ImmutableSignData {

    private final ImmutableList<Text> lines;
    private final ImmutableListValue<Text> linesValues;

    public ImmutableSpongeSignData(List<Text> lines) {
        super(ImmutableSignData.class);
        this.lines = ImmutableList.copyOf(lines);
        this.linesValues = new ImmutableSpongeListValue<>(Keys.SIGN_LINES, this.lines);
        registerGetters();
    }

    @Override
    public ImmutableListValue<Text> lines() {
        return this.linesValues;
    }

    @Override
    public SignData asMutable() {
        return new SpongeSignData(Lists.newArrayList(this.lines));
    }

    @Override
    public DataContainer toContainer() {
        List<String> jsonLines = Lists.newArrayListWithExpectedSize(4);
        jsonLines.addAll(this.lines.stream().map(line -> Texts.json().to(line)).collect(Collectors.toList()));
        return new MemoryDataContainer().set(Keys.SIGN_LINES.getQuery(), jsonLines);
    }

    @Override
    public int compareTo(ImmutableSignData o) {
        return 0;
    }

    @Override
    protected void registerGetters() {

    }
}
