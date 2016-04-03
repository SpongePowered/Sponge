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

import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.action.LightningEvent;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.phase.BlockPhase;
import org.spongepowered.common.event.tracking.phase.TrackingPhases;
import org.spongepowered.common.event.tracking.phase.WorldPhase;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.entity.IMixinEntityLightningBolt;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.BlockChange;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

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
        SpongeCommonEventFactory.handleEntityMovement(entityIn);
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
        causeTracker.switchToPhase(TrackingPhases.WORLD, WorldPhase.Tick.BLOCK, PhaseContext.start()
                .add(NamedCause.source(snapshot))
                .addCaptures()
                .complete());
        block.updateTick(minecraftWorld, pos, state, random);
        causeTracker.completePhase();
    }

    public static void randomTickBlock(CauseTracker causeTracker, Block block, BlockPos pos, IBlockState state, Random random) {
        final IMixinWorldServer mixinWorld = causeTracker.getMixinWorld();
        final World minecraftWorld = causeTracker.getMinecraftWorld();
        final BlockSnapshot currentTickBlock = mixinWorld.createSpongeBlockSnapshot(state, state.getBlock().getActualState(state,
                minecraftWorld, pos), pos, 0);
        causeTracker.switchToPhase(TrackingPhases.GENERAL, WorldPhase.Tick.RANDOM_BLOCK, PhaseContext.start()
                .add(NamedCause.source(currentTickBlock))
                .addCaptures()
                .complete());
        block.randomTick(minecraftWorld, pos, state, random);
        causeTracker.completePhase();
    }

    public static boolean fireMinecraftBlockEvent(CauseTracker causeTracker, WorldServer worldIn, BlockEventData event,
            Map<BlockPos, User> trackedBlockEvents) {
        IBlockState currentState = worldIn.getBlockState(event.getPosition());
        final World minecraftWorld = causeTracker.getMinecraftWorld();
        final IMixinWorldServer mixinWorld = causeTracker.getMixinWorld();
        final BlockSnapshot currentTickBlock = mixinWorld.createSpongeBlockSnapshot(currentState, currentState.getBlock().getActualState(currentState,
                minecraftWorld, event.getPosition()), event.getPosition(), 3);
        final PhaseContext phaseContext = PhaseContext.start()
                .addCaptures()
                .add(NamedCause.source(currentTickBlock));
        if (trackedBlockEvents.get(event.getPosition()) != null) {
            User user = trackedBlockEvents.get(event.getPosition());
            phaseContext.add(NamedCause.notifier(user));
        }
        phaseContext.complete();
        causeTracker.switchToPhase(TrackingPhases.GENERAL, WorldPhase.Tick.BLOCK, phaseContext);
        boolean result = worldIn.fireBlockEvent(event);
        causeTracker.completePhase();
        trackedBlockEvents.remove(event.getPosition());
        return result;
    }

    public static boolean addWeatherEffect(final net.minecraft.entity.Entity entity, World minecraftWorld) {
        if (entity instanceof EntityLightningBolt) {
            LightningEvent.Pre event = SpongeEventFactory.createLightningEventPre(((IMixinEntityLightningBolt) entity).getCause());
            SpongeImpl.postEvent(event);
            if (!event.isCancelled()) {
                return minecraftWorld.addWeatherEffect(entity);
            }
        } else {
            return minecraftWorld.addWeatherEffect(entity);
        }
        return false;
    }

    public static void associateEntityCreator(PhaseContext context, net.minecraft.entity.Entity minecraftEntity, World minecraftWorld) {
        context.firstNamed(NamedCause.SOURCE, BlockSnapshot.class).ifPresent(tickingSnapshot -> {
            BlockPos sourcePos = VecHelper.toBlockPos(tickingSnapshot.getPosition());
            Block targetBlock = minecraftWorld.getBlockState(minecraftEntity.getPosition()).getBlock();
            SpongeHooks.tryToTrackBlockAndEntity(minecraftWorld, tickingSnapshot, minecraftEntity, sourcePos, targetBlock, minecraftEntity.getPosition(), PlayerTracker.Type.NOTIFIER);

        });
        context.firstNamed(NamedCause.SOURCE, Entity.class).ifPresent(tickingEntity -> {
            Optional<User> creator = ((IMixinEntity) tickingEntity).getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR);
            if (creator.isPresent()) { // transfer user to next entity. This occurs with falling blocks that change into items
                ((IMixinEntity) minecraftEntity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, creator.get().getUniqueId());
            }
        });

    }


    static boolean trackBlockChange(CauseTracker causeTracker, IBlockState currentState, IBlockState newState, BlockPos pos, int flags, PhaseContext phaseContext, IPhaseState phaseState) {
        final WorldServer minecraftWorld = causeTracker.getMinecraftWorld();
        final IBlockState actualState = currentState.getBlock().getActualState(currentState, minecraftWorld, pos);
        final BlockSnapshot originalBlockSnapshot = causeTracker.getMixinWorld().createSpongeBlockSnapshot(currentState, actualState, pos, flags);
        final List<BlockSnapshot> capturedSpongeBlockSnapshots = phaseContext.getCapturedBlocks().get();
        final Block newBlock = newState.getBlock();

        if (phaseState == BlockPhase.State.BLOCK_DECAY) {
            if (newBlock == Blocks.air) {
                ((SpongeBlockSnapshot) originalBlockSnapshot).blockChange = BlockChange.DECAY;
                capturedSpongeBlockSnapshots.add(originalBlockSnapshot);
            }
        } else if (newBlock == Blocks.air) {
            ((SpongeBlockSnapshot) originalBlockSnapshot).blockChange = BlockChange.BREAK;
            capturedSpongeBlockSnapshots.add(originalBlockSnapshot);
        } else if (newBlock != currentState.getBlock()) {
            ((SpongeBlockSnapshot) originalBlockSnapshot).blockChange = BlockChange.PLACE;
            capturedSpongeBlockSnapshots.add(originalBlockSnapshot);
        } else {
            ((SpongeBlockSnapshot) originalBlockSnapshot).blockChange = BlockChange.MODIFY;
            capturedSpongeBlockSnapshots.add(originalBlockSnapshot);
        }
        final Chunk chunk = minecraftWorld.getChunkFromBlockCoords(pos);
        final IMixinChunk mixinChunk = (IMixinChunk) chunk;
        final IBlockState changedBlockState = mixinChunk.setBlockState(pos, newState, currentState, originalBlockSnapshot);
        if (changedBlockState == null) {
            capturedSpongeBlockSnapshots.remove(originalBlockSnapshot);
            return false;
        }
        final Block currentblock = changedBlockState.getBlock();

        if (newBlock.getLightOpacity() != currentblock.getLightOpacity() || newBlock.getLightValue() != currentblock.getLightValue()) {
            minecraftWorld.theProfiler.startSection("checkLight");
            minecraftWorld.checkLight(pos);
            minecraftWorld.theProfiler.endSection();
        }

        if ((flags & 2) != 0 && (!minecraftWorld.isRemote || (flags & 4) == 0) && chunk.isPopulated()) {
            minecraftWorld.markBlockForUpdate(pos);
        }

        if (!minecraftWorld.isRemote && (flags & 1) != 0) {
            minecraftWorld.notifyNeighborsRespectDebug(pos, changedBlockState.getBlock());

            if (newBlock.hasComparatorInputOverride()) {
                minecraftWorld.updateComparatorOutputLevel(pos, newBlock);
            }
        }

        return true;
    }

    private TrackingUtil() {
    }

}
