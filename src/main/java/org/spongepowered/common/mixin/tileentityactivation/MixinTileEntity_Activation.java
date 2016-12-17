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
package org.spongepowered.common.mixin.tileentityactivation;

import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.mixin.plugin.entityactivation.interfaces.IModData_Activation;

@Mixin(value = TileEntity.class, priority = 1002)
public class MixinTileEntity_Activation implements IModData_Activation {

    private boolean refreshCache = false;
    public boolean defaultActivationState = false;
    public long activatedTick = Integer.MIN_VALUE;
    private int activationRange;
    private int ticksExisted;
    private int tickRate = 1;

    @Override
    public void incrementTicksExisted() {
        this.ticksExisted++;
    }

    @Override
    public int getTicksExisted() {
        return this.ticksExisted;
    }

    @Override
    public void inactiveTick() {
    }

    @Override
    public byte getActivationType() {
        return 0;
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
    public void setDefaultActivationState(boolean defaultState) {
        this.defaultActivationState = defaultState;
    }

    @Override
    public void setActivatedTick(long tick) {
        this.activatedTick = tick;
    }

    @Override
    public int getTickRate() {
        return this.tickRate;
    }

    @Override
    public void setTickRate(int tickRate) {
        this.tickRate = tickRate;
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
    public void requiresActivationCacheRefresh(boolean flag) {
        this.refreshCache = flag;
    }

    @Override
    public boolean requiresActivationCacheRefresh() {
        return this.refreshCache;
    }
}
