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

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ReportedException;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import org.apache.logging.log4j.Level;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.EventConsumer;
import org.spongepowered.common.event.tracking.phase.BlockPhase;
import org.spongepowered.common.event.tracking.phase.GeneralPhase;
import org.spongepowered.common.event.tracking.phase.TrackingPhase;
import org.spongepowered.common.event.tracking.phase.TrackingPhases;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.BlockChange;
import org.spongepowered.common.world.SpongeProxyBlockAccess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Nullable;

/**
 * A helper object that is hard attached to a {@link World} that acts as a
 * proxy object entering and processing between different states of the
 * world and it's objects.
 */
public final class CauseTracker {

    public static final int DEFAULT_QUEUE_SIZE = 16;
    private final WorldServer targetWorld;

    private final CauseStack stack = new CauseStack(DEFAULT_QUEUE_SIZE);

    public CauseTracker(WorldServer targetWorld) {
        if (((IMixinWorldServer) targetWorld).getCauseTracker() != null) {
            throw new IllegalArgumentException("Attempting to create a new CauseTracker for a world that already has a CauseTracker!!");
        }
        this.targetWorld = targetWorld;
    }

    // ----------------- STATE ACCESS ----------------------------------

    public void switchToPhase(TrackingPhase phase, IPhaseState state, PhaseContext phaseContext) {
        checkNotNull(phase, "Phase cannot be null!");
        checkNotNull(state, "State cannot be null!");
        checkNotNull(phaseContext, "PhaseContext cannot be null!");
        checkArgument(phaseContext.isComplete(), "PhaseContext must be complete!");
        if (this.stack.size() > 6) {
            // This printing is to detect possibilities of a phase not being cleared properly
            // and resulting in a "runaway" phase state accumilation.
            PrettyPrinter printer = new PrettyPrinter(60);
            printer.add("Switching Phase").centre().hr();
            printer.add("Detecting a runaway phase! Potentially a problem where something isn't completing a phase!!!");
            printer.add("  %s : %s", "Entering Phase", phase);
            printer.add("  %s : %s", "Entering State", state);
            printer.add("%s : %s", "Current phases", this.stack.currentStates());
            printer.add("  %s :", "Printing stack trace");
            printer.add(new Exception("Stack trace"));
            printer.trace(System.err, SpongeImpl.getLogger(), Level.TRACE);
        }
        IPhaseState currentState = this.stack.peekState();
        if (!currentState.canSwitchTo(state) || (state != GeneralPhase.Post.UNWINDING || currentState == GeneralPhase.Post.UNWINDING)) {
            // This is to detect incompatible phase switches.
            PrettyPrinter printer = new PrettyPrinter(60);
            printer.add("Switching Phase").centre().hr();
            printer.add("Phase incompatibility detected! Attempting to switch to an invalid phase!");
            printer.add("  %s : %s", "Current Phase", currentState.getPhase());
            printer.add("  %s : %s", "Current State", currentState);
            printer.add("  %s : %s", "Entering incompatible Phase", phase);
            printer.add("  %s : %s", "Entering incompatible State", state);
            printer.add("%s : %s", "Current phases", this.stack.currentStates());
            printer.add("  %s :", "Printing stack trace");
            printer.add(new Exception("Stack trace"));
            printer.trace(System.err, SpongeImpl.getLogger(), Level.TRACE);
        }

        this.stack.push(state, phaseContext);
    }

