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
package org.spongepowered.common.mixin.api.minecraft.world.item.component;

import net.minecraft.world.item.component.BlocksAttacks;
import org.spongepowered.api.data.type.ShieldItemDamageFunction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlocksAttacks.ItemDamageFunction.class)
public abstract class BlocksAttacks_ItemDamageFunction_API implements ShieldItemDamageFunction.MultiplyAdd, ShieldItemDamageFunction<ShieldItemDamageFunction.MultiplyAdd> {

    @Shadow @Final private float threshold;
    @Shadow @Final private float base;
    @Shadow @Final private float factor;

    @Shadow public abstract int shadow$apply(float $$0);

    @Override
    public MultiplyAdd configuration() {
        return this;
    }

    @Override
    public double resolve(final double damage) {
        return this.shadow$apply((float) damage);
    }

    @Override
    public double minAttackDamage() {
        return this.threshold;
    }

    @Override
    public double constantDamage() {
        return this.base;
    }

    @Override
    public double fractionalDamage() {
        return this.factor;
    }

}
