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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.living.monster.boss.dragon.EnderDragon;
import org.spongepowered.api.entity.living.monster.boss.dragon.EnderDragonPart;
import org.spongepowered.api.entity.living.monster.boss.dragon.phase.DragonPhaseManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.mixin.api.minecraft.world.entity.MobMixin_API;
import java.util.Set;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhaseManager;

@Mixin(net.minecraft.world.entity.boss.enderdragon.EnderDragon.class)
public abstract class EnderDragonMixin_API extends MobMixin_API implements EnderDragon {

    // @formatter:off
    @Shadow @Final private net.minecraft.world.entity.boss.EnderDragonPart[] subEntities;
    @Shadow @Final private EnderDragonPhaseManager phaseManager;
    // @formatter:on

    @Override
    public Set<EnderDragonPart> parts() {
        Builder<EnderDragonPart> builder = ImmutableSet.builder();

        for (net.minecraft.world.entity.boss.EnderDragonPart part : this.subEntities) {
            builder.add((EnderDragonPart) part);
        }

        return builder.build();
    }

    @Override
    public DragonPhaseManager phaseManager() {
        return (DragonPhaseManager) this.phaseManager;
    }

    @Override
    protected Set<Value.Immutable<?>> api$getVanillaValues() {
        final Set<Value.Immutable<?>> values = super.api$getVanillaValues();

        // Boss
        values.add(this.bossBar().asImmutable());

        this.healingCrystal().map(Value::asImmutable).ifPresent(values::add);

        return values;
    }

}
