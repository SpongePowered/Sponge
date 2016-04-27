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
package org.spongepowered.common.mixin.entityactivation;

import net.minecraft.world.World;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.entity.SpongeEntityType;
import org.spongepowered.common.interfaces.world.IMixinWorldInfo;
import org.spongepowered.common.mixin.plugin.entityactivation.ActivationRange;
import org.spongepowered.common.mixin.plugin.entityactivation.interfaces.IModData_Activation;

@NonnullByDefault
@Mixin(value = net.minecraft.entity.Entity.class, priority = 1002)
public abstract class MixinEntity_Activation implements Entity, IModData_Activation {

    public final byte activationType = ActivationRange.initializeEntityActivationType((net.minecraft.entity.Entity) (Object) this);
    public boolean defaultActivationState;
    public long activatedTick = Integer.MIN_VALUE;
    private EntityType entityType;
    private int activationRange;
    private String modId;
    private boolean refreshCache = false;

    @Shadow public World worldObj;
    @Shadow public boolean onGround;

    @Shadow
    public abstract void setDead();

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onEntityActivationConstruction(World world, CallbackInfo ci) {
        if (world != null && ((IMixinWorldInfo) world.getWorldInfo()).isValid()) {
            this.defaultActivationState = ActivationRange.initializeEntityActivationState((net.minecraft.entity.Entity) (Object) this);
            if (!this.defaultActivationState && this.entityType != null) {
                ActivationRange.addEntityToConfig(world, (SpongeEntityType) this.entityType, this.activationType);
            }
        } else {
            this.defaultActivationState = false;
        }
    }

    @Override
    public void inactiveTick() {
    }

    @Override
    public byte getActivationType() {
        return this.activationType;
    }

    @Override
    public long getActivatedTick() {
        return this.activatedTick;
    }

    @Override
    public boolean getDefaultActivationState() {
        return this.defaultActivationState;
    }

    @Override
    public void setActivatedTick(long tick) {
        this.activatedTick = tick;
    }

    @Override
    public int getActivationRange() {
        return this.activationRange;
    }

    @Override
    public void setActivationRange(int range) {
        this.activationRange = range;
    }

    @Override
    public String getModDataId() {
        return this.modId;
    }

    @Override
    public void setModDataId(String mod) {
        this.modId = mod;
    }

    @Override
    public void requiresCacheRefresh(boolean flag) {
        this.refreshCache = flag;
    }

    @Override
    public boolean requiresCacheRefresh() {
        return this.refreshCache;
    }
}
