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

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableSizeData;
import org.spongepowered.api.data.manipulator.mutable.entity.SizeData;
import org.spongepowered.api.data.value.immutable.ImmutableBoundedValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeSizeData;
import org.spongepowered.common.data.value.SpongeValueFactory;

public class ImmutableSpongeSizeData extends AbstractImmutableData<ImmutableSizeData, SizeData> implements ImmutableSizeData {

    private final float base;
    private final float height;
    private final float scale;

    private final ImmutableBoundedValue<Float> baseValue;
    private final ImmutableBoundedValue<Float> heightValue;
    private final ImmutableBoundedValue<Float> scaleValue;

    public ImmutableSpongeSizeData(float base, float height, float scale) {
        super(ImmutableSizeData.class);

        checkArgument(base > 0, "The base size must be greater than 0.");
        checkArgument(height > 0, "The height must be greater than 0.");
        checkArgument(scale > 0, "The scale must be greater than 0.");

        this.base = base;
        this.height = height;
        this.scale = scale;

        this.baseValue = SpongeValueFactory.boundedBuilder(Keys.BASE_SIZE)
                .defaultValue(1f)
                .minimum(0f)
                .maximum(Float.MAX_VALUE)
                .actualValue(this.base)
                .build()
                .asImmutable();
        this.heightValue = SpongeValueFactory.boundedBuilder(Keys.HEIGHT)
                .defaultValue(1f)
                .minimum(0f)
                .maximum(Float.MAX_VALUE)
                .actualValue(this.height)
                .build()
                .asImmutable();
        this.scaleValue = SpongeValueFactory.boundedBuilder(Keys.SCALE)
                .defaultValue(1f)
                .minimum(0f)
                .maximum(Float.MAX_VALUE)
                .actualValue(this.scale)
                .build()
                .asImmutable();

        registerGetters();
    }

    @Override
    public ImmutableBoundedValue<Float> base() {
        return this.baseValue;
    }

    @Override
    public ImmutableBoundedValue<Float> height() {
        return this.heightValue;
    }

    @Override
    public ImmutableBoundedValue<Float> scale() {
        return this.scaleValue;
    }

    @Override
    protected void registerGetters() {
        registerFieldGetter(Keys.BASE_SIZE, () -> this.base);
        registerKeyValue(Keys.BASE_SIZE, this::base);

        registerFieldGetter(Keys.HEIGHT, () -> this.height);
        registerKeyValue(Keys.HEIGHT, this::height);

        registerFieldGetter(Keys.SCALE, () -> this.scale);
        registerKeyValue(Keys.SCALE, this::scale);
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

}
