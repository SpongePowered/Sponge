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

import static org.spongepowered.api.data.DataQuery.of;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.component.entity.VelocityComponent;
import org.spongepowered.common.data.component.AbstractSingleValueComponent;

public class SpongeVelocityComponent extends AbstractSingleValueComponent<Vector3d, VelocityComponent> implements VelocityComponent {

    public static final DataQuery X_VELOCITY = of("Velocity", "X");
    public static final DataQuery Y_VELOCITY = of("Velocity", "Y");
    public static final DataQuery Z_VELOCITY = of("Velocity", "Z");

    public SpongeVelocityComponent() {
        super(VelocityComponent.class, new Vector3d());
    }

    @Override
    public VelocityComponent copy() {
        return new SpongeVelocityComponent().setValue(this.getValue());
    }

    @Override
    public VelocityComponent reset() {
        return setValue(new Vector3d());
    }

    @Override
    public int compareTo(VelocityComponent o) {
        return o.getValue().compareTo(this.getValue());
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(X_VELOCITY, this.getValue().getX())
                .set(Y_VELOCITY, this.getValue().getY())
                .set(Z_VELOCITY, this.getValue().getZ());
    }
}
