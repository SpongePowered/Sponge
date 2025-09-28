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
package org.spongepowered.common.item.shield;

import net.minecraft.world.item.component.BlocksAttacks;
import org.spongepowered.api.data.type.ShieldItemDamageFunction;
import org.spongepowered.common.util.Preconditions;

public final class SpongeShieldItemDamageFunctionMultiplyAddBuilder implements ShieldItemDamageFunction.MultiplyAdd.Builder {
    private double minAttackDamage = 0;
    private double constantDamage = 0;
    private double fractionalDamage = 0;

    @Override
    public ShieldItemDamageFunction.MultiplyAdd.Builder minAttackDamage(final double minDamage) {
        Preconditions.checkArgument(minDamage >= 0, "minAttackDamage must not be negative");
        this.minAttackDamage = minDamage;

        return this;
    }

    @Override
    public ShieldItemDamageFunction.MultiplyAdd.Builder constantDamage(final double constantDamage) {
        this.constantDamage = constantDamage;

        return this;
    }

    @Override
    public ShieldItemDamageFunction.MultiplyAdd.Builder fractionalDamage(final double fractionalDamage) {
        this.fractionalDamage = fractionalDamage;

        return this;
    }

    @Override
    public ShieldItemDamageFunction.MultiplyAdd build() {
        return (ShieldItemDamageFunction.MultiplyAdd) (Object) new BlocksAttacks.ItemDamageFunction(
            (float) this.minAttackDamage,
            (float) this.constantDamage,
            (float) this.fractionalDamage
        );
    }

    @Override
    public ShieldItemDamageFunction.MultiplyAdd.Builder reset() {
        this.minAttackDamage = 0;
        this.constantDamage = 0;
        this.fractionalDamage = 0;

        return this;
    }

}
