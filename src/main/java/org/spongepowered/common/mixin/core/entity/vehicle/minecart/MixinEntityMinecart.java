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
package org.spongepowered.common.mixin.core.entity.vehicle.minecart;

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.entity.vehicle.minecart.Minecart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.interfaces.IMixinMinecart;
import org.spongepowered.common.mixin.core.entity.MixinEntity;
import org.spongepowered.common.util.VectorSerializer;

@Mixin(EntityMinecart.class)
public abstract class MixinEntityMinecart extends MixinEntity implements Minecart, IMixinMinecart {

    private double maxSpeed = 0.4D;
    private boolean slowWhenEmpty = true;
    private Vector3d airborneMod = new Vector3d(0.5D, 0.5D, 0.5D);
    private Vector3d derailedMod = new Vector3d(0.5D, 0.5D, 0.5D);

    // this method overwrites vanilla behavior to allow for a custom deceleration rate when derailed
    @Inject(method = "moveDerailedMinecart()V", at = @At(value = "INVOKE", target = "net.minecraft.entity.Entity.moveEntity(DDD)V"))
    public void implementCustomDerailedDeceleration(CallbackInfo ci) {
        if (this.isOnGround()) {
            this.motionX /= 0.5D;
            this.motionY /= 0.5D;
            this.motionZ /= 0.5D;
            this.motionX *= this.derailedMod.getX();
            this.motionY *= this.derailedMod.getY();
            this.motionZ *= this.derailedMod.getZ();
        }
    }

    // this method overwrites vanilla behavior to allow the cart not to slow when empty
    @Overwrite
    protected void applyDrag() {
        if (this.riddenByEntity != null || !this.slowWhenEmpty) {
            this.motionX *= 1.0D;
            this.motionY *= 0.0D;
            this.motionZ *= 1.0D;
        } else {
            this.motionX *= 0.96D;
            this.motionY *= 0.0D;
            this.motionZ *= 0.96D;
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
        return this.getMaximumMinecartSpeed();
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
