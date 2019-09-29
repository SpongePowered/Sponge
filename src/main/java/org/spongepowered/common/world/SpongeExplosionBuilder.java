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
import org.spongepowered.common.bridge.world.ExplosionBridge;

public class SpongeExplosionBuilder implements Explosion.Builder {

    private Location<World> location;
    private Explosive sourceExplosive;
    private float radius;
    private boolean canCauseFire;
    private boolean shouldBreakBlocks = true;
    private boolean shouldSmoke;
    private boolean shouldDamageEntities = true;
    private int resolution = 16;
    private float randomness = 1.0F;
    private double knockback = 1;

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
    public Explosion.Builder location(Location<World> location) {
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
    public Explosion.Builder resolution(int resolution) {
        //A value of 1 would cause a DivideByZeroException
        this.resolution = Math.max(resolution, 2);
        return this;
    }

    @Override
    public Explosion.Builder randomness(float randomness) {
        this.randomness = randomness;
        return this;
    }

    @Override
    public Explosion.Builder knockback(double knockback) {
        this.knockback = knockback;
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
        this.resolution = value.getResolution();
        this.randomness = value.getRandomness();
        this.knockback = value.getKnockback();
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
        this.resolution = 16;
        this.randomness = 1.0F;
        this.knockback = 1;
        return this;
    }

    @Override
    public Explosion build() throws IllegalStateException {
        // TODO Check coordinates and if world is loaded here.
        checkState(this.location != null, "Location is null!");

        World world = this.location.getExtent();
        Vector3d origin = this.location.getPosition();
        final net.minecraft.world.Explosion explosion = new net.minecraft.world.Explosion((net.minecraft.world.World) world,
                (Entity) this.sourceExplosive, origin.getX(), origin.getY(), origin.getZ(), this.radius,
                this.canCauseFire, this.shouldSmoke);
        ((ExplosionBridge) explosion).bridge$setShouldBreakBlocks(this.shouldBreakBlocks);
        ((ExplosionBridge) explosion).bridge$setShouldDamageEntities(this.shouldDamageEntities);
        ((ExplosionBridge) explosion).bridge$setResolution(this.resolution);
        ((ExplosionBridge) explosion).bridge$setRandomness(this.randomness);
        ((ExplosionBridge) explosion).bridge$setKnockback(this.knockback);
        return (Explosion) explosion;
    }
}
