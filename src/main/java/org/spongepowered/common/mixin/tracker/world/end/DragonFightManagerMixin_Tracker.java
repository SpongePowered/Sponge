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
package org.spongepowered.common.mixin.tracker.world.end;

import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.end.DragonFightManager;
import net.minecraft.world.end.DragonSpawnState;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.context.GeneralizedContext;
import org.spongepowered.common.event.tracking.phase.world.dragon.DragonPhase;

import java.util.List;

@Mixin(DragonFightManager.class)
public abstract class DragonFightManagerMixin_Tracker {

    /**
     * @author i509vcb - February 6th 2020
     * @reason Add sponge necessary phase state switches
     *
     * @param dragonSpawnState The dragon spawn state.
     * @param worldIn The world this respawnState is occuring in.
     * @param manager The current DragonFightManager.
     * @param crystals List of all currently present end crystals.
     * @param respawnStateTicks The amount of this this respawn state has been running for.
     * @param exitPortalLocation The position of the exit portal.
     */
    @Redirect(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/end/DragonSpawnState;process(Lnet/minecraft/world/server/ServerWorld;Lnet/minecraft/world/end/DragonFightManager;Ljava/util/List;ILnet/minecraft/util/math/BlockPos;)V"))
    private void tracker$wrapSpawnStateWithPhaseEntry(DragonSpawnState dragonSpawnState, ServerWorld worldIn, DragonFightManager manager, List<EnderCrystalEntity> crystals, int respawnStateTicks, BlockPos exitPortalLocation) {
        try (final GeneralizedContext context = DragonPhase.State.RESPAWN_DRAGON.createPhaseContext(PhaseTracker.SERVER)) {
            context.buildAndSwitch();
            ++respawnStateTicks;
            dragonSpawnState.process(worldIn, manager, crystals, respawnStateTicks, exitPortalLocation);
        }
    }
}
