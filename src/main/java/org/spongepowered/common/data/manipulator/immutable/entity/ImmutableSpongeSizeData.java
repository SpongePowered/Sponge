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
package org.spongepowered.common.data.manipulator.immutable.entity;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableSizeData;
import org.spongepowered.api.data.manipulator.mutable.entity.SizeData;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeSizeData;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

public class ImmutableSpongeSizeData extends AbstractImmutableData<ImmutableSizeData, SizeData> implements ImmutableSizeData {

    private final float base;
    private final float height;
    private final float scale;

    private final ImmutableBoundedValue<Float> scaleValue;

    public ImmutableSpongeSizeData(float base, float height, float scale) {
        super(ImmutableSizeData.class);
        checkArgument(scale > 0);
        this.scale = scale;
        this.base = base;
        this.height = height;

        this.scaleValue = SpongeValueFactory.boundedBuilder(Keys.SCALE)
                .actualValue(this.scale)
                .defaultValue(1f)
                .minimum(0f)
                .maximum(Float.MAX_VALUE)
                .build()
                .asImmutable();

        registerGetters();
    }

    @Override
    public ImmutableValue<Float> base() {
        return new ImmutableSpongeValue<>(Keys.BASE_SIZE, this.base);
    }

    @Override
    public ImmutableValue<Float> height() {
        return new ImmutableSpongeValue<>(Keys.HEIGHT, this.height);
    }

    @Override
    public ImmutableBoundedValue<Float> scale() {
        return this.scaleValue;
    }

    @Override
    public SizeData asMutable() {
        return new SpongeSizeData(this.base, this.height, this.scale);
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.BASE_SIZE.getQuery(), this.base)
                .set(Keys.HEIGHT.getQuery(), this.height)
                .set(Keys.SCALE.getQuery(), this.scale);
    }

    @Override
    public int compareTo(ImmutableSizeData o) {
        return ComparisonChain.start()
                .compare(o.base().get().intValue(), this.base)
                .compare(o.height().get().intValue(), this.height)
                .compare(o.scale().get().intValue(), this.scale)
                .result();
    }

    public float getBase() {
        return base;
    }

    public float getHeight() {
        return height;
    }

    public float getScale() {
        return scale;
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(Keys.BASE_SIZE, ImmutableSpongeSizeData.this::getBase);
        registerKeyValue(Keys.BASE_SIZE, ImmutableSpongeSizeData.this::base);

        registerFieldGetter(Keys.HEIGHT, ImmutableSpongeSizeData.this::getHeight);
        registerKeyValue(Keys.HEIGHT, ImmutableSpongeSizeData.this::height);

        registerFieldGetter(Keys.SCALE, ImmutableSpongeSizeData.this::getScale);
        registerKeyValue(Keys.SCALE, ImmutableSpongeSizeData.this::scale);
    }

}
