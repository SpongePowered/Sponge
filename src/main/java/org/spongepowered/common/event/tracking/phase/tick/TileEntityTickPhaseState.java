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

import com.google.common.collect.ListMultimap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.BlockUtil;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.context.BlockTransaction;
import org.spongepowered.common.event.tracking.phase.general.ExplosionContext;
import org.spongepowered.common.interfaces.block.tile.IMixinTileEntity;
import org.spongepowered.common.interfaces.server.management.IMixinPlayerChunkMapEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

class TileEntityTickPhaseState extends LocationBasedTickPhaseState<TileEntityTickContext> {
    private final BiConsumer<StackFrame, TileEntityTickContext> TILE_ENTITY_MODIFIER =
        super.getFrameModifier().andThen((frame, context) ->
            context.getSource(TileEntity.class)
                .ifPresent(frame::pushCause)
        );

    private String name;

    TileEntityTickPhaseState(String name) {
        this.name = name;
    }

    @Override
    public TileEntityTickContext createPhaseContext() {
        return new TileEntityTickContext(this)
                .addEntityCaptures()
                .addEntityDropCaptures()
                .addBlockCaptures();
    }

    @Override
    public BiConsumer<StackFrame, TileEntityTickContext> getFrameModifier() {
        return this.TILE_ENTITY_MODIFIER;
    }

    @Override
    public boolean tracksEntitySpecificDrops() {
        return true;
    }

    @Override
    public boolean doesCaptureEntityDrops(TileEntityTickContext context) {
        return true;
    }

    @Override
    LocatableBlock getLocatableBlockSourceFromContext(PhaseContext<?> context) {
        return context.getSource(TileEntity.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected to be ticking over a TileEntity!", context))
                .getLocatableBlock();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void unwind(TileEntityTickContext context) {
        final TileEntity tickingTile = context.getSource(TileEntity.class)
                .orElseThrow(TrackingUtil.throwWithContext("Not ticking on a TileEntity!", context));
        try (StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {

            // TODO - Determine if we need to pass the supplier or perform some parameterized
            //  process if not empty method on the capture object.
            TrackingUtil.processBlockCaptures(this, context);
            frame.pushCause(tickingTile.getLocatableBlock());
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.BLOCK_SPAWNING);
            context.getCapturedItemsSupplier()
                    .acceptAndClearIfNotEmpty(entities -> {
                        final ArrayList<Entity> capturedEntities = new ArrayList<>();
                        for (EntityItem entity : entities) {
                            capturedEntities.add(EntityUtil.fromNative(entity));
                        }
                        SpongeCommonEventFactory.callSpawnEntity(capturedEntities, context);
                    });
            // Unwind the spawn type for the tile entity. because occasionaly, we have to handle
            // when mods interact with "internalized" entities within the TileEntity such that
            // the entity can be "used" to spawn items that are specifically captured to the entity
            // to multiple drops mapping.
            frame.removeContext(EventContextKeys.SPAWN_TYPE);
            context.getPerEntityItemEntityDropSupplier()
                .acceptAndClearIfNotEmpty((id, item) -> {
                    frame.popCause();
                    final List<Entity> entities = new ArrayList<>();
                    entities.add(EntityUtil.fromNative(item));
                    final Optional<Entity> entity = tickingTile.getWorld().getEntity(id);
                    if (!entity.isPresent()) {
                        // Means that the tile entity is spawning an entity from another entity that doesn't exist
                        // in the world in normal circumstances. Because this is only achievalbe through mods,
                        // we have to spawn the entities anyways, in the event the mod is expecting those drops to
                        // be spawned regardless (because their entrance is through Entity#entityDropItem, which we
                        // reroute to captures)
                        frame.pushCause(tickingTile); // We only have the tile entity to consider
                        frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.BLOCK_SPAWNING);
                        SpongeCommonEventFactory.callSpawnEntity(entities, context);
                        return;
                    }
                    frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);
                    // Now we can actually push the entity onto the stack, and then push the tile entity onto the stack as well.
                    final Entity nestedEntity = entity.get();
                    frame.pushCause(nestedEntity);
                    frame.pushCause(tickingTile);
                    SpongeCommonEventFactory.callSpawnEntityCustom(entities, context);
                    // Now to clean up the list that is tied to the entity, so that this phase context isn't continuously wrapped
                    EntityUtil.toMixin(nestedEntity).clearWrappedCaptureList();
                });
        }
    }

