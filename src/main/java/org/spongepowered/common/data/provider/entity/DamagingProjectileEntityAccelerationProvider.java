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
package org.spongepowered.common.data.provider.entity;

import net.minecraft.entity.projectile.DamagingProjectileEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;
import org.spongepowered.common.accessor.entity.projectile.DamagingProjectileEntityAccessor;
import org.spongepowered.math.vector.Vector3d;

import java.util.Optional;

public class DamagingProjectileEntityAccelerationProvider extends GenericMutableDataProvider<DamagingProjectileEntity, Vector3d> {

    public DamagingProjectileEntityAccelerationProvider() {
        super(Keys.ACCELERATION);
    }

    @Override
    protected Optional<Vector3d> getFrom(DamagingProjectileEntity dataHolder) {
        return Optional.of(new Vector3d(dataHolder.accelerationX, dataHolder.accelerationY, dataHolder.accelerationZ));
    }

    @Override
    protected boolean set(DamagingProjectileEntity dataHolder, Vector3d value) {
        ((DamagingProjectileEntityAccessor) dataHolder).accessor$setAccelerationX(value.getX());
        ((DamagingProjectileEntityAccessor) dataHolder).accessor$setAccelerationY(value.getY());
        ((DamagingProjectileEntityAccessor) dataHolder).accessor$setAccelerationZ(value.getZ());
        return true;
    }
}
