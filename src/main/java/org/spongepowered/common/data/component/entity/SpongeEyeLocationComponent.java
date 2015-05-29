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
package org.spongepowered.common.data.component.entity;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.data.DataQuery.of;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.component.entity.EyeLocationComponent;
import org.spongepowered.common.data.component.SpongeAbstractComponent;

public class SpongeEyeLocationComponent extends SpongeAbstractComponent<EyeLocationComponent> implements EyeLocationComponent {

    public static final DataQuery EYE_LOCATION = of("EyeLocation");
    public static final DataQuery POS_X = of("PosX");
    public static final DataQuery POS_Y = of("PosY");
    public static final DataQuery POS_Z = of("PosZ");
    public static final DataQuery EYE_HEIGHT = of("EyeHeight");
    private final Vector3d eyeLocation;
    private final double eyeHeight;

    public SpongeEyeLocationComponent(Vector3d location, double eyeHeight) {
        super(EyeLocationComponent.class);
        this.eyeLocation = checkNotNull(location);
        this.eyeHeight = eyeHeight;
    }

    @Override
    public double getEyeHeight() {
        return this.eyeHeight;
    }

    @Override
    public Vector3d getEyeLocation() {
        return this.eyeLocation;
    }

    @Override
    public EyeLocationComponent copy() {
        return new SpongeEyeLocationComponent(this.eyeLocation, this.eyeHeight);
    }

    @Override
    public EyeLocationComponent reset() {
        return this;
    }

    @Override
    public int compareTo(EyeLocationComponent o) {
        return ((int) Math.floor(o.getEyeHeight() - this.getEyeHeight())) - (int) Math.floor(o.getEyeLocation().distance(this.getEyeLocation()));
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .createView(EYE_LOCATION)
                .set(POS_X, this.eyeLocation.getX())
                .set(POS_Y, this.eyeLocation.getY())
                .set(POS_Z, this.eyeLocation.getZ())
                .getContainer()
                .set(EYE_HEIGHT, this.eyeHeight);
    }
}
