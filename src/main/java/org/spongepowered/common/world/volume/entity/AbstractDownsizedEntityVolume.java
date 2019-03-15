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
package org.spongepowered.common.world.volume.entity;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.fluid.FluidState;
import org.spongepowered.api.world.volume.entity.ReadableEntityVolume;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.volume.AbstractDownsizedVolume;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class AbstractDownsizedEntityVolume<V extends ReadableEntityVolume> extends AbstractDownsizedVolume<V> implements ReadableEntityVolume {

    public AbstractDownsizedEntityVolume(V volume, Vector3i min, Vector3i max) {
        super(volume, min, max);
    }

    @Override
    public Optional<Entity> getEntity(UUID uuid) {
        return this.volume.getEntity(uuid).filter(entity -> VecHelper.inBounds(entity.getPosition(), this.min, this.max));
    }

    @Override
    public Collection<Entity> getEntities() {
        return this.volume.getEntities().stream().filter(entity -> VecHelper.inBounds(entity.getPosition(), this.min, this.max)).collect(Collectors.toList());
    }

    @Override
    public Collection<Entity> getEntities(Predicate<Entity> filter) {
        return this.volume.getEntities().stream()
            .filter(entity -> VecHelper.inBounds(entity.getPosition(), this.min, this.max))
            .filter(filter)
            .collect(Collectors.toList());
    }


    @Override
    public AbstractDownsizedEntityVolume<V> getView(Vector3i newMin, Vector3i newMax) {
        return null;
    }

    @Override
    public BlockState getBlock(int x, int y, int z) {
        return null;
    }

    @Override
    public FluidState getFluid(int x, int y, int z) {
        return null;
    }

    @Override
    public int getHighestYAt(int x, int z) {
        return 0;
    }
}
