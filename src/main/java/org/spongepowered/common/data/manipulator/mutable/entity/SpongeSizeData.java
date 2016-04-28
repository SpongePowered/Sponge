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
package org.spongepowered.common.data.manipulator.mutable.entity;

import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableSizeData;
import org.spongepowered.api.data.manipulator.mutable.entity.SizeData;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeSizeData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.data.value.mutable.SpongeValue;

public class SpongeSizeData extends AbstractData<SizeData, ImmutableSizeData> implements SizeData {

    private float base;
    private float height;
    private float scale;

    public SpongeSizeData(float base, float height, float scale) {
        super(SizeData.class);
        this.base = base;
        this.height = height;
        this.scale = scale;
        registerGettersAndSetters();
    }

    public SpongeSizeData() {
        this(1f, 1f, 1f);
    }

    @Override
    public SizeData copy() {
        return new SpongeSizeData(this.base, this.height, this.scale);
    }

    @Override
    public ImmutableSizeData asImmutable() {
        return new ImmutableSpongeSizeData(this.base, this.height, this.scale);
    }

    @Override
    public int compareTo(SizeData o) {
        return ComparisonChain.start()
                .compare(o.base().get().intValue(), this.base)
                .compare(o.height().get().floatValue(), this.height)
                .compare(o.scale().get().floatValue(), this.scale)
                .result();
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
                .set(Keys.BASE_SIZE.getQuery(), this.base)
                .set(Keys.HEIGHT.getQuery(), this.height)
                .set(Keys.SCALE.getQuery(), this.scale);
    }
    
    @Override
    public Value<Float> base() {
        return new SpongeValue<>(Keys.BASE_SIZE, base);
    }

    @Override
    public Value<Float> height() {
        return new SpongeValue<>(Keys.HEIGHT, height);
    }

    @Override
    public MutableBoundedValue<Float> scale() {
        return SpongeValueFactory.boundedBuilder(Keys.SCALE)
                .defaultValue(1f)
                .minimum(0f)
                .maximum(Float.MAX_VALUE)
                .actualValue(this.scale)
                .build();
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(Keys.BASE_SIZE, SpongeSizeData.this::getBase);
        registerFieldSetter(Keys.BASE_SIZE, SpongeSizeData.this::setBase);
        registerKeyValue(Keys.BASE_SIZE, SpongeSizeData.this::base);

        registerFieldGetter(Keys.HEIGHT, SpongeSizeData.this::getHeight);
        registerFieldSetter(Keys.HEIGHT, SpongeSizeData.this::setHeight);
        registerKeyValue(Keys.HEIGHT, SpongeSizeData.this::height);

        registerFieldGetter(Keys.SCALE, SpongeSizeData.this::getScale);
        registerFieldSetter(Keys.SCALE, SpongeSizeData.this::setScale);
        registerKeyValue(Keys.SCALE, SpongeSizeData.this::scale);
    }
    
    public float getBase() {
        return base;
    }
    
    public void setBase(float base) {
        this.base = base;
    }
    
    public float getHeight() {
        return height;
    }
    
    public void setHeight(float height) {
        this.height = height;
    }
    
    public float getScale() {
        return scale;
    }
    
    public void setScale(float scale) {
        this.scale = scale;
    }
}
