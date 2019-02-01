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
import static com.google.common.base.Preconditions.checkState;

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.entity.Entity;
import org.spongepowered.api.entity.explosive.Explosive;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.common.interfaces.world.IMixinExplosion;

public class SpongeExplosionBuilder implements Explosion.Builder {

    private Location location;
    private Explosive sourceExplosive;
    private float radius;
    private boolean canCauseFire;
    private boolean shouldBreakBlocks = true;
    private boolean shouldSmoke;
    private boolean shouldDamageEntities = true;

    public SpongeExplosionBuilder() {
        reset();
    }

    @Override
    public Explosion.Builder sourceExplosive(Explosive source) {
        this.sourceExplosive = source;
        return this;
    }

    @Override
    public Explosion.Builder radius(float radius) {
        this.radius = radius;
        return this;
    }

    @Override
    public Explosion.Builder location(Location location) {
        this.location = checkNotNull(location, "location");
        return this;
    }

    @Override
    public Explosion.Builder canCauseFire(boolean fire) {
        this.canCauseFire = fire;
        return this;
    }

    @Override
    public Explosion.Builder shouldDamageEntities(boolean damage) {
        this.shouldDamageEntities = damage;
        return this;
    }

    @Override
    public Explosion.Builder shouldPlaySmoke(boolean smoke) {
        this.shouldSmoke = smoke;
        return this;
    }

    @Override
    public Explosion.Builder shouldBreakBlocks(boolean destroy) {
        this.shouldBreakBlocks = destroy;
        return this;
    }

    @Override
    public Explosion.Builder from(Explosion value) {
        this.location = value.getLocation();
        this.sourceExplosive = value.getSourceExplosive().orElse(null);
        this.radius = value.getRadius();
        this.canCauseFire = value.canCauseFire();
        this.shouldBreakBlocks = value.shouldBreakBlocks();
        this.shouldSmoke = value.shouldPlaySmoke();
        this.shouldDamageEntities = value.shouldDamageEntities();
        return this;
    }

    @Override
    public SpongeExplosionBuilder reset() {
        this.location = null;
        this.sourceExplosive = null;
        this.radius = 0;
        this.canCauseFire = false;
        this.shouldBreakBlocks = true;
        this.shouldSmoke = false;
        this.shouldDamageEntities = true;
        return this;
    }

    @Override
    public Explosion build() throws IllegalStateException {
        // TODO Check coordinates and if world is loaded here.
        checkState(this.location != null, "Location is null!");

        World world = this.location.getWorld();
        Vector3d origin = this.location.getPosition();
        final net.minecraft.world.Explosion explosion = new net.minecraft.world.Explosion((net.minecraft.world.World) world,
                (Entity) this.sourceExplosive, origin.getX(), origin.getY(), origin.getZ(), this.radius,
                this.canCauseFire, this.shouldSmoke);
        ((IMixinExplosion) explosion).setShouldBreakBlocks(this.shouldBreakBlocks);
        ((IMixinExplosion) explosion).setShouldDamageEntities(this.shouldDamageEntities);
        return (Explosion) explosion;
    }
}
