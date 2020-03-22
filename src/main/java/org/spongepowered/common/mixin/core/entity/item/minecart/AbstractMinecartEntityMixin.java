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
package org.spongepowered.common.mixin.core.entity.item.minecart;

import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.vehicle.minecart.Minecart;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.AttackEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.entity.item.minecart.MinecartEntityBridge;
import org.spongepowered.common.mixin.core.entity.EntityMixin;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.VectorSerializer;
import org.spongepowered.math.vector.Vector3d;

import java.util.ArrayList;

@Mixin(AbstractMinecartEntity.class)
public abstract class AbstractMinecartEntityMixin extends EntityMixin implements MinecartEntityBridge {

    protected double impl$maxSpeed = Constants.Entity.Minecart.DEFAULT_MAX_SPEED;
    private boolean impl$slowWhenEmpty = true;
    private Vector3d impl$airborneMod = new Vector3d(Constants.Entity.Minecart.DEFAULT_AIRBORNE_MOD,
        Constants.Entity.Minecart.DEFAULT_AIRBORNE_MOD,
        Constants.Entity.Minecart.DEFAULT_AIRBORNE_MOD);
    private Vector3d
        impl$derailedMod = new Vector3d(Constants.Entity.Minecart.DEFAULT_DERAILED_MOD,
        Constants.Entity.Minecart.DEFAULT_DERAILED_MOD,
        Constants.Entity.Minecart.DEFAULT_DERAILED_MOD);

    /**
     * @author Minecrell - December 5th, 2016
     * @reason Use our custom maximum speed for the Minecart.
     */
    @Overwrite
    protected double getMaximumSpeed() {
        return this.impl$maxSpeed;
    }

    @Redirect(method = "moveDerailedMinecart",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/util/math/Vec3d;scale(D)Lnet/minecraft/util/math/Vec3d;"
        ),
        slice = @Slice(
            from = @At(value = "FIELD", target = "Lnet/minecraft/util/math/Vec3d;z:D"),
            to = @At(value = "FIELD", target = "Lnet/minecraft/entity/MoverType;SELF:Lnet/minecraft/entity/MoverType;")
        ),
        expect = 1,
        require = 1
    )
    private Vec3d impl$applyDerailedModifierOnGround(final Vec3d vec3d, final double factor) {
        return vec3d.mul(this.impl$derailedMod.getX(), this.impl$derailedMod.getY(), this.impl$derailedMod.getZ());
    }

    @Redirect(method = "moveDerailedMinecart",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/util/math/Vec3d;scale(D)Lnet/minecraft/util/math/Vec3d;"
        ),
        slice = @Slice(
            from = @At(value = "FIELD", target = "Lnet/minecraft/entity/MoverType;SELF:Lnet/minecraft/entity/MoverType;"),
            to = @At(value = "TAIL")
        ),
        expect = 1,
        require = 1
    )
    private Vec3d impl$applyDerailedModifierInAir(final Vec3d vec3d, final double factor) {
        return vec3d.mul(this.impl$airborneMod.getX(), this.impl$airborneMod.getY(), this.impl$airborneMod.getZ());
    }

    @Redirect(method = "applyDrag",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/item/minecart/AbstractMinecartEntity;isBeingRidden()Z"
        )
    )
    private boolean impl$applyDragIfEmpty(final AbstractMinecartEntity self) {
        return !this.impl$slowWhenEmpty || this.shadow$isBeingRidden();
    }

    @Inject(method = "attackEntityFrom",
        at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/item/minecart/AbstractMinecartEntity;removePassengers()V"
        ),
        cancellable = true)
    private void impl$postOnAttackEntityFrom(final DamageSource source, final float amount, final CallbackInfoReturnable<Boolean> cir) {
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(this);
            frame.pushCause(source);
            final AttackEntityEvent event = SpongeEventFactory.createAttackEntityEvent(frame.getCurrentCause(), (Minecart) this, new ArrayList<>(), 0, amount);
            SpongeImpl.postEvent(event);
            if (event.isCancelled()) {
                cir.setReturnValue(true);
            }
        }
    }

    @Override
    public void impl$readFromSpongeCompound(final CompoundNBT compound) {
        super.impl$readFromSpongeCompound(compound);
        if (compound.contains(Constants.Entity.Minecart.MAX_SPEED)) {
            this.impl$maxSpeed = compound.getDouble(Constants.Entity.Minecart.MAX_SPEED);
        }
        if (compound.contains(Constants.Entity.Minecart.SLOW_WHEN_EMPTY)) {
            this.impl$slowWhenEmpty = compound.getBoolean(Constants.Entity.Minecart.SLOW_WHEN_EMPTY);
        }
        if (compound.contains(Constants.Entity.Minecart.AIRBORNE_MODIFIER)) {
            this.impl$airborneMod = VectorSerializer.fromNbt(compound.getCompound(Constants.Entity.Minecart.AIRBORNE_MODIFIER));
        }
        if (compound.contains(Constants.Entity.Minecart.DERAILED_MODIFIER)) {
            this.impl$derailedMod = VectorSerializer.fromNbt(compound.getCompound(Constants.Entity.Minecart.DERAILED_MODIFIER));
        }
    }

    @Override
    public void impl$writeToSpongeCompound(final CompoundNBT compound) {
        super.impl$writeToSpongeCompound(compound);
        compound.putDouble(Constants.Entity.Minecart.MAX_SPEED, this.impl$maxSpeed);
        compound.putBoolean(Constants.Entity.Minecart.SLOW_WHEN_EMPTY, this.impl$slowWhenEmpty);
        compound.put(Constants.Entity.Minecart.AIRBORNE_MODIFIER, VectorSerializer.toNbt(this.impl$airborneMod));
        compound.put(Constants.Entity.Minecart.DERAILED_MODIFIER, VectorSerializer.toNbt(this.impl$derailedMod));
    }

    @Override
    public Vector3d bridge$getAirboneVelocityModifier() {
        return this.impl$airborneMod;
    }
}
