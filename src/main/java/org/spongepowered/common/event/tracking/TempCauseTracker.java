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
import org.spongepowered.api.event.Event;
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
public final class TempCauseTracker {

    private final net.minecraft.world.World targetWorld;
    private final List<Entity> capturedEntities = new ArrayList<>();
    private final List<BlockSnapshot> capturedSpongeBlockSnapshots = new ArrayList<>();
    private final List<Transaction<BlockSnapshot>> invalidTransactions = new ArrayList<>();
    private final List<Entity> capturedEntityItems = new ArrayList<>();

    private boolean captureBlocks = false;

    private final TrackingPhases phases = new TrackingPhases();

    public TempCauseTracker(net.minecraft.world.World targetWorld) {
        if (((IMixinWorld) targetWorld).getCauseTracker() != null) {
            throw new IllegalArgumentException("Attempting to create a new CauseTracker for a world that already has a CauseTracker!!");
        }
        this.targetWorld = targetWorld;
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
        // Inventory Events
        Optional<Container> openContainer = context.firstNamed(TrackingHelper.OPEN_CONTAINER, Container.class);

        // Handle Entity captures
        if (this.capturedEntityItems.size() > 0) {
            handleDroppedItems(cause, phaseState, context, this.invalidTransactions);
        }
        if (this.capturedEntities.size() > 0) {
            handleEntitySpawns(cause, phaseState, context);
        }

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
        SpawnEntityEvent event = null;
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
                TrackingHelper.processList(null, listIterator);

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
                TrackingHelper.processList(null, listIterator);

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
                        transaction.getOriginal().restore(true, false);
                    }
                    MoveToPhases.handlePostPlayerBlockEvent(getMinecraftWorld(), captureType, this.invalidTransactions, phaseState, context);
                }

//                this.markAndNotifyBlockPost(blockEvent.getTransactions(), captureType, cause);

                if (captureType == CaptureType.PLACE && player != null && packet != null && packet.getStack() != null) {
                    player.addStat(StatList.objectUseStats[net.minecraft.item.Item.getIdFromItem(packet.getStack().getItem())], 1);
                }
            }
        }
    }

    private void handleDroppedItems(final Cause cause, final IPhaseState phaseState, PhaseContext context, List<Transaction<BlockSnapshot>> invalidTransactions) {
        Iterator<Entity> iter = this.capturedEntityItems.iterator();
        ImmutableList.Builder<EntitySnapshot> entitySnapshotBuilder = new ImmutableList.Builder<>();
        MoveToPhases.preProcessItemDrops(cause, this.invalidTransactions, iter, entitySnapshotBuilder);

        List<EntitySnapshot> entitySnapshots = entitySnapshotBuilder.build();
        if (entitySnapshots.isEmpty()) {
            return;
        }
        DropItemEvent event;
        final boolean destructItemOnDrop = context.firstNamed(TrackingHelper.DESTRUCT_ITEM_DROPS, Boolean.class).orElse(false);

        if (destructItemOnDrop) {
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
}
