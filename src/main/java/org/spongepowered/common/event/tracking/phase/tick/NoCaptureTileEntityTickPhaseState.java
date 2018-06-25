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
package org.spongepowered.common.event.tracking.phase.tick;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.interfaces.block.tile.IMixinTileEntity;
import org.spongepowered.common.world.BlockChange;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

class NoCaptureTileEntityTickPhaseState extends TileEntityTickPhaseState {

    NoCaptureTileEntityTickPhaseState(String name) {
        super(name);
    }

    @Override
    public boolean tracksBlockSpecificDrops() {
        return false;
    }

    @Override
    public boolean requiresBlockBulkCaptures() {
        return false;
    }

    @Override
    public boolean alreadyCapturingItemSpawns() {
        return true;
    }

    @Override
    public boolean alreadyCapturingEntitySpawns() {
        return true;
    }

    @Override
    public boolean doesCaptureEntityDrops() {
        return false;
    }

    @Override
    public boolean performOrCaptureItemDrop(TileEntityTickContext phaseContext, Entity entity, EntityItem entityitem) {
        return false;
    }

    /**
     * Since we're not bulk capturing, we're directly spawning entities and throwing events for it.
     * Also checking whether spawn events need to be thrown. If they don't need to be thrown, just
     * directly spawn the entity. Even without the event though, the notifiers/owners will still be
     * checked and applied to the spawned entity.
     *
     * @param context The context
     * @param entity The entity to spawn
     * @param chunkX The chunk x position
     * @param chunkZ The chunk y position
     * @return True if the entity is spawned
     */
    @Override
    public boolean spawnEntityOrCapture(TileEntityTickContext context, org.spongepowered.api.entity.Entity entity, int chunkX, int chunkZ) {
        final TileEntity tickingTile = context.getSource(TileEntity.class)
            .orElseThrow(TrackingUtil.throwWithContext("Not ticking on a TileEntity!", context));
        final IMixinTileEntity mixinTileEntity = (IMixinTileEntity) tickingTile;

        if (!ShouldFire.SPAWN_ENTITY_EVENT) { // We don't want to throw an event if we don't need to.
            return EntityUtil.processEntitySpawn(entity, EntityUtil.ENTITY_CREATOR_FUNCTION.apply(context));
        }
        // Separate experience from other entities
        if (entity instanceof EntityXPOrb) {
            try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                frame.pushCause(tickingTile.getLocatableBlock());
                frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.EXPERIENCE);
                context.addNotifierAndOwnerToCauseStack(frame);
                final ArrayList<org.spongepowered.api.entity.Entity> exp = new ArrayList<>(1);
                exp.add(entity);
                return SpongeCommonEventFactory.callSpawnEntity(exp, context);
            }
        }
        final List<org.spongepowered.api.entity.Entity> nonExpEntities = new ArrayList<>(1);
        nonExpEntities.add(entity);
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(tickingTile.getLocatableBlock());
            frame.addContext(EventContextKeys.SPAWN_TYPE, mixinTileEntity.getTickedSpawnType());
            context.addNotifierAndOwnerToCauseStack(frame);
            return SpongeCommonEventFactory.callSpawnEntity(nonExpEntities, context);

        }
    }

    @Override
    public boolean requiresPost() {
        return false;
    }

    @Override
    public void unwind(TileEntityTickContext context) {
        // We didn't capture anything, so there's nothing to do here
    }
}
