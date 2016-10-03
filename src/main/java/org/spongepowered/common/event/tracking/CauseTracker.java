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
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.helpers.Booleans;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.phase.TrackingPhase;
import org.spongepowered.common.event.tracking.phase.general.GeneralPhase;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

import java.util.Arrays;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

/**
 * A helper object that is hard attached to a {@link World} that acts as a
 * proxy object entering and processing between different states of the
 * world and its objects.
 */
public final class CauseTracker {

    public static final boolean ENABLED = Booleans.parseBoolean(System.getProperty("sponge.causeTracking"), true);

    static final BiConsumer<PrettyPrinter, PhaseContext> CONTEXT_PRINTER = (printer, context) ->
            context.forEach(namedCause -> {
                        printer.add("        - Name: %s", namedCause.getName());
                        printer.addWrapped(100, "          Object: %s", namedCause.getCauseObject());
                    }
            );

    private static final BiConsumer<PrettyPrinter, PhaseData> PHASE_PRINTER = (printer, data) -> {
        printer.add("  - Phase: %s", data.state);
        printer.add("    Context:");
        data.context.forEach(namedCause -> {
            printer.add("    - Name: %s", namedCause.getName());
            final Object causeObject = namedCause.getCauseObject();
            if (causeObject instanceof PhaseContext) {
                CONTEXT_PRINTER.accept(printer, (PhaseContext) causeObject);
            } else {
                printer.addWrapped(100, "      Object: %s", causeObject);
            }
        });
    };

    private final WorldServer targetWorld;

    private final CauseStack stack = new CauseStack();

    @Nullable private PhaseData currentProcessingState = null;

    private final boolean isVerbose = SpongeImpl.getGlobalConfig().getConfig().getCauseTracker().isVerbose();

    @SuppressWarnings("ConstantConditions")
    public CauseTracker(WorldServer targetWorld) {
        if (((IMixinWorldServer) targetWorld).getCauseTracker() != null) {
            throw new IllegalArgumentException("Attempting to create a new CauseTracker for a world that already has a CauseTracker!!");
        }
        this.targetWorld = targetWorld;
    }

    // ----------------- STATE ACCESS ----------------------------------

    public void switchToPhase(IPhaseState state, PhaseContext phaseContext) {
        checkNotNull(state, "State cannot be null!");
        checkNotNull(state.getPhase(), "Phase cannot be null!");
        checkNotNull(phaseContext, "PhaseContext cannot be null!");
        checkArgument(phaseContext.isComplete(), "PhaseContext must be complete!");
        final IPhaseState currentState = this.stack.peek().state;
        if (this.isVerbose) {
            if (this.stack.size() > 6 && !currentState.isExpectedForReEntrance()) {
                // This printing is to detect possibilities of a phase not being cleared properly
                // and resulting in a "runaway" phase state accumulation.
                printRunawayPhase(state, phaseContext);
            }
            if (!currentState.canSwitchTo(state) && state != GeneralPhase.Post.UNWINDING && currentState == GeneralPhase.Post.UNWINDING) {
                // This is to detect incompatible phase switches.
                printPhaseIncompatibility(currentState, state);
            }
        }

        this.stack.push(state, phaseContext);
    }

