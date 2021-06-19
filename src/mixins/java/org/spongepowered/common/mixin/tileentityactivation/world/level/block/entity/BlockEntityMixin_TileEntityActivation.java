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
package org.spongepowered.common.mixin.tileentityactivation.world.level.block.entity;

import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.activation.ActivationCapabilityBridge;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin_TileEntityActivation implements ActivationCapabilityBridge {

    private boolean tileEntityActivation$refreshCache = false;
    private boolean tileEntityActivation$defaultActivationState = true;
    private long tileEntityActivation$activatedTick = Integer.MIN_VALUE;
    private int tileEntityActivation$activationRange;
    private int tileEntityActivation$ticksExisted;
    private int tileEntityActivation$tickRate = 1;

    @Override
    public final void activation$incrementSpongeTicksExisted() {
        this.tileEntityActivation$ticksExisted++;
    }

    @Override
    public int activation$getSpongeTicksExisted() {
        return this.tileEntityActivation$ticksExisted;
    }

    @Override
    public void activation$inactiveTick() {
    }

    @Override
    public byte activation$getActivationType() {
        return 0;
    }

    @Override
    public long activation$getActivatedTick() {
        return this.tileEntityActivation$activatedTick;
    }

    @Override
    public boolean activation$getDefaultActivationState() {
        return this.tileEntityActivation$defaultActivationState;
    }

    @Override
    public void activation$setDefaultActivationState(boolean defaultState) {
        this.tileEntityActivation$defaultActivationState = defaultState;
    }

    @Override
    public void activation$setActivatedTick(long tick) {
        this.tileEntityActivation$activatedTick = tick;
    }

    @Override
    public int activation$getSpongeTickRate() {
        return this.tileEntityActivation$tickRate;
    }

    @Override
    public void activation$setSpongeTickRate(int tickRate) {
        this.tileEntityActivation$tickRate = tickRate;
    }

    @Override
    public int activation$getActivationRange() {
        return this.tileEntityActivation$activationRange;
    }

    @Override
    public void activation$setActivationRange(int range) {
        this.tileEntityActivation$activationRange = range;
    }

    @Override
    public void activation$requiresActivationCacheRefresh(boolean flag) {
        this.tileEntityActivation$refreshCache = flag;
    }

    @Override
    public boolean activation$requiresActivationCacheRefresh() {
        return this.tileEntityActivation$refreshCache;
    }
}
