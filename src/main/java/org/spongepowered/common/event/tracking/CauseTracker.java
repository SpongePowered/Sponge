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
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
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
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.stats.StatList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ReportedException;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import org.apache.commons.lang3.tuple.MutablePair;
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
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.tracking.phase.BlockPhase;
import org.spongepowered.common.event.tracking.phase.GeneralPhase;
import org.spongepowered.common.event.tracking.phase.PacketPhase;
import org.spongepowered.common.event.tracking.phase.SpawningPhase;
import org.spongepowered.common.event.tracking.phase.TrackingPhase;
import org.spongepowered.common.event.tracking.phase.TrackingPhases;
import org.spongepowered.common.event.tracking.phase.WorldPhase;
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
import java.util.Random;

import javax.annotation.Nullable;

public final class CauseTracker {

    private final net.minecraft.world.World targetWorld;
    private final List<Entity> capturedEntities = new ArrayList<>();
    private final List<BlockSnapshot> capturedSpongeBlockSnapshots = new ArrayList<>();
    private final Map<PopulatorType, LinkedHashMap<Vector3i, Transaction<BlockSnapshot>>> capturedSpongePopulators = Maps.newHashMap();
    private final List<Transaction<BlockSnapshot>> invalidTransactions = new ArrayList<>();
    private final List<Entity> capturedEntityItems = new ArrayList<>();

    private boolean captureBlocks = false;

    private final TrackingPhases phases = new TrackingPhases();

    @Nullable private BlockSnapshot currentTickBlock;
    @Nullable private Entity currentTickEntity;
    @Nullable private TileEntity currentTickTileEntity;
    @Nullable private Cause pluginCause;
    @Nullable private ICommand command;
    @Nullable private ICommandSender commandSender;

    public CauseTracker(net.minecraft.world.World targetWorld) {
        if (((IMixinWorld) targetWorld).getCauseTracker() != null) {
            throw new IllegalArgumentException("Attempting to create a new CauseTracker for a world that already has a CauseTracker!!");
        }
        this.targetWorld = targetWorld;
    }

    public World getWorld() {
        return (World) this.targetWorld;
    }

    public net.minecraft.world.World getMinecraftWorld() {
        return this.targetWorld;
    }

    public IMixinWorld getMixinWorld() {
        return (IMixinWorld) this.targetWorld;
    }

    public CauseTracker pop() {
        this.phases.pop();
        return this;
    }

    public TrackingPhases getPhases() {
        return this.phases;
    }

    public boolean isCapturing() {
        final IPhaseState state = this.phases.peek();
        return state != null && state.isBusy();
    }

    public boolean isProcessingBlockRandomTicks() {
        return this.phases.peek() == WorldPhase.State.RANDOM_TICK_BLOCK;
    }

    public boolean isCapturingTerrainGen() {
        return this.phases.peek() == WorldPhase.State.TERRAIN_GENERATION;
    }

    public boolean isCapturingBlocks() {
        return this.captureBlocks;
    }

    public void setCaptureBlocks(boolean captureBlocks) {
        this.captureBlocks = captureBlocks;
    }

    public boolean isCaptureCommand() {
        return this.phases.peek() == GeneralPhase.State.COMMAND;
    }

    public boolean isRestoringBlocks() {
        return this.phases.peek() == BlockPhase.State.RESTORING_BLOCKS;
    }

    public boolean isSpawningDeathDrops() {
        return this.phases.peek() == SpawningPhase.State.DEATH_DROPS_SPAWNING;
    }

    public boolean isWorldSpawnerRunning() {
        return this.phases.peek() == SpawningPhase.State.WORLD_SPAWNER_SPAWNING;
    }

    public boolean isChunkSpawnerRunning() {
        return this.phases.peek() == SpawningPhase.State.CHUNK_SPAWNING;
    }


    public List<Entity> getCapturedEntities() {
        return this.capturedEntities;
    }

    public List<Entity> getCapturedEntityItems() {
        return this.capturedEntityItems;
    }

    public boolean hasTickingBlock() {
        return this.currentTickBlock != null;
    }

    public Optional<BlockSnapshot> getCurrentTickBlock() {
        return Optional.ofNullable(this.currentTickBlock);
    }

