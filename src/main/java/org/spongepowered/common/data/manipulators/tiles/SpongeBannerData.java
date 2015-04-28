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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.manipulators.tileentities.BannerData;
import org.spongepowered.api.data.types.BannerPatternShape;
import org.spongepowered.api.data.types.DyeColor;
import org.spongepowered.api.data.types.DyeColors;
import org.spongepowered.common.data.manipulators.AbstractListData;
import org.spongepowered.common.data.manipulators.SpongeAbstractData;
import org.spongepowered.common.data.meta.SpongePatternLayer;

import java.util.List;

public class SpongeBannerData extends AbstractListData<BannerData.PatternLayer, BannerData> implements BannerData {

    private DyeColor baseColor = DyeColors.WHITE;

    public SpongeBannerData() {
        super(BannerData.class);
    }

    @Override
    public int compareTo(BannerData o) {
        return this.baseColor.getColor().getRGB() - o.getBaseColor().getColor().getRGB(); // TODO maybe also compare the pattern layers
    }

    @Override
    public DataContainer toContainer() {
        DataContainer container = new MemoryDataContainer();
        container.set(of("BaseColor"), this.baseColor.getId());
        container.set(of("Layers"), this.elementList);
        return container;
    }

    @Override
    public DyeColor getBaseColor() {
        return this.baseColor;
    }

    @Override
    public BannerData setBaseColor(DyeColor color) {
        this.baseColor = checkNotNull(color);
        return this;
    }

    @Override
    public List<PatternLayer> getPatternsList() {
        return ImmutableList.copyOf(this.elementList);
    }

    @Override
    public BannerData clearPatterns() {
        this.elementList.clear();
        return this;
    }

    @Override
    public BannerData addPatternLayer(PatternLayer pattern) {
        return this.add(pattern);
    }

    @Override
    public BannerData addPatternLayer(BannerPatternShape patternShape, DyeColor color) {
        return this.add(new SpongePatternLayer(checkNotNull(patternShape), checkNotNull(color)));
    }

}
