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
package org.spongepowered.common.mixin.api.minecraft.world.entity.animal;

import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.living.animal.Fox;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;
import java.util.UUID;

@Mixin(net.minecraft.world.entity.animal.Fox.class)
@Implements(@Interface(iface = Fox.class, prefix = "fox$"))
public abstract class FoxMixin_API extends AnimalMixin_API implements Fox {

    // @formatter:off
    @Shadow protected abstract boolean shadow$trusts(UUID p_213468_1_);
    // @formatter:on

    @Intrinsic
    public boolean fox$trusts(UUID uniqueId) {
        return this.shadow$trusts(uniqueId);
    }

    @Override
    protected Set<Value.Immutable<?>> api$getVanillaValues() {
        final Set<Value.Immutable<?>> values = super.api$getVanillaValues();

        values.add(this.foxType().asImmutable());
        values.add(this.sitting().asImmutable());
        values.add(this.faceplanted().asImmutable());
        values.add(this.defending().asImmutable());
        values.add(this.sleeping().asImmutable());
        values.add(this.pouncing().asImmutable());
        values.add(this.crouching().asImmutable());
        values.add(this.interested().asImmutable());

        this.firstTrusted().map(Value::asImmutable).ifPresent(values::add);
        this.secondTrusted().map(Value::asImmutable).ifPresent(values::add);

        return values;
    }

}