    public void setCurrentTickBlock(BlockSnapshot currentTickBlock) {
        checkNotNull(currentTickBlock, "Cannot tick on a null ticking block!");
        switchToPhase(TrackingPhases.GENERAL, WorldPhase.State.TICKING_BLOCK);
        this.currentTickBlock = currentTickBlock;
    }

    public boolean hasTickingEntity() {
        return this.currentTickEntity != null;
    }

    public Optional<Entity> getCurrentTickEntity() {
        return Optional.ofNullable(this.currentTickEntity);
    }

    public void setCurrentTickEntity(Entity currentTickEntity) {
        checkNotNull(currentTickEntity, "Cannot capture on a null ticking entity!");
        switchToPhase(TrackingPhases.GENERAL, WorldPhase.State.TICKING_ENTITY);
        this.currentTickEntity = currentTickEntity;
    }

    public boolean hasTickingTileEntity() {
        return this.currentTickTileEntity != null;
    }

    public Optional<TileEntity> getCurrentTickTileEntity() {
        return Optional.ofNullable(this.currentTickTileEntity);
    }

    public void setCurrentTickTileEntity(TileEntity currentTickTileEntity) {
        checkNotNull(currentTickTileEntity, "Cannot capture on a null ticking tile entity!");
        switchToPhase(TrackingPhases.GENERAL, WorldPhase.State.TICKING_TILE_ENTITY);
        this.currentTickTileEntity = currentTickTileEntity;
    }

    public List<BlockSnapshot> getCapturedSpongeBlockSnapshots() {
        return this.capturedSpongeBlockSnapshots;
    }

    public Map<PopulatorType, LinkedHashMap<Vector3i, Transaction<BlockSnapshot>>> getCapturedPopulators() {
        return this.capturedSpongePopulators;
    }

    public Optional<Cause> getPluginCause() {
        return Optional.ofNullable(this.pluginCause);
    }

    public void setPluginCause(@Nullable Cause pluginCause) {
        this.pluginCause = pluginCause;
    }

    public boolean hasPluginCause() {
        return this.pluginCause != null;
    }

    // HANDLED - partially...
    private void handleEntitySpawns(Cause cause, IPhaseState currentPhase) {
        if (this.capturedEntities.isEmpty() && this.capturedEntityItems.isEmpty()) {
            return; // there's nothing to do.
        }

        if (!(currentPhase instanceof ISpawnablePhase)) {
            throw new IllegalArgumentException(String.format("Invalid state detected. Current state is: %s. Expected a SpawningState or a currently Ticking phase.", currentPhase));
        }

        SpawnEntityEvent event = ((ISpawnablePhase) currentPhase).createEntityEvent(cause, this);
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

    // NOT HANDLED TODO
    @SuppressWarnings("unchecked")
    public void handlePostTickCaptures(Cause cause) {
        final IPhaseState phase = this.phases.pop();
        if (this.getMinecraftWorld().isRemote || phase.isManaged()) {
            return;
        } else if (this.capturedEntities.isEmpty() && this.capturedEntityItems.isEmpty() && this.capturedSpongeBlockSnapshots.isEmpty()
                   && this.capturedSpongePopulators.isEmpty() && StaticMixinHelper.packetPlayer == null) {
            return; // nothing was captured, return
        }

        EntityPlayerMP player = StaticMixinHelper.packetPlayer;
        Packet<?> packetIn = StaticMixinHelper.processingPacket;

        // todo
        // Attempt to find a Player cause if we do not have one
        cause = TrackingHelper.identifyCauses(cause, this.capturedSpongeBlockSnapshots, this.getMinecraftWorld());

        // todo
        // Handle Block Captures
        handleBlockCaptures(cause);

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
        MoveToPhases.handleInventoryEvents(player, packetIn, StaticMixinHelper.lastOpenContainer);

        // Handle Entity captures
        if (this.capturedEntityItems.size() > 0) {
            if (StaticMixinHelper.dropCause != null) {
                cause = StaticMixinHelper.dropCause;
                StaticMixinHelper.destructItemDrop = true;
            }
            handleDroppedItems(cause, phase);
        }
        if (this.capturedEntities.size() > 0) {
            handleEntitySpawns(cause, phase);
        }

        StaticMixinHelper.dropCause = null;
        StaticMixinHelper.destructItemDrop = false;
        this.invalidTransactions.clear();
    }


    public void handleDroppedItems(final Cause cause, final IPhaseState phaseState) {
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
            if (cause.root() == this.packetPlayer) {
                TrackingHelper.sendItemChangeToPlayer(this.packetPlayer);
            }
            this.capturedEntityItems.clear();
        }
    }

