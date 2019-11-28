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
package org.spongepowered.common.mixin.api.mcp.entity.monster;

import static com.google.common.base.Preconditions.checkState;

import net.minecraft.entity.monster.CreeperEntity;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.entity.FuseData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.explosives.FusedExplosiveBridge;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeFuseData;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.util.Constants;

@Mixin(CreeperEntity.class)
public abstract class EntityCreeperMixin_API extends EntityMobMixin_API implements Creeper {

    @Shadow public abstract int getCreeperState();
    @Shadow public abstract void setCreeperState(int state);
    @Shadow private void explode() { } // explode

    @Shadow public abstract boolean getPowered();

    @Override
    public Value<Boolean> charged() {
        return new SpongeValue<>(Keys.CREEPER_CHARGED, false, this.getPowered());
    }

    @Override
    public FuseData getFuseData() {
        return new SpongeFuseData(((FusedExplosiveBridge) this).bridge$getFuseDuration(), ((FusedExplosiveBridge) this).bridge$getFuseTicksRemaining());
    }

    @Override
    public void prime() {
        checkState(!isPrimed(), "already primed");
        setCreeperState(Constants.Entity.Creeper.STATE_PRIMED);
    }

    @Override
    public void defuse() {
        checkState(isPrimed(), "not primed");
        setCreeperState(Constants.Entity.Creeper.STATE_IDLE);
    }

    @Override
    public boolean isPrimed() {
        return getCreeperState() == Constants.Entity.Creeper.STATE_PRIMED;
    }

    @Override
    public void detonate() {
        this.explode();
    }

}
