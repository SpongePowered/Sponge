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

import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.util.Identifiable;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.phase.BlockPhase;
import org.spongepowered.common.event.tracking.phase.TrackingPhases;
import org.spongepowered.common.event.tracking.phase.WorldPhase;
import org.spongepowered.common.event.tracking.phase.util.PhaseUtil;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.IMixinNextTickListEntry;
import org.spongepowered.common.interfaces.block.IMixinBlockEventData;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.world.BlockChange;

import java.util.ArrayList;
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
        causeTracker.switchToPhase(TrackingPhases.GENERAL, WorldPhase.Tick.ENTITY, PhaseContext.start()
                .add(NamedCause.source(entityIn))
                .addCaptures()
                .complete());
        entityIn.onUpdate();
        causeTracker.completePhase();
    }

    public static void tickTileEntity(CauseTracker causeTracker, ITickable tile) {
        causeTracker.switchToPhase(TrackingPhases.GENERAL, WorldPhase.Tick.TILE_ENTITY, PhaseContext.start()
                .add(NamedCause.source(tile))
                .addCaptures()
                .complete());
        checkArgument(tile instanceof TileEntity, "ITickable %s is not a TileEntity!", tile);
        checkNotNull(tile, "Cannot capture on a null ticking tile entity!");
        tile.update();
        causeTracker.completePhase();
    }

    public static void updateTickBlock(CauseTracker causeTracker, Block block, BlockPos pos, IBlockState state, Random random) {
        final IMixinWorldServer mixinWorld = causeTracker.getMixinWorld();
        final World minecraftWorld = causeTracker.getMinecraftWorld();
        BlockSnapshot snapshot = mixinWorld.createSpongeBlockSnapshot(state, state.getBlock().getActualState(state, minecraftWorld, pos), pos, 0);
        final PhaseContext phaseContext = PhaseContext.start()
                .add(NamedCause.source(snapshot))
                .addCaptures();
        // We have to associate any notifiers in case of scheduled block updates from other sources
        final PhaseData current = causeTracker.getStack().peek();
        final IPhaseState currentState = current.getState();
        currentState.getPhase().appendNotifierPreBlockTick(causeTracker, pos, currentState, current.getContext(), phaseContext);
        // Now actually switch to the new phase
        causeTracker.switchToPhase(TrackingPhases.WORLD, WorldPhase.Tick.BLOCK, phaseContext.complete());
        block.updateTick(minecraftWorld, pos, state, random);
        causeTracker.completePhase();
    }

    public static void randomTickBlock(CauseTracker causeTracker, Block block, BlockPos pos, IBlockState state, Random random) {
        final IMixinWorldServer mixinWorld = causeTracker.getMixinWorld();
        final World minecraftWorld = causeTracker.getMinecraftWorld();
        final BlockSnapshot currentTickBlock = mixinWorld.createSpongeBlockSnapshot(state, state.getBlock().getActualState(state,
                minecraftWorld, pos), pos, 0);
        final PhaseContext phaseContext = PhaseContext.start()
                .add(NamedCause.source(currentTickBlock))
                .addCaptures();
        // We have to associate any notifiers in case of scheduled block updates from other sources
        final PhaseData current = causeTracker.getStack().peek();
        final IPhaseState currentState = current.getState();
        currentState.getPhase().appendNotifierPreBlockTick(causeTracker, pos, currentState, current.getContext(), phaseContext);
        // Now actually switch to the new phase
        causeTracker.switchToPhase(TrackingPhases.GENERAL, WorldPhase.Tick.RANDOM_BLOCK, phaseContext.complete());
        block.randomTick(minecraftWorld, pos, state, random);
        causeTracker.completePhase();
    }

    public static boolean fireMinecraftBlockEvent(CauseTracker causeTracker, WorldServer worldIn, BlockEventData event) {
        IBlockState currentState = worldIn.getBlockState(event.getPosition());
        final IMixinBlockEventData blockEvent = (IMixinBlockEventData) event;
        final PhaseContext phaseContext = PhaseContext.start()
                .addCaptures();

        Stream.<Supplier<Optional<?>>>of(blockEvent::getCurrentTickBlock, blockEvent::getCurrentTickTileEntity, () -> Optional.of(blockEvent))
                .map(Supplier::get)
                .filter(Optional::isPresent)
                .findFirst()
                .ifPresent(obj -> phaseContext.add(NamedCause.source(obj)));

        blockEvent.getSourceUser().ifPresent(user -> phaseContext.add(NamedCause.notifier(user)));

        phaseContext.complete();
        causeTracker.switchToPhase(TrackingPhases.GENERAL, WorldPhase.Tick.BLOCK_EVENT, phaseContext);
        boolean result = currentState.getBlock().func_189539_a(currentState, worldIn, event.getPosition(), event.getEventID(), event.getEventParameter());
        causeTracker.completePhase();
        return result;
    }

    public static void associateEntityCreator(PhaseContext context, Entity spongeEntity, World world) {
        associateEntityCreator(context, EntityUtil.toNative(spongeEntity), world);
    }

    public static void associateEntityCreator(PhaseContext context, net.minecraft.entity.Entity minecraftEntity, World minecraftWorld) {
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
        final IBlockState actualState = currentState.getBlock().getActualState(currentState, minecraftWorld, pos);
        final BlockSnapshot originalBlockSnapshot = causeTracker.getMixinWorld().createSpongeBlockSnapshot(currentState, actualState, pos, flags);
        final List<BlockSnapshot> capturedSnapshots = phaseContext.getCapturedBlocks();
        final Block newBlock = newState.getBlock();

        associateBlockChangeWithSnapshot(phaseState, newBlock, currentState, (SpongeBlockSnapshot) originalBlockSnapshot, capturedSnapshots);
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

    public static Optional<User> tryAndTrackActiveUser(IMixinWorldServer mixinWorldServer, BlockPos pos, PlayerTracker.Type type) {
        net.minecraft.world.World world = (WorldServer) mixinWorldServer;
        if (pos == null || !world.isBlockLoaded(pos)) {
            return Optional.empty();
        }

        User user = null;
        final Chunk chunk = world.getChunkFromBlockCoords(pos);
        IMixinChunk spongeChunk = (IMixinChunk) chunk;
        if (spongeChunk != null && !chunk.isEmpty()) {
//            final CauseTracker causeTracker = spongeWorld.getCauseTracker();
//            if (StaticMixinHelper.packetPlayer != null) {
//                user = (User) StaticMixinHelper.packetPlayer;
//                spongeChunk.addTrackedBlockPosition(world.getBlockState(pos).getBlock(), pos, user, type);
//            } else if (causeTracker.hasNotifier()) {
//                user = causeTracker.getCurrentNotifier().get();
//                spongeChunk.addTrackedBlockPosition(world.getBlockState(pos).getBlock(), pos, user, type);
//            }
            // check if a non-living entity exists at target block position (ex. minecarts)
            if (user != null) {
                List<net.minecraft.entity.Entity> entityList = new ArrayList<>();
                chunk.getEntitiesOfTypeWithinAAAB(net.minecraft.entity.Entity.class, getEntityAABBForBlockPos(pos), entityList,
                        entityTrackerPredicate);
                for (net.minecraft.entity.Entity entity : entityList) {
                    ((IMixinEntity) entity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_NOTIFIER, user.getUniqueId());
                }
            }
        }
        return Optional.empty();
    }

    private static final Predicate<net.minecraft.entity.Entity> entityTrackerPredicate = input -> {
        return !(input instanceof EntityLivingBase) && !(input instanceof EntityItem);
    };
    private static final AxisAlignedBB entityAABB = new AxisAlignedBB(0, 0, 0, 0, 0, 0);
    private static AxisAlignedBB getEntityAABBForBlockPos(BlockPos pos) {
        entityAABB.minX = pos.getX();
        entityAABB.minY = pos.getY();
        entityAABB.minZ = pos.getZ();
        entityAABB.maxX = pos.getX() + 0.1;
        entityAABB.maxY = pos.getY() + 0.1;
        entityAABB.maxZ = pos.getZ() + 0.1;
        return entityAABB;
    }

    public static Optional<User> tryAndTrackActiveUser(CauseTracker causeTracker, BlockPos targetPos, PlayerTracker.Type type) {
        net.minecraft.world.World world = causeTracker.getMinecraftWorld();
        if (targetPos == null || !world.isBlockLoaded(targetPos)) {
            return Optional.empty();
        }

        User user = null;
        final Chunk chunk = world.getChunkFromBlockCoords(targetPos);
        IMixinChunk spongeChunk = (IMixinChunk) chunk;
        if (spongeChunk != null && !chunk.isEmpty()) {
//            if (StaticMixinHelper.packetPlayer != null) {
//                user = (User) StaticMixinHelper.packetPlayer;
//                spongeChunk.addTrackedBlockPosition(world.getBlockState(targetPos).getBlock(), targetPos, user, type);
//            } else if (causeTracker.hasNotifier()) {
//                user = causeTracker.getCurrentNotifier().get();
//                spongeChunk.addTrackedBlockPosition(world.getBlockState(targetPos).getBlock(), targetPos, user, type);
//            }
            // check if a non-living entity exists at target block position (ex. minecarts)
            if (user != null) {
                List<net.minecraft.entity.Entity> entityList = new ArrayList<>();
                chunk.getEntitiesOfTypeWithinAAAB(net.minecraft.entity.Entity.class, getEntityAABBForBlockPos(targetPos), entityList, entityTrackerPredicate);
                for (net.minecraft.entity.Entity entity : entityList) {
                    ((IMixinEntity) entity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_NOTIFIER, user.getUniqueId());
                }
            }
        }

        return Optional.ofNullable(user);
    }

    public static Optional<User> trackTargetBlockFromSource(CauseTracker causeTracker, Object source, BlockPos sourcePos, Block targetBlock, BlockPos targetPos, PlayerTracker.Type type) {
        // first check to see if we have an active user
        Optional<User> user = tryAndTrackActiveUser(causeTracker, sourcePos, type);
        if (user.isPresent()) {
            return user;
        }

        net.minecraft.world.World world = causeTracker.getMinecraftWorld();
        if (sourcePos == null || !world.isBlockLoaded(sourcePos)) {
            return Optional.empty();
        }

        final Chunk chunk = world.getChunkFromBlockCoords(sourcePos);
        IMixinChunk spongeChunk = (IMixinChunk) chunk;
        if (spongeChunk != null && !chunk.isEmpty()) {
            Optional<User> owner = spongeChunk.getBlockOwner(sourcePos);
            Optional<User> notifier = spongeChunk.getBlockNotifier(sourcePos);
            if (notifier.isPresent()) {
                spongeChunk = (IMixinChunk) world.getChunkFromBlockCoords(targetPos);
                spongeChunk.addTrackedBlockPosition(world.getBlockState(targetPos).getBlock(), targetPos, notifier.get(), type);
                return notifier;
            } else if (owner.isPresent()) {
                spongeChunk.addTrackedBlockPosition(world.getBlockState(targetPos).getBlock(), targetPos, owner.get(), type);
                return owner;
            }
        }
        return Optional.empty();
    }

}