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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEventData;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.server.ServerWorld;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.World;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.bridge.block.BlockEventDataBridge;
import org.spongepowered.common.bridge.world.ServerWorldBridge;
import org.spongepowered.common.bridge.world.chunk.ChunkBridge;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.context.transaction.ChangeBlock;
import org.spongepowered.common.event.tracking.phase.entity.EntityPhase;
import org.spongepowered.common.event.tracking.phase.general.ExplosionContext;
import org.spongepowered.common.event.tracking.phase.generation.GenerationPhase;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhase;
import org.spongepowered.common.event.tracking.phase.tick.BlockTickContext;
import org.spongepowered.common.event.tracking.phase.tick.TickPhase;
import org.spongepowered.common.world.BlockChange;
import org.spongepowered.math.vector.Vector3i;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * A literal phase state of which the {@link World} is currently running
 * in. As these should be enums, there's no data that should be stored on
 * this state. It can have control flow with {@link #isNotReEntrant()}
 * where preventing switching to another state is possible (likely points out
 * either errors or runaway states not being unwound).
 */
@DefaultQualifier(NonNull.class)
public interface IPhaseState<C extends PhaseContext<C>> {

    BiConsumer<CauseStackManager.StackFrame, ? extends PhaseContext<?>> DEFAULT_OWNER_NOTIFIER = (frame, ctx) -> {
        if (ctx.usedFrame == null) {
            ctx.usedFrame = new ArrayDeque<>();
        }
        ctx.usedFrame.push(frame); // WE NEED TO STORE THIS SO WE CAN PROPERLY POP THE FRAME
        if (ctx.creator != null) {
            frame.addContext(EventContextKeys.CREATOR, ctx.creator);
        }
        if (ctx.notifier != null) {
            frame.addContext(EventContextKeys.NOTIFIER, ctx.notifier);
        }
    };

    /**
     * Creates a minimalized {@link PhaseContext} for this specific state. In some cases,
     * the context will be pre-populated with captures and essentially 'set up' with
     * fields for the state's expected usage.
     *
     * @return The new phase context
     * @param server The PhaseTracker instance for thread handling
     */
    C createPhaseContext(PhaseTracker server);

    /**
     * Gets the frame modifier for default frame modifications, like pushing
     * the source of the phase, owner, notifier, etc. of the context. Used specifically
     * for lazy evaluating stack frames to push causes and contexts guaranteed at any point
     * in this state.
     * @return
     */
    @SuppressWarnings("unchecked")
    default BiConsumer<CauseStackManager.StackFrame, C> getFrameModifier() {
        return (BiConsumer<CauseStackManager.StackFrame, C>) IPhaseState.DEFAULT_OWNER_NOTIFIER; // Default does nothing
    }

    /**
     * Gets whether this phase is expected to potentially re-enter itself, in some cases where
     * other operations tend to cause extra operations being performed. Examples include but are
     * not limited to: World Generation, {@link GenerationPhase.State#TERRAIN_GENERATION} or
     * {@link GenerationPhase.State#POPULATOR_RUNNING}. If thi
     *
     * @return True if this phase is potentially expected to re-enter on itself
     */
    default boolean isNotReEntrant() {
        return true;
    }

    /**
     * Gets whether this state is considered an interaction, specifically to determine
     * whether a pre-block event check can be performed prior to actual block modifications
     * are done and potentially "captured" as a result. This is specific to allow mod compatibility
     * with common protection plugins having the ability to determine whether a proposed block
     * change is allowed or not.
     *
     * @return Whether this state is considered a player caused interaction or not
     */
    default boolean isInteraction() {
        return false;
    }

    /**
     * Gets whether this state is considered a "ticking" state. Specifically such that when
     * {@link Chunk#getEntitiesWithinAABBForEntity(Entity, AxisAlignedBB, List, Predicate)} is used,
     * we are not filtering any of the lists, whereas if this state is a ticking state, it will
     * filter the proposed list of entities to supply any potentially captured entities.
     *
     * @return Whether this state is a ticking state or not
     */
    default boolean isTicking() {
        return false;
    }

    /**
     * Gets whether this state is considered a "world generation" state. Usually world generation
     * is a common flag to say "hey, don't bother capturing anything". So, as it would be expected,
     * block changes, entity spawns, and whatnot are not tracked in any way during generation
     * states.
     *
     * @return Whether this state is a world generation state or not
     */
    default boolean isWorldGeneration() {
        return false;
    }

    /**
     * If this returns {@link true}, block decays will be processed in this
     * phase state. If this returns {@link false}, block decays will be
     * processed in a separate phase state.
     *
     * @return Whether this phase should track decays
     */
    default boolean includesDecays() {
        return false;
    }

    /**
     * Specifically designed to allow certain registries use the event listener hooks to prevent unnecessary off-threaded
     * checks and allows for registries to restrict additional registrations ouside of events.
     *
     * @return True if this is an event listener state
     */
    default boolean isEvent() {
        return false;
    }

    /**
     * The exit point of any phase. Every phase should have an unwinding
     * process where if anything is captured, events should be thrown and
     * processed accordingly. The outcome of each phase is dependent on
     * the {@link IPhaseState} provided, as different states require different
     * handling.
     *
     * <p>Examples of this include: {@link PacketPhase}, {@link TickPhase}, etc.
     * </p>
     *
     * <p>Note that the {@link PhaseTracker} is only provided for easy access
     * to the {@link ServerWorld}, {@link ServerWorldBridge}, and
     * {@link World} instances.</p>
     *
     * @param phaseContext The context of the current state being unwound
     */
    void unwind(C phaseContext);

    /**
     * Based on whether this state is allowed to capture entity spawns in bulk
     * for later processing in {@link #unwind(PhaseContext)}, or whether entities
     * are to be spawned directly after throwing an event is used here. By default,
     * this will create and call a single {@link SpawnEntityEvent} and then spawn
     * the entity. Other states may override and provide their own custom handling
     * based on various situations (like world generation).
     *
     * <p>NOTE: This method should only be called and handled if and only if {@link IPhaseState#doesAllowEntitySpawns()}
     * returns {@code true}. Violation of this will have unforeseen consequences.</p>
     *
     *
     * @param context The current context
     * @param entity The entity being captured
     * @return True if the entity was successfully captured
     */
    default boolean spawnEntityOrCapture(final C context, final org.spongepowered.api.entity.Entity entity) {
        final ArrayList<org.spongepowered.api.entity.Entity> entities = new ArrayList<>(1);
        entities.add(entity);
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PASSIVE);
            return SpongeCommonEventFactory.callSpawnEntity(entities, context);
        }
    }

    /**
     * A phase specific method that determines whether it is needed to capture the entity based onto the
     * entity-specific lists of drops, or a generalized list of drops.
     *
     * Cases for entity specific drops:
     * - Explosions
     * - Entity deaths
     * - Commands killing mass entities and those entities dropping items
     *
     * Cases for generalized drops:
     * - Phase states for specific entity deaths
     * - Phase states for generalization, like packet handling
     * - Using items
     *
     * @param phaseContext The current context
     * @param entity The entity performing the drop or "source" of the drop
     * @param entityitem The item to be dropped
     * @return True if we are capturing, false if we are to let the item spawn
     */
    default boolean spawnItemOrCapture(final C phaseContext, final Entity entity, final ItemEntity entityitem) {
        if (this.doesCaptureEntityDrops(phaseContext)) {
            // Return the item, even if it wasn't spawned in the world.
            return true;
        }
        return false;
    }

    /**
     * Used to create any extra specialized events for {@link ChangeBlockEvent.Post} as necessary.
     * An example of this being used specially is for explosions needing to create a child classed
     * post event.
     *
     * @param context
     * @param transactions
     * @param cause
     * @return
     */
    default ChangeBlockEvent.Post createChangeBlockPostEvent(final C context, final ImmutableList<Transaction<BlockSnapshot>> transactions,
        final Cause cause) {
        return SpongeEventFactory.createChangeBlockEventPost(cause, transactions);
    }

    /**
     * Performs any necessary custom logic after the provided {@link BlockSnapshot}
     * {@link Transaction} has taken place.
     *
     * @param blockChange The block change performed
     * @param snapshotTransaction The transaction of the old and new snapshots
     * @param context The context for information
     */
    default void postBlockTransactionApplication(final BlockChange blockChange, final Transaction<? extends BlockSnapshot> snapshotTransaction,
        final C context) { }

    /**
     * During {@link UnwindingState#unwind(UnwindingPhaseContext)}, this delegates to the "unwinding" state to perform
     * any extra handling with contexts to spawn entities that were captured.
     *
     * @param unwindingContext
     * @param entities
     */
    default void postProcessSpawns(final C unwindingContext, final ArrayList<org.spongepowered.api.entity.Entity> entities) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.BLOCK_SPAWNING);
            SpongeCommonEventFactory.callSpawnEntity(entities, unwindingContext);
        }
    }

    /**
     * Specifically gets whether this state ignores any attempts at storing
     * or retrieving an owner/notifier from a particular {@link BlockPos}
     * within a {@link net.minecraft.world.World} or {@link Chunk}.
     *
     * <p>Specifically used in
     * {@code ChunkMixin_OwnershipTracked#bridge$addTrackedBlockPosition(Block, BlockPos, User, PlayerTracker.Type)}
     * to make sure that the current state would be providing said information,
     * instead of spending the processing to query for it.</p>
     *
     * @return Simple true false to determine whether this phase is providing owner/notifier information
     */
    default boolean tracksCreatorsAndNotifiers() {
        return true;
    }
    /**
     * Gets whether this state specifically supports splitting up {@link Block#spawnDrops(BlockState, net.minecraft.world.World, BlockPos, TileEntity, Entity, net.minecraft.item.ItemStack)}
     * drops as some blocks may drop multiple items at once. In some cases, the individual block
     * transactions can be associated directly with captured item/entity spawns. In other
     * cases, we cannot safely perform these captures as some mods may be expecting those items to
     * throw events immediately after the blocks have been changed. In other cases, like when an
     * explosion occurs, we can safely track the block drops and entity spawns per block.
     *
     * <p>This has potential for being configurable on a block id based basis.</p>
     * @return Whether per-block drops are being captured
     * @param context
     */
    default boolean tracksBlockSpecificDrops(final C context) {
        return false;
    }
    /**
     * Gets whether this state will capture entity spawns per entity during this specific phase.
     * Occasionally we can expect some phases to be able to determine these custom drops per
     * entity, however, they are far and few. Some to name the least will be certain ones like
     * Commands, explosions, etc. where multiple entities can be killed and drops can occur.
     *
     * @return True if this phase is aware enough to handle entity death drops per entity, or will
     *     cause {@link EntityPhase.State#DEATH} to be entered and handle it's own drops
     */
    default boolean tracksEntitySpecificDrops() {
        return false;
    }

    /**
     * Gets whether this state is performing logic/captures for entity deaths. Usually deaths
     * are very specific depending on the {@link DamageSource} that killed an entity, so
     * depending on the phase state this is, we may need to switch to {@link EntityPhase.State#DEATH}
     * when an entity is dying. Naturally, this state will be "parented" so the death state will
     * enter and exit, possibly multiple times.
     *
     * Specifically however, this means that entity, item, and block captures related to
     * an entity dying will be handled in THIS state if this returns true.
     *
     * @return True if this state already handles the block, item, and entity captures for
     *      entity deaths
     */
    default boolean tracksEntityDeaths() {
        return false;
    }

    /**
     * Gets whether this {@link IPhaseState} is going to actually capture entity drops,
     * or whether entity drops are going to be directly spawned into the world (potentially with
     * an event, depending on {@link #doesDropEventTracking(PhaseContext)}).
     *
     * @param context The context, usually to provide the boolean value depending on the source of the phase
     * @return True if entity drops are captured
     */
    default boolean doesCaptureEntityDrops(final C context) {
        return false;
    }

    /**
     * Gets whether this state will allow entities to spawn, in general, not whether they're captured,
     * directly spawned, or throw an event, but whether the entity will be *able* to spawn. In general
     * this is returned {@code false} for block restoration, since restoring blocks is a restorative
     * process, we should not be respawning any entities as a side effect.
     *
     * @return True if entities are allowed to spawn
     */
    default boolean doesAllowEntitySpawns() {
        return true;
    }

    /**
     * Gets whether this state, with the provided context, will perform bulk block capturing to
     * later perform said block mechanics during {@link #unwind(PhaseContext)}. This is usually
     * dependent on the provided {@link PhaseContext} since some contexts cannot be bulk capturing
     * due to mod compatibility reasons. In vanilla, everything usually can be bulk captured,
     * except in corner cases for things like the ender dragon due to their precise nature.
     *
     * @param context The context to provide extra information whether captures can take place
     * @return True or false
     */
    default boolean doesBulkBlockCapture(final C context) {
        return true;
    }

    /**
     * Whether this state can deny chunk load/generation requests. Certain states can allow them
     * and certain others can deny them. Usually the denials are coming from states like ticks
     * where we are not intending to allow chunks to be loaded due to possible generation and
     * runaway chunk loading.
     *
     * @return Whether this state denies chunk requests, usually false
     */
    default boolean doesDenyChunkRequests(final C context) {
        return false;
    }

    /**
     * An alternative to {@link #doesBulkBlockCapture(PhaseContext)} to where if capturing is expressly
     * disabled, we can still track the block change through normal methods, and throw events,
     * but we won't be capturing directly or delaying any block related physics.
     *
     * <p>If this and {@link #doesBulkBlockCapture(PhaseContext)} both return {@code false}, vanilla
     * mechanics will take place, and no tracking or capturing is taking place unless otherwise
     * noted by
     * {@link #associateNeighborStateNotifier(PhaseContext, BlockPos, Block, BlockPos, ServerWorld, PlayerTracker.Type)}</p>
     *
     * @return True by default, false for things like world gen
     * @param context
     */
    default boolean doesBlockEventTracking(final C context) {
        return true;
    }

    /**
     * An alternative to {@link #doesCaptureEntityDrops(PhaseContext)} to where if capturing is expressly
     * disabled, we can still track the item drop through normal methods, and throw events, but the items
     * will not be directly added to our capture lists.
     * // TODO - not implemented as of yet. Supposed to mimic what we did for block events
     * @param context
     * @return
     */
    // TODO -implement this into the config, and wherever else
    default boolean doesDropEventTracking(final C context) {
        return true;
    }

    /**
     * Gets whether this state will ignore triggering entity collision events or not. Since there are
     * many states that perform operations that would be slowed down by having spammed events, we
     * can occasionally ignore collision events for those states. Examples include world generation,
     * or explosions.
     *
     * @return Whether this state will throw entity collision events when calling {@link Chunk#getEntitiesWithinAABBForEntity(Entity, AxisAlignedBB, List, java.util.function.Predicate)}
     */
    default boolean ignoresEntityCollisions() {
        return false;
    }

    /**
     * Gets whether this state will ignore {@link net.minecraft.world.World#addBlockEvent(BlockPos, Block, int, int)}
     * additions when potentially performing notification updates etc. Usually true for world generation.
     *
     * @return False if block events are to be processed in some way by the state
     */
    default boolean ignoresBlockEvent() {
        return false;
    }

    /**
     * Gets whether this state will already consider any captures or extra processing for a
     * {@link Block#tick(BlockState, net.minecraft.world.World, BlockPos, Random)}. Again usually
     * considered for world generation or post states or block restorations.
     *
     * @param context The phase data currently present
     * @return True if it's going to be ignored
     */
    default boolean ignoresBlockUpdateTick(final C context) {
        return false;
    }

    /**
     * Gets whether this state will need to perform any extra processing for
     * scheduled block updates, specifically linking the block update event to
     * the world, the state and possibly context. Usually only necessary for
     * post states so that no extra processing takes place.
     *
     * @return False if scheduled block updates are normally processed
     */
    default boolean ignoresScheduledUpdates() {
        return false;
    }

    /**
     * Gets whether this state will specifically ignore attempting to merge {@link ItemStack}s
     * within capture lists and avoid creating the {@link ItemEntity} speicifcally. In some cases
     * however, these items need to be directly created as entities for them to be acted upon
     * during the phase process and therefor cannot be captured. Examples can include where
     * mods are attempting to modify the captured entities by providing their own form of a
     * "captured" loot bag of sorts.
     *
     * @return True if itemstack pre-merging is ignored
     */
    default boolean ignoresItemPreMerging() {
        return false;
    }

    /**
     * Gets whether this state will capture the provided position block change, or not.
     * This does not bypass the creation of the block changes, it just bypasses whether
     * the block change is going to be captured. May be qualified for removal pending some
     * cleanup with block captures and method duplications.
     *
     * @param phaseContext
     * @param pos
     * @param currentState
     * @param newState
     * @param flags
     * @return
     */
    default boolean shouldCaptureBlockChangeOrSkip(final C phaseContext, final BlockPos pos, final BlockState currentState,
        final BlockState newState, final BlockChangeFlag flags) {
        return true;
    }

    /**
     * Gets whether this state is already capturing block tick changes, specifically in
     * that some states (like post) will be smart enough to capture multiple changes for
     * multiple block positions without the need to enter new phases. Currently gone unused
     * since some refactor.
     * // TODO - clean up usage? Find out where this came from and why it was used
     *
     * @param context
     * @return
     */
    default boolean alreadyCapturingBlockTicks(final C context) {
        return false;
    }

    /**
     * Gets whether this state is already capturing custom entity spawns from plugins.
     * Examples include listener states, post states, or explosion states.
     *
     * @return True if entity spawns are already expected to be processed
     */
    default boolean alreadyCapturingEntitySpawns() {
        return false;
    }

    /**
     * Gets whether this state is already expecting to capture or process changes from
     * entity ticks. Usually only used for Post states.
     *
     * @return True if entity tick processing is already handled in this state
     */
    default boolean alreadyCapturingEntityTicks() {
        return false;
    }

    /**
     * Gets whether this state is already expecting to capture or process changes from
     * tile entity ticks. Used in Post states. (this avoids re-entering new phases during post processing)
     *
     * @return True if entity tick processing is already handled in this state
     */
    default boolean alreadyCapturingTileTicks() {
        return false;
    }

    /**
     * Gets whether this state is alraedy expecting to capture or process item drops from
     * blocks. Usually used for post states, explosions, interaction packets, and a few other cases.
     *
     * @return
     */
    default boolean alreadyProcessingBlockItemDrops() {
        return false;
    }

    /**
     * Gets whether this state is expecting to capture a block position. Used for explosions
     * to determine where the origination of the explosion took place.
     *
     * @return True if a block position is going to be captured for explosions
     */
    default boolean requiresBlockPosTracking() {
        return false;
    }

    /**
     * Gets whether this state requires a post state entry for any captured objects. Usually
     * does not, get used uless this is already a post state, or an invalid packet state.
     * TODO - Investigate whether world generation states could use this.
     *
     * @return True if this state is expecting to be unwound with an unwinding state to cpature additional changes
     */
    default boolean requiresPost() {
        return true;
    }


    /**
     * Gets whether this state is going to complete itself for plugin provided
     * changes. Used for BlockWorkers.
     * TODO - Investigate whether we can enable listener phase states to handle
     * this as well.
     * @return True if this state does not need a custom block worker state for plugin changes
     */
    default boolean handlesOwnStateCompletion() {
        return false;
    }

    /**
     * Associates any notifier/owner information from expected states that will assuredly provide
     * said information. In some states, like world gen, there is no information to provide.
     *
     * @param unwindingContext The unwinding context providing context information
     * @param sourcePos The source position performing the notification
     * @param block The block type providing the notification
     * @param notifyPos The notified position
     * @param minecraftWorld The world
     * @param notifier The tracker type (owner or notifier)
     */
    default void associateNeighborStateNotifier(final C unwindingContext, @Nullable final BlockPos sourcePos, final Block block, final BlockPos notifyPos,
        final ServerWorld minecraftWorld, final PlayerTracker.Type notifier) {

    }

    /**
     * Provides additional information from this state in the event an explosion is going to be
     * performed, providing information like entity owners, notifiers, or potentially even sources
     * from blocks.
     *
     * @param explosionContext The explosion context to populate
     * @param currentPhaseData The current context to provide information
     */
    default void appendContextPreExplosion(final ExplosionContext explosionContext, final C currentPhaseData) {

    }

    /**
     * Appends additional information from the block's position in the world to provide notifier/owner
     * information. Overridden in world generation states to reduce chunk lookup costs and since
     * world generation does not track owners/notifiers.
     *
     * @param world The world reference
     * @param pos The position being updated
     * @param context The context
     * @param phaseContext the block tick context being entered
     */
    default void appendNotifierPreBlockTick(final ServerWorld world, final BlockPos pos, final C context, final BlockTickContext phaseContext) {
        final Chunk chunk = world.getChunkAt(pos);
        final ChunkBridge mixinChunk = (ChunkBridge) chunk;
        if (chunk != null && !chunk.isEmpty()) {
            mixinChunk.bridge$getBlockCreator(pos).ifPresent(phaseContext::creator);
            mixinChunk.bridge$getBlockNotifier(pos).ifPresent(phaseContext::notifier);
        }
    }

    /**
     * Appends any additional information to the block tick context from this context.
     *  @param context
     * @param currentContext
     * @param mixinWorldServer
     * @param pos
     * @param blockEvent
     */
    default void appendNotifierToBlockEvent(final C context, final PhaseContext<?> currentContext,
        final ServerWorldBridge mixinWorldServer, final BlockPos pos, final BlockEventDataBridge blockEvent) {

    }

    /**
     * Attempts to capture the player using the item stack in this state. Some states do not care for
     * this information. Usually packets do care and some scheduled tasks.
     *
     * @param itemStack
     * @param playerIn
     * @param context
     */
    default void capturePlayerUsingStackToBreakBlock(final ItemStack itemStack, final ServerPlayerEntity playerIn, final C context) {

    }

    /**
     * Used in the {@link org.spongepowered.api.event.EventManager} and mod event manager equivalent for
     * world generation tasks to avoid event listener state entrance due to listeners
     * during world generation performing various operations that should not be tracked.
     *
     * <p>Refer to spongeforge issue:
     * https://github.com/SpongePowered/SpongeForge/issues/2407#issuecomment-415850841
     * for more information and context of why this is needed.
     * </p>
     *
     * @return True if an {@link org.spongepowered.common.event.tracking.phase.plugin.PluginPhase.Listener#GENERAL_LISTENER}
     *     is to be entered during this state
     */
    default boolean allowsEventListener() {
        return true;
    }

    /**
     * Gets the populator offset for the given {@link Chunk} that will be passed to
     * {@link Feature}s. Normally, during any sort of world generation, the offset
     * is 8, but sometimes, for chunk regeneration, we don't want to use an offset.
     *
     * @param chunk The chunk
     * @param chunkX the x position
     * @param chunkZ the z position
     * @return The chunk populator offset
     */
    default Vector3i getChunkPopulatorOffset(final org.spongepowered.api.world.chunk.Chunk chunk, final int chunkX, final int chunkZ) {
        return  new Vector3i(chunkX * 16 + 8, 0, chunkZ * 16 + 8);
    }

    default boolean isRegeneration() {
        return false;
    }

    default boolean getShouldCancelAllTransactions(final C context, final List<ChangeBlockEvent> blockEvents, final ChangeBlockEvent.Post postEvent,
        final ListMultimap<BlockPos, BlockEventData> scheduledEvents, final boolean noCancelledTransactions) {
        return false;
    }

    /**
     * Specifically captures a block change by {@link org.spongepowered.common.event.tracking.context.transaction.TransactionalCaptureSupplier#logBlockChange(SpongeBlockSnapshot, BlockState, BlockChangeFlag)}
     * such that the change of a {@link BlockState} will be appropriately logged, along with any changes of tile entities being removed
     * or added, likewise, this will avoid duplicating transactions later after the fact, in the event that multiple changes are taking
     * place, including but not withstanding, tile entity replacements after the fact.
     * @return
     */
    default ChangeBlock createTransaction(final C phaseContext,
        final SpongeBlockSnapshot originalBlockSnapshot, final BlockState newState, final BlockChangeFlag flags) {
        final ChangeBlock changeBlock = phaseContext.getTransactor()
            .logBlockChange(originalBlockSnapshot, newState, flags);

        return changeBlock;
    }

    default boolean doesCaptureNeighborNotifications(final C context) {
        return false;
    }

    default BlockChange associateBlockChangeWithSnapshot(final C phaseContext, final BlockState newState, final Block newBlock,
        final BlockState currentState, final SpongeBlockSnapshot snapshot,
        final Block originalBlock) {
        if (newBlock == Blocks.AIR) {
            return BlockChange.BREAK;
        } else if (newBlock != originalBlock && !TrackingUtil.forceModify(originalBlock, newBlock)) {
            return BlockChange.PLACE;
        }
        return BlockChange.MODIFY;
    }

    /**
     * Gets whether this {@link IPhaseState} entry with a provided {@link PhaseContext}
     * will be allowed to register it's {@link #getFrameModifier()} to push along the
     * {@link CauseStackManager}. In certain cases, there are states that can have
     * excessive modifiers being pushed and popped with and without causes that may cause
     * performance degredation due to the excessive amounts of how many recyclings occur
     * with {@link CauseStackManager#getCurrentCause()} lacking a cached context
     * and therefor needing to re-create the context each and every time.
     *
     * @param phaseContext The appropriate phase context
     * @return True if the modifiers should be pushed to the manager
     */
    default boolean shouldProvideModifiers(final C phaseContext) {
        return true;
    }
    default boolean isRestoring() {
        return false;
    }

    default boolean isConvertingMaps() {
        return false;
    }
    default boolean allowsGettingQueuedRemovedTiles() {
        return false;
    }

    /**
     * Allows phases to be notified when an entity successfully teleports
     * between dimensions.
     *
     * @param phaseContext The appropriate phase context
     */
    default void markTeleported(final C phaseContext) {
    }
}
