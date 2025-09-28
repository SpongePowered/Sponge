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

import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.world.item.component.BlocksAttacks;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.type.ShieldDamageReduction;
import org.spongepowered.api.event.cause.entity.damage.DamageType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.tag.Tag;
import org.spongepowered.common.bridge.tags.TagBridge;
import org.spongepowered.common.util.Preconditions;

import java.util.Optional;
import java.util.Set;

public final class SpongeShieldDamageReductionMultiplyAddBuilder implements ShieldDamageReduction.MultiplyAdd.Builder {
    private HolderSet<net.minecraft.world.damagesource.DamageType> damageTypes;
    private Double horizontalBlockingAngle;
    private double base = 0;
    private double factor = 0;

    @Override
    public ShieldDamageReduction.MultiplyAdd.Builder damageTypes(final Set<DamageType> damageTypes) {
        final Registry<net.minecraft.world.damagesource.DamageType> registry = (Registry<net.minecraft.world.damagesource.DamageType>) Sponge.server().registry(RegistryTypes.DAMAGE_TYPE);

        this.damageTypes = HolderSet.direct(damageTypes.stream()
            .map(dt -> registry.wrapAsHolder((net.minecraft.world.damagesource.DamageType) (Object) dt))
            .toList());

        return this;
    }

    @Override
    public ShieldDamageReduction.MultiplyAdd.Builder damageTypes(final Tag<DamageType> tag) {
        final Registry<net.minecraft.world.damagesource.DamageType> registry = (Registry<net.minecraft.world.damagesource.DamageType>) Sponge.server().registry(RegistryTypes.DAMAGE_TYPE);
        final var vanillaTag = ((TagBridge<net.minecraft.world.damagesource.DamageType>) tag).bridge$asVanillaTag();
        this.damageTypes = registry.getOrThrow(vanillaTag);

        return this;
    }

    @Override
    public ShieldDamageReduction.MultiplyAdd.Builder horizontalBlockingAngle(final double angle) {
        Preconditions.checkArgument(angle > 0, "angle must be positive");
        this.horizontalBlockingAngle = angle;

        return this;
    }

    @Override
    public ShieldDamageReduction.MultiplyAdd.Builder constantReduction(final double constant) {
        this.base = constant;

        return this;
    }

    @Override
    public ShieldDamageReduction.MultiplyAdd.Builder fractionalReduction(final double fraction) {
        this.factor = fraction;

        return this;
    }

    @Override
    public ShieldDamageReduction.MultiplyAdd build() {
        return (ShieldDamageReduction.MultiplyAdd) (Object) new BlocksAttacks.DamageReduction(
            this.horizontalBlockingAngle != null ? this.horizontalBlockingAngle.floatValue() : 90,
            Optional.ofNullable(this.damageTypes),
            (float) this.base,
            (float) this.factor
        );
    }

    @Override
    public ShieldDamageReduction.MultiplyAdd.Builder reset() {
        this.damageTypes = null;
        this.horizontalBlockingAngle = null;
        this.base = 0;
        this.factor = 0;

        return this;
    }

}