    @Override
    public boolean tracksTileEntityChanges(TileEntityTickContext currentContext) {
        return this.doesBulkBlockCapture(currentContext);
    }

    @Override
    public void appendContextPreExplosion(ExplosionContext explosionContext, TileEntityTickContext context) {
        context.applyNotifierIfAvailable(explosionContext::notifier);
        context.applyOwnerIfAvailable(explosionContext::owner);
        final TileEntity tickingTile = context.getSource(TileEntity.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected to be processing over a ticking TileEntity!", context));
        explosionContext.source(tickingTile);
    }

    @Override
    public boolean spawnEntityOrCapture(TileEntityTickContext context, Entity entity, int chunkX, int chunkZ) {
        final TileEntity tickingTile = context.getSource(TileEntity.class)
            .orElseThrow(TrackingUtil.throwWithContext("Not ticking on a TileEntity!", context));
        final IMixinTileEntity mixinTileEntity = (IMixinTileEntity) tickingTile;

        // If we do allow events, but there are no event listeners, just spawn.
        // Otherwise, if we forbid events, we want to spawn anyways, don't throw an event.
        if (!context.allowsEntityEvents() || !ShouldFire.SPAWN_ENTITY_EVENT) { // We don't want to throw an event if we don't need to.
            return EntityUtil.processEntitySpawn(entity, EntityUtil.ENTITY_CREATOR_FUNCTION.apply(context));
        }
        // Separate experience from other entities
        if (entity instanceof EntityXPOrb) {
            try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                frame.pushCause(tickingTile.getLocatableBlock());
                frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.EXPERIENCE);
                context.addNotifierAndOwnerToCauseStack(frame);
                final ArrayList<Entity> exp = new ArrayList<>();
                exp.add(entity);
                return SpongeCommonEventFactory.callSpawnEntity(exp, context);
            }
        }
        final List<Entity> nonExpEntities = new ArrayList<>(1);
        nonExpEntities.add(entity);
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(tickingTile.getLocatableBlock());
            frame.addContext(EventContextKeys.SPAWN_TYPE, mixinTileEntity.getTickedSpawnType());
            context.addNotifierAndOwnerToCauseStack(frame);
            return SpongeCommonEventFactory.callSpawnEntity(nonExpEntities, context);

        }
    }

    @Override
    public boolean doesCaptureEntitySpawns() {
        return false;
    }

    @Override
    public boolean getShouldCancelAllTransactions(TileEntityTickContext context, List<ChangeBlockEvent> blockEvents, ChangeBlockEvent.Post postEvent,
        ListMultimap<BlockPos, BlockEventData> scheduledEvents, boolean noCancelledTransactions) {
        if (!postEvent.getTransactions().isEmpty()) {
            return postEvent.getTransactions().stream().anyMatch(transaction -> {
                final BlockState state = transaction.getOriginal().getState();
                final BlockType type = state.getType();
                final boolean hasTile = SpongeImplHooks.hasBlockTileEntity((Block) type, BlockUtil.toNative(state));
                final BlockPos pos = context.getSource(net.minecraft.tileentity.TileEntity.class).get().getPos();
                final BlockPos blockPos = ((SpongeBlockSnapshot) transaction.getOriginal()).getBlockPos();
                if (pos.equals(blockPos) && !transaction.isValid()) {
                    return true;
                }
                if (!hasTile && !transaction.getIntermediary().isEmpty()) { // Check intermediary
                    return transaction.getIntermediary().stream().anyMatch(inter -> {
                        final BlockState iterState = inter.getState();
                        final BlockType interType = state.getType();
                        return SpongeImplHooks.hasBlockTileEntity((Block) interType, BlockUtil.toNative(iterState));
                    });
                }
                return hasTile;
            });
        }
        return false;
    }

    @Override
    public boolean doesCaptureNeighborNotifications(TileEntityTickContext context) {
        return context.allowsBulkBlockCaptures();
    }

    @Override
    public boolean doesBulkBlockCapture(TileEntityTickContext context) {
        return context.allowsBulkBlockCaptures();
    }

    @Override
    public boolean doesBlockEventTracking(TileEntityTickContext context) {
        return context.allowsBlockEvents();
    }

    @Override
    public boolean hasSpecificBlockProcess(TileEntityTickContext context) {
        return true;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
