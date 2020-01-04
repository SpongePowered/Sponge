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
package org.spongepowered.common.mixin.api.mcp.world.raid;

import net.minecraft.entity.monster.AbstractRaiderEntity;
import net.minecraft.world.ServerBossInfo;
import net.minecraft.world.World;
import net.minecraft.world.raid.Raid;
import org.spongepowered.api.boss.ServerBossBar;
import org.spongepowered.api.data.type.RaidStatus;
import org.spongepowered.api.raid.Wave;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.raid.RaidBridge;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

@Mixin(Raid.class)
public abstract class RaidMixin_API implements org.spongepowered.api.raid.Raid {

    @Shadow public abstract World shadow$getWorld();
    @Shadow public abstract float shadow$getCurrentHealth();
    @Shadow public abstract int shadow$func_221315_l();

    @Shadow @Final public Map<Integer, Set<AbstractRaiderEntity>> raiders;
    @Shadow @Final @Mutable private ServerBossInfo bossInfo;
    @Shadow private Raid.Status status;

    @Override
    public ServerWorld getWorld() {
        return (ServerWorld) this.shadow$getWorld();
    }

    @Override
    public ServerBossBar getBossBar() {
        return (ServerBossBar) this.bossInfo;
    }

    @Override
    public void setBossBar(ServerBossBar bossBar) {
        checkNotNull(bossBar, "BossBar cannot be null.");
        this.bossInfo = (ServerBossInfo) bossBar;
    }

    @Override
    public RaidStatus getStatus() {
        return (RaidStatus) (Object) this.status;
    }

    @Override
    public Optional<Wave> getCurrentWave() {
        return Optional.ofNullable(((RaidBridge) this).bridge$getWaves().get(this.shadow$func_221315_l()));
    }

    @Override
    public List<Wave> getWaves() {
        return new ArrayList<>(((RaidBridge) this).bridge$getWaves().values());
    }

    @Override
    public int getTotalWaveAmount() {
        return this.raiders.size();
    }

    @Override
    public double getHealth() {
        return this.shadow$getCurrentHealth();
    }
}
