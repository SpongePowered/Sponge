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

import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Optional;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.world.Explosion;
import org.spongepowered.api.world.ExplosionBuilder;
import org.spongepowered.api.world.World;

public class SpongeExplosionBuilder implements ExplosionBuilder {

    private World world;
    private Entity sourceEntity;
    private float radius;
    private Vector3d origin;
    private boolean canCauseFire;
    private boolean shouldBreakBlocks;

    public SpongeExplosionBuilder() {
        reset();
    }

    @Override
    public ExplosionBuilder world(World world) {
        this.world = checkNotNull(world, "world");
        return this;
    }

    @Override
    public ExplosionBuilder sourceEntity(Entity source) {
        this.sourceEntity = source;
        return this;
    }

    @Override
    public ExplosionBuilder radius(float radius) {
        this.radius = radius;
        return this;
    }

    @Override
    public ExplosionBuilder origin(Vector3d origin) {
        this.origin = checkNotNull(origin, "origin");
        return this;
    }

    @Override
    public ExplosionBuilder canCauseFire(boolean fire) {
        this.canCauseFire = fire;
        return this;
    }

    @Override
    public ExplosionBuilder shouldBreakBlocks(boolean destroy) {
        this.shouldBreakBlocks = destroy;
        return this;
    }

    @Override
    public Optional<Explosion> build() throws IllegalStateException {
        if (this.world == null || this.origin == null) {
            return Optional.absent();
        }

        return Optional.of((Explosion) new net.minecraft.world.Explosion((net.minecraft.world.World) this.world,
                (net.minecraft.entity.Entity) this.sourceEntity, this.origin.getX(), this.origin.getY(), this.origin.getZ(), this.radius,
                this.canCauseFire, this.shouldBreakBlocks));
    }

    @Override
    public ExplosionBuilder reset() {
        this.world = null;
        this.sourceEntity = null;
        this.radius = 0;
        this.origin = null;
        this.canCauseFire = false;
        this.shouldBreakBlocks = false;
        return this;
    }
}
