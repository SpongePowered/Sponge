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
package org.spongepowered.common.mixin.core.world.end;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BossInfo;
import net.minecraft.world.ServerBossInfo;
import net.minecraft.world.end.DragonFightManager;
import net.minecraft.world.end.DragonSpawnState;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.end.DragonFightManagerBridge;
import org.spongepowered.common.event.tracking.context.GeneralizedContext;
import org.spongepowered.common.event.tracking.phase.world.dragon.DragonPhase;

import java.util.List;
import java.util.UUID;

@Mixin(DragonFightManager.class)
public abstract class DragonFightManagerMixin implements DragonFightManagerBridge {

    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private ServerBossInfo bossInfo;
    @Shadow @Final private ServerWorld world;
    @Shadow private int ticksSinceDragonSeen;
    @Shadow private int ticksSinceCrystalsScanned;
    @Shadow private int ticksSinceLastPlayerScan;
    @Shadow private boolean dragonKilled;
    @Shadow private boolean previouslyKilled;
    @Shadow private UUID dragonUniqueId;
    @Shadow private boolean scanForLegacyFight;
    @Shadow private BlockPos exitPortalLocation;
    @Shadow private DragonSpawnState respawnState;
    @Shadow private int respawnStateTicks;
    @Shadow private List<EnderCrystalEntity> crystals;

    @Shadow public abstract void respawnDragon();
    @Shadow private boolean hasDragonBeenKilled() {
        return false; // Shadowed
    }
    @Shadow private void updatePlayers() { }
    @Shadow private void findAliveCrystals() { }
    @Shadow private void loadChunks() { }
    @Shadow private void generatePortal(final boolean flag) { }
    @Shadow private EnderDragonEntity createNewDragon() {
        return null; // Shadowed
    }

    /**
     * @author gabizou - January 22nd, 2017
     * @reason Injects Sponge necessary phase state switches
     */
    @Overwrite
    public void tick() {
        this.bossInfo.func_186758_d(!this.dragonKilled);

        if (++this.ticksSinceLastPlayerScan >= 20) {
            this.updatePlayers();
            this.ticksSinceLastPlayerScan = 0;
        }

        if (!this.bossInfo.func_186757_c().isEmpty()) {
            if (this.scanForLegacyFight) {
                LOGGER.info("Scanning for legacy world dragon fight...");
                this.loadChunks();
                this.scanForLegacyFight = false;
                final boolean flag = this.hasDragonBeenKilled();

                if (flag) {
                    LOGGER.info("Found that the dragon has been killed in this world already.");
                    this.previouslyKilled = true;
                } else {
                    LOGGER.info("Found that the dragon has not yet been killed in this world.");
                    this.previouslyKilled = false;
                    this.generatePortal(false);
                }

                final List<EnderDragonEntity> list = this.world.func_175644_a(EnderDragonEntity.class, EntityPredicates.field_94557_a);

                if (list.isEmpty()) {
                    this.dragonKilled = true;
                } else {
                    final EnderDragonEntity entitydragon = list.get(0);
                    this.dragonUniqueId = entitydragon.func_110124_au();
                    LOGGER.info("Found that there\'s a dragon still alive ({})", entitydragon);
                    this.dragonKilled = false;

                    if (!flag) {
                        LOGGER.info("But we didn\'t have a portal, let\'s remove it.");
                        entitydragon.func_70106_y();
                        this.dragonUniqueId = null;
                    }
                }

                if (!this.previouslyKilled && this.dragonKilled) {
                    this.dragonKilled = false;
                }
            }

            if (this.respawnState != null) {
                if (this.crystals == null) {
                    this.respawnState = null;
                    this.respawnDragon();
                }

                // Sponge Start - Cause tracker - todo: do more logistical configuration of how this all works.
                try (final GeneralizedContext context = DragonPhase.State.RESPAWN_DRAGON.createPhaseContext()) {
                    context.buildAndSwitch();
                    // Sponge End
                    this.respawnState
                        .func_186079_a(this.world, (DragonFightManager) (Object) this, this.crystals, this.respawnStateTicks++, this.exitPortalLocation);
                }// Sponge - Complete cause tracker
            }

            if (!this.dragonKilled) {
                if (this.dragonUniqueId == null || ++this.ticksSinceDragonSeen >= 1200) {
                    this.loadChunks();
                    final List<EnderDragonEntity> list1 = this.world.func_175644_a(EnderDragonEntity.class, EntityPredicates.field_94557_a);

                    if (list1.isEmpty()) {
                        LOGGER.debug("Haven\'t seen the dragon, respawning it");
                        this.createNewDragon();
                    } else {
                        LOGGER.debug("Haven\'t seen our dragon, but found another one to use.");
                        this.dragonUniqueId = list1.get(0).func_110124_au();
                    }

                    this.ticksSinceDragonSeen = 0;
                }

                if (++this.ticksSinceCrystalsScanned >= 100) {
                    this.findAliveCrystals();
                    this.ticksSinceCrystalsScanned = 0;
                }
            }
        }
    }


    @Override
    public BossInfo bridge$getBossInfo() {
        return this.bossInfo;
    }
}