    public void completePhase() {
        final PhaseData currentPhaseData = this.stack.peek();
        final IPhaseState state = currentPhaseData.state;
        final boolean isEmpty = this.stack.isEmpty();
        if (isEmpty) {
            if (this.isVerbose) {
                // The random occurrence that we're told to complete a phase
                // while a world is being changed unknowingly.
                new PrettyPrinter(60).add("Unexpected ").centre().hr()
                        .add("Sponge's tracking system is very dependent on knowing when\n"
                             + "a change to any world takes place, however, we have been told\n"
                             + "to complete a \"phase\" without having entered any phases.\n"
                             + "This is an error usually on Sponge's part, so a report\n"
                             + "is required on the issue tracker on GitHub.").hr()
                        .add("  %s : %s", "Affected World", this.targetWorld)
                        .add("StackTrace:")
                        .add(new Exception())
                        .trace(System.err, SpongeImpl.getLogger(), Level.DEBUG);

            }
            return;
        }
        if (this.isVerbose && this.stack.size() > 6 && state != GeneralPhase.Post.UNWINDING && !state.isExpectedForReEntrance()) {
            // This printing is to detect possibilities of a phase not being cleared properly
            // and resulting in a "runaway" phase state accumulation.
            final PrettyPrinter printer = new PrettyPrinter(60);
            printer.add("Completing Phase").centre().hr();
            printer.add("Detecting a runaway phase! Potentially a problem where something isn't completing a phase!!!");
            printer.add("  %s : %s", "Affected World", this.targetWorld);
            printer.addWrapped(60, "%s : %s", "Completing phase", state);
            printer.add(" Phases Remaining:");
            this.stack.forEach(data -> PHASE_PRINTER.accept(printer, data));
            printer.add("Stacktrace:");
            printer.add(new Exception("Stack trace"));
            printer.trace(System.err, SpongeImpl.getLogger(), Level.TRACE);
        }
        this.stack.pop();
        // If pop is called, the Deque will already throw an exception if there is no element
        // so it's an error properly handled.
        final TrackingPhase phase = state.getPhase();
        final PhaseContext context = currentPhaseData.context;
        try {
            if (state != GeneralPhase.Post.UNWINDING && phase.requiresPost(state)) {
                // Note that UnwindingPhaseContext is required for something? I don't think it requires anything tbh.
                switchToPhase(GeneralPhase.Post.UNWINDING, UnwindingPhaseContext.unwind(state, context)
                        .addCaptures()
                        .addEntityDropCaptures()
                        .complete());
            }
            try { // Yes this is a nested try, but in the event the current phase cannot be unwound, at least unwind UNWINDING
                this.currentProcessingState = currentPhaseData;
                phase.unwind(this, state, context);
                this.currentProcessingState = null;
            } catch (Exception e) {
                printMessageWithCaughtException("Exception Exiting Phase", "Something happened when trying to unwind", state, context, e);
            }
            if (state != GeneralPhase.Post.UNWINDING && phase.requiresPost(state)) {
                try {
                    completePhase();
                } catch (Exception e) {
                    printMessageWithCaughtException("Exception attempting to capture or spawn an Entity!", "Something happened trying to unwind", state, context, e);
                }
            }
        } catch (Exception e) {
            printMessageWithCaughtException("Exception Post Dispatching Phase", "Something happened when trying to post dispatch state", state, context, e);
        }
    }

    private void printRunawayPhase(IPhaseState state, PhaseContext context) {
        final PrettyPrinter printer = new PrettyPrinter(40);
        printer.add("Switching Phase").centre().hr();
        printer.add("Detecting a runaway phase! Potentially a problem where something isn't completing a phase!!!");
        printer.add("  %s : %s", "Affected World", this.targetWorld);
        printer.add("  %s : %s", "Entering Phase", state.getPhase());
        printer.add("  %s : %s", "Entering State", state);
        CONTEXT_PRINTER.accept(printer, context);
        printer.addWrapped(60, "%s :", "Phases remaining");
        this.stack.forEach(data -> PHASE_PRINTER.accept(printer, data));
        printer.add("  %s :", "Printing stack trace")
                .add(new Exception("Stack trace"))
                .trace(System.err, SpongeImpl.getLogger(), Level.TRACE);
    }

    private void printPhaseIncompatibility(IPhaseState currentState, IPhaseState incompatibleState) {
        PrettyPrinter printer = new PrettyPrinter(80);
        printer.add("Switching Phase").centre().hr();
        printer.add("Phase incompatibility detected! Attempting to switch to an invalid phase!");
        printer.add("  %s : %s", "Affected World", this.targetWorld);
        printer.add("  %s : %s", "Current Phase", currentState.getPhase());
        printer.add("  %s : %s", "Current State", currentState);
        printer.add("  %s : %s", "Entering incompatible Phase", incompatibleState.getPhase());
        printer.add("  %s : %s", "Entering incompatible State", incompatibleState);
        printer.add("%s :", "Current phases");
        this.stack.forEach(data -> PHASE_PRINTER.accept(printer, data));
        printer.add("  %s :", "Printing stack trace");
        printer.add(new Exception("Stack trace"));
        printer.trace(System.err, SpongeImpl.getLogger(), Level.TRACE);
    }

    private void printMessageWithCaughtException(String header, String subHeader, IPhaseState state, PhaseContext context, Exception e) {
        final PrettyPrinter printer = new PrettyPrinter(40);
        printer.add(header).centre().hr()
                .add("%s %s", subHeader, state)
                .addWrapped(40, "%s :", "PhaseContext");
        printer.add("  %s : %s", "Affected World", this.targetWorld);
        CONTEXT_PRINTER.accept(printer, context);
        printer.addWrapped(60, "%s :", "Phases remaining");
        this.stack.forEach(data -> PHASE_PRINTER.accept(printer, data));
        printer.add("Stacktrace:")
                .add(e)
                .trace(System.err, SpongeImpl.getLogger(), Level.TRACE);
    }

    // ----------------- SIMPLE GETTERS --------------------------------------
//https://github.com/OpenModLoader/OpenModLoader
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

    public PhaseData getCurrentPhaseData() {
        return this.stack.peek();
    }

