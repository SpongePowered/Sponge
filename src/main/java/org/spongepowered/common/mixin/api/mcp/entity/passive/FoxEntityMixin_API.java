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
package org.spongepowered.common.mixin.api.mcp.entity.passive;

import net.minecraft.entity.passive.FoxEntity;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.living.animal.Fox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.mixin.accessor.entity.passive.FoxEntityAccessor;

import java.util.Set;
import java.util.UUID;

@Mixin(FoxEntity.class)
public abstract class FoxEntityMixin_API extends AnimalEntityMixin_API implements Fox {

    @Shadow protected abstract boolean shadow$isTrustedUUID(UUID p_213468_1_);

    @Override
    public boolean trusts(UUID uniqueId) {
        return this.shadow$isTrustedUUID(uniqueId);
    }

    @Override
    protected Set<Value.Immutable<?>> api$getVanillaValues() {
        final Set<Value.Immutable<?>> values = super.api$getVanillaValues();

        values.add(this.type().asImmutable());
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
