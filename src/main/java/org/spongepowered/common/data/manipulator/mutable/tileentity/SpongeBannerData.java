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
package org.spongepowered.common.data.manipulator.mutable.tileentity;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Lists;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableBannerData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.BannerData;
import org.spongepowered.api.data.meta.PatternLayer;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.data.value.mutable.PatternListValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.immutable.tileentity.ImmutableSpongeBannerData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.mutable.SpongePatternListValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpongeBannerData extends AbstractData<BannerData, ImmutableBannerData> implements BannerData {

    private DyeColor base;
    private List<PatternLayer> layers;

    public SpongeBannerData(DyeColor base, List<PatternLayer> layers) {
        super(BannerData.class);
        this.base = checkNotNull(base, "base");
        this.layers = Lists.newArrayList(checkNotNull(layers, "layers"));
        registerGettersAndSetters();
    }

    public SpongeBannerData() {
        this(DyeColors.WHITE, Collections.emptyList());
    }

    public DyeColor getBase() {
        return this.base;
    }

    public void setBase(DyeColor base) {
        this.base = checkNotNull(base, "Null DyeColor!");
    }

    public List<PatternLayer> getLayers() {
        return Lists.newArrayList(this.layers);
    }

    public void setLayers(List<PatternLayer> layers) {
        this.layers = new ArrayList<>(checkNotNull(layers, "Null pattern layers!"));
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(Keys.BANNER_BASE_COLOR, this::getBase);
        registerFieldSetter(Keys.BANNER_BASE_COLOR, this::setBase);
        registerKeyValue(Keys.BANNER_BASE_COLOR, this::baseColor);

        registerFieldGetter(Keys.BANNER_PATTERNS, this::getLayers);
        registerFieldSetter(Keys.BANNER_PATTERNS, this::setLayers);
        registerKeyValue(Keys.BANNER_PATTERNS, this::patternsList);
    }

    @Override
    public BannerData copy() {
        return new SpongeBannerData(this.base, this.layers);
    }

    @Override
    public ImmutableBannerData asImmutable() {
        return new ImmutableSpongeBannerData(this.base, this.layers);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
            .set(Keys.BANNER_BASE_COLOR.getQuery(), this.base.getId())
            .set(Keys.BANNER_PATTERNS, this.layers);
    }

    @Override
    public Value<DyeColor> baseColor() {
        return new SpongeValue<>(Keys.BANNER_BASE_COLOR, DyeColors.WHITE, this.base);
    }

    @Override
    public PatternListValue patternsList() {
        return new SpongePatternListValue(Keys.BANNER_PATTERNS, this.getLayers());
    }
}
