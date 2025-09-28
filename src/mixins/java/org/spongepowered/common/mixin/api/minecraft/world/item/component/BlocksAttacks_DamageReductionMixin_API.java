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

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.component.BlocksAttacks;
import org.spongepowered.api.data.type.ShieldDamageReduction;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.asm.mixin.*;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(BlocksAttacks.DamageReduction.class)
@Implements({
    @Interface(iface = ShieldDamageReduction.class, prefix = "shielddamagereduction$"),
    @Interface(iface = ShieldDamageReduction.MultiplyAdd.class, prefix = "shielddamagereductionmultiplyadd$")
})
public abstract class BlocksAttacks_DamageReductionMixin_API implements ShieldDamageReduction<ShieldDamageReduction.MultiplyAdd>, ShieldDamageReduction.MultiplyAdd {

    @Shadow @Final private Optional<HolderSet<net.minecraft.world.damagesource.DamageType>> type;
    @Shadow @Final private float base;
    @Shadow @Final private float factor;
    @Shadow @Final private float horizontalBlockingAngle;

    @Shadow public abstract float shadow$resolve(net.minecraft.world.damagesource.DamageSource $$0, float $$1, double $$2);

    @Override
    public MultiplyAdd configuration() {
        return this;
    }

    public double shielddamagereduction$resolve(final DamageSource source, final double damage, final double angle) {
        return this.shadow$resolve((net.minecraft.world.damagesource.DamageSource) source, (float) damage, angle);
    }

    @Override
    public Optional<Set<DamageType>> damageTypes() {
        return this.type.map(set -> set.stream()
            .map(Holder::value)
            .map(DamageType.class::cast)
            .collect(Collectors.toSet()));
    }

    public double shielddamagereductionmultiplyadd$horizontalBlockingAngle() {
        return this.horizontalBlockingAngle;
    }

    @Override
    public double constantReduction() {
        return this.base;
    }

    @Override
    public double fractionalReduction() {
        return this.factor;
    }

}