    public IPhaseState getCurrentState() {
        return this.stack.peekState();
    }

    public PhaseContext getCurrentContext() {
        return this.stack.peekContext();
    }

    public PhaseData getCurrentProcessingPhase() {
        return this.currentProcessingState == null ? CauseStack.EMPTY_DATA : this.currentProcessingState;
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
    @SuppressWarnings("deprecation")
    public void notifyBlockOfStateChange(final BlockPos notifyPos, final Block sourceBlock, @Nullable final BlockPos sourcePos) {
        final IBlockState iblockstate = this.targetWorld.getBlockState(notifyPos);

        try {
            // Sponge start - prepare notification
            if (CauseTracker.ENABLED) {
                final PhaseData peek = this.stack.peek();
                final IPhaseState state = peek.state;
                state.getPhase().associateNeighborStateNotifier(state, peek.context, sourcePos, iblockstate.getBlock(), notifyPos, this.targetWorld, PlayerTracker.Type.NOTIFIER);
            }
            // Sponge End

            iblockstate.getBlock().neighborChanged(iblockstate, this.targetWorld, notifyPos, sourceBlock);
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception while updating neighbours");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being updated");
            crashreportcategory.setDetail("Source block type", () -> {
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

        if (currentState == newState) {
            // Some micro optimization in case someone is trying to set the new state to the same as current
            return false;
        }

        // Now we need to do some of our own logic to see if we need to capture.
        final PhaseData phaseData = this.stack.peek();
        final IPhaseState phaseState = phaseData.state;
        final boolean isComplete = phaseState == GeneralPhase.State.COMPLETE;
        if (CauseTracker.ENABLED && this.isVerbose && isComplete) {
            // The random occurrence that we're told to complete a phase
            // while a world is being changed unknowingly.
            new PrettyPrinter(60).add("Unexpected World Change Detected").centre().hr()
                    .add("Sponge's tracking system is very dependent on knowing when\n"
                         + "a change to any world takes place, however there are chances\n"
                         + "where Sponge does not know of changes that mods may perform.\n"
                         + "In cases like this, it is best to report to Sponge to get this\n"
                         + "change tracked correctly and accurately.").hr()
                    .add("StackTrace:")
                    .add(new Exception())
                    .trace(System.err, SpongeImpl.getLogger(), Level.DEBUG);

        }
        if (CauseTracker.ENABLED && phaseState.getPhase().requiresBlockCapturing(phaseState)) {
            try {
                // Default, this means we've captured the block. Keeping with the semantics
                // of the original method where true means it successfully changed.
                return TrackingUtil.trackBlockChange(this, chunk, currentState, newState, pos, flags, phaseData.context, phaseState);
            } catch (Exception e) {
                final PrettyPrinter printer = new PrettyPrinter(60).add("Exception attempting to capture a block change!").centre().hr();
                printer.addWrapped(40, "%s :", "PhaseContext");
                CONTEXT_PRINTER.accept(printer, phaseData.context);
                printer.addWrapped(60, "%s :", "Phases remaining");
                this.stack.forEach(data -> PHASE_PRINTER.accept(printer, data));
                printer.add("Stacktrace:");
                printer.add(e);
                printer.trace(System.err, SpongeImpl.getLogger(), Level.TRACE);
                return false;
            }
        }
        // Sponge End - continue with vanilla mechanics
        final IBlockState iblockstate = chunk.setBlockState(pos, newState);

        if (iblockstate == null) {
            return false;
        }
        if (newState.getLightOpacity() != iblockstate.getLightOpacity() || newState.getLightValue() != iblockstate.getLightValue()) {
            minecraftWorld.theProfiler.startSection("checkLight");
            minecraftWorld.checkLight(pos);
            minecraftWorld.theProfiler.endSection();
        }

        if ((flags & 2) != 0 && (flags & 4) == 0 && chunk.isPopulated()) {
            minecraftWorld.notifyBlockUpdate(pos, iblockstate, newState, flags);
        }

        if (!minecraftWorld.isRemote && (flags & 1) != 0) {
            minecraftWorld.notifyNeighborsRespectDebug(pos, iblockstate.getBlock());

            if (newState.hasComparatorInputOverride()) {
                minecraftWorld.updateComparatorOutputLevel(pos, newBlock);
            }
        }

        return true;
    }

    public boolean setBlockStateWithFlag(BlockPos pos, IBlockState newState, BlockChangeFlag flag) {
        final net.minecraft.world.World minecraftWorld = this.getMinecraftWorld();
        final Chunk chunk = minecraftWorld.getChunkFromBlockCoords(pos);
        final IMixinChunk mixinChunk = (IMixinChunk) chunk;
        final Block newBlock = newState.getBlock();
        // Sponge Start - Up to this point, we've copied exactly what Vanilla minecraft does.
        final IBlockState currentState = chunk.getBlockState(pos);

        if (currentState == newState) {
            // Some micro optimization in case someone is trying to set the new state to the same as current
            return false;
        }

        // Sponge End - continue with vanilla mechanics
        final IBlockState iblockstate = mixinChunk.setBlockState(pos, newState, currentState, null, flag);

        if (iblockstate == null) {
            return false;
        }
        if (newState.getLightOpacity() != iblockstate.getLightOpacity() || newState.getLightValue() != iblockstate.getLightValue()) {
            minecraftWorld.theProfiler.startSection("checkLight");
            minecraftWorld.checkLight(pos);
            minecraftWorld.theProfiler.endSection();
        }

        if (chunk.isPopulated()) {
            minecraftWorld.notifyBlockUpdate(pos, iblockstate, newState, flag.updateNeighbors() ? 3 : 2);
        }

        if (flag.updateNeighbors()) { // Sponge - remove the isRemote check
            minecraftWorld.notifyNeighborsRespectDebug(pos, iblockstate.getBlock());

            if (newState.hasComparatorInputOverride()) {
                minecraftWorld.updateComparatorOutputLevel(pos, newBlock);
            }
        }

        return true;
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
        final PhaseData phaseData = this.stack.peek();
        final IPhaseState phaseState = phaseData.state;
        final PhaseContext context = phaseData.context;
        final TrackingPhase phase = phaseState.getPhase();
        final boolean isForced = minecraftEntity.forceSpawn || minecraftEntity instanceof EntityPlayer;

        // Certain phases disallow entity spawns (such as block restoration)
        if (!isForced && !phase.allowEntitySpawns(phaseState)) {
            return false;
        }

        // Sponge End - continue with vanilla mechanics
        final int chunkX = MathHelper.floor_double(minecraftEntity.posX / 16.0D);
        final int chunkZ = MathHelper.floor_double(minecraftEntity.posZ / 16.0D);

        if (!isForced && !getMixinWorld().isMinecraftChunkLoaded(chunkX, chunkZ, true)) {
            return false;
        } else {
            if (minecraftEntity instanceof EntityPlayer) {
                EntityPlayer entityplayer = (EntityPlayer) minecraftEntity;
                minecraftWorld.playerEntities.add(entityplayer);
                minecraftWorld.updateAllPlayersSleepingFlag();
                SpongeImplHooks.firePlayerJoinSpawnEvent((EntityPlayerMP) entityplayer);
            }
            // Sponge Start
            // First, check if the owning world is a remote world. Then check if the spawn is forced.
            // Finally, if all checks are true, then let the phase process the entity spawn. Most phases
            // will not actively capture entity spawns, but will still throw events for them. Some phases
            // capture all entities until the phase is marked for completion.
            if (!isForced) {
                try {
                    return phase.spawnEntityOrCapture(phaseState, context, entity, chunkX, chunkZ);
                } catch (Exception e) {
                    // Just in case something really happened, we should print a nice exception for people to
                    // paste us
                    final PrettyPrinter printer = new PrettyPrinter(60).add("Exception attempting to capture or spawn an Entity!").centre().hr();
                    printer.addWrapped(40, "%s :", "PhaseContext");
                    CONTEXT_PRINTER.accept(printer, context);
                    printer.addWrapped(60, "%s :", "Phases remaining");
                    this.stack.forEach(data -> PHASE_PRINTER.accept(printer, data));
                    printer.add("Stacktrace:");
                    printer.add(e);
                    printer.trace(System.err, SpongeImpl.getLogger(), Level.TRACE);
                    return false;
                }
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
        final PhaseData phaseData = this.stack.peek();
        // Sponge End - continue with vanilla mechanics

        final int chunkX = MathHelper.floor_double(minecraftEntity.posX / 16.0D);
        final int chunkZ = MathHelper.floor_double(minecraftEntity.posZ / 16.0D);
        final boolean isForced = minecraftEntity.forceSpawn || minecraftEntity instanceof EntityPlayer;

        if (!isForced && !getMixinWorld().isMinecraftChunkLoaded(chunkX, chunkZ, true)) {
            return false;
        } else {

            // Sponge Start - throw an event

            final SpawnEntityEvent.Custom
                    event =
                    SpongeEventFactory.createSpawnEntityEventCustom(cause, Arrays.asList(entity), getWorld());
            SpongeImpl.postEvent(event);
            if (entity instanceof EntityPlayer || !event.isCancelled()) {
                getMixinWorld().forceSpawnEntity(entity);
            }
            // Sponge end

            return true;
        }
    }

}
