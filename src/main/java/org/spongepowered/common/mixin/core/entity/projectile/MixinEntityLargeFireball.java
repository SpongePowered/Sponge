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
package org.spongepowered.common.mixin.core.entity.projectile;

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.world.GameRules;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.projectile.explosive.fireball.LargeFireball;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import org.spongepowered.common.interfaces.entity.IMixinGriefer;
import org.spongepowered.common.interfaces.entity.explosive.IMixinExplosive;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(EntityLargeFireball.class)
public abstract class MixinEntityLargeFireball extends MixinEntityFireball implements LargeFireball, IMixinExplosive {

    private static final int DEFAULT_EXPLOSION_RADIUS = 1;

    @Shadow public int explosionPower;

    /**
     * @author gabizou April 13th, 2018
     * @reason Due to changes from Forge, we have to redirect or modify the gamerule check,
     * but since forge doesn't allow us to continue to check the gamerule method call here,
     * we have to modify the arguments passed in (the two booleans). There may be a better way,
     * which may include redirecting the world.newExplosion method call instead of modifyargs,
     * but, it is what it is.
     * @return
     */
    @SuppressWarnings("deprecation")
    @Redirect(method = "onImpact",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/World;newExplosion(Lnet/minecraft/entity/Entity;DDDFZZ)Lnet/minecraft/world/Explosion;"
        )
    )
    private net.minecraft.world.Explosion onSpongeExplosion(net.minecraft.world.World worldObj, @Nullable Entity nil,
        double x, double y, double z, float strength, boolean flaming,
        boolean smoking) {
        boolean griefer = ((IMixinGriefer) this).canGrief();
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(this);
            frame.addContext(EventContextKeys.PROJECTILE_SOURCE, getShooter());
            frame.pushCause(getShooter());
            Optional<net.minecraft.world.Explosion> ex = detonate(Explosion.builder()
                .location(new Location((World) worldObj, new Vector3d(x, y, z)))
                .sourceExplosive(this)
                .radius(strength)
                .canCauseFire(flaming && griefer)
                .shouldPlaySmoke(smoking && griefer)
                .shouldBreakBlocks(smoking && griefer));

            return ex.orElse(null);
        }
    }

    // Explosive Impl

    @Override
    public Optional<Integer> getExplosionRadius() {
        return Optional.of(this.explosionPower);
    }

    @Override
    public void setExplosionRadius(Optional<Integer> radius) {
        this.explosionPower = radius.orElse(DEFAULT_EXPLOSION_RADIUS);
    }

    @Override
    public void detonate() {
        onSpongeExplosion(this.world, null, this.posX, this.posY, this.posZ, this.explosionPower, true, true);
        remove();
    }

}
