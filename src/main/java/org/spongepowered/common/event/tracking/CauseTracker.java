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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.effect.EntityWeatherEffect;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.stats.StatList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ReportedException;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.phase.BlockPhase;
import org.spongepowered.common.event.tracking.phase.SpawningPhase;
import org.spongepowered.common.event.tracking.phase.TrackingPhase;
import org.spongepowered.common.event.tracking.phase.TrackingPhases;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.entity.IMixinEntityLightningBolt;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.util.StaticMixinHelper;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.CaptureType;
import org.spongepowered.common.world.SpongeProxyBlockAccess;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

/**
 * A helper object that is hard attached to a {@link World} that acts as a
 * proxy object entering and processing between different states of the
 * world and it's objects.
 */
public final class CauseTracker {

    private final net.minecraft.world.World targetWorld;

    private boolean captureBlocks = false;

    private final TrackingPhases phases = new TrackingPhases();

    public CauseTracker(net.minecraft.world.World targetWorld) {
        if (((IMixinWorld) targetWorld).getCauseTracker() != null) {
            throw new IllegalArgumentException("Attempting to create a new CauseTracker for a world that already has a CauseTracker!!");
        }
        this.targetWorld = targetWorld;
    }

    // ----------------- STATE ACCESS ----------------------------------

    public void switchToPhase(TrackingPhase phase, IPhaseState state, PhaseContext phaseContext) {
        if (this.phases.states.size() > 6) {
            // This printing is to detect possibilities of a phase not being cleared properly
            // and resulting in a "runaway" phase state accumilation.
            PrettyPrinter printer = new PrettyPrinter(60);
            printer.add("Switching to Incompatible Phase!!!").centre().hr();
            printer.add("Detecting a runaway phase! Potentially a problem where something isn't completing a phase!!!");
            printer.add("  %s : %s", "Entering Phase", phase);
            printer.add("  %s : %s", "Entering State", state);
            printer.addWrapped(60, "%s : %s", "Current phases", this.phases.states.currentStates());
            printer.add("  %s :", "Printing stack trace");
            Exception exception = new Exception("Stack trace");
            for (StackTraceElement element : exception.getStackTrace()) {
                printer.add("    %s", element);
            }
            printer.print(System.err).log(SpongeImpl.getLogger(), Level.TRACE);
        }
        IPhaseState currentState = this.phases.peekState();
        if (!currentState.canSwitchTo(state)) {
            // This is to detect incompatible phase switches.
            PrettyPrinter printer = new PrettyPrinter(60);
            printer.add("Switching Phase").centre().hr();
            printer.add("Phase incompatibility detected! Attempting to switch to an invalid phase!");
            printer.add("  %s : %s", "Current Phase", currentState.getPhase());
            printer.add("  %s : %s", "Current State", currentState);
            printer.add("  %s : %s", "Entering incompatible Phase", phase);
            printer.add("  %s : %s", "Entering incompatible State", state);
            printer.addWrapped(60, "%s : %s", "Current phases", this.phases.states.currentStates());
            printer.add("  %s :", "Printing stack trace");
            Exception exception = new Exception("Stack trace");
            for (StackTraceElement element : exception.getStackTrace()) {
                printer.add("    %s", element);
            }
            printer.print(System.err).log(SpongeImpl.getLogger(), Level.TRACE);
        }

        this.phases.push(state, phaseContext);
    }

    public void completePhase() {
        final PhaseData tuple = this.phases.peek();
        IPhaseState state = tuple.getState();
        if (this.phases.states.size() > 6) {
            // This printing is to detect possibilities of a phase not being cleared properly
            // and resulting in a "runaway" phase state accumilation.
            PrettyPrinter printer = new PrettyPrinter(60);
            printer.add("Completing Phase").centre().hr();
            printer.add("Detecting a runaway phase! Potentially a problem where something isn't completing a phase!!!");
            printer.addWrapped(60, "  %s : %s", "Completing phase", state);
            printer.addWrapped(60, "  %s : %s", "Phases remaining", this.phases.states.currentStates());
            Exception exception = new Exception("Stack trace");
            for (StackTraceElement element : exception.getStackTrace()) {
                printer.add("     %s", element);
            }
            printer.print(System.err).log(SpongeImpl.getLogger(), Level.TRACE);
        }
        this.phases.pop();
        // If pop is called, the Deque will already throw an exception if there is no element
        // so it's an error properly handled.
        final TrackingPhase phase = state.getPhase();
        phase.unwind(this, state, tuple.getContext());
    }

    // ----------------- SIMPLE GETTERS --------------------------------------

