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
import org.spongepowered.common.mixin.core.entity.MixinEntity;
import org.spongepowered.common.util.VectorSerializer;

import java.util.ArrayList;

@Mixin(EntityMinecart.class)
public abstract class MixinEntityMinecart extends MixinEntity implements Minecart {

    private static final double DEFAULT_AIRBORNE_MOD = 0.94999998807907104D;
    private static final double DEFAULT_DERAILED_MOD = 0.5D;

    private double maxSpeed = 0.4D;
    private boolean slowWhenEmpty = true;
    private Vector3d airborneMod = new Vector3d(DEFAULT_AIRBORNE_MOD, DEFAULT_AIRBORNE_MOD, DEFAULT_AIRBORNE_MOD);
    private Vector3d derailedMod = new Vector3d(DEFAULT_DERAILED_MOD, DEFAULT_DERAILED_MOD, DEFAULT_DERAILED_MOD);

    /**
     * @author Minecrell - December 5th, 2016
     * @reason Use our custom maximum speed for the Minecart.
     */
    @Overwrite
    protected double getMaximumSpeed() {
        return this.maxSpeed;
    }

    @ModifyConstant(method = "moveDerailedMinecart", constant = @Constant(doubleValue = DEFAULT_DERAILED_MOD, ordinal = 0))
    private double onDecelerateX(double defaultValue) {
        return this.derailedMod.getX();
    }

    @ModifyConstant(method = "moveDerailedMinecart", constant = @Constant(doubleValue = DEFAULT_DERAILED_MOD, ordinal = 1))
    private double onDecelerateY(double defaultValue) {
        return this.derailedMod.getY();
    }

    @ModifyConstant(method = "moveDerailedMinecart", constant = @Constant(doubleValue = DEFAULT_DERAILED_MOD, ordinal = 2))
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
            AttackEntityEvent event = SpongeEventFactory.createAttackEntityEvent(frame.getCurrentCause(), new ArrayList<>(), this, 0, amount);
            SpongeImpl.postEvent(event);
            if (event.isCancelled()) {
                cir.setReturnValue(true);
            }
        }
    }

    @Override
    public double getSwiftness() {
        return this.maxSpeed;
    }

    @Override
    public void setSwiftness(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    @Override
    public double getPotentialMaxSpeed() {
        // SpongeForge replaces this method so it returns the result of the Forge method
        return getMaximumSpeed();
    }

    @Override
    public boolean doesSlowWhenEmpty() {
        return this.slowWhenEmpty;
    }

    @Override
    public void setSlowWhenEmpty(boolean slowWhenEmpty) {
        this.slowWhenEmpty = slowWhenEmpty;
    }

    @Override
    public Vector3d getAirborneVelocityMod() {
        return this.airborneMod;
    }

    @Override
    public void setAirborneVelocityMod(Vector3d airborneMod) {
        this.airborneMod = airborneMod;
    }

    @Override
    public Vector3d getDerailedVelocityMod() {
        return this.derailedMod;
    }

    @Override
    public void setDerailedVelocityMod(Vector3d derailedVelocityMod) {
        this.derailedMod = derailedVelocityMod;
    }

    @Override
    public void readFromNbt(NBTTagCompound compound) {
        super.readFromNbt(compound);
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
    public void writeToNbt(NBTTagCompound compound) {
        super.writeToNbt(compound);
        compound.setDouble("maxSpeed", this.maxSpeed);
        compound.setBoolean("slowWhenEmpty", this.slowWhenEmpty);
        compound.setTag("airborneModifier", VectorSerializer.toNbt(this.airborneMod));
        compound.setTag("derailedModifier", VectorSerializer.toNbt(this.derailedMod));
    }

}
