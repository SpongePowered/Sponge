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
package org.spongepowered.common.mixin.core.world;

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import org.spongepowered.api.entity.explosive.Explosive;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.util.VecHelper;

import java.util.List;
import java.util.Optional;

@Mixin(net.minecraft.world.Explosion.class)
public abstract class MixinExplosion implements Explosion {

    public Vector3d origin;
    public Vec3 position; // Added for Forge

    @Shadow public boolean isFlaming;
    @Shadow public boolean isSmoking;
    @Shadow public net.minecraft.world.World worldObj;
    @Shadow public double explosionX;
    @Shadow public double explosionY;
    @Shadow public double explosionZ;
    @Shadow public Entity exploder;
    @Shadow public float explosionSize;
    @SuppressWarnings("rawtypes")
    @Shadow public List affectedBlockPositions;

    @Inject(method = "<init>*", at = @At("RETURN"))
    public void onConstructed(net.minecraft.world.World world, Entity entity, double originX, double originY,
            double originZ, float radius, boolean isFlaming, boolean isSmoking,
            CallbackInfo ci) {
        this.origin = new Vector3d(this.explosionX, this.explosionY, this.explosionZ);
    }

    @Override
    public World getWorld() {
        return (World) this.worldObj;
    }

    @Override
    public void setWorld(World world) {
        this.worldObj = (net.minecraft.world.World) world;
    }

    @Override
    public Optional<Explosive> getSourceExplosive() {
        return Optional.ofNullable((Explosive) this.exploder);
    }

    @Override
    public void setSourceExplosive(Explosive source) {
        this.exploder = (Entity) source;
    }

    @Override
    public float getRadius() {
        return this.explosionSize;
    }

    @Override
    public void setRadius(float radius) {
        this.explosionSize = radius;
    }

    @Override
    public Vector3d getOrigin() {
        return this.origin;
    }

    @Override
    public void setOrigin(Vector3d origin) {
        this.position = VecHelper.toVector(origin);
        this.origin = origin;
        this.explosionX = origin.getX();
        this.explosionY = origin.getY();
        this.explosionZ = origin.getZ();
    }

    @Override
    public boolean canCauseFire() {
        return this.isFlaming;
    }

    @Override
    public void canCauseFire(boolean fire) {
        this.isFlaming = fire;
    }

    @Override
    public boolean shouldBreakBlocks() {
        return this.isSmoking;
    }

    @Override
    public void shouldBreakBlocks(boolean destroy) {
        this.isSmoking = destroy;
    }

}