    public void handleBlockCaptures(Cause cause) {
        final EntityPlayerMP player = this.packetPlayer;
        final Packet<?> packetIn = this.packetProcessing;

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
                        MoveToPhases.handlePostPlayerBlockEvent(getMinecraftWorld(), captureType, changeBlockEvent.getTransactions());
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
        processBlockEvents(cause, blockEvents, packetIn, player, breakEvent);
    }

    private void processBlockEvents(Cause cause, List<ChangeBlockEvent> blockEvents, Packet<?> packetIn, @Nullable EntityPlayer player, @Nullable ChangeBlockEvent.Break breakEvent) {
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

                MoveToPhases.handlePostPlayerBlockEvent(getMinecraftWorld(), captureType, blockEvent.getTransactions());

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
                            spongeChunk.addTrackedBlockPosition((net.minecraft.block.Block) transaction.getFinal().getState().getType(), pos,
                                (User) player, PlayerTracker.Type.OWNER);
                            spongeChunk.addTrackedBlockPosition((net.minecraft.block.Block) transaction.getFinal().getState().getType(), pos,
                                (User) player, PlayerTracker.Type.NOTIFIER);
                        }
                    }
                }

                if (this.invalidTransactions.size() > 0) {
                    for (Transaction<BlockSnapshot> transaction : Lists.reverse(this.invalidTransactions)) {
                        switchToPhase(TrackingPhases.BLOCK, BlockPhase.State.RESTORING_BLOCKS);
                        transaction.getOriginal().restore(true, false);
                        pop();
                    }
                    MoveToPhases.handlePostPlayerBlockEvent(getMinecraftWorld(), captureType, this.invalidTransactions);
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

    // Note, this is called directly by MixinWorld_Tracker
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
        if (!minecraftWorld.isRemote && entityIn instanceof EntityItem && this.isRestoringBlocks()) {
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
            if (minecraftWorld.isRemote || isForced || this.isSpawningDeathDrops()) {
                // Basically, if it's forced, or it's remote, OR we're already spawning death drops, then go ahead.
                minecraftWorld.getChunkFromChunkCoords(chunkX, chunkZ).addEntity(entityIn);
                minecraftWorld.loadedEntityList.add(entityIn);
                mixinWorld.onSpongeEntityAdded(entityIn);
                return true;
            }

            return MoveToPhases.completeEntitySpawn(entity, cause, this, chunkX, chunkZ);
        }
    }

    public void randomTickBlock(Block block, BlockPos pos, IBlockState state, Random random) {
        setCurrentTickBlock(this.getMixinWorld().createSpongeBlockSnapshot(state, state.getBlock().getActualState(state, this.getMinecraftWorld(), pos), pos, 0));
        pop();
        switchToPhase(TrackingPhases.BLOCK, BlockPhase.State.RESTORING_BLOCKS);
        block.randomTick(this.getMinecraftWorld(), pos, state, random);
        completePhase();
    }

    public void updateTickBlock(Block block, BlockPos pos, IBlockState state, Random rand) {
        setCurrentTickBlock(this.getMixinWorld().createSpongeBlockSnapshot(state, state.getBlock().getActualState(state, this.getMinecraftWorld(), pos), pos, 0));
        block.updateTick(this.getMinecraftWorld(), pos, state, rand);
        completePhase();
    }

    public void notifyBlockOfStateChange(BlockPos notifyPos, final Block sourceBlock, BlockPos sourcePos) {
        if (!this.getMinecraftWorld().isRemote) {
            IBlockState iblockstate = this.getMinecraftWorld().getBlockState(notifyPos);

            try {
                if (!this.getMinecraftWorld().isRemote) {
                    final Chunk chunkFromBlockCoords = this.getMinecraftWorld().getChunkFromBlockCoords(notifyPos);
                    if (StaticMixinHelper.packetPlayer != null) {
                        IMixinChunk spongeChunk = (IMixinChunk) chunkFromBlockCoords;
                        if (!(spongeChunk instanceof EmptyChunk)) {
                            spongeChunk.addTrackedBlockPosition(iblockstate.getBlock(), notifyPos, (User) StaticMixinHelper.packetPlayer,
                                    PlayerTracker.Type.NOTIFIER);
                        }
                    } else {
                        Object source = null;
                        if (this.hasTickingBlock()) {
                            source = this.getCurrentTickBlock().get();
                            sourcePos = VecHelper.toBlockPos(this.getCurrentTickBlock().get().getPosition());
                        } else if (this.hasTickingTileEntity()) {
                            source = this.getCurrentTickTileEntity().get();
                            sourcePos = ((net.minecraft.tileentity.TileEntity) this.getCurrentTickTileEntity().get()).getPos();
                        } else if (this.hasTickingEntity()) { // Falling Blocks
                            IMixinEntity spongeEntity = (IMixinEntity) this.getCurrentTickEntity().get();
                            sourcePos = ((net.minecraft.entity.Entity) this.getCurrentTickEntity().get()).getPosition();
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

    public void markAndNotifyBlockPost(List<Transaction<BlockSnapshot>> transactions, @Nullable CaptureType type, Cause cause) {
        // We have to use a proxy so that our pending changes are notified such that any accessors from block
        // classes do not fail on getting the incorrect block state from the IBlockAccess
        SpongeProxyBlockAccess proxyBlockAccess = new SpongeProxyBlockAccess(this.getMinecraftWorld(), transactions);
        for (Transaction<BlockSnapshot> transaction : transactions) {
            if (!transaction.isValid()) {
                continue; // Don't use invalidated block transactions during notifications, these only need to be restored
            }
            // Handle custom replacements
            if (transaction.getCustom().isPresent()) {
                switchToPhase(TrackingPhases.BLOCK, BlockPhase.State.RESTORING_BLOCKS);
                transaction.getFinal().restore(true, false);
                pop();
            }

            SpongeBlockSnapshot oldBlockSnapshot = (SpongeBlockSnapshot) transaction.getOriginal();
            SpongeBlockSnapshot newBlockSnapshot = (SpongeBlockSnapshot) transaction.getFinal();
            SpongeHooks.logBlockAction(cause, this.getMinecraftWorld(), type, transaction);
            int updateFlag = oldBlockSnapshot.getUpdateFlag();
            BlockPos pos = VecHelper.toBlockPos(oldBlockSnapshot.getPosition());
            IBlockState originalState = (IBlockState) oldBlockSnapshot.getState();
            IBlockState newState = (IBlockState) newBlockSnapshot.getState();
            BlockSnapshot currentTickingBlock = this.getCurrentTickBlock().orElse(null);
            // Containers get placed automatically
            if (!SpongeImplHooks.blockHasTileEntity(newState.getBlock(), newState)) {
                this.setCurrentTickBlock(this.getMixinWorld().createSpongeBlockSnapshot(newState,
                        newState.getBlock().getActualState(newState, proxyBlockAccess, pos), pos, updateFlag));
                newState.getBlock().onBlockAdded(this.getMinecraftWorld(), pos, newState);
                if (TrackingHelper.shouldChainCause(this, cause)) {
                    Cause currentCause = cause;
                    List<NamedCause> causes = new ArrayList<>();
                    causes.add(NamedCause.source(this.getCurrentTickBlock().get()));
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
            }

            proxyBlockAccess.proceed();
            this.getMixinWorld().markAndNotifyNeighbors(pos, null, originalState, newState, updateFlag);

            // Handle any additional captures during notify
            // This is to ensure new captures do not leak into next tick with wrong cause
            if (this.getCapturedEntities().size() > 0 && this.pluginCause == null) {
                this.handlePostTickCaptures(cause);
            }

            if (currentTickingBlock != null) {
                this.setCurrentTickBlock(currentTickingBlock);
            } else {
                this.currentTickBlock = null;
            }
        }
    }


    public void setCommandCapture(ICommandSender sender, ICommand command) {
        this.commandSender = sender;
        this.command = command;
        switchToPhase(TrackingPhases.GENERAL, GeneralPhase.State.COMMAND);
    }

    public void completeCommand() {
        checkState(this.command != null);
        checkState(this.commandSender != null);
        handlePostTickCaptures(Cause.of(NamedCause.of("Command", this.command), NamedCause.of("CommandSender", this.commandSender)));
        this.command = null;
        this.commandSender = null;
    }

    public void completePacketProcessing(EntityPlayerMP packetPlayer) {
        final IPhaseState phaseState = this.phases.peek();
        if (phaseState.getPhase() != TrackingPhases.PACKET) {
            System.err.printf("We aren't capturing a packet!!! Curren phase: %s%n", phaseState);
        }
        handlePostTickCaptures(Cause.of(NamedCause.source(packetPlayer)));
    }

    public void completePopulate() {
        getCapturedPopulators().clear();
        pop();

    }

    public boolean setBlockState(BlockPos pos, IBlockState newState, int flags) {
        final net.minecraft.world.World minecraftWorld = this.getMinecraftWorld();
        net.minecraft.world.chunk.Chunk chunk = minecraftWorld.getChunkFromBlockCoords(pos);
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
        if (!minecraftWorld.isRemote && !this.isRestoringBlocks() && !this.isWorldSpawnerRunning() && !this.isChunkSpawnerRunning()) {
            originalBlockSnapshot = null;
            MutablePair<BlockSnapshot, Transaction<BlockSnapshot>> pair = MoveToPhases.handleEvents(this, originalBlockSnapshot, currentState, newState, block, pos, flags, transaction, populatorSnapshotList);
            originalBlockSnapshot = pair.getLeft();
            transaction = pair.getRight();
        }

        int oldLight = currentState.getBlock().getLightValue();

        IBlockState iblockstate1 = ((IMixinChunk) chunk).setBlockState(pos, newState, currentState, newBlockSnapshot);

        if (iblockstate1 == null) {
            if (originalBlockSnapshot != null) {
                this.getCapturedSpongeBlockSnapshots().remove(originalBlockSnapshot);
                if (populatorSnapshotList != null) {
                    populatorSnapshotList.remove(transaction);
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

            if (this.hasPluginCause()) {
                this.handleBlockCaptures(this.getPluginCause().get());
            } else {
                // Don't notify clients or update physics while capturing blockstates
                if (originalBlockSnapshot == null) {
                    // Modularize client and physic updates
                    this.getMixinWorld().markAndNotifyNeighbors(pos, chunk, iblockstate1, newState, flags);
                }
            }

            return true;
        }
    }

    public void switchToPhase(TrackingPhase general, IPhaseState state) {
        IPhaseState currentState = this.phases.peek();
        if (!currentState.canSwitchTo(state)) {
//            throw new IllegalArgumentException(String.format("Cannot switch from %s to %s", currentState, state));
        }
        final TrackingPhase current = this.phases.current();
        if (!current.getChildren().contains(general) && current != general) {
            throw new IllegalStateException("Cannot switch to invalid phase!");
        }
        this.phases.push(state);

    }

    public void completePhase() {
        IPhaseState state = this.phases.peek();
        checkState(state != null, "On completing a state, the current state cannot be null!");
        if (state instanceof ITickingPhase) {
            if (!((ITickingPhase) state).isTicking()) {
                throw new IllegalStateException(String.format("Cannot switch from a non-currently ticking phase: %s", state));
            }
            ((ITickingPhase) state).processPostTick(this);
        }
    }

    @Nullable private EntityPlayerMP packetPlayer = null;
    @Nullable private Packet<?> packetProcessing = null;
    private boolean ignoreCreative = false;
    @Nullable private Container openContainer = null;
    @Nullable private ItemStackSnapshot packetCursor = null;
    @Nullable private ItemStack itemStackUsed = null;

    public void setPacketCapture(EntityPlayerMP packetPlayer, Packet<?> packetIn, boolean ignoreCreative, Container openContainer,
            ItemStackSnapshot cursor, ItemStack itemUsed) {
        switchToPhase(TrackingPhases.PACKET, TrackingPhases.PACKET.getStateForPacket(packetIn));
        this.packetPlayer = packetPlayer;
        this.packetProcessing = packetIn;
        this.ignoreCreative = ignoreCreative;
        this.openContainer = openContainer;
        this.packetCursor = cursor;
        this.itemStackUsed = itemUsed;

    }

    public void resetTickEntity() {
        this.currentTickEntity = null;
    }

    public void resetTickBlock() {
        this.currentTickBlock = null;
    }

    public void resetTickTile() {
        this.currentTickTileEntity = null;
    }

    public List<Transaction<BlockSnapshot>> getInvalidTransactions() {
        return this.invalidTransactions;
    }
}
