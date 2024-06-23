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
package org.spongepowered.common.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.world.PositionSource;
import org.spongepowered.math.vector.Vector3i;

import java.util.Objects;

public final class SpongePositionSourceFactory implements PositionSource.Factory {

    @Override
    public PositionSource of(final Vector3i position) {
        Objects.requireNonNull(position, "position");
        return this.of(position.x(), position.y(), position.z());
    }

    @Override
    public PositionSource of(final int x, final int y, final int z) {
        return (PositionSource) new BlockPositionSource(new BlockPos(x, y, z));
    }

    @Override
    public PositionSource of(final Entity entity) {
        return this.of(entity, entity.eyeHeight().get());
    }

    @Override
    public PositionSource of(final Entity entity, final double yOffset) {
        Objects.requireNonNull(entity, "entity");
        return (PositionSource) new EntityPositionSource((net.minecraft.world.entity.Entity) entity, (float) yOffset);
    }
}
