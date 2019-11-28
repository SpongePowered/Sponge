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
package org.spongepowered.common.mixin.api.mcp.entity.boss;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.PhaseManager;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.world.end.DragonFightManager;
import org.spongepowered.api.boss.ServerBossBar;
import org.spongepowered.api.entity.EnderCrystal;
import org.spongepowered.api.entity.living.complex.EnderDragon;
import org.spongepowered.api.entity.living.complex.EnderDragonPart;
import org.spongepowered.api.entity.living.complex.dragon.phase.EnderDragonPhaseManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.end.DragonFightManagerBridge;
import org.spongepowered.common.mixin.api.mcp.entity.EntityLivingMixin_API;

import java.util.Optional;
import java.util.Set;

@Mixin(EnderDragonEntity.class)
public abstract class EntityDragonMixin_API extends EntityLivingMixin_API implements EnderDragon {

    @Shadow public MultiPartEntityPart[] dragonPartArray;
    @Shadow public EnderCrystalEntity healingEnderCrystal;
    @Final @Shadow private DragonFightManager fightManager;
    @Shadow @Final private PhaseManager phaseManager;

    @Override
    public Set<EnderDragonPart> getParts() {
        Builder<EnderDragonPart> builder = ImmutableSet.builder();

        for (MultiPartEntityPart part : this.dragonPartArray) {
            builder.add((EnderDragonPart) part);
        }

        return builder.build();
    }

    @Override
    public Optional<EnderCrystal> getHealingCrystal() {
        return Optional.ofNullable((EnderCrystal) this.healingEnderCrystal);
    }


    @Override
    public ServerBossBar getBossBar() {
        return (ServerBossBar) ((DragonFightManagerBridge) this.fightManager).bridge$getBossInfo();
    }

    @Override
    public EnderDragonPhaseManager getPhaseManager() {
        return (EnderDragonPhaseManager) this.phaseManager;
    }
}
