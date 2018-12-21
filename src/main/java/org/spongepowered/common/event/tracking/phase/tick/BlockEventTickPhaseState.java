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

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.CauseStackManager.StackFrame;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.BlockChange;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

class BlockEventTickPhaseState extends TickPhaseState<BlockEventTickContext> {

    BlockEventTickPhaseState() {
    }

    @Override
    public BlockEventTickContext createPhaseContext() {
        return new BlockEventTickContext()
                .addBlockCaptures()
                .addEntityCaptures();
    }

    @Override
    public void associateNeighborStateNotifier(BlockEventTickContext context, @Nullable BlockPos sourcePos, Block block, BlockPos notifyPos,
                                               WorldServer minecraftWorld, PlayerTracker.Type notifier) {
        // If we do not have a notifier at this point then there is no need to attempt to retrieve one from the chunk
        context.applyNotifierIfAvailable(user -> {
            final IMixinChunk mixinChunk = (IMixinChunk) minecraftWorld.getChunk(notifyPos);
            mixinChunk.addTrackedBlockPosition(block, notifyPos, user, PlayerTracker.Type.NOTIFIER);
        });
    }

    @Override
    public boolean spawnEntityOrCapture(BlockEventTickContext context, Entity entity, int chunkX, int chunkZ) {
        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.CUSTOM);

            final List<Entity> entities = new ArrayList<>(1);
            entities.add(entity);
            return SpongeCommonEventFactory.callSpawnEntity(entities, context);
        }
    }

    @Override
    public boolean doesCaptureEntitySpawns() {
        return false;
    }

    @Override
    public void postBlockTransactionApplication(BlockChange blockChange,
        Transaction<BlockSnapshot> snapshotTransaction, BlockEventTickContext context) {
        final Block block = (Block) snapshotTransaction.getOriginal().getState().getType();
        final Location<World> changedLocation = snapshotTransaction.getOriginal().getLocation().get();
        final Vector3d changedPosition = changedLocation.getPosition();
        final BlockPos changedBlockPos = VecHelper.toBlockPos(changedPosition);
        final IMixinChunk changedMixinChunk = (IMixinChunk) ((WorldServer) changedLocation.getExtent()).getChunk(changedBlockPos);
        changedMixinChunk.getBlockOwner(changedBlockPos)
                .ifPresent(owner -> changedMixinChunk.addTrackedBlockPosition(block, changedBlockPos, owner, PlayerTracker.Type.OWNER));
        final User user = TrackingUtil.getNotifierOrOwnerFromBlock(changedLocation);
        if (user != null) {
            changedMixinChunk.addTrackedBlockPosition(block, changedBlockPos, user, PlayerTracker.Type.NOTIFIER);
        }
    }

    @Override
    public void unwind(BlockEventTickContext context) {
        try (StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.CUSTOM);
            // TODO - Determine if we need to pass the supplier or perform some parameterized
            //  process if not empty method on the capture object.
            TrackingUtil.processBlockCaptures(context.getCapturedBlockSupplier(), this, context);
            context.getCapturedItemsSupplier()
                    .acceptAndClearIfNotEmpty(items -> {
                        final ArrayList<Entity> capturedEntities = new ArrayList<>();
                        for (EntityItem entity : items) {
                            capturedEntities.add(EntityUtil.fromNative(entity));
                        }
                        SpongeCommonEventFactory.callSpawnEntity(capturedEntities, context);
                    });
        }
    }

    @Override
    public String toString() {
        return "BlockEventTickPhase";
    }
}
