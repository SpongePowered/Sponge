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
package org.spongepowered.common.mixin.entityactivation.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.storage.WorldInfoBridge;
import org.spongepowered.common.mixin.plugin.entityactivation.EntityActivationRange;
import org.spongepowered.common.mixin.plugin.entityactivation.interfaces.ActivationCapability;

@Mixin(value = net.minecraft.entity.Entity.class, priority = 1002)
public abstract class EntityMixin_EntityActivation implements ActivationCapability {

    @Shadow public World world;
    @Shadow public boolean onGround;
    @Shadow public abstract World shadow$getEntityWorld();
    @Shadow public abstract void shadow$remove();

    private final byte entityActivation$type = EntityActivationRange.initializeEntityActivationType((net.minecraft.entity.Entity) (Object) this);
    private boolean entityActivation$defaultState = true;
    private long entityActivation$activatedTick = Integer.MIN_VALUE;
    private int entityActivation$range;
    private boolean entityActivation$refreshCache = false;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void entityActivation$initActivationRanges(EntityType<?> type, World world, CallbackInfo ci) {
        if (world != null && !((WorldBridge) world).bridge$isFake() && ((WorldInfoBridge) world.getWorldInfo()).bridge$isValid()) {
            EntityActivationRange.initializeEntityActivationState((net.minecraft.entity.Entity) (Object) this);
        }
    }

    @Override
    public void entityActivation$inactiveTick() {
    }

    @Override
    public byte entityActivation$getActivationType() {
        return this.entityActivation$type;
    }

    @Override
    public long entityActivation$getActivatedTick() {
        return this.entityActivation$activatedTick;
    }

    @Override
    public boolean entityActivation$getDefaultActivationState() {
        return this.entityActivation$defaultState;
    }

    @Override
    public void entityActivation$setDefaultActivationState(final boolean defaultState) {
        this.entityActivation$defaultState = defaultState;
    }

    @Override
    public void entityActivation$setActivatedTick(final long tick) {
        this.entityActivation$activatedTick = tick;
    }

    @Override
    public int entityActivation$getActivationRange() {
        return this.entityActivation$range;
    }

    @Override
    public void entityActivation$setActivationRange(final int range) {
        this.entityActivation$range = range;
    }

    @Override
    public void entityActivation$requiresActivationCacheRefresh(final boolean flag) {
        this.entityActivation$refreshCache = flag;
    }

    @Override
    public boolean entityActivation$requiresActivationCacheRefresh() {
        return this.entityActivation$refreshCache;
    }
}
