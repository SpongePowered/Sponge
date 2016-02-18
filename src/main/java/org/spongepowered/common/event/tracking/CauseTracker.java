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
import org.spongepowered.api.util.Tuple;
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
    private final List<Entity> capturedEntities = new ArrayList<>();
    private final List<BlockSnapshot> capturedSpongeBlockSnapshots = new ArrayList<>();
    private final List<Transaction<BlockSnapshot>> invalidTransactions = new ArrayList<>();
    private final List<Entity> capturedEntityItems = new ArrayList<>();

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
            PrettyPrinter printer = new PrettyPrinter(60);
            printer.add("Switching Phase").centre().hr();
            printer.add("Detecting a runaway phase! Potentially a problem where something isn't completing a phase!!!");
            printer.add("  %s : %s", "Entering Phase", phase);
            printer.add("  %s : %s", "Entering State", state);
            printer.addWrapped(60, "%s : %s", "Current phases", this.phases.states.currentStates());
            printer.add("  %s :", "Printing stack trace");
            Exception exception = new Exception("Stack trace");
            for (StackTraceElement element : exception.getStackTrace()) {
                printer.add("    %s", element);
            }
            printer.print(System.err);
        }
        IPhaseState currentState = this.phases.peekState();
        if (currentState != null && !currentState.canSwitchTo(state)) {
//            throw new IllegalArgumentException(String.format("Cannot switch from %s to %s", currentState, state));
        }
        final TrackingPhase current = this.phases.current();

        this.phases.push(state, phaseContext);
    }

    public void completePhase() {
        final Tuple<IPhaseState, PhaseContext> tuple = this.phases.peek();
        IPhaseState state = tuple.getFirst();
        if (this.phases.states.size() > 6) {
            PrettyPrinter printer = new PrettyPrinter(60);
            printer.add("Completing Phase").centre().hr();
            printer.add("Detecting a runaway phase! Potentially a problem where something isn't completing a phase!!!");
            printer.addWrapped(60, "  %s : %s", "Completing phase", state);
            printer.addWrapped(60, "  %s : %s", "Phases remaining", this.phases.states.currentStates());
            Exception exception = new Exception("Stack trace");
            for (StackTraceElement element : exception.getStackTrace()) {
                printer.add("     %s", element);
            }
            printer.print(System.err);
        }
        this.phases.pop();
        // If pop is called, the Deque will already throw an exception if there is no element
        // so it's an error properly handled.
        final TrackingPhase phase = state.getPhase();
        phase.unwind(this, state, tuple.getSecond());
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

    // ----------------- ACCESSORS ----------------------------------

    public List<Entity> getCapturedEntities() {
        return this.capturedEntities;
    }

    public List<Entity> getCapturedEntityItems() {
        return this.capturedEntityItems;
    }

    public List<BlockSnapshot> getCapturedSpongeBlockSnapshots() {
        return this.capturedSpongeBlockSnapshots;
    }

    public List<Transaction<BlockSnapshot>> getInvalidTransactions() {
        return this.invalidTransactions;
    }


    // ----------------- PROCESSORS ----------------------------------


    // now it's only handled in phases.
    @SuppressWarnings("unchecked")
    public void handlePostTickCaptures(Cause cause, IPhaseState phaseState, PhaseContext context) {
        if (this.getMinecraftWorld().isRemote || phaseState.isManaged()) {
            return;
        } else {
            final Map<?, ?> map = context.firstNamed(TrackingHelper.POPULATOR_CAPTURE_MAP, Map.class).orElse(null);
            if (this.capturedEntities.isEmpty() && this.capturedEntityItems.isEmpty() && this.capturedSpongeBlockSnapshots.isEmpty()
                && (map == null || map.isEmpty()) && context.firstNamed(NamedCause.SOURCE, EntityPlayerMP.class).isPresent()) {
                return; // nothing was captured, return
            }
        }

        EntityPlayerMP player = context.first(EntityPlayerMP.class).orElse(null);
        Packet<?> packetIn = context.first(Packet.class).orElse(null);

        // todo
        // Attempt to find a Player cause if we do not have one
        cause = TrackingHelper.identifyCauses(cause, this.capturedSpongeBlockSnapshots, this.getMinecraftWorld());

        // todo
        // Handle Block Captures
        handleBlockCaptures(cause, phaseState, context);

        // todo
        // Handle Player Toss
        MoveToPhases.handleToss(player, packetIn);

        // todo
        // Handle Player kill commands
        cause = MoveToPhases.handleKill(cause, player, packetIn);

        // todo
        // Handle Player Entity destruct
        MoveToPhases.handleEntityDestruct(cause, player, packetIn, getMinecraftWorld());

        // todo
        // Inventory Events
        Optional<Container> openContainer = context.firstNamed(TrackingHelper.OPEN_CONTAINER, Container.class);
        MoveToPhases.handleInventoryEvents(player, packetIn, openContainer.orElse(null), phaseState, context);

        // Handle Entity captures
        if (this.capturedEntityItems.size() > 0) {
            if (StaticMixinHelper.dropCause != null) {
                cause = StaticMixinHelper.dropCause;
                StaticMixinHelper.destructItemDrop = true;
            }
            handleDroppedItems(cause, phaseState, context);
        }
        if (this.capturedEntities.size() > 0) {
            handleEntitySpawns(cause, phaseState, context);
        }

        StaticMixinHelper.dropCause = null;
        StaticMixinHelper.destructItemDrop = false;
        this.invalidTransactions.clear();
    }

    // HANDLED - partially...
    private void handleEntitySpawns(Cause cause, IPhaseState currentPhase, PhaseContext context) {
        if (this.capturedEntities.isEmpty() && this.capturedEntityItems.isEmpty()) {
            return; // there's nothing to do.
        }

        if (!(currentPhase instanceof ISpawnableState)) {
            return;
        }

        SpawnEntityEvent event = ((ISpawnableState) currentPhase).createEntityEvent(cause, this);
        if (event == null) {
            return;
        }

        if (!(SpongeImpl.postEvent(event))) {
            Iterator<Entity> iterator = event.getEntities().iterator();
            while (iterator.hasNext()) {
                Entity entity = iterator.next();
                if (entity.isRemoved()) { // Entity removed in an event handler
                    iterator.remove();
                    continue;
                }
                net.minecraft.entity.Entity nmsEntity = (net.minecraft.entity.Entity) entity;
                if (nmsEntity instanceof EntityWeatherEffect) {
                    MoveToPhases.addWeatherEffect(nmsEntity, getMinecraftWorld());
                } else {
                    int x = MathHelper.floor_double(nmsEntity.posX / 16.0D);
                    int z = MathHelper.floor_double(nmsEntity.posZ / 16.0D);
                    this.getMinecraftWorld().getChunkFromChunkCoords(x, z).addEntity(nmsEntity);
                    this.getMinecraftWorld().loadedEntityList.add(nmsEntity);
                    this.getMixinWorld().onSpongeEntityAdded(nmsEntity);
                    SpongeHooks.logEntitySpawn(cause, nmsEntity);
                }
                iterator.remove();
            }
        } else {
            this.capturedEntities.clear();
        }
    }

    private void handleBlockCaptures(Cause cause, IPhaseState currentPhase, PhaseContext context) {
        final EntityPlayerMP player = context.first(EntityPlayerMP.class).orElse(null);
        final Packet<?> packetIn = context.first(Packet.class).orElse(null);

        final ImmutableList<Transaction<BlockSnapshot>> blockBreakTransactions;
        final ImmutableList<Transaction<BlockSnapshot>> blockModifyTransactions;
        final ImmutableList<Transaction<BlockSnapshot>> blockPlaceTransactions;
        final ImmutableList<Transaction<BlockSnapshot>> blockDecayTransactions;
        final ImmutableList<Transaction<BlockSnapshot>> blockMultiTransactions;
        final ImmutableList.Builder<Transaction<BlockSnapshot>> breakBuilder = new ImmutableList.Builder<>();
        final ImmutableList.Builder<Transaction<BlockSnapshot>> placeBuilder = new ImmutableList.Builder<>();
        final ImmutableList.Builder<Transaction<BlockSnapshot>> decayBuilder = new ImmutableList.Builder<>();
        final ImmutableList.Builder<Transaction<BlockSnapshot>> modifyBuilder = new ImmutableList.Builder<>();
        final ImmutableList.Builder<Transaction<BlockSnapshot>> multiBuilder = new ImmutableList.Builder<>();
        ChangeBlockEvent.Break breakEvent = null;
        ChangeBlockEvent.Modify modifyEvent = null;
        ChangeBlockEvent.Place placeEvent = null;
        List<ChangeBlockEvent> blockEvents = new ArrayList<>();

        Iterator<BlockSnapshot> iterator = this.capturedSpongeBlockSnapshots.iterator();
        while (iterator.hasNext()) {
            SpongeBlockSnapshot blockSnapshot = (SpongeBlockSnapshot) iterator.next();
            CaptureType captureType = blockSnapshot.captureType;
            BlockPos pos = VecHelper.toBlockPos(blockSnapshot.getPosition());
            IBlockState currentState = this.getMinecraftWorld().getBlockState(pos);
            Transaction<BlockSnapshot>
                    transaction =
                    new Transaction<>(blockSnapshot, this.getMixinWorld().createSpongeBlockSnapshot(currentState, currentState.getBlock()
                            .getActualState(currentState, this.getMinecraftWorld(), pos), pos, 0));
            if (captureType == CaptureType.BREAK) {
                breakBuilder.add(transaction);
            } else if (captureType == CaptureType.DECAY) {
                decayBuilder.add(transaction);
            } else if (captureType == CaptureType.PLACE) {
                placeBuilder.add(transaction);
            } else if (captureType == CaptureType.MODIFY) {
                modifyBuilder.add(transaction);
            }
            multiBuilder.add(transaction);
            iterator.remove();
        }

        blockBreakTransactions = breakBuilder.build();
        blockDecayTransactions = decayBuilder.build();
        blockModifyTransactions = modifyBuilder.build();
        blockPlaceTransactions = placeBuilder.build();
        blockMultiTransactions = multiBuilder.build();
        ChangeBlockEvent changeBlockEvent;
        if (blockBreakTransactions.size() > 0) {
            changeBlockEvent = SpongeEventFactory.createChangeBlockEventBreak(cause, this.getWorld(), blockBreakTransactions);
            SpongeImpl.postEvent(changeBlockEvent);
            breakEvent = (ChangeBlockEvent.Break) changeBlockEvent;
            blockEvents.add(changeBlockEvent);
        }
        if (blockModifyTransactions.size() > 0) {
            changeBlockEvent = SpongeEventFactory.createChangeBlockEventModify(cause, this.getWorld(), blockModifyTransactions);
            SpongeImpl.postEvent(changeBlockEvent);
            modifyEvent = (ChangeBlockEvent.Modify) changeBlockEvent;
            blockEvents.add(changeBlockEvent);
        }
        if (blockPlaceTransactions.size() > 0) {
            changeBlockEvent = SpongeEventFactory.createChangeBlockEventPlace(cause, this.getWorld(), blockPlaceTransactions);
            SpongeImpl.postEvent(changeBlockEvent);
            placeEvent = (ChangeBlockEvent.Place) changeBlockEvent;
            blockEvents.add(changeBlockEvent);
        }
        if (blockEvents.size() > 1) {
            if (breakEvent != null) {
                int count = cause.allOf(ChangeBlockEvent.Break.class).size();
                String namedCause = "BreakEvent" + (count != 0 ? count : "");
                cause = cause.with(NamedCause.of(namedCause, breakEvent));
            }
            if (modifyEvent != null) {
                int count = cause.allOf(ChangeBlockEvent.Modify.class).size();
                String namedCause = "ModifyEvent" + (count != 0 ? count : "");
                cause = cause.with(NamedCause.of(namedCause, modifyEvent));
            }
            if (placeEvent != null) {
                int count = cause.allOf(ChangeBlockEvent.Place.class).size();
                String namedCause = "PlaceEvent" + (count != 0 ? count : "");
                cause = cause.with(NamedCause.of(namedCause, placeEvent));
            }
            changeBlockEvent = SpongeEventFactory.createChangeBlockEventPost(cause, this.getWorld(), blockMultiTransactions);
            SpongeImpl.postEvent(changeBlockEvent);
            if (changeBlockEvent.isCancelled()) {
                // Restore original blocks
                ListIterator<Transaction<BlockSnapshot>>
                        listIterator =
                        changeBlockEvent.getTransactions().listIterator(changeBlockEvent.getTransactions().size());
                TrackingHelper.processList(this, listIterator);

                if (player != null) {
                    CaptureType captureType = null;
                    if (packetIn instanceof C08PacketPlayerBlockPlacement) {
                        captureType = CaptureType.PLACE;
                    } else if (packetIn instanceof C07PacketPlayerDigging) {
                        captureType = CaptureType.BREAK;
                    }
                    if (captureType != null) {
                        MoveToPhases.handlePostPlayerBlockEvent(getMinecraftWorld(), captureType, changeBlockEvent.getTransactions(), currentPhase,
                                context);
                    }
                }

                // clear entity list and return to avoid spawning items
                this.capturedEntities.clear();
                this.capturedEntityItems.clear();
                return;
            }
        }

        if (blockDecayTransactions.size() > 0) {
            changeBlockEvent = SpongeEventFactory.createChangeBlockEventDecay(cause, this.getWorld(), blockDecayTransactions);
            SpongeImpl.postEvent(changeBlockEvent);
            blockEvents.add(changeBlockEvent);
        }
        processBlockEvents(cause, blockEvents, packetIn, player, breakEvent, currentPhase, context);
    }

    private void processBlockEvents(Cause cause, List<ChangeBlockEvent> blockEvents, Packet<?> packetIn, @Nullable EntityPlayer player,
            @Nullable ChangeBlockEvent.Break breakEvent, IPhaseState phaseState, PhaseContext context) {
        for (ChangeBlockEvent blockEvent : blockEvents) {
            CaptureType captureType = null;
            if (blockEvent instanceof ChangeBlockEvent.Break) {
                captureType = CaptureType.BREAK;
            } else if (blockEvent instanceof ChangeBlockEvent.Decay) {
                captureType = CaptureType.DECAY;
            } else if (blockEvent instanceof ChangeBlockEvent.Modify) {
                captureType = CaptureType.MODIFY;
            } else if (blockEvent instanceof ChangeBlockEvent.Place) {
                captureType = CaptureType.PLACE;
            }

            C08PacketPlayerBlockPlacement packet = null;

            if (packetIn instanceof C08PacketPlayerBlockPlacement) {
                packet = (C08PacketPlayerBlockPlacement) packetIn;
            }

            if (blockEvent.isCancelled()) {
                // Restore original blocks
                ListIterator<Transaction<BlockSnapshot>>
                        listIterator =
                        blockEvent.getTransactions().listIterator(blockEvent.getTransactions().size());
                TrackingHelper.processList(this, listIterator);

                MoveToPhases.handlePostPlayerBlockEvent(getMinecraftWorld(), captureType, blockEvent.getTransactions(), phaseState, context);

                // clear entity list and return to avoid spawning items
                this.capturedEntities.clear();
                this.capturedEntityItems.clear();
                return;
            } else {
                for (Transaction<BlockSnapshot> transaction : blockEvent.getTransactions()) {
                    if (!transaction.isValid()) {
                        this.invalidTransactions.add(transaction);
                    } else {
                        if (captureType == CaptureType.BREAK && cause.first(User.class).isPresent()) {
                            BlockPos pos = VecHelper.toBlockPos(transaction.getOriginal().getPosition());
                            for (EntityHanging hanging : SpongeHooks.findHangingEntities(this.getMinecraftWorld(), pos)) {
                                if (hanging != null) {
                                    if (hanging instanceof EntityItemFrame) {
                                        EntityItemFrame itemFrame = (EntityItemFrame) hanging;
                                        net.minecraft.entity.Entity dropCause = null;
                                        if (cause.root() instanceof net.minecraft.entity.Entity) {
                                            dropCause = (net.minecraft.entity.Entity) cause.root();
                                        }

                                        itemFrame.dropItemOrSelf(dropCause, true);
                                        itemFrame.setDead();
                                    }
                                }
                            }
                        }

                        if (captureType == CaptureType.PLACE && packetIn instanceof C08PacketPlayerBlockPlacement) {
                            BlockPos pos = VecHelper.toBlockPos(transaction.getFinal().getPosition());
                            IMixinChunk spongeChunk = (IMixinChunk) this.getMinecraftWorld().getChunkFromBlockCoords(pos);
                            spongeChunk.addTrackedBlockPosition((Block) transaction.getFinal().getState().getType(), pos,
                                    (User) player, PlayerTracker.Type.OWNER);
                            spongeChunk.addTrackedBlockPosition((Block) transaction.getFinal().getState().getType(), pos,
                                    (User) player, PlayerTracker.Type.NOTIFIER);
                        }
                    }
                }

                if (this.invalidTransactions.size() > 0) {
                    for (Transaction<BlockSnapshot> transaction : Lists.reverse(this.invalidTransactions)) {
                        switchToPhase(TrackingPhases.BLOCK, BlockPhase.State.RESTORING_BLOCKS, PhaseContext.start().add(NamedCause.of(
                                TrackingHelper.RESTORING_BLOCK, transaction.getOriginal())));
                        transaction.getOriginal().restore(true, false);
                        completePhase();
                    }
                    MoveToPhases.handlePostPlayerBlockEvent(getMinecraftWorld(), captureType, this.invalidTransactions, phaseState, context);
                }

                if (this.capturedEntityItems.size() > 0 && blockEvents.get(0) == breakEvent) {
                    StaticMixinHelper.destructItemDrop = true;
                }

                this.markAndNotifyBlockPost(blockEvent.getTransactions(), captureType, cause);

                if (captureType == CaptureType.PLACE && player != null && packet != null && packet.getStack() != null) {
                    player.addStat(StatList.objectUseStats[net.minecraft.item.Item.getIdFromItem(packet.getStack().getItem())], 1);
                }
            }
        }
    }

    private void handleDroppedItems(final Cause cause, final IPhaseState phaseState, PhaseContext context) {
        if (phaseState.getPhase() == TrackingPhases.PACKET) {

        }
        Iterator<Entity> iter = this.capturedEntityItems.iterator();
        ImmutableList.Builder<EntitySnapshot> entitySnapshotBuilder = new ImmutableList.Builder<>();
        MoveToPhases.preProcessItemDrops(cause, this.invalidTransactions, iter, entitySnapshotBuilder);

        List<EntitySnapshot> entitySnapshots = entitySnapshotBuilder.build();
        if (entitySnapshots.isEmpty()) {
            return;
        }
        DropItemEvent event;

        if (StaticMixinHelper.destructItemDrop) {
            event = SpongeEventFactory.createDropItemEventDestruct(cause, this.capturedEntityItems, entitySnapshots, this.getWorld());
        } else {
            event = SpongeEventFactory.createDropItemEventDispense(cause, this.capturedEntityItems, entitySnapshots, this.getWorld());
        }

        if (!(SpongeImpl.postEvent(event))) {
            // Handle player deaths
            for (Player causePlayer : cause.allOf(Player.class)) {
                EntityPlayerMP playermp = (EntityPlayerMP) causePlayer;
                if (playermp.getHealth() <= 0 || playermp.isDead) {
                    if (!playermp.worldObj.getGameRules().getBoolean("keepInventory")) {
                        playermp.inventory.clear();
                    } else {
                        // don't drop anything if keepInventory is enabled
                        this.capturedEntityItems.clear();
                    }
                }
            }

            Iterator<Entity> iterator =
                    event instanceof DropItemEvent.Destruct ? ((DropItemEvent.Destruct) event).getEntities().iterator()
                                                            : ((DropItemEvent.Dispense) event).getEntities().iterator();
            while (iterator.hasNext()) {
                Entity entity = iterator.next();
                if (entity.isRemoved()) { // Entity removed in an event handler
                    iterator.remove();
                    continue;
                }
                net.minecraft.entity.Entity nmsEntity = (net.minecraft.entity.Entity) entity;
                int x = MathHelper.floor_double(nmsEntity.posX / 16.0D);
                int z = MathHelper.floor_double(nmsEntity.posZ / 16.0D);
                this.getMinecraftWorld().getChunkFromChunkCoords(x, z).addEntity(nmsEntity);
                this.getMinecraftWorld().loadedEntityList.add(nmsEntity);
                this.getMixinWorld().onSpongeEntityAdded(nmsEntity);
                SpongeHooks.logEntitySpawn(cause, nmsEntity);
                iterator.remove();
            }
        } else {
            final EntityPlayerMP entityPlayerMP = context.first(EntityPlayerMP.class).orElse(null);
            if (cause.root() == entityPlayerMP) {
                TrackingHelper.sendItemChangeToPlayer(entityPlayerMP, context);
            }
            this.capturedEntityItems.clear();
        }
    }

    // --------------------- DELEGATED WORLD METHODS -------------------------

    public void notifyBlockOfStateChange(BlockPos notifyPos, final Block sourceBlock, BlockPos sourcePos) {
        if (!this.getMinecraftWorld().isRemote) {
            IBlockState iblockstate = this.getMinecraftWorld().getBlockState(notifyPos);
            final Tuple<IPhaseState, PhaseContext> phaseContextTuple = this.getPhases().peek();
            Optional<User> packetUser = phaseContextTuple.getSecond().firstNamed(TrackingHelper.PACKET_PLAYER, User.class);

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
        Chunk chunk = minecraftWorld.getChunkFromBlockCoords(pos);
        IBlockState currentState = chunk.getBlockState(pos);
        if (currentState == newState) {
            return false;
        }

        Block block = newState.getBlock();
        BlockSnapshot originalBlockSnapshot = null;
        BlockSnapshot newBlockSnapshot = null;
        Transaction<BlockSnapshot> transaction = null;
        LinkedHashMap<Vector3i, Transaction<BlockSnapshot>> populatorSnapshotList = null;

        // Don't capture if we are restoring blocks
        // uncomment when you want to handle block events. for now just want to set block state and
        // track the phases
        final Tuple<IPhaseState, PhaseContext> currentPhaseContext = this.getPhases().peek();
        final IPhaseState phaseState = currentPhaseContext.getFirst();
        final PhaseContext phaseContext = currentPhaseContext.getSecond();
        if (!minecraftWorld.isRemote && phaseState != BlockPhase.State.RESTORING_BLOCKS
            && phaseState != SpawningPhase.State.WORLD_SPAWNER_SPAWNING && phaseState != SpawningPhase.State.CHUNK_SPAWNING) {
            BlockStateTriplet pair = MoveToPhases.handleEvents(this, currentState, newState, block, pos, flags, phaseContext);
            originalBlockSnapshot = pair.getBlockSnapshot();
            transaction = pair.getTransaction();
            populatorSnapshotList = pair.getPopulatorList();
        }

        int oldLight = currentState.getBlock().getLightValue();

        IBlockState iblockstate1 = ((IMixinChunk) chunk).setBlockState(pos, newState, currentState, newBlockSnapshot);

        if (iblockstate1 == null) {
            if (originalBlockSnapshot != null) {
                this.getCapturedSpongeBlockSnapshots().remove(originalBlockSnapshot);
                if (populatorSnapshotList != null && transaction != null) {
                    populatorSnapshotList.remove(transaction.getOriginal().getPosition());
                }
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
        final IPhaseState phaseState = this.getPhases().peekState();
        if (!minecraftWorld.isRemote && entityIn instanceof EntityItem && phaseState == BlockPhase.State.RESTORING_BLOCKS) {
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

            return MoveToPhases.completeEntitySpawn(entity, cause, this, chunkX, chunkZ);
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
            final Tuple<IPhaseState, PhaseContext> peekedPhase = this.getPhases().peek();
            final Optional<PluginContainer> pluginContainer = peekedPhase.getSecond().firstNamed(NamedCause.SOURCE, PluginContainer.class);
            if (!this.getCapturedEntities().isEmpty() && !pluginContainer.isPresent()) {
                this.handlePostTickCaptures(cause, peekedPhase.getFirst(), peekedPhase.getSecond());
            }

        }
    }

}
