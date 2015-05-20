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
package org.spongepowered.common.data.manipulator.tileentity;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.data.DataQuery.of;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.manipulator.tileentity.BannerData;
import org.spongepowered.api.data.type.BannerPatternShape;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.common.data.manipulator.AbstractListData;
import org.spongepowered.common.data.meta.SpongePatternLayer;

import java.util.List;

public class SpongeBannerData extends AbstractListData<BannerData.PatternLayer, BannerData> implements BannerData {

    public static final DataQuery BASE_COLOR = of("BaseColor");
    public static final DataQuery LAYERS = of("Layers");
    private DyeColor baseColor = DyeColors.WHITE;

    public SpongeBannerData() {
        super(BannerData.class);
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

    @Override
    public BannerData copy() {
        return new SpongeBannerData().set(this.elementList).setBaseColor(this.baseColor);
    }

    @Override
    public int compareTo(BannerData o) {
        return (this.baseColor.getColor().getRGB() - o.getBaseColor().getColor().getRGB())
                - (o.getPatternsList().hashCode() - this.getAll().hashCode());
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer().set(BASE_COLOR, this.baseColor.getId()).set(LAYERS, this.elementList);
    }
}
