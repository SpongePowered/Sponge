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
import org.spongepowered.common.bridge.entity.item.EnderCrystalEntityBridge;
import org.spongepowered.common.bridge.explosives.ExplosiveBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.mixin.core.entity.EntityMixin;
import org.spongepowered.common.util.Constants;
import org.spongepowered.math.vector.Vector3d;
import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(EnderCrystalEntity.class)
public abstract class EnderCrystalEntityMixin extends EntityMixin implements ExplosiveBridge, EnderCrystalEntityBridge {

    private int impl$explosionStrength = Constants.Entity.EnderCrystal.DEFAULT_EXPLOSION_STRENGTH;

    // Explosive Impl

    @Override
    public Optional<Integer> bridge$getExplosionRadius() {
        return Optional.of(this.impl$explosionStrength);
    }

    @Override
    public void bridge$setExplosionRadius(@Nullable final Integer radius) {
        this.impl$explosionStrength = radius == null ? Constants.Entity.EnderCrystal.DEFAULT_EXPLOSION_STRENGTH : radius;
    }

    @Redirect(method = "attackEntityFrom",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;createExplosion(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/world/Explosion$Mode;)Lnet/minecraft/world/Explosion;"
        )
    )
    @Nullable
    private net.minecraft.world.Explosion impl$throwEventWithEntity(final net.minecraft.world.World world,
        final Entity entityIn, final double xIn, final double yIn, final double zIn, final float explosionRadius,
        final net.minecraft.world.Explosion.Mode modeIn, final DamageSource source, final float damage) {
        return this.bridge$ThrowEventWithDetonation(world, entityIn, xIn, yIn, zIn, modeIn.compareTo(net.minecraft.world.Explosion.Mode.DESTROY) <= 0, source);
    }

    @Nullable
    @Override
    public net.minecraft.world.Explosion bridge$ThrowEventWithDetonation(final net.minecraft.world.World world,
        @Nullable final Entity nil, final double x, final double y, final double z, final boolean smoking,
        @Nullable final DamageSource source) {
        final CauseStackManager causeStackManager = Sponge.getCauseStackManager();
        try (final CauseStackManager.StackFrame frame = causeStackManager.pushCauseFrame()){
            frame.pushCause(this);
            if (source != null) {
                frame.pushCause(source);
            }
            return SpongeCommonEventFactory.detonateExplosive(this, Explosion.builder()
                .location(Location.of((World) world, new Vector3d(x, y, z)))
                .radius(this.impl$explosionStrength)
                .shouldPlaySmoke(smoking))
                .orElse(null);
        }
    }

}