    public void completePhase() {
        final PhaseData tuple = this.stack.peek();
        IPhaseState state = tuple.getState();
        if (this.stack.size() > 6) {
            // This printing is to detect possibilities of a phase not being cleared properly
            // and resulting in a "runaway" phase state accumilation.
            PrettyPrinter printer = new PrettyPrinter(60);
            printer.add("Completing Phase").centre().hr();
            printer.add("Detecting a runaway phase! Potentially a problem where something isn't completing a phase!!!");
            printer.addWrapped(60, "%s : %s", "Completing phase", state);
            printer.addWrapped(60, "%s : %s", "Phases remaining", this.stack.currentStates());
            printer.add("Stacktrace:");
            printer.add(new Exception("Stack trace"));
            printer.trace(System.err, SpongeImpl.getLogger(), Level.TRACE);
        }
        this.stack.pop();
        // If pop is called, the Deque will already throw an exception if there is no element
        // so it's an error properly handled.
        final TrackingPhase phase = state.getPhase();
        final PhaseContext context = tuple.getContext();
        try {
            if (state != GeneralPhase.Post.UNWINDING && phase.requiresPost(state)) {
                // Note that UnwindingPhaseContext is required for something? I don't think it requires anything tbh.
                switchToPhase(TrackingPhases.GENERAL, GeneralPhase.Post.UNWINDING, UnwindingPhaseContext.unwind(state, context)
                        .addCaptures()
                        .complete());
            }
            try { // Yes this is a nested try, but in the event the current phase cannot be unwound, at least unwind UNWINDING
                phase.unwind(this, state, context);
            } catch (Exception e) {
                final PrettyPrinter printer = new PrettyPrinter(40);
                printer.add("Exception exiting phase").centre().hr();
                printer.add("Something happened when trying to unwind the phase %s", state);
                printer.addWrapped(40, "   %s : %s", "PhaseContext", context);
                printer.addWrapped(60, "   %s : %s", "Phases remaining", this.stack.currentStates());
                printer.add("Stacktrace:");
                printer.add(e);
                printer.trace(System.err, SpongeImpl.getLogger(), Level.ERROR);
            }
            if (state != GeneralPhase.Post.UNWINDING && phase.requiresPost(state)) {
                completePhase();
            }
        } catch (Exception e) {
            final PrettyPrinter printer = new PrettyPrinter(40);
            printer.add("Exception exiting phase").centre().hr();
            printer.add("Something happened when trying to post dispatch state %s", state);
            printer.addWrapped(40, "   %s : %s", "PhaseContext", context);
            printer.addWrapped(60, "   %s : %s", "Phases remaining", this.stack.currentStates());
            printer.add("Stacktrace:");
            printer.add(e);
            printer.trace(System.err, SpongeImpl.getLogger(), Level.ERROR);
        }
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
     * Gets the {@link World} as a Minecraft {@link WorldServer}.
     *
     * @return The world as it's original object
     */
    public WorldServer getMinecraftWorld() {
        return this.targetWorld;
    }

    /**
     * Gets the world casted as a {@link IMixinWorld}.
     *
     * @return The world casted as a mixin world
     */
    public IMixinWorldServer getMixinWorld() {
        return (IMixinWorldServer) this.targetWorld;
    }

    public CauseStack getStack() {
        return this.stack;
    }

    // --------------------- DELEGATED WORLD METHODS -------------------------

    /**
     * Replacement of {@link net.minecraft.world.World#notifyBlockOfStateChange(BlockPos, Block)}
     * that adds tracking into play.
     *
     * @param notifyPos The original notification position
     * @param sourceBlock The source block type
     * @param sourcePos The source block position
     */
    public void notifyBlockOfStateChange(final BlockPos notifyPos, final Block sourceBlock, final BlockPos sourcePos) {
        final IBlockState iblockstate = this.getMinecraftWorld().getBlockState(notifyPos);

        final PhaseData phaseContextTuple = this.getStack().peek(); // Sponge

        try {
            // Sponge start - prepare notification
            final Chunk chunkFromBlockCoords = this.getMinecraftWorld().getChunkFromBlockCoords(notifyPos);
            final Optional<User> packetUser = phaseContextTuple.getContext().firstNamed(TrackingUtil.PACKET_PLAYER, User.class);
            if (packetUser.isPresent()) {
                IMixinChunk spongeChunk = (IMixinChunk) chunkFromBlockCoords;
                if (!(spongeChunk instanceof EmptyChunk)) {
                    spongeChunk.addTrackedBlockPosition(iblockstate.getBlock(), notifyPos, packetUser.get(), PlayerTracker.Type.NOTIFIER);
                }
            } else {
                final PhaseContext context = this.stack.peekContext();
                final Object source;
                final BlockPos predictedPos;

                final Optional<BlockSnapshot> currentTickingBlock = context.firstNamed(NamedCause.SOURCE, BlockSnapshot.class);
                final Optional<TileEntity> currentTickingTile = context.firstNamed(NamedCause.SOURCE, TileEntity.class);
                final Optional<Entity> currentTickingEntity = context.firstNamed(NamedCause.SOURCE, Entity.class);
                if (currentTickingBlock.isPresent()) {
                    source = currentTickingBlock.get();
                    predictedPos = VecHelper.toBlockPos(currentTickingBlock.get().getPosition());
                } else if (currentTickingTile.isPresent()) {
                    source = currentTickingTile.get();
                    predictedPos = ((net.minecraft.tileentity.TileEntity) currentTickingTile.get()).getPos();
                } else if (currentTickingEntity.isPresent()) { // Falling Blocks
                    source = null;
                    IMixinEntity spongeEntity = (IMixinEntity) currentTickingEntity.get();
                    predictedPos = ((net.minecraft.entity.Entity) currentTickingEntity.get()).getPosition();
                    final IMixinChunk spongeChunk = (IMixinChunk) chunkFromBlockCoords;

                    Stream.<Supplier<Optional<User>>>of(
                        () -> spongeEntity.getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_NOTIFIER),
                        () -> spongeEntity.getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR))
                        .map(Supplier::get)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .findFirst()
                        .ifPresent(tracked -> spongeChunk.addTrackedBlockPosition(iblockstate.getBlock(), notifyPos, tracked,
                            PlayerTracker.Type.NOTIFIER));
                } else {
                    source = null;
                    predictedPos = sourcePos;
                }

                if (source != null) {
                    SpongeHooks.tryToTrackBlock(this.getMinecraftWorld(), source, predictedPos, iblockstate.getBlock(), notifyPos,
                        PlayerTracker.Type.NOTIFIER);
                }
            }
            // Sponge End

            iblockstate.getBlock().onNeighborBlockChange(this.getMinecraftWorld(), notifyPos, iblockstate, sourceBlock);
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception while updating neighbours");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being updated");
            crashreportcategory.addCrashSectionCallable("Source block type", () -> {
                try {
                    return String.format("ID #%d (%s // %s)", Block.getIdFromBlock(sourceBlock),
                        sourceBlock.getUnlocalizedName(), sourceBlock.getClass().getCanonicalName());
                } catch (Throwable var2) {
                    return "ID #" + Block.getIdFromBlock(sourceBlock);
                }
            });
            CrashReportCategory.addBlockInfo(crashreportcategory, notifyPos, iblockstate);
            throw new ReportedException(crashreport);
        }
    }

    /**
     * Replacement of {@link WorldServer#setBlockState(BlockPos, IBlockState, int)}
     * that adds cause tracking.
     *
     * @param pos The position of the block state to set
     * @param newState The new state
     * @param flags The notification flags
     * @return True if the block was successfully set (or captured)
     */
    public boolean setBlockState(final BlockPos pos, final IBlockState newState, final int flags) {
        final net.minecraft.world.World minecraftWorld = this.getMinecraftWorld();
        final Chunk chunk = minecraftWorld.getChunkFromBlockCoords(pos);
        final Block newBlock = newState.getBlock();
        // Sponge Start - Up to this point, we've copied exactly what Vanilla minecraft does.
        final IBlockState currentState = chunk.getBlockState(pos);
        final Block currentBlock = currentState.getBlock();

        if (currentState == newState) {
            // Some micro optimization in case someone is trying to set the new state to the same as current
            return false;
        }

        // Now we need to do some of our own logic to see if we need to capture.
        final PhaseData phaseData = this.getStack().peek();
        final IPhaseState phaseState = phaseData.getState();
        final TrackingPhase phase = phaseState.getPhase();
        if (phase.requiresBlockCapturing(phaseState)) {
            phase.captureBlockChange(this, currentState, newState, newBlock, pos, flags, phaseData.getContext(), phaseState);
            return true; // Default, this means we've captured the block. Keeping with the semantics
            // of the original method where true means it successfully changed.
        } else {
            // Sponge End - continue with vanilla mechanics
            final IBlockState iblockstate = chunk.setBlockState(pos, newState);

            if (iblockstate == null) {
                return false;
            } else {
                final Block currentblock = iblockstate.getBlock();

                if (newBlock.getLightOpacity() != currentblock.getLightOpacity() || newBlock.getLightValue() != currentblock.getLightValue()) {
                    minecraftWorld.theProfiler.startSection("checkLight");
                    minecraftWorld.checkLight(pos);
                    minecraftWorld.theProfiler.endSection();
                }

                if ((flags & 2) != 0 && (!minecraftWorld.isRemote || (flags & 4) == 0) && chunk.isPopulated()) {
                    minecraftWorld.markBlockForUpdate(pos);
                }

                if (!minecraftWorld.isRemote && (flags & 1) != 0) {
                    minecraftWorld.notifyNeighborsRespectDebug(pos, iblockstate.getBlock());

                    if (newBlock.hasComparatorInputOverride()) {
                        minecraftWorld.updateComparatorOutputLevel(pos, newBlock);
                    }
                }

                return true;
            }
        }
    }

    /**
     * This is the replacement of {@link WorldServer#spawnEntityInWorld(net.minecraft.entity.Entity)}
     * where it captures into phases. The causes and relations are processed by the phases.
     *
     * The difference between {@link #spawnEntityWithCause(Entity, Cause)} is that it bypasses
     * any phases and directly throws a spawn entity event.
     *
     * @param entity The entity
     * @return True if the entity spawn was successful
     */
    public boolean spawnEntity(Entity entity) {
        checkNotNull(entity, "Entity cannot be null!");

        // Sponge Start - handle construction phases
        if (((IMixinEntity) entity).isInConstructPhase()) {
            ((IMixinEntity) entity).firePostConstructEvents();
        }

        final net.minecraft.entity.Entity minecraftEntity = EntityUtil.toNative(entity);
        final WorldServer minecraftWorld = this.getMinecraftWorld();
        final PhaseData phaseData = this.getStack().peek();
        final IPhaseState phaseState = phaseData.getState();
        final PhaseContext context = phaseData.getContext();
        final TrackingPhase phase = phaseState.getPhase();
        // Certain phases disallow entity spawns (such as block restoration)
        if (!phase.allowEntitySpawns(phaseState)) {
            return false;
        }

        // Sponge End - continue with vanilla mechanics
        final int chunkX = MathHelper.floor_double(minecraftEntity.posX / 16.0D);
        final int chunkZ = MathHelper.floor_double(minecraftEntity.posZ / 16.0D);
        final boolean isForced = minecraftEntity.forceSpawn || minecraftEntity instanceof EntityPlayer;

        if (!isForced && !minecraftWorld.isChunkLoaded(chunkX, chunkZ, true)) {
            return false;
        } else {
            if (minecraftEntity instanceof EntityPlayer) {
                EntityPlayer entityplayer = (EntityPlayer) minecraftEntity;
                minecraftWorld.playerEntities.add(entityplayer);
                minecraftWorld.updateAllPlayersSleepingFlag();
            }
            // Sponge Start
            // First, check if the owning world is a remote world. Then check if the spawn is forced. Then finally check
            // that the phase does not need to actually capture the entity spawn (at which case
            if (!isForced && phase.attemptEntitySpawnCapture(phaseState, context, entity, chunkX, chunkZ)) {
                return true;
            }
            // Sponge end - continue on with the checks.
            minecraftWorld.getChunkFromChunkCoords(chunkX, chunkZ).addEntity(minecraftEntity);
            minecraftWorld.loadedEntityList.add(minecraftEntity);
            getMixinWorld().onSpongeEntityAdded(minecraftEntity); // Sponge - Cannot add onEntityAdded to the access transformer because forge makes it public
            return true;
        }
    }

    /**
     * The core implementation of {@link World#spawnEntity(Entity, Cause)} that
     * bypasses any sort of cause tracking and throws an event directly
     *
     * @param entity
     * @param cause
     * @return
     */
    public boolean spawnEntityWithCause(Entity entity, Cause cause) {
        checkNotNull(entity, "Entity cannot be null!");
        checkNotNull(cause, "Cause cannot be null!");

        // Sponge Start - handle construction phases
        if (((IMixinEntity) entity).isInConstructPhase()) {
            ((IMixinEntity) entity).firePostConstructEvents();
        }

        final net.minecraft.entity.Entity minecraftEntity = EntityUtil.toNative(entity);
        final WorldServer minecraftWorld = this.getMinecraftWorld();
        final PhaseData phaseData = this.getStack().peek();
        final IPhaseState phaseState = phaseData.getState();
        // Sponge End - continue with vanilla mechanics

        final int chunkX = MathHelper.floor_double(minecraftEntity.posX / 16.0D);
        final int chunkZ = MathHelper.floor_double(minecraftEntity.posZ / 16.0D);
        final boolean isForced = minecraftEntity.forceSpawn || minecraftEntity instanceof EntityPlayer;

        if (!isForced && !minecraftWorld.isChunkLoaded(chunkX, chunkZ, true)) {
            return false;
        } else {
            if (minecraftEntity instanceof EntityPlayer) {
                EntityPlayer entityplayer = (EntityPlayer) minecraftEntity;
                minecraftWorld.playerEntities.add(entityplayer);
                minecraftWorld.updateAllPlayersSleepingFlag();
            }

            // Sponge Start - throw an event
            EventConsumer.event(SpongeEventFactory.createSpawnEntityEventCustom(cause, Arrays.asList(entity), ImmutableList.of(entity.createSnapshot()), getWorld()))
                .nonCancelled(event -> {
                    // Sponge end
                    minecraftWorld.getChunkFromChunkCoords(chunkX, chunkZ).addEntity(minecraftEntity);
                    minecraftWorld.loadedEntityList.add(minecraftEntity);
                    // Sponge start
                    getMixinWorld().onSpongeEntityAdded(minecraftEntity); // Sponge - Cannot add onEntityAdded to the access transformer because forge makes it public
                })
                .process();
                // Sponge end

            return true;
        }
    }

    // --------------------- POPULATOR DELEGATED METHODS ---------------------

    public void markAndNotifyBlockPost(List<Transaction<BlockSnapshot>> transactions, @Nullable BlockChange type, Cause cause) {
        SpongeProxyBlockAccess proxyBlockAccess = new SpongeProxyBlockAccess(this.getMinecraftWorld(), transactions);
        for (Transaction<BlockSnapshot> transaction : transactions) {
            if (!transaction.isValid()) {
                continue; // Don't use invalidated block transactions during notifications, these only need to be restored
            }
            // Handle custom replacements
            if (transaction.getCustom().isPresent()) {
                switchToPhase(TrackingPhases.BLOCK, BlockPhase.State.RESTORING_BLOCKS, PhaseContext.start()
                        .add(NamedCause.of(TrackingUtil.RESTORING_BLOCK, transaction.getFinal()))
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
                final IBlockState extendedState = newState.getBlock().getActualState(newState, proxyBlockAccess, pos);
                final BlockSnapshot blockSnapshot = this.getMixinWorld().createSpongeBlockSnapshot(newState, extendedState, pos, updateFlag);
//                switchToPhase(TrackingPhases.BLOCK, BlockPhase.State.POST_NOTIFICATION_EVENT, PhaseContext.start()
//                    .add(NamedCause.source(blockSnapshot))
//                    .complete());
                newState.getBlock().onBlockAdded(this.getMinecraftWorld(), pos, newState);
                if (TrackingUtil.shouldChainCause(this, cause)) {
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
//                completePhase();
            }

            proxyBlockAccess.proceed();
            this.getMixinWorld().markAndNotifyNeighbors(pos, null, originalState, newState, updateFlag);

            // Handle any additional captures during notify
            // This is to ensure new captures do not leak into next tick with wrong cause
            final PhaseData peekedPhase = this.getStack().peek();
            final Optional<PluginContainer> pluginContainer = peekedPhase.getContext().firstNamed(NamedCause.SOURCE, PluginContainer.class);
            final Optional<PhaseContext.CapturedSupplier<Entity>> capturedEntities = this.getStack().peekContext().getCapturedEntitySupplier();
            if (!capturedEntities.isPresent() || !capturedEntities.get().isEmpty() && !pluginContainer.isPresent()) {
                // TODO tell the phase to handle any captures.
            }

        }
    }


}
