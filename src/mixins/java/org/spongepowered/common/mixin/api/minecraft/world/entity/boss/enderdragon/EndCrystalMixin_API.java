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
package org.spongepowered.common.mixin.api.minecraft.world.entity.boss.enderdragon;

import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.explosive.EndCrystal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.world.entity.boss.enderdragon.EndCrystalBridge;
import org.spongepowered.common.mixin.api.minecraft.world.entity.EntityMixin_API;
import java.util.Set;

@Mixin(net.minecraft.world.entity.boss.enderdragon.EndCrystal.class)
public abstract class EndCrystalMixin_API extends EntityMixin_API implements EndCrystal {

    @Override
    public void detonate() {
        this.shadow$remove();
        ((EndCrystalBridge) this).bridge$throwExplosionEventAndExplode(this.shadow$getCommandSenderWorld(), null, this.shadow$getX(),
                this.shadow$getY(), this.shadow$getZ(), true, null);
    }

    @Override
    protected Set<Value.Immutable<?>> api$getVanillaValues() {
        final Set<Value.Immutable<?>> values = super.api$getVanillaValues();

        // Explosive
        this.explosionRadius().map(Value::asImmutable).ifPresent(values::add);

        values.add(this.showBottom().asImmutable());

        this.beamTarget().map(Value::asImmutable).ifPresent(values::add);

        return values;
    }
}
