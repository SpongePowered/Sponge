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
package org.spongepowered.common.mixin.core.entity.item;

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.util.DamageSource;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.bridge.entity.item.EntityEnderCrystalBridge;
import org.spongepowered.common.bridge.explosives.ExplosiveBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.mixin.core.entity.EntityMixin;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(EnderCrystalEntity.class)
public abstract class EntityEnderCrystalMixin extends EntityMixin implements ExplosiveBridge, EntityEnderCrystalBridge {

    private int explosionStrength = Constants.Entity.EnderCrystal.DEFAULT_EXPLOSION_STRENGTH;

    // Explosive Impl

    @Override
    public Optional<Integer> bridge$getExplosionRadius() {
        return Optional.of(this.explosionStrength);
    }

    @Override
    public void bridge$setExplosionRadius(@Nullable Integer radius) {
        this.explosionStrength = radius == null ? Constants.Entity.EnderCrystal.DEFAULT_EXPLOSION_STRENGTH : radius;
    }

    @Redirect(method = "attackEntityFrom",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;createExplosion(Lnet/minecraft/entity/Entity;DDDFZ)Lnet/minecraft/world/Explosion;"
        )
    )
    @Nullable
    private net.minecraft.world.Explosion impl$throwEventWithEntity(net.minecraft.world.World world, @Nullable Entity nil, double x,
                                                    double y, double z, float strength, boolean smoking, DamageSource source, float damage) {
        return bridge$ThrowEventWithDetonation(world, nil, x, y, z, smoking, source);
    }

    @Nullable
    @Override
    public net.minecraft.world.Explosion bridge$ThrowEventWithDetonation(net.minecraft.world.World world, @Nullable Entity nil, double x,
        double y, double z, boolean smoking, @Nullable DamageSource source) {
        final CauseStackManager causeStackManager = Sponge.getCauseStackManager();
        try (CauseStackManager.StackFrame frame = causeStackManager.pushCauseFrame()){
            frame.pushCause(this);
            if (source != null) {
                frame.pushCause(source);
            }
            return SpongeCommonEventFactory.detonateExplosive(this, Explosion.builder()
                .location(new Location<>((World) world, new Vector3d(x, y, z)))
                .radius(this.explosionStrength)
                .shouldPlaySmoke(smoking))
                .orElse(null);
        }
    }

}
