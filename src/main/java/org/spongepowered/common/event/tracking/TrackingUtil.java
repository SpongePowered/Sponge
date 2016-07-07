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
package org.spongepowered.common.event.tracking;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import co.aikar.timings.Timing;
import com.flowpowered.math.vector.Vector3d;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.util.Identifiable;
import org.spongepowered.api.world.Location;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.phase.BlockPhase;
import org.spongepowered.common.event.tracking.phase.WorldPhase;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.block.IMixinBlockEventData;
import org.spongepowered.common.interfaces.block.tile.IMixinTileEntity;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.BlockChange;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A simple utility for aiding in tracking, either with resolving notifiers
 * and owners, or proxying out the logic for ticking a block, entity, etc.
 */
public final class TrackingUtil {

    public static void tickEntity(CauseTracker causeTracker, net.minecraft.entity.Entity entityIn) {
        checkArgument(entityIn instanceof Entity, "Entity %s is not an instance of SpongeAPI's Entity!", entityIn);
        checkNotNull(entityIn, "Cannot capture on a null ticking entity!");
        causeTracker.switchToPhase(WorldPhase.Tick.ENTITY, PhaseContext.start()
                .add(NamedCause.source(entityIn))
                .addEntityCaptures()
                .addBlockCaptures()
                .complete());
        final Timing entityTiming = EntityUtil.toMixin(entityIn).getTimingsHandler();
        entityTiming.startTiming();
        entityIn.onUpdate();
        entityTiming.stopTiming();
        causeTracker.completePhase();
    }

    public static void tickRidingEntity(CauseTracker causeTracker, net.minecraft.entity.Entity entity) {
        checkArgument(entity instanceof Entity, "Entity %s is not an instance of SpongeAPI's Entity!", entity);
        checkNotNull(entity, "Cannot capture on a null ticking entity!");
        causeTracker.switchToPhase(WorldPhase.Tick.ENTITY, PhaseContext.start()
                .add(NamedCause.source(entity))
                .addEntityCaptures()
                .addBlockCaptures()
                .complete());
        final Timing entityTiming = EntityUtil.toMixin(entity).getTimingsHandler();
        entityTiming.startTiming();
        entity.updateRidden();
        entityTiming.stopTiming();
        causeTracker.completePhase();
    }

    public static void tickTileEntity(CauseTracker causeTracker, ITickable tile) {
        causeTracker.switchToPhase(WorldPhase.Tick.TILE_ENTITY, PhaseContext.start()
                .add(NamedCause.source(tile))
                .addEntityCaptures()
                .addBlockCaptures()
                .complete());
        checkArgument(tile instanceof TileEntity, "ITickable %s is not a TileEntity!", tile);
        checkNotNull(tile, "Cannot capture on a null ticking tile entity!");
        final IMixinTileEntity mixinTileEntity = (IMixinTileEntity) tile;
        mixinTileEntity.getTimingsHandler().startTiming();
        tile.update();
        mixinTileEntity.getTimingsHandler().stopTiming();
        causeTracker.completePhase();

    }

    public static void updateTickBlock(CauseTracker causeTracker, Block block, BlockPos pos, IBlockState state, Random random) {
        final IMixinWorldServer mixinWorld = causeTracker.getMixinWorld();
        final WorldServer minecraftWorld = causeTracker.getMinecraftWorld();
        BlockSnapshot snapshot = mixinWorld.createSpongeBlockSnapshot(state, state.getActualState(minecraftWorld, pos), pos, 0);
        final PhaseContext phaseContext = PhaseContext.start()
                .add(NamedCause.source(snapshot))
                .addBlockCaptures()
                .addEntityCaptures();
        // We have to associate any notifiers in case of scheduled block updates from other sources
        final PhaseData current = causeTracker.getStack().peek();
        final IPhaseState currentState = current.getState();
        currentState.getPhase().appendNotifierPreBlockTick(causeTracker, pos, currentState, current.getContext(), phaseContext);
        // Now actually switch to the new phase
        causeTracker.switchToPhase(WorldPhase.Tick.BLOCK, phaseContext.complete());
        block.updateTick(minecraftWorld, pos, state, random);
        causeTracker.completePhase();
    }

