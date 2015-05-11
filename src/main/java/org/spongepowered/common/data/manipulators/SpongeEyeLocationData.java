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
package org.spongepowered.common.data.manipulators;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.data.DataQuery.of;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.manipulators.entities.EyeLocationData;

public class SpongeEyeLocationData extends SpongeAbstractData<EyeLocationData> implements EyeLocationData {

    private final Vector3d eyeLocation;
    private final double eyeHeight;

    public SpongeEyeLocationData(Vector3d location, double eyeHeight) {
        super(EyeLocationData.class);
        this.eyeLocation = checkNotNull(location);
        this.eyeHeight = eyeHeight;
    }

    @Override
    public int compareTo(EyeLocationData o) {
        return ((int) Math.floor(o.getEyeHeight() - this.getEyeHeight())) - (int) Math.floor(o.getEyeLocation().distance(this.getEyeLocation()));
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
    public EyeLocationData copy() {
        return new SpongeEyeLocationData(this.eyeLocation, this.eyeHeight);
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .createView(of("EyeLocation"))
                .set(of("PosX"), this.eyeLocation.getX())
                .set(of("PosY"), this.eyeLocation.getY())
                .set(of("PosZ"), this.eyeLocation.getZ())
                .getContainer()
                .set(of("EyeHeight"), this.eyeHeight);
    }
}
