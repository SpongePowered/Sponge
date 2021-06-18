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
package org.spongepowered.common.mixin.api.minecraft.world.entity.vehicle;

import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.vehicle.minecart.TNTMinecart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.explosives.FusedExplosiveBridge;

import java.util.Set;
import net.minecraft.world.entity.vehicle.MinecartTNT;

@Mixin(MinecartTNT.class)
public abstract class MinecartTNTMixin_API extends AbstractMinecartMixin_API implements TNTMinecart {

    @Override
    public void detonate() {
        ((FusedExplosiveBridge) this).bridge$setFuseTicksRemaining(0);
    }

    @Override
    protected Set<Value.Immutable<?>> api$getVanillaValues() {
        final Set<Value.Immutable<?>> values = super.api$getVanillaValues();

        // BlockOccupiedMinecart
        values.add(this.block().asImmutable());

        // FusedExplosive
        values.add(this.primed().asImmutable());
        values.add(this.fuseDuration().asImmutable());

        // Explosive
        this.explosionRadius().map(Value::asImmutable).ifPresent(values::add);

        return values;
    }

}
