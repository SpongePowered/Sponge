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
import org.spongepowered.common.mixin.core.entity.EntityMixin;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.VectorSerializer;

import java.util.ArrayList;

@Mixin(EntityMinecart.class)
public abstract class EntityMinecartMixin extends EntityMixin {

    private double maxSpeed = 0.4D;
    private boolean slowWhenEmpty = true;
    private Vector3d airborneMod = new Vector3d(Constants.Entity.Minecart.DEFAULT_AIRBORNE_MOD, Constants.Entity.Minecart.DEFAULT_AIRBORNE_MOD, Constants.Entity.Minecart.DEFAULT_AIRBORNE_MOD);
    private Vector3d derailedMod = new Vector3d(Constants.Entity.Minecart.DEFAULT_DERAILED_MOD, Constants.Entity.Minecart.DEFAULT_DERAILED_MOD, Constants.Entity.Minecart.DEFAULT_DERAILED_MOD);

    /**
     * @author Minecrell - December 5th, 2016
     * @reason Use our custom maximum speed for the Minecart.
     */
    @Overwrite
    protected double getMaximumSpeed() {
        return this.maxSpeed;
    }

    @ModifyConstant(method = "moveDerailedMinecart", constant = @Constant(doubleValue = Constants.Entity.Minecart.DEFAULT_DERAILED_MOD, ordinal = 0))
    private double onDecelerateX(double defaultValue) {
        return this.derailedMod.getX();
    }

    @ModifyConstant(method = "moveDerailedMinecart", constant = @Constant(doubleValue = Constants.Entity.Minecart.DEFAULT_DERAILED_MOD, ordinal = 1))
    private double onDecelerateY(double defaultValue) {
        return this.derailedMod.getY();
    }

    @ModifyConstant(method = "moveDerailedMinecart", constant = @Constant(doubleValue = Constants.Entity.Minecart.DEFAULT_DERAILED_MOD, ordinal = 2))
    private double onDecelerateZ(double defaultValue) {
        return this.derailedMod.getZ();
    }

    @Redirect(method = "applyDrag", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityMinecart;isBeingRidden()Z"))
    private boolean onIsRidden(EntityMinecart self) {
        return !this.slowWhenEmpty || isBeingRidden();
    }

    @Inject(method = "attackEntityFrom", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityMinecart;removePassengers()V"),
      cancellable = true)
    private void onAttackEntityFrom(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(source);
            AttackEntityEvent event = SpongeEventFactory.createAttackEntityEvent(frame.getCurrentCause(), new ArrayList<>(), (Minecart) this, 0, amount);
            SpongeImpl.postEvent(event);
            if (event.isCancelled()) {
                cir.setReturnValue(true);
            }
        }
    }

    @Override
    public void spongeImpl$readFromSpongeCompound(NBTTagCompound compound) {
        super.spongeImpl$readFromSpongeCompound(compound);
        if (compound.hasKey("maxSpeed")) {
            this.maxSpeed = compound.getDouble("maxSpeed");
        }
        if (compound.hasKey("slowWhenEmpty")) {
            this.slowWhenEmpty = compound.getBoolean("slowWhenEmpty");
        }
        if (compound.hasKey("airborneModifier")) {
            this.airborneMod = VectorSerializer.fromNbt(compound.getCompoundTag("airborneModifier"));
        }
        if (compound.hasKey("derailedModifier")) {
            this.derailedMod = VectorSerializer.fromNbt(compound.getCompoundTag("derailedModifier"));
        }
    }

    @Override
    public void spongeImpl$writeToSpongeCompound(NBTTagCompound compound) {
        super.spongeImpl$writeToSpongeCompound(compound);
        compound.setDouble("maxSpeed", this.maxSpeed);
        compound.setBoolean("slowWhenEmpty", this.slowWhenEmpty);
        compound.setTag("airborneModifier", VectorSerializer.toNbt(this.airborneMod));
        compound.setTag("derailedModifier", VectorSerializer.toNbt(this.derailedMod));
    }

}
