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
package org.spongepowered.common.mixin.core.world.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Dolphin;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Keys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.data.SpongeDataHolderBridge;
import org.spongepowered.common.bridge.world.entity.EntityMaxAirBridge;

@Mixin({Entity.class, Dolphin.class})
public abstract class EntityMaxAirMixin implements EntityMaxAirBridge {

    @Nullable
    private Integer impl$maxAir;

    @Override
    public int bridge$getDefaultMaxAir() {
        if (this.impl$maxAir == null) {
            return this.bridge$getMaxAir();
        }
        int customMaxAir = this.impl$maxAir;
        this.impl$maxAir = null;
        int defaultMaxAir = this.bridge$getMaxAir();
        this.impl$maxAir = customMaxAir;
        return defaultMaxAir;
    }

    @Override
    public int bridge$getMaxAir() {
        return ((Entity) (Object) this).getMaxAirSupply();
    }

    @Override
    public void bridge$setMaxAir(int maxAir) {
        Entity entity = (Entity) (Object) this;
        if (entity.getAirSupply() > maxAir) {
            entity.setAirSupply(maxAir);
        }
        if (this.bridge$getDefaultMaxAir() == maxAir) {
            this.impl$maxAir = null;
            ((SpongeDataHolderBridge) this).bridge$remove(Keys.MAX_AIR);
            return;
        }
        this.impl$maxAir = maxAir;
        ((SpongeDataHolderBridge) this).bridge$offer(Keys.MAX_AIR, maxAir);
    }

    @Inject(method = "getMaxAirSupply", at = @At("RETURN"), cancellable = true)
    private void impl$supplyMaxAir(CallbackInfoReturnable<Integer> ci) {
        if (this.impl$maxAir != null) {
            ci.setReturnValue(this.impl$maxAir);
        }
    }
}
