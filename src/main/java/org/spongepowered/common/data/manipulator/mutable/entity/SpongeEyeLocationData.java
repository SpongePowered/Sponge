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

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableEyeLocationData;
import org.spongepowered.api.data.manipulator.mutable.entity.EyeLocationData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.immutable.entity.ImmutableSpongeEyeLocationData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractData;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.util.GetterFunction;
import org.spongepowered.common.util.SetterFunction;

@SuppressWarnings("ConstantConditions")
public class SpongeEyeLocationData extends AbstractData<EyeLocationData, ImmutableEyeLocationData> implements EyeLocationData {

    private final Vector3d entityLocation;
    private double eyeHeight;
    private Vector3d eyeLocation;

    public SpongeEyeLocationData() {
        this(Vector3d.ZERO, 0, Vector3d.ZERO);
    }

    public SpongeEyeLocationData(Vector3d entityLocation, double eyeHeight) {
        this(entityLocation, eyeHeight, entityLocation.add(0, eyeHeight, 0));
    }

    public SpongeEyeLocationData(Vector3d entityLocation, double eyeHeight, Vector3d eyeLocation) {
        super(EyeLocationData.class);
        this.entityLocation = entityLocation;
        this.eyeHeight = eyeHeight;
        this.eyeLocation = eyeLocation;
        registerGettersAndSetters();
    }

    @Override
    public Value<Double> eyeHeight() {
        return new SpongeValue<>(Keys.EYE_HEIGHT, 0d, this.eyeHeight);
    }

    @Override
    public Value<Vector3d> eyeLocation() {
        return new SpongeValue<>(Keys.EYE_LOCATION, this.entityLocation, this.eyeLocation);
    }

    @Override
    public EyeLocationData copy() {
        return new SpongeEyeLocationData(this.entityLocation, this.eyeHeight, this.eyeLocation);
    }

    @Override
    public ImmutableEyeLocationData asImmutable() {
        return new ImmutableSpongeEyeLocationData(this.entityLocation, this.eyeHeight, this.eyeLocation);
    }

    @Override
    public int compareTo(EyeLocationData o) {
        return (int) Math.signum(this.eyeHeight - o.eyeHeight().get());
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer().set(Keys.EYE_HEIGHT.getQuery(), this.eyeHeight).set(Keys.EYE_LOCATION.getQuery(), this.eyeLocation);
    }

    public double getEyeHeight() {
        return eyeHeight;
    }

    public EyeLocationData setEyeHeight(double eyeHeight) {
        if (this.eyeHeight != eyeHeight) {
            this.eyeHeight = eyeHeight;
            this.eyeLocation = this.entityLocation.add(0, eyeHeight, 0);
        }
        return this;
    }

    public Vector3d getEyeLocation() {
        return eyeLocation;
    }

    public EyeLocationData setEyeLocation(Vector3d eyeLocation) {
        this.eyeLocation = eyeLocation;
        this.eyeHeight = eyeLocation.getY() - entityLocation.getY();
        return this;
    }

    @Override
    protected void registerGettersAndSetters() {
        registerFieldGetter(Keys.EYE_HEIGHT, new GetterFunction<Object>() {
            @Override
            public Object get() {
                return getEyeHeight();
            }
        });
        registerFieldSetter(Keys.EYE_HEIGHT, new SetterFunction<Object>() {
            @Override
            public void set(Object value) {
                setEyeHeight(((Number) value).doubleValue());
            }
        });
        registerKeyValue(Keys.EYE_HEIGHT, new GetterFunction<Value<?>>() {
            @Override
            public Value<?> get() {
                return eyeHeight();
            }
        });

        registerFieldGetter(Keys.EYE_LOCATION, new GetterFunction<Object>() {
            @Override
            public Object get() {
                return getEyeLocation();
            }
        });
        registerFieldSetter(Keys.EYE_LOCATION, new SetterFunction<Object>() {
            @Override
            public void set(Object value) {
                setEyeLocation((Vector3d) value);
            }
        });
        registerKeyValue(Keys.EYE_LOCATION, new GetterFunction<Value<?>>() {
            @Override
            public Value<?> get() {
                return eyeLocation();
            }
        });
    }

}