    public static void randomTickBlock(CauseTracker causeTracker, Block block, BlockPos pos, IBlockState state, Random random) {
        final IMixinWorldServer mixinWorld = causeTracker.getMixinWorld();
        final WorldServer minecraftWorld = causeTracker.getMinecraftWorld();
        final BlockSnapshot currentTickBlock = mixinWorld.createSpongeBlockSnapshot(state, state.getActualState(minecraftWorld, pos), pos, 0);
        final PhaseContext phaseContext = PhaseContext.start()
                .add(NamedCause.source(currentTickBlock))
                .addEntityCaptures()
                .addBlockCaptures();
        // We have to associate any notifiers in case of scheduled block updates from other sources
        final PhaseData current = causeTracker.getStack().peek();
        final IPhaseState currentState = current.getState();
        currentState.getPhase().appendNotifierPreBlockTick(causeTracker, pos, currentState, current.getContext(), phaseContext);
        // Now actually switch to the new phase
        causeTracker.switchToPhase(WorldPhase.Tick.RANDOM_BLOCK, phaseContext.complete());
        block.randomTick(minecraftWorld, pos, state, random);
        causeTracker.completePhase();
    }

    public static void tickWorldProvider(IMixinWorldServer worldServer) {
        final CauseTracker causeTracker = worldServer.getCauseTracker();
        final WorldProvider worldProvider = ((WorldServer) worldServer).provider;
        causeTracker.switchToPhase(WorldPhase.Tick.DIMENSION, PhaseContext.start()
                .add(NamedCause.source(worldProvider))
                .addBlockCaptures()
                .addEntityCaptures()
                .addEntityDropCaptures()
                .complete());
        worldProvider.onWorldUpdateEntities();
        causeTracker.completePhase();
    }