    /**
     * Gets the {@link World} as a SpongeAPI world.
     *
     * @return The world casted as a SpongeAPI world
     */
    public World getWorld() {
        return (World) this.targetWorld;
    }

    /**
     * Gets the {@link World} as a Minecraft {@link net.minecraft.world.World}.
     *
     * @return The world as it's original object
     */
    public net.minecraft.world.World getMinecraftWorld() {
        return this.targetWorld;
    }

    /**
     * Gets the world casted as a {@link IMixinWorld}.
     *
     * @return The world casted as a mixin world
     */
    public IMixinWorld getMixinWorld() {
        return (IMixinWorld) this.targetWorld;
    }

    public TrackingPhases getPhases() {
        return this.phases;
    }

    public boolean isCapturingBlocks() {
        return this.captureBlocks;
    }

    public void setCaptureBlocks(boolean captureBlocks) {
        this.captureBlocks = captureBlocks;
    }

    // --------------------- DELEGATED WORLD METHODS -------------------------

    public void notifyBlockOfStateChange(BlockPos notifyPos, final Block sourceBlock, BlockPos sourcePos) {
        if (!this.getMinecraftWorld().isRemote) {
            IBlockState iblockstate = this.getMinecraftWorld().getBlockState(notifyPos);
            final PhaseData phaseContextTuple = this.getPhases().peek();
            Optional<User> packetUser = phaseContextTuple.getContext().firstNamed(TrackingHelper.PACKET_PLAYER, User.class);

            try {
                if (!this.getMinecraftWorld().isRemote) {
                    final Chunk chunkFromBlockCoords = this.getMinecraftWorld().getChunkFromBlockCoords(notifyPos);
                    if (packetUser.isPresent()) {
                        IMixinChunk spongeChunk = (IMixinChunk) chunkFromBlockCoords;
                        if (!(spongeChunk instanceof EmptyChunk)) {
                            spongeChunk.addTrackedBlockPosition(iblockstate.getBlock(), notifyPos, packetUser.get(),
                                    PlayerTracker.Type.NOTIFIER);
                        }
                    } else {
                        Object source = null;
                        final PhaseContext context = this.phases.peekContext();
                        Optional<BlockSnapshot> currentTickingBlock = context.firstNamed(NamedCause.SOURCE, BlockSnapshot.class);
                        final Optional<TileEntity> currentTickingTile = context.firstNamed(NamedCause.SOURCE, TileEntity.class);
                        final Optional<Entity> currentTickingEntity = context.firstNamed(NamedCause.SOURCE, Entity.class);
                        if (currentTickingBlock.isPresent()) {
                            source = currentTickingBlock.get();
                            sourcePos = VecHelper.toBlockPos(currentTickingBlock.get().getPosition());
                        } else if (currentTickingTile.isPresent()) {
                            source = currentTickingTile.get();
                            sourcePos = ((net.minecraft.tileentity.TileEntity) currentTickingTile.get()).getPos();
                        } else if (currentTickingEntity.isPresent()) { // Falling Blocks
                            IMixinEntity spongeEntity = (IMixinEntity) currentTickingEntity.get();
                            sourcePos = ((net.minecraft.entity.Entity) currentTickingEntity.get()).getPosition();
                            Optional<User> owner = spongeEntity.getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR);
                            Optional<User> notifier = spongeEntity.getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_NOTIFIER);
                            IMixinChunk spongeChunk = (IMixinChunk) chunkFromBlockCoords;
                            if (notifier.isPresent()) {
                                spongeChunk.addTrackedBlockPosition(iblockstate.getBlock(), notifyPos, notifier.get(), PlayerTracker.Type.NOTIFIER);
                            } else if (owner.isPresent()) {
                                spongeChunk.addTrackedBlockPosition(iblockstate.getBlock(), notifyPos, owner.get(), PlayerTracker.Type.NOTIFIER);
                            }
                        }

                        if (source != null) {
                            SpongeHooks.tryToTrackBlock(this.getMinecraftWorld(), source, sourcePos, iblockstate.getBlock(), notifyPos,
                                    PlayerTracker.Type.NOTIFIER);
                        }
                    }
                }

                iblockstate.getBlock().onNeighborBlockChange(this.getMinecraftWorld(), notifyPos, iblockstate, sourceBlock);
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception while updating neighbours");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being updated");
                // TODO
                /*crashreportcategory.addCrashSectionCallable("Source block type", new Callable()
                {
                    public String call() {
                        try {
                            return String.format("ID #%d (%s // %s)", new Object[] {Integer.valueOf(Block.getIdFromBlock(blockIn)), blockIn.getUnlocalizedName(), blockIn.getClass().getCanonicalName()});
                        } catch (Throwable throwable1) {
                            return "ID #" + Block.getIdFromBlock(blockIn);
                        }
                    }
                });*/
                CrashReportCategory.addBlockInfo(crashreportcategory, notifyPos, iblockstate);
                throw new ReportedException(crashreport);
            }
        }
    }

    public boolean setBlockState(BlockPos pos, IBlockState newState, int flags) {
        final net.minecraft.world.World minecraftWorld = this.getMinecraftWorld();
        final Chunk chunk = minecraftWorld.getChunkFromBlockCoords(pos);
        final IBlockState currentState = chunk.getBlockState(pos);
        if (currentState == newState) {
            return false;
        }

        final Block block = newState.getBlock();
        BlockSnapshot originalBlockSnapshot = null;

        // Don't capture if we are restoring blocks
        // uncomment when you want to handle block events. for now just want to set block state and
        // track the phases
        final PhaseData currentPhaseContext = this.getPhases().peek();
        final IPhaseState phaseState = currentPhaseContext.getState();
        final TrackingPhase phase = phaseState.getPhase();
        final PhaseContext phaseContext = currentPhaseContext.getContext();
        // This is going to be handled in the phase.
        if (!minecraftWorld.isRemote && phase.requiresBlockCapturing(phaseState)) {
            BlockStateTriplet pair = phase.captureBlockChange(this, currentState, newState, block, pos, flags, phaseContext, phaseState);
            originalBlockSnapshot = pair.getBlockSnapshot();
        }

        int oldLight = currentState.getBlock().getLightValue();

        IBlockState iblockstate1 = ((IMixinChunk) chunk).setBlockState(pos, newState, currentState, null);

        if (iblockstate1 == null) {
            if (originalBlockSnapshot != null) {
                phaseContext.getCapturedBlocks().get().remove(originalBlockSnapshot);
            }
            return false;
        } else {
            Block block1 = iblockstate1.getBlock();

            if (block.getLightOpacity() != block1.getLightOpacity() || block.getLightValue() != oldLight) {
                minecraftWorld.theProfiler.startSection("checkLight");
                minecraftWorld.checkLight(pos);
                minecraftWorld.theProfiler.endSection();
            }

            // Don't notify clients or update physics while capturing blockstates
            if (originalBlockSnapshot == null) {
                // Modularize client and physic updates
                this.getMixinWorld().markAndNotifyNeighbors(pos, chunk, iblockstate1, newState, flags);
            }

            return true;
        }
    }

    public boolean processSpawnEntity(Entity entity, Cause cause) {
        checkNotNull(entity, "Entity cannot be null!");
        checkNotNull(cause, "Cause cannot be null!");

        // Very first thing - fire events that are from entity construction
        if (((IMixinEntity) entity).isInConstructPhase()) {
            ((IMixinEntity) entity).firePostConstructEvents();
        }

        final net.minecraft.entity.Entity entityIn = (net.minecraft.entity.Entity) entity;
        // do not drop any items while restoring blocksnapshots. Prevents dupes
        final net.minecraft.world.World minecraftWorld = this.getMinecraftWorld();
        final PhaseData phaseData = this.getPhases().peek();
        final IPhaseState phaseState = phaseData.getState();
        final PhaseContext context = phaseData.getContext();
        if (!minecraftWorld.isRemote && entityIn instanceof EntityItem && phaseState.getPhase().ignoresEntitySpawns(phaseState)) {
            return false;
        }

        int chunkX = MathHelper.floor_double(entityIn.posX / 16.0D);
        int chunkZ = MathHelper.floor_double(entityIn.posZ / 16.0D);
        boolean isForced = entityIn.forceSpawn;

        if (entityIn instanceof EntityPlayer) {
            isForced = true;
        } else if (entityIn instanceof EntityLightningBolt) {
            ((IMixinEntityLightningBolt) entityIn).setCause(cause);
        }

        if (!isForced && !minecraftWorld.isChunkLoaded(chunkX, chunkZ, true)) {
            // don't spawn any entities if there's no chunks loaded and it's not forced, quite simple.
            return false;
        } else {
            if (entityIn instanceof EntityPlayer) {
                // Players should NEVER EVER EVER be cause tracked, period.
                EntityPlayer entityplayer = (EntityPlayer) entityIn;
                minecraftWorld.playerEntities.add(entityplayer);
                minecraftWorld.updateAllPlayersSleepingFlag();
            }

            final IMixinWorld mixinWorld = this.getMixinWorld();
            if (minecraftWorld.isRemote || isForced || phaseState == SpawningPhase.State.DEATH_DROPS_SPAWNING) {
                // Basically, if it's forced, or it's remote, OR we're already spawning death drops, then go ahead.
                minecraftWorld.getChunkFromChunkCoords(chunkX, chunkZ).addEntity(entityIn);
                minecraftWorld.loadedEntityList.add(entityIn);
                mixinWorld.onSpongeEntityAdded(entityIn);
                return true;
            }

            return MoveToPhases.completeEntitySpawn(entity, cause, this, chunkX, chunkZ, phaseState, context);
        }
    }

    // --------------------- POPULATOR DELEGATED METHODS ---------------------

    public void markAndNotifyBlockPost(List<Transaction<BlockSnapshot>> transactions, @Nullable CaptureType type, Cause cause) {
        SpongeProxyBlockAccess proxyBlockAccess = new SpongeProxyBlockAccess(this.getMinecraftWorld(), transactions);
        for (Transaction<BlockSnapshot> transaction : transactions) {
            if (!transaction.isValid()) {
                continue; // Don't use invalidated block transactions during notifications, these only need to be restored
            }
            // Handle custom replacements
            if (transaction.getCustom().isPresent()) {
                switchToPhase(TrackingPhases.BLOCK, BlockPhase.State.RESTORING_BLOCKS, PhaseContext.start()
                        .add(NamedCause.of(TrackingHelper.RESTORING_BLOCK, transaction.getFinal()))
                        .complete());
                transaction.getFinal().restore(true, false);
                completePhase();
            }

            final SpongeBlockSnapshot oldBlockSnapshot = (SpongeBlockSnapshot) transaction.getOriginal();
            final SpongeBlockSnapshot newBlockSnapshot = (SpongeBlockSnapshot) transaction.getFinal();
            SpongeHooks.logBlockAction(cause, this.getMinecraftWorld(), type, transaction);
            final int updateFlag = oldBlockSnapshot.getUpdateFlag();
            final BlockPos pos = VecHelper.toBlockPos(oldBlockSnapshot.getPosition());
            final IBlockState originalState = (IBlockState) oldBlockSnapshot.getState();
            final IBlockState newState = (IBlockState) newBlockSnapshot.getState();
            // TODO fix
            BlockSnapshot currentTickingBlock = null;
            // Containers get placed automatically
            if (!SpongeImplHooks.blockHasTileEntity(newState.getBlock(), newState)) {
                switchToPhase(TrackingPhases.BLOCK, BlockPhase.State.POST_NOTIFICATION_EVENT, PhaseContext.start()
                    .add(NamedCause.source(this.getMixinWorld().createSpongeBlockSnapshot(newState,
                            newState.getBlock().getActualState(newState, proxyBlockAccess, pos), pos, updateFlag)))
                    .complete());
                newState.getBlock().onBlockAdded(this.getMinecraftWorld(), pos, newState);
                if (TrackingHelper.shouldChainCause(this, cause)) {
                    Cause currentCause = cause;
                    List<NamedCause> causes = new ArrayList<>();
                    causes.add(NamedCause.source(currentTickingBlock));
                    List<String> namesUsed = new ArrayList<>();
                    int iteration = 1;
                    final Map<String, Object> namedCauses = currentCause.getNamedCauses();
                    for (Map.Entry<String, Object> entry : namedCauses.entrySet()) {
                        String name = entry.getKey().equalsIgnoreCase("Source")
                                      ? "AdditionalSource" : entry.getKey().equalsIgnoreCase("AdditionalSource")
                                                             ? "PreviousSource" : entry.getKey();
                        if (!namesUsed.contains(name)) {
                            name += iteration++;
                        }
                        namesUsed.add(name);
                        causes.add(NamedCause.of(name, entry.getValue()));
                    }
                    cause = Cause.of(causes);
                }
                completePhase();
            }

            proxyBlockAccess.proceed();
            this.getMixinWorld().markAndNotifyNeighbors(pos, null, originalState, newState, updateFlag);

            // Handle any additional captures during notify
            // This is to ensure new captures do not leak into next tick with wrong cause
            final PhaseData peekedPhase = this.getPhases().peek();
            final Optional<PluginContainer> pluginContainer = peekedPhase.getContext().firstNamed(NamedCause.SOURCE, PluginContainer.class);
            final Optional<List<Entity>> capturedEntities = this.getPhases().peekContext().getCapturedEntities();
            if (!capturedEntities.isPresent() || !capturedEntities.get().isEmpty() && !pluginContainer.isPresent()) {
                // TODO tell the phase to handle any captures.
            }

        }
    }

}
