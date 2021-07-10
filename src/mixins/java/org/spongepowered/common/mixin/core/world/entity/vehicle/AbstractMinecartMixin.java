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
package org.spongepowered.common.mixin.core.world.entity.vehicle;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.vehicle.minecart.MinecartLike;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.AttackEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.data.SpongeDataHolderBridge;
import org.spongepowered.common.bridge.world.entity.vehicle.AbstractMinecartBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.mixin.core.world.entity.EntityMixin;
import org.spongepowered.common.util.Constants;
import org.spongepowered.math.vector.Vector3d;

import java.util.ArrayList;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartMixin extends EntityMixin implements AbstractMinecartBridge {

    protected double impl$maxSpeed = Constants.Entity.Minecart.DEFAULT_MAX_SPEED;
    private boolean impl$slowWhenEmpty = true;
    private Vector3d impl$airborneMod = new Vector3d(Constants.Entity.Minecart.DEFAULT_AIRBORNE_MOD,
        Constants.Entity.Minecart.DEFAULT_AIRBORNE_MOD,
        Constants.Entity.Minecart.DEFAULT_AIRBORNE_MOD);
    private Vector3d impl$derailedMod = new Vector3d(Constants.Entity.Minecart.DEFAULT_DERAILED_MOD,
        Constants.Entity.Minecart.DEFAULT_DERAILED_MOD,
        Constants.Entity.Minecart.DEFAULT_DERAILED_MOD);

    /**
     * @author Minecrell - December 5th, 2016
     * @reason Use our custom maximum speed for the Minecart.
     */
    @Overwrite
    protected double getMaxSpeed() {
        return this.impl$maxSpeed;
    }

    @Redirect(method = "comeOffTrack",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/phys/Vec3;scale(D)Lnet/minecraft/world/phys/Vec3;"
        ),
        slice = @Slice(
            from = @At(value = "FIELD", target = "Lnet/minecraft/world/phys/Vec3;z:D"),
            to = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/MoverType;SELF:Lnet/minecraft/world/entity/MoverType;")
        ),
        expect = 1,
        require = 1
    )
    private Vec3 impl$applyDerailedModifierOnGround(final Vec3 vec3d, final double factor) {
        return vec3d.multiply(this.impl$derailedMod.x(), this.impl$derailedMod.y(), this.impl$derailedMod.z());
    }

    @Redirect(method = "comeOffTrack",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/phys/Vec3;scale(D)Lnet/minecraft/world/phys/Vec3;"
        ),
        slice = @Slice(
            from = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/MoverType;SELF:Lnet/minecraft/world/entity/MoverType;"),
            to = @At(value = "TAIL")
        ),
        expect = 1,
        require = 1
    )
    private Vec3 impl$applyDerailedModifierInAir(final Vec3 vec3d, final double factor) {
        return vec3d.multiply(this.impl$airborneMod.x(), this.impl$airborneMod.y(), this.impl$airborneMod.z());
    }

    @Redirect(method = "applyNaturalSlowdown",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/vehicle/AbstractMinecart;isVehicle()Z"
        )
    )
    private boolean impl$applyDragIfEmpty(final AbstractMinecart self) {
        return !this.impl$slowWhenEmpty || this.shadow$isVehicle();
    }

    @Inject(method = "hurt",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/vehicle/AbstractMinecart;ejectPassengers()V"
        ),
        cancellable = true)
    private void impl$postOnAttackEntityFrom(final DamageSource source, final float amount, final CallbackInfoReturnable<Boolean> cir) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(this);
            frame.pushCause(source);
            final AttackEntityEvent event = SpongeEventFactory.createAttackEntityEvent(frame.currentCause(), (MinecartLike) this, new ArrayList<>(), 0, amount);
            SpongeCommon.post(event);
            if (event.isCancelled()) {
                cir.setReturnValue(true);
            }
        }
    }

    @Override
    public double bridge$getMaxSpeed() {
        return this.impl$maxSpeed;
    }

    @Override
    public void bridge$setMaxSpeed(double impl$maxSpeed) {
        this.impl$maxSpeed = impl$maxSpeed;
        if (impl$maxSpeed == Constants.Entity.Minecart.DEFAULT_MAX_SPEED) {
            ((SpongeDataHolderBridge) this).bridge$remove(Keys.POTENTIAL_MAX_SPEED);
        } else {
            ((SpongeDataHolderBridge) this).bridge$offer(Keys.POTENTIAL_MAX_SPEED, impl$maxSpeed);
        }
    }

    @Override
    public boolean bridge$getSlowWhenEmpty() {
        return this.impl$slowWhenEmpty;
    }

    @Override
    public void bridge$setSlowWhenEmpty(final boolean impl$slowWhenEmpty) {
        this.impl$slowWhenEmpty = impl$slowWhenEmpty;
        if (impl$slowWhenEmpty) {
            ((SpongeDataHolderBridge) this).bridge$remove(Keys.SLOWS_UNOCCUPIED);
        } else {
            ((SpongeDataHolderBridge) this).bridge$offer(Keys.SLOWS_UNOCCUPIED, false);
        }
    }

    @Override
    public Vector3d bridge$getAirborneMod() {
        return this.impl$airborneMod;
    }

    @Override
    public void bridge$setAirborneMod(final Vector3d impl$airborneMod) {
        this.impl$airborneMod = impl$airborneMod;
        if (impl$airborneMod.equals(new Vector3d(Constants.Entity.Minecart.DEFAULT_AIRBORNE_MOD,
                                                 Constants.Entity.Minecart.DEFAULT_AIRBORNE_MOD,
                                                 Constants.Entity.Minecart.DEFAULT_AIRBORNE_MOD))) {
            ((SpongeDataHolderBridge) this).bridge$remove(Keys.AIRBORNE_VELOCITY_MODIFIER);
        } else {
            ((SpongeDataHolderBridge) this).bridge$offer(Keys.AIRBORNE_VELOCITY_MODIFIER, impl$airborneMod);
        }
    }

    @Override
    public Vector3d bridge$getDerailedMod() {
        return this.impl$derailedMod;
    }
    
    @Override
    public void bridge$setDerailedMod(final Vector3d impl$derailedMod) {
        this.impl$derailedMod = impl$derailedMod;
        if (impl$derailedMod.equals(new Vector3d(Constants.Entity.Minecart.DEFAULT_DERAILED_MOD,
                                                 Constants.Entity.Minecart.DEFAULT_DERAILED_MOD,
                                                 Constants.Entity.Minecart.DEFAULT_DERAILED_MOD))) {
            ((SpongeDataHolderBridge) this).bridge$remove(Keys.DERAILED_VELOCITY_MODIFIER);
        } else {
            ((SpongeDataHolderBridge) this).bridge$offer(Keys.DERAILED_VELOCITY_MODIFIER, impl$derailedMod);
        }
    }
}