    public static boolean fireMinecraftBlockEvent(CauseTracker causeTracker, WorldServer worldIn, BlockEventData event) {
        IBlockState currentState = worldIn.getBlockState(event.getPosition());
        final IMixinBlockEventData blockEvent = (IMixinBlockEventData) event;
        final PhaseContext phaseContext = PhaseContext.start()
                .addBlockCaptures()
                .addEntityCaptures();

        Stream.<Supplier<Optional<?>>>of(blockEvent::getCurrentTickBlock, blockEvent::getCurrentTickTileEntity, () -> Optional.of(blockEvent))
                .map(Supplier::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .ifPresent(obj -> phaseContext.add(NamedCause.source(obj)));

        blockEvent.getSourceUser().ifPresent(user -> phaseContext.add(NamedCause.notifier(user)));

        phaseContext.complete();
        causeTracker.switchToPhase(WorldPhase.Tick.BLOCK_EVENT, phaseContext);
        boolean result = currentState.onBlockEventReceived(worldIn, event.getPosition(), event.getEventID(), event.getEventParameter());
        causeTracker.completePhase();
        return result;
    }

    public static void performBlockDrop(Block block, IMixinWorldServer mixinWorld, BlockPos pos, IBlockState state, float chance, int fortune) {
        final CauseTracker causeTracker = mixinWorld.getCauseTracker();
        final IPhaseState currentState = causeTracker.getStack().peekState();
        final boolean shouldEnterBlockDropPhase = !currentState.getPhase().alreadyCapturingItemSpawns(currentState);
        if (shouldEnterBlockDropPhase) {
            causeTracker.switchToPhase(BlockPhase.State.BLOCK_DROP_ITEMS, PhaseContext.start()
                    .add(NamedCause.source(mixinWorld.createSpongeBlockSnapshot(state, state, pos, 4)))
                    .addBlockCaptures()
                    .addEntityCaptures()
                    .add(NamedCause.of(InternalNamedCauses.General.BLOCK_BREAK_FORTUNE, fortune))
                    .add(NamedCause.of(InternalNamedCauses.General.BLOCK_BREAK_POSITION, pos))
                    .complete());
        }
        block.dropBlockAsItemWithChance((WorldServer) mixinWorld, pos, state, chance, fortune);
        if (shouldEnterBlockDropPhase) {
            causeTracker.completePhase();
        }
    }

    public static void associateEntityCreator(PhaseContext context, Entity spongeEntity, WorldServer world) {
        associateEntityCreator(context, EntityUtil.toNative(spongeEntity), world);
    }

    public static void associateEntityCreator(PhaseContext context, net.minecraft.entity.Entity minecraftEntity, WorldServer minecraftWorld) {
        context.firstNamed(NamedCause.SOURCE, Entity.class).ifPresent(tickingEntity -> {
            final IMixinEntity mixinEntity = EntityUtil.toMixin(tickingEntity);
            Stream.<Supplier<Optional<UUID>>>of(
                    () -> mixinEntity.getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_NOTIFIER)
                            .map(Identifiable::getUniqueId),
                    () -> mixinEntity.getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR)
                            .map(Identifiable::getUniqueId)
            )
                    .map(Supplier::get)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst()
                    .ifPresent(uuid -> EntityUtil.toMixin(minecraftEntity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, uuid));
            mixinEntity.getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR)
                    .ifPresent(creator -> EntityUtil.toMixin(minecraftEntity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, creator.getUniqueId()));
        });

    }


    static boolean trackBlockChange(CauseTracker causeTracker, Chunk chunk, IBlockState currentState, IBlockState newState, BlockPos pos, int flags,
            PhaseContext phaseContext, IPhaseState phaseState) {
        final WorldServer minecraftWorld = causeTracker.getMinecraftWorld();
        final IBlockState actualState = currentState.getActualState(minecraftWorld, pos);
        final SpongeBlockSnapshot originalBlockSnapshot = causeTracker.getMixinWorld().createSpongeBlockSnapshot(currentState, actualState, pos, flags);
        final List<BlockSnapshot> capturedSnapshots = phaseContext.getCapturedBlocks();
        final Block newBlock = newState.getBlock();

        associateBlockChangeWithSnapshot(phaseState, newBlock, currentState, originalBlockSnapshot, capturedSnapshots);
        final IMixinChunk mixinChunk = (IMixinChunk) chunk;
        final IBlockState originalBlockState = mixinChunk.setBlockState(pos, newState, currentState, originalBlockSnapshot);
        if (originalBlockState == null) {
            capturedSnapshots.remove(originalBlockSnapshot);
            return false;
        }

        if (newState.getLightOpacity() != currentState.getLightOpacity() || newState.getLightValue() != currentState.getLightValue()) {
            minecraftWorld.theProfiler.startSection("checkLight");
            minecraftWorld.checkLight(pos);
            minecraftWorld.theProfiler.endSection();
        }

        return true;
    }

    private static void associateBlockChangeWithSnapshot(IPhaseState phaseState, Block newBlock, IBlockState currentState, SpongeBlockSnapshot snapshot, List<BlockSnapshot> capturedSnapshots) {
        if (phaseState == BlockPhase.State.BLOCK_DECAY) {
            if (newBlock == Blocks.AIR) {
                snapshot.blockChange = BlockChange.DECAY;
                capturedSnapshots.add(snapshot);
            }
        } else if (newBlock == Blocks.AIR) {
            snapshot.blockChange = BlockChange.BREAK;
            capturedSnapshots.add(snapshot);
        } else if (newBlock != currentState.getBlock()) {
            snapshot.blockChange = BlockChange.PLACE;
            capturedSnapshots.add(snapshot);
        } else {
            snapshot.blockChange = BlockChange.MODIFY;
            capturedSnapshots.add(snapshot);
        }
    }

    private TrackingUtil() {
    }

    public static void breakBlock(Block currentBlock, World worldObj, BlockPos pos, IBlockState currentState) {
        currentBlock.breakBlock(worldObj, pos, currentState);
    }

    public static Optional<User> getNotifierOrOwnerFromBlock(Location<org.spongepowered.api.world.World> location) {
        final Vector3d position = location.getPosition();
        final BlockPos blockPos = VecHelper.toBlockPos(position);
        final IMixinChunk mixinChunk = (IMixinChunk) ((WorldServer) location.getExtent()).getChunkFromBlockCoords(blockPos);
        return Stream.<Supplier<Optional<User>>>of(() -> mixinChunk.getBlockNotifier(blockPos), () -> mixinChunk.getBlockOwner(blockPos))
                .map(Supplier::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }
}