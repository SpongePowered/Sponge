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

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.general.ExplosionContext;

import java.util.ArrayList;
import java.util.List;

class BlockTickPhaseState extends LocationBasedTickPhaseState<BlockTickContext> {

    private final String name;

    BlockTickPhaseState(String name) {
        this.name = name;
    }

    @Override
    public BlockTickContext createPhaseContext() {
        return new BlockTickContext(this)
                .addCaptures();
    }

    @Override
    LocatableBlock getLocatableBlockSourceFromContext(PhaseContext<?> context) {
        return context.getSource(LocatableBlock.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected to be ticking over at a location!", context));
    }

    @Override
    public void unwind(BlockTickContext context) {
        // TODO - Determine if we need to pass the supplier or perform some parameterized
        //  process if not empty method on the capture object.
        TrackingUtil.processBlockCaptures(context.getCapturedBlockSupplier(), this, context);
            context.getCapturedItemsSupplier()
                    .acceptAndClearIfNotEmpty(items -> {
                        Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
                        final ArrayList<Entity> capturedEntities = new ArrayList<>();
                        for (EntityItem entity : items) {
                            capturedEntities.add(EntityUtil.fromNative(entity));
                        }
                        SpongeCommonEventFactory.callSpawnEntity(capturedEntities, context);
                    });

    }

    @Override
    public void appendContextPreExplosion(ExplosionContext explosionContext, BlockTickContext context) {
        context.applyOwnerIfAvailable(explosionContext::owner);
        context.applyNotifierIfAvailable(explosionContext::notifier);
        final LocatableBlock locatableBlock = getLocatableBlockSourceFromContext(context);
        explosionContext.source(locatableBlock);
    }

    @Override
    public boolean spawnEntityOrCapture(BlockTickContext context, Entity entity, int chunkX, int chunkZ) {
        final LocatableBlock locatableBlock = getLocatableBlockSourceFromContext(context);
        if (!context.allowsEntityEvents() || !ShouldFire.SPAWN_ENTITY_EVENT) { // We don't want to throw an event if we don't need to.
            return EntityUtil.processEntitySpawn(entity, EntityUtil.ENTITY_CREATOR_FUNCTION.apply(context));
        }
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(locatableBlock);
            if (entity instanceof EntityXPOrb) {
                frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.EXPERIENCE);
                final ArrayList<org.spongepowered.api.entity.Entity> entities = new ArrayList<>(1);
                entities.add(entity);
                return SpongeCommonEventFactory.callSpawnEntity(entities, context);
            }
            final List<org.spongepowered.api.entity.Entity> nonExpEntities = new ArrayList<>(1);
            nonExpEntities.add(entity);
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.BLOCK_SPAWNING);
            return SpongeCommonEventFactory.callSpawnEntity(nonExpEntities, context);
        }
    }


    @Override
    public boolean doesCaptureEntitySpawns() {
        return false;
    }

    @Override
    public void postTrackBlock(BlockSnapshot snapshot, BlockTickContext context) {
        if (false) {
            TrackingUtil.processBlockCaptures(context.getCapturedBlockSupplier(), this, context);
            context.getCapturedBlockSupplier().prune(snapshot);
        }
    }

    /**
     * Specifically overridden here because some states have defaults and don't check the context.
     * @param context The context
     * @return True if bulk block captures are usable for this entity type (default true)
     */
    @Override
    public boolean doesBulkBlockCapture(BlockTickContext context) {
        return context.allowsBulkBlockCaptures();
    }

    /**
     * Specifically overridden here because some states have defaults and don't check the context.
     * @param context The context
     * @return True if block events are to be tracked by the specific type of entity (default is true)
     */
    @Override
    public boolean doesBlockEventTracking(BlockTickContext context) {
        return context.allowsBlockEvents();
    }

    @Override
    public boolean doesCaptureEntityDrops(BlockTickContext context) {
        return true; // Maybe make this configurable as well.
    }

    @Override
    public String toString() {
        return this.name;
    }

}
