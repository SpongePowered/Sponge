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
package org.spongepowered.common.mixin.api.minecraft.world.entity.raid;

import net.kyori.adventure.bossbar.BossBar;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;
import org.spongepowered.api.data.type.RaidStatus;
import org.spongepowered.api.raid.RaidWave;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.world.entity.raid.RaidBridge;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

@Mixin(Raid.class)
public abstract class RaidMixin_API implements org.spongepowered.api.raid.Raid {

    //@formatter:off
    @Shadow @Final private Map<Integer, Set<Raider>> groupRaiderMap;
    @Shadow @Final @Mutable private ServerBossEvent raidEvent;
    @Shadow private Raid.RaidStatus status;

    @Shadow public abstract Level shadow$getLevel();
    @Shadow public abstract float shadow$getHealthOfLivingRaiders();
    @Shadow public abstract int shadow$getGroupsSpawned();
    //@formatter:on

    @Override
    public ServerWorld world() {
        return (ServerWorld) this.shadow$getLevel();
    }

    @Override
    public BossBar bossBar() {
        return SpongeAdventure.asAdventure(this.raidEvent);
    }

    @Override
    public void setBossBar(final BossBar bossBar) {
        checkNotNull(bossBar, "BossBar cannot be null.");
        this.raidEvent = SpongeAdventure.asVanillaServer(bossBar);
    }

    @Override
    public RaidStatus status() {
        return (RaidStatus) (Object) this.status;
    }

    @Override
    public Optional<RaidWave> currentWave() {
        return Optional.ofNullable(((RaidBridge) this).bridge$getWaves().get(this.shadow$getGroupsSpawned()));
    }

    @Override
    public List<RaidWave> waves() {
        return new ArrayList<>(((RaidBridge) this).bridge$getWaves().values());
    }

    @Override
    public int totalWaveAmount() {
        return this.groupRaiderMap.size();
    }

    @Override
    public double health() {
        return this.shadow$getHealthOfLivingRaiders();
    }
}
