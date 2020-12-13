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

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.end.DragonFightManager;
import net.minecraft.world.end.DragonSpawnState;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.generation.FeaturePhaseContext;
import org.spongepowered.common.event.tracking.phase.generation.GenerationPhase;
import org.spongepowered.common.event.tracking.phase.world.dragon.DragonPhase;
import org.spongepowered.common.event.tracking.phase.world.dragon.SpawnDragonContext;

import java.util.List;
import java.util.Random;

@Mixin(DragonFightManager.class)
public abstract class DragonFightManagerMixin_Tracker {

    // @formatter:off
    @Shadow protected abstract EnderDragonEntity shadow$createNewDragon();
    // @formatter:on

    @Redirect(method = "spawnNewGateway(Lnet/minecraft/util/math/BlockPos;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/feature/ConfiguredFeature;place(Lnet/minecraft/world/ISeedReader;Lnet/minecraft/world/gen/ChunkGenerator;Ljava/util/Random;Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean tracker$switchToFeatureState(final ConfiguredFeature configuredFeature, final ISeedReader worldIn, final ChunkGenerator generator,
        final Random rand,
        final BlockPos pos
    ) {

        try (final FeaturePhaseContext context = GenerationPhase.State.FEATURE_PLACEMENT.createPhaseContext(PhaseTracker.SERVER)) {
            context
                    .world((ServerWorld) worldIn)
                    .generator(generator)
                    .feature(configuredFeature.feature)
                    .origin(pos)
            ;
            context.buildAndSwitch();

            return configuredFeature.place(worldIn, generator, rand, pos);
        }
    }

    @Redirect(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/end/DragonSpawnState;tick(Lnet/minecraft/world/server/ServerWorld;Lnet/minecraft/world/end/DragonFightManager;Ljava/util/List;ILnet/minecraft/util/math/BlockPos;)V"))
    private void tracker$switchToSpawnDragonState(final DragonSpawnState dragonSpawnState, final ServerWorld worldIn,
            final DragonFightManager manager, final List<EnderCrystalEntity> crystals, int respawnStateTicks, final BlockPos exitPortalLocation) {
        try (final SpawnDragonContext context = DragonPhase.State.SPAWN_DRAGON.createPhaseContext(PhaseTracker.SERVER)) {
            context
                    .manager(manager)
                    .setIsRespawn(true)
                    .buildAndSwitch()
            ;
            ++respawnStateTicks;
            dragonSpawnState.tick(worldIn, manager, crystals, respawnStateTicks, exitPortalLocation);
        }
    }

    @Redirect(method = "findOrCreateDragon", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/end/DragonFightManager;createNewDragon()Lnet/minecraft/entity/boss/dragon/EnderDragonEntity;"))
    private EnderDragonEntity tracker$switchToSpawnDragonState(final DragonFightManager manager) {
        try (final SpawnDragonContext context = DragonPhase.State.SPAWN_DRAGON.createPhaseContext(PhaseTracker.SERVER)) {
            context
                    .manager(manager)
                    .setIsRespawn(false)
                    .buildAndSwitch()
            ;

            return this.shadow$createNewDragon();
        }
    }
}
