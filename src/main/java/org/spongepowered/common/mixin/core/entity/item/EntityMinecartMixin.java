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
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.entity.item.EntityMinecartBridge;
import org.spongepowered.common.mixin.core.entity.EntityMixin;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.VectorSerializer;

import java.util.ArrayList;

@Mixin(EntityMinecart.class)
public abstract class EntityMinecartMixin extends EntityMixin implements EntityMinecartBridge {

    private double impl$maxSpeed = Constants.Entity.Minecart.DEFAULT_MAX_SPEED;
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

    @ModifyConstant(method = "moveDerailedMinecart", constant = @Constant(doubleValue = Constants.Entity.Minecart.DEFAULT_DERAILED_MOD, ordinal = 0))
    private double onDecelerateX(final double defaultValue) {
        return this.impl$derailedMod.getX();
    }

    @ModifyConstant(method = "moveDerailedMinecart", constant = @Constant(doubleValue = Constants.Entity.Minecart.DEFAULT_DERAILED_MOD, ordinal = 1))
    private double onDecelerateY(final double defaultValue) {
        return this.impl$derailedMod.getY();
    }

    @ModifyConstant(method = "moveDerailedMinecart", constant = @Constant(doubleValue = Constants.Entity.Minecart.DEFAULT_DERAILED_MOD, ordinal = 2))
    private double onDecelerateZ(final double defaultValue) {
        return this.impl$derailedMod.getZ();
    }

    @Redirect(method = "applyDrag", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityMinecart;isBeingRidden()Z"))
    private boolean onIsRidden(final EntityMinecart self) {
        return !this.impl$slowWhenEmpty || isBeingRidden();
    }

    @Inject(method = "attackEntityFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityMinecart;removePassengers()V"),
      cancellable = true)
    private void onAttackEntityFrom(final DamageSource source, final float amount, final CallbackInfoReturnable<Boolean> cir) {
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(source);
            final AttackEntityEvent event = SpongeEventFactory.createAttackEntityEvent(frame.getCurrentCause(), new ArrayList<>(), (Minecart) this, 0, amount);
            SpongeImpl.postEvent(event);
            if (event.isCancelled()) {
                cir.setReturnValue(true);
            }
        }
    }

    @Override
    public void spongeImpl$readFromSpongeCompound(final NBTTagCompound compound) {
        super.spongeImpl$readFromSpongeCompound(compound);
        if (compound.func_74764_b(Constants.Entity.Minecart.MAX_SPEED)) {
            this.impl$maxSpeed = compound.func_74769_h(Constants.Entity.Minecart.MAX_SPEED);
        }
        if (compound.func_74764_b(Constants.Entity.Minecart.SLOW_WHEN_EMPTY)) {
            this.impl$slowWhenEmpty = compound.func_74767_n(Constants.Entity.Minecart.SLOW_WHEN_EMPTY);
        }
        if (compound.func_74764_b(Constants.Entity.Minecart.AIRBORNE_MODIFIER)) {
            this.impl$airborneMod = VectorSerializer.fromNbt(compound.func_74775_l(Constants.Entity.Minecart.AIRBORNE_MODIFIER));
        }
        if (compound.func_74764_b(Constants.Entity.Minecart.DERAILED_MODIFIER)) {
            this.impl$derailedMod = VectorSerializer.fromNbt(compound.func_74775_l(Constants.Entity.Minecart.DERAILED_MODIFIER));
        }
    }

    @Override
    public void spongeImpl$writeToSpongeCompound(final NBTTagCompound compound) {
        super.spongeImpl$writeToSpongeCompound(compound);
        compound.func_74780_a(Constants.Entity.Minecart.MAX_SPEED, this.impl$maxSpeed);
        compound.func_74757_a(Constants.Entity.Minecart.SLOW_WHEN_EMPTY, this.impl$slowWhenEmpty);
        compound.func_74782_a(Constants.Entity.Minecart.AIRBORNE_MODIFIER, VectorSerializer.toNbt(this.impl$airborneMod));
        compound.func_74782_a(Constants.Entity.Minecart.DERAILED_MODIFIER, VectorSerializer.toNbt(this.impl$derailedMod));
    }

    @Override
    public Vector3d bridge$getAirboneVelocityModifier() {
        return this.impl$airborneMod;
    }
}
