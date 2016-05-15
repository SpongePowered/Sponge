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
package org.spongepowered.common.event;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.effect.EntityWeatherEffect;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Slot;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.client.C10PacketCreativeInventoryAction;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.stats.StatList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
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
import org.spongepowered.api.event.action.LightningEvent;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.damage.source.IndirectEntityDamageSource;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.IMixinNextTickListEntry;
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
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Random;

import javax.annotation.Nullable;

public final class CauseTracker {

    private final net.minecraft.world.World targetWorld;
    private boolean processingBlockRandomTicks = false;
    private boolean processingVanillaBlockEvent = false;
    private boolean captureSpawnedEntities = false;
    private boolean captureBlockDecay = false;
    private boolean captureTerrainGen = false;
    private boolean captureBlocks = false;
    private boolean captureCommand = false;
    private boolean restoringBlocks = false;
    private boolean ignoreSpawnEvents = false;
    private List<Entity> capturedSpawnedEntities = new ArrayList<>();
    private List<Entity> capturedSpawnedEntityItems = new ArrayList<>();
    private List<BlockSnapshot> capturedSpongeBlockSnapshots = new ArrayList<>();
    private List<Transaction<BlockSnapshot>> invalidTransactions = new ArrayList<>();
    @Nullable private User currentNotifier;
    @Nullable private BlockSnapshot currentTickBlock;
    @Nullable private Entity currentTickEntity;
    @Nullable private TileEntity currentTickTileEntity;
    @Nullable public IMixinNextTickListEntry currentPendingBlockUpdate;
    @Nullable private Cause pluginCause;
    private boolean worldSpawnerRunning;
    private boolean chunkSpawnerRunning;
    private Cause currentCause;
    private Packet<?> currentPlayerPacket;

    public CauseTracker(net.minecraft.world.World targetWorld) {
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

    public boolean isIgnoringSpawnEvents() {
        return this.ignoreSpawnEvents;
    }

    public void setIgnoreSpawnEvents(boolean ignoreSpawnEvents) {
        this.ignoreSpawnEvents = ignoreSpawnEvents;
    }

    public boolean isProcessingBlockRandomTicks() {
        return this.processingBlockRandomTicks;
    }

    public void setProcessingBlockRandomTicks(boolean processingBlockRandomTicks) {
        this.processingBlockRandomTicks = processingBlockRandomTicks;
    }

    public boolean isProcessingVanillaBlockEvent() {
        return this.processingVanillaBlockEvent;
    }

    public void setProcessingVanillaBlockEvent(boolean processingVanillaBlockEvent) {
        this.processingVanillaBlockEvent = processingVanillaBlockEvent;
    }

    public boolean isCapturingSpawnedEntities() {
        return this.captureSpawnedEntities;
    }

    public void setCaptureSpawnedEntities(boolean captureSpawnedEntities) {
        this.captureSpawnedEntities = captureSpawnedEntities;
    }

    public List<Entity> getCapturedSpawnedEntities() {
        return this.capturedSpawnedEntities;
    }

    public List<Entity> getCapturedSpawnedEntityItems() {
        return this.capturedSpawnedEntityItems;
    }

    public boolean isCaptureBlockDecay() {
        return this.captureBlockDecay;
    }

    public void setCapturingBlockDecay(boolean captureBlockDecay) {
        this.captureBlockDecay = captureBlockDecay;
    }

    public boolean isCapturingTerrainGen() {
        return this.captureTerrainGen;
    }

    public void setCapturingTerrainGen(boolean captureTerrainGen) {
        this.captureTerrainGen = captureTerrainGen;
    }

    public boolean isCapturingBlocks() {
        return this.captureBlocks;
    }

    public void setCaptureBlocks(boolean captureBlocks) {
        this.captureBlocks = captureBlocks;
    }

    public boolean isCaptureCommand() {
        return this.captureCommand;
    }

    public void setCapturingCommand(boolean captureCommand) {
        this.captureCommand = captureCommand;
    }

    public boolean isRestoringBlocks() {
        return this.restoringBlocks;
    }

    public void setRestoringBlocks(boolean restoringBlocks) {
        this.restoringBlocks = restoringBlocks;
    }

    public boolean hasNotifier() {
        return this.currentNotifier != null;
    }

    public Optional<User> getCurrentNotifier() {
        return Optional.ofNullable(this.currentNotifier);
    }

    public void setCurrentNotifier(@Nullable User currentNotifier) {
        this.currentNotifier = currentNotifier;
    }

    public boolean hasTickingBlock() {
        return this.currentTickBlock != null;
    }

    public Optional<BlockSnapshot> getCurrentTickBlock() {
        return Optional.ofNullable(this.currentTickBlock);
    }

    public void setCurrentTickBlock(@Nullable BlockSnapshot currentTickBlock) {
        this.currentTickBlock = currentTickBlock;
    }

    public boolean hasTickingEntity() {
        return this.currentTickEntity != null;
    }

    public Optional<Entity> getCurrentTickEntity() {
        return Optional.ofNullable(this.currentTickEntity);
    }

    public void setCurrentTickEntity(@Nullable Entity currentTickEntity) {
        this.currentTickEntity = currentTickEntity;
    }

    public boolean hasTickingTileEntity() {
        return this.currentTickTileEntity != null;
    }

    public Optional<TileEntity> getCurrentTickTileEntity() {
        return Optional.ofNullable(this.currentTickTileEntity);
    }

    public void setCurrentTickTileEntity(TileEntity currentTickTileEntity) {
        this.currentTickTileEntity = currentTickTileEntity;
    }

    public List<BlockSnapshot> getCapturedSpongeBlockSnapshots() {
        return this.capturedSpongeBlockSnapshots;
    }

    public List<Transaction<BlockSnapshot>> getInvalidTransactions() {
        return this.invalidTransactions;
    }

    public void setInvalidTransactions(List<Transaction<BlockSnapshot>> invalidTransactions) {
        this.invalidTransactions = invalidTransactions;
    }

    public boolean isWorldSpawnerRunning() {
        return this.worldSpawnerRunning;
    }

    public void setWorldSpawnerRunning(boolean worldSpawnerRunning) {
        this.worldSpawnerRunning = worldSpawnerRunning;
    }

    public boolean isChunkSpawnerRunning() {
        return this.chunkSpawnerRunning;
    }

    public void setChunkSpawnerRunning(boolean chunkSpawnerRunning) {
        this.chunkSpawnerRunning = chunkSpawnerRunning;
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

    public Cause getCurrentCause() {
        return this.currentCause;
    }

    public void setCurrentCause(Cause cause) {
        this.currentCause = cause;
    }

    public Packet<?> getCurrentPlayerPacket() {
        return this.currentPlayerPacket;
    }

    public void setCurrentPlayerPacket(@Nullable Packet<?> currentPlayerPacket) {
        this.currentPlayerPacket = currentPlayerPacket;
    }

    public void preTrackEntity(Entity entity) {
        this.currentTickEntity = entity;
        this.currentCause = Cause.of(NamedCause.source(entity));
        this.trackEntityCausePreTick((net.minecraft.entity.Entity) entity);
    }

    public void postTrackEntity() {
        this.handlePostTickCaptures();
        this.currentTickEntity = null;
        this.currentCause = null;
        this.currentNotifier = null;
    }

    public void preTrackTileEntity(TileEntity tile) {
        this.currentTickTileEntity = tile;
        this.currentCause = Cause.of(NamedCause.source(tile));
        this.trackBlockPositionCausePreTick(((net.minecraft.tileentity.TileEntity) tile).getPos());
    }

    public void postTrackTileEntity() {
        this.handlePostTickCaptures();
        this.currentTickTileEntity = null;
        this.currentCause = null;
        this.currentNotifier = null;
    }

    public void preTrackBlock(IBlockState state, BlockPos pos) {
        this.currentTickBlock = this.getMixinWorld().createSpongeBlockSnapshot(state, state.getBlock().getActualState(state, this.getMinecraftWorld(), pos), pos, 0);
        if (currentCause != null) {
            this.currentCause = this.currentCause.merge(Cause.of(NamedCause.source(this.currentTickBlock)));
        } else {
            this.currentCause = Cause.of(NamedCause.source(this.currentTickBlock));
        }
        this.trackBlockPositionCausePreTick(pos);
    }

    public void postTrackBlock() {
        this.handlePostTickCaptures();
        this.currentTickBlock = null;
        this.currentCause = null;
        this.currentNotifier = null;
    }

    public void handleSpawnerEntities() {
        if (this.capturedSpawnedEntities.size() == 0 || (!this.worldSpawnerRunning && !this.chunkSpawnerRunning)) {
            return;
        }

        Cause cause = this.currentCause;
        Iterator<Entity> iter = this.capturedSpawnedEntities.iterator();
        ImmutableList.Builder<EntitySnapshot> entitySnapshotBuilder = new ImmutableList.Builder<>();
        while (iter.hasNext()) {
            Entity currentEntity = iter.next();
            if (this.invalidTransactions != null) {
                // check to see if this spawn is invalid and if so, remove
                boolean invalid = false;
                for (Transaction<BlockSnapshot> blockSnapshot : this.invalidTransactions) {
                    if (blockSnapshot.getOriginal().getLocation().get().getBlockPosition().equals(currentEntity.getLocation().getBlockPosition())) {
                        invalid = true;
                        iter.remove();
                        break;
                    }
                }
                if (invalid) {
                    continue;
                }
            }

            entitySnapshotBuilder.add(currentEntity.createSnapshot());
        }

        List<EntitySnapshot> entitySnapshots = entitySnapshotBuilder.build();
        if (entitySnapshots.isEmpty()) {
            return;
        }
        SpawnEntityEvent event = null;

        if (this.worldSpawnerRunning) {
            event = SpongeEventFactory.createSpawnEntityEventSpawner(cause, this.capturedSpawnedEntities, entitySnapshots, this.getWorld());
        } else {
            event = SpongeEventFactory.createSpawnEntityEventChunkLoad(cause, this.capturedSpawnedEntities, entitySnapshots, this.getWorld());
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
                    addWeatherEffect(nmsEntity, cause);
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
            this.capturedSpawnedEntities.clear();
        }
    }

    public void handlePostTickCaptures() {
        if (this.getMinecraftWorld().isRemote || this.restoringBlocks || this.currentCause == null) {
            return;
        } else if (this.capturedSpawnedEntities.size() == 0 && this.capturedSpawnedEntityItems.size() == 0 && this.capturedSpongeBlockSnapshots.size() == 0
                   && StaticMixinHelper.packetPlayer == null) {
            return; // nothing was captured, return
        }

        EntityPlayerMP player = StaticMixinHelper.packetPlayer;
        Cause cause = this.currentCause;

        // Handle Block Captures
        handleBlockCaptures();

        // Handle Player kill commands
        if (player != null && this.currentPlayerPacket instanceof C01PacketChatMessage) {
            C01PacketChatMessage chatPacket = (C01PacketChatMessage) this.currentPlayerPacket;
            if (chatPacket.getMessage().contains("kill")) {
                if (!cause.contains(player)) {
                    cause = cause.with(NamedCause.of("Player", player));
                }
            }
        }

        // Inventory Events
        if (player != null && player.getHealth() > 0 && StaticMixinHelper.lastOpenContainer != null) {
            if (this.currentPlayerPacket instanceof C10PacketCreativeInventoryAction && !StaticMixinHelper.ignoreCreativeInventoryPacket) {
                SpongeCommonEventFactory.handleCreativeClickInventoryEvent(Cause.of(NamedCause.source(player)), player,
                    (C10PacketCreativeInventoryAction) this.currentPlayerPacket);
            } else {
                SpongeCommonEventFactory.handleInteractInventoryOpenCloseEvent(Cause.of(NamedCause.source(player)), player, this.currentPlayerPacket);
                if (this.currentPlayerPacket instanceof C0EPacketClickWindow) {
                    SpongeCommonEventFactory.handleClickInteractInventoryEvent(Cause.of(NamedCause.source(player)), player,
                        (C0EPacketClickWindow) this.currentPlayerPacket);
                }
            }
        }

        // Handle Entity captures
        if (this.capturedSpawnedEntityItems.size() > 0) {
            handleDroppedItems();
        }
        if (this.capturedSpawnedEntities.size() > 0) {
            handleSpawnerEntities();
        }

        this.invalidTransactions.clear();
    }

    public void handleDroppedItems() {
        if (this.capturedSpawnedEntityItems.size() == 0 || !this.captureSpawnedEntities) {
            return;
        }

        Cause cause = this.currentCause;
        Iterator<Entity> iter = this.capturedSpawnedEntityItems.iterator();
        ImmutableList.Builder<EntitySnapshot> entitySnapshotBuilder = new ImmutableList.Builder<>();
        while (iter.hasNext()) {
            Entity currentEntity = iter.next();
            if (this.invalidTransactions != null) {
                // check to see if this drop is invalid and if so, remove
                boolean invalid = false;
                for (Transaction<BlockSnapshot> blockSnapshot : this.invalidTransactions) {
                    if (blockSnapshot.getOriginal().getLocation().get().getBlockPosition().equals(currentEntity.getLocation().getBlockPosition())) {
                        invalid = true;
                        iter.remove();
                        break;
                    }
                }
                if (invalid) {
                    continue;
                }
            }
            if (cause.first(User.class).isPresent()) {
                // store user UUID with entity to track later
                User user = cause.first(User.class).get();
                ((IMixinEntity) currentEntity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, user.getUniqueId());
            } else if (cause.first(Entity.class).isPresent()) {
                IMixinEntity spongeEntity = (IMixinEntity) cause.first(Entity.class).get();
                Optional<User> owner = spongeEntity.getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR);
                if (owner.isPresent()) {
                    if (!cause.containsNamed(NamedCause.OWNER)) {
                        cause = cause.with(NamedCause.of(NamedCause.OWNER, owner.get()));
                    }
                    ((IMixinEntity) currentEntity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, owner.get().getUniqueId());
                }
                if (spongeEntity instanceof EntityLivingBase) {
                    DamageSource lastDamageSource = spongeEntity.getLastDamageSource();
                    if (lastDamageSource != null && !cause.contains(lastDamageSource)) {
                        if (!cause.containsNamed("Attacker")) {
                            cause = cause.with(NamedCause.of("Attacker", lastDamageSource));
                        }

                    }
                }
            }
            entitySnapshotBuilder.add(currentEntity.createSnapshot());
        }

        List<EntitySnapshot> entitySnapshots = entitySnapshotBuilder.build();
        if (entitySnapshots.isEmpty()) {
            return;
        }

        Cause.Builder builder = null;
        if (cause.root() instanceof Entity) {
            builder = Cause.source(EntitySpawnCause.builder().entity((Entity) cause.root()).type(SpawnTypes.DROPPED_ITEM).build());
        } else {
            builder = Cause.source(SpawnCause.builder().type(SpawnTypes.DROPPED_ITEM).build());
        }

        Cause spawnCause = builder.build();
        if (cause.root() == StaticMixinHelper.packetPlayer) {
            cause = spawnCause.merge(Cause.of(NamedCause.owner(StaticMixinHelper.packetPlayer)));
        } else {
            cause = spawnCause.merge(cause);
        }

        DropItemEvent event = null;
        if (this.currentPlayerPacket instanceof C0EPacketClickWindow) {
            event = SpongeEventFactory.createDropItemEventDispense(cause, this.capturedSpawnedEntityItems, entitySnapshots, this.getWorld());
        } else {
            event = SpongeEventFactory.createDropItemEventDestruct(cause, this.capturedSpawnedEntityItems, entitySnapshots, this.getWorld());
        }

        if (!(SpongeImpl.postEvent(event))) {
            Iterator<Entity> iterator = event instanceof DropItemEvent.Destruct ? ((DropItemEvent.Destruct) event).getEntities().iterator()
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
            sendItemChangeToPlayer(StaticMixinHelper.packetPlayer);
            this.capturedSpawnedEntityItems.clear();
        }
    }

    private boolean addWeatherEffect(net.minecraft.entity.Entity entity, Cause cause) {
        if (entity instanceof EntityLightningBolt) {
            LightningEvent.Pre event = SpongeEventFactory.createLightningEventPre(((IMixinEntityLightningBolt) entity).getCause());
            SpongeImpl.postEvent(event);
            if (!event.isCancelled()) {
                return getMinecraftWorld().addWeatherEffect(entity);
            }
        } else {
            return getMinecraftWorld().addWeatherEffect(entity);
        }
        return false;
    }

    public void handleBlockCaptures() {
        Cause cause = this.currentCause;
        EntityPlayerMP player = StaticMixinHelper.packetPlayer;

        ImmutableList<Transaction<BlockSnapshot>> blockBreakTransactions = null;
        ImmutableList<Transaction<BlockSnapshot>> blockModifyTransactions = null;
        ImmutableList<Transaction<BlockSnapshot>> blockPlaceTransactions = null;
        ImmutableList<Transaction<BlockSnapshot>> blockDecayTransactions = null;
        ImmutableList<Transaction<BlockSnapshot>> blockMultiTransactions = null;
        ImmutableList.Builder<Transaction<BlockSnapshot>> breakBuilder = new ImmutableList.Builder<>();
        ImmutableList.Builder<Transaction<BlockSnapshot>> placeBuilder = new ImmutableList.Builder<>();
        ImmutableList.Builder<Transaction<BlockSnapshot>> decayBuilder = new ImmutableList.Builder<>();
        ImmutableList.Builder<Transaction<BlockSnapshot>> modifyBuilder = new ImmutableList.Builder<>();
        ImmutableList.Builder<Transaction<BlockSnapshot>> multiBuilder = new ImmutableList.Builder<>();
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
            Transaction<BlockSnapshot> transaction = new Transaction<>(blockSnapshot, this.getMixinWorld().createSpongeBlockSnapshot(currentState, currentState.getBlock()
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
                processList(listIterator);

                if (player != null) {
                    CaptureType captureType = null;
                    if (this.currentPlayerPacket instanceof C08PacketPlayerBlockPlacement) {
                        captureType = CaptureType.PLACE;
                    } else if (this.currentPlayerPacket instanceof C07PacketPlayerDigging) {
                        captureType = CaptureType.BREAK;
                    }
                    if (captureType != null) {
                        handlePostPlayerBlockEvent(captureType, changeBlockEvent.getTransactions());
                    }
                }

                // clear entity list and return to avoid spawning items
                this.capturedSpawnedEntities.clear();
                this.capturedSpawnedEntityItems.clear();
                return;
            }
        }

        if (blockDecayTransactions.size() > 0) {
            changeBlockEvent = SpongeEventFactory.createChangeBlockEventDecay(cause, this.getWorld(), blockDecayTransactions);
            SpongeImpl.postEvent(changeBlockEvent);
            blockEvents.add(changeBlockEvent);
        }

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

            if (this.currentPlayerPacket instanceof C08PacketPlayerBlockPlacement) {
                packet = (C08PacketPlayerBlockPlacement) this.currentPlayerPacket;
            }

            if (blockEvent.isCancelled()) {
                // Restore original blocks
                ListIterator<Transaction<BlockSnapshot>>
                    listIterator =
                    blockEvent.getTransactions().listIterator(blockEvent.getTransactions().size());
                processList(listIterator);

                handlePostPlayerBlockEvent(captureType, blockEvent.getTransactions());

                // clear entity list and return to avoid spawning items
                this.capturedSpawnedEntities.clear();
                this.capturedSpawnedEntityItems.clear();
                return;
            } else {
                for (Transaction<BlockSnapshot> transaction : blockEvent.getTransactions()) {
                    if (!transaction.isValid()) {
                        this.invalidTransactions.add(transaction);
                    } else {
                        if (captureType == CaptureType.BREAK && !(transaction.getOriginal().getState().getType() instanceof BlockLiquid) && cause.first(User.class).isPresent()) {
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

                        if (captureType == CaptureType.PLACE && player != null && this.currentPlayerPacket instanceof C08PacketPlayerBlockPlacement) {
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
                        this.restoringBlocks = true;
                        transaction.getOriginal().restore(true, false);
                        this.restoringBlocks = false;
                    }
                    handlePostPlayerBlockEvent(captureType, this.invalidTransactions);
                }

                this.markAndNotifyBlockPost(blockEvent.getTransactions(), captureType);

                if (captureType == CaptureType.PLACE && player != null && packet != null && packet.getStack() != null) {
                    player.addStat(StatList.objectUseStats[net.minecraft.item.Item.getIdFromItem(packet.getStack().getItem())], 1);
                }
            }
        }
    }

    private void handlePostPlayerBlockEvent(CaptureType captureType, List<Transaction<BlockSnapshot>> transactions) {
        if (StaticMixinHelper.packetPlayer == null) {
            return;
        }

        if (captureType == CaptureType.BREAK) {
            // Let the client know the blocks still exist
            for (Transaction<BlockSnapshot> transaction : transactions) {
                BlockSnapshot snapshot = transaction.getOriginal();
                BlockPos pos = VecHelper.toBlockPos(snapshot.getPosition());
                StaticMixinHelper.packetPlayer.playerNetServerHandler.sendPacket(new S23PacketBlockChange(this.getMinecraftWorld(), pos));

                // Update any tile entity data for this block
                net.minecraft.tileentity.TileEntity tileentity = this.getMinecraftWorld().getTileEntity(pos);
                if (tileentity != null) {
                    Packet<?> pkt = tileentity.getDescriptionPacket();
                    if (pkt != null) {
                        StaticMixinHelper.packetPlayer.playerNetServerHandler.sendPacket(pkt);
                    }
                }
            }
        }

        sendItemChangeToPlayer(StaticMixinHelper.packetPlayer);
    }

    public void handleNonLivingEntityDestruct(net.minecraft.entity.Entity entityIn) {
        if (entityIn.isDead && (!(entityIn instanceof EntityLivingBase) || entityIn instanceof EntityArmorStand)) {
            MessageChannel originalChannel = MessageChannel.TO_NONE;

            IMixinEntity spongeEntity = (IMixinEntity) entityIn;
            Cause cause = spongeEntity.getNonLivingDestructCause();;
            if (cause == null) {
                cause = Cause.of(NamedCause.source(entityIn.worldObj));
            }

            if (cause.root() instanceof EntityDamageSource) {
                EntityDamageSource entityDamageSource = (EntityDamageSource) cause.root();
                if (entityDamageSource.getSourceOfDamage() instanceof Player) {
                    originalChannel = ((Player) entityDamageSource.getSourceOfDamage()).getMessageChannel();
                } else if (entityDamageSource instanceof IndirectEntityDamageSource) {
                    IndirectEntityDamageSource indirectDamageSource = (IndirectEntityDamageSource) entityDamageSource;
                    if (indirectDamageSource.getIndirectSource() instanceof Player) {
                        originalChannel = ((Player) indirectDamageSource.getIndirectSource()).getMessageChannel();
                    }
                }
            } else if (cause.root() instanceof Player) {
                originalChannel = ((Player) cause.root()).getMessageChannel();
            }

            DestructEntityEvent event = SpongeEventFactory.createDestructEntityEvent(
                    cause, originalChannel, Optional.of(originalChannel), new MessageEvent.MessageFormatter(),
                    (Entity) entityIn, true
            );
            SpongeImpl.getGame().getEventManager().post(event);
            if (!event.isMessageCancelled()) {
                event.getChannel().ifPresent(channel -> channel.send(entityIn, event.getMessage()));
            }
        }
    }

    private void sendItemChangeToPlayer(EntityPlayerMP player) {
        if (StaticMixinHelper.prePacketProcessItem == null) {
            return;
        }

        // handle revert
        player.isChangingQuantityOnly = true;
        player.inventory.mainInventory[player.inventory.currentItem] = StaticMixinHelper.prePacketProcessItem;
        Slot slot = player.openContainer.getSlotFromInventory(player.inventory, player.inventory.currentItem);
        player.openContainer.detectAndSendChanges();
        player.isChangingQuantityOnly = false;
        // force client itemstack update if place event was cancelled
        player.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(player.openContainer.windowId, slot.slotNumber,
            StaticMixinHelper.prePacketProcessItem));
    }

    private void processList(ListIterator<Transaction<BlockSnapshot>> listIterator) {
        while (listIterator.hasPrevious()) {
            Transaction<BlockSnapshot> transaction = listIterator.previous();
            this.restoringBlocks = true;
            transaction.getOriginal().restore(true, false);
            this.restoringBlocks = false;
        }
    }

    public boolean processSpawnEntity(Entity entity, Cause spawnCause) {
        checkNotNull(entity, "Entity cannot be null!");
        checkNotNull(spawnCause, "Cause cannot be null!");

        this.trackEntityOwner(entity);
        // Very first thing - fire events that are from entity construction
        if (((IMixinEntity) entity).isInConstructPhase()) {
            ((IMixinEntity) entity).firePostConstructEvents();
        }

        net.minecraft.entity.Entity entityIn = (net.minecraft.entity.Entity) entity;
        // do not drop any items while restoring blocksnapshots. Prevents dupes
        if (!this.getMinecraftWorld().isRemote && (entityIn == null || (entityIn instanceof net.minecraft.entity.item.EntityItem && this.restoringBlocks))) {
            return false;
        }

        int i = MathHelper.floor_double(entityIn.posX / 16.0D);
        int j = MathHelper.floor_double(entityIn.posZ / 16.0D);
        boolean flag = entityIn.forceSpawn;

        // This is usually null during world gen
        if (this.currentCause != null && !this.currentCause.first(SpawnCause.class).isPresent()) {
            this.currentCause =  spawnCause.merge(this.currentCause);
        } else if (this.currentCause == null) {
            this.currentCause = spawnCause;
        }

        if (entityIn instanceof EntityPlayer) {
            flag = true;
        } else if (entityIn instanceof EntityLightningBolt) {
            ((IMixinEntityLightningBolt) entityIn).setCause(this.currentCause);
        }

        if (!flag && !this.getMinecraftWorld().isChunkLoaded(i, j, true)) {
            return false;
        } else {
            if (flag) {
                EntityPlayer entityplayer = (EntityPlayer) entityIn;
                net.minecraft.world.World world = this.targetWorld;
                world.playerEntities.add(entityplayer);
                world.updateAllPlayersSleepingFlag();
                if (SpongeImpl.postEvent(SpongeEventFactory.createSpawnEntityEvent(this.currentCause, Lists.newArrayList(entity),
                        Lists.newArrayList(entity.createSnapshot()), getWorld())) && !flag) {
                    return false;
                }
                this.getMinecraftWorld().getChunkFromChunkCoords(i, j).addEntity(entityIn);
                this.getMinecraftWorld().loadedEntityList.add(entityIn);
                this.getMixinWorld().onSpongeEntityAdded(entityIn);
                return true;
            }

            if (entityIn instanceof EntityFishHook && ((EntityFishHook) entityIn).angler == null) {
                // TODO MixinEntityFishHook.setShooter makes angler null
                // sometimes, but that will cause NPE when ticking
                return false;
            }

            org.spongepowered.api.event.Event event = null;
            List<Entity> entitiesToSpawn = Lists.newArrayList(entity);
            ImmutableList<EntitySnapshot> entitySnapshots = ImmutableList.of(entity.createSnapshot());
            EntityLivingBase entityLiving = null;
            net.minecraft.entity.Entity nonLivingEntity = null;
            if (this.currentTickEntity instanceof EntityLivingBase) {
                entityLiving = (EntityLivingBase) this.currentTickEntity;
            } else if (this.currentTickEntity != null) {
                nonLivingEntity = (net.minecraft.entity.Entity) this.currentTickEntity;
            }
            if (entityIn instanceof EntityItem) {
                if ((nonLivingEntity != null && nonLivingEntity.isDead) || entityIn instanceof EntityXPOrb || (entityLiving != null && (entityLiving.getHealth() <= 0 || entityLiving.isDead))) {
                    event = SpongeEventFactory.createDropItemEventDestruct(this.currentCause, entitiesToSpawn, entitySnapshots, this.getWorld());
                } else {
                    event = SpongeEventFactory.createDropItemEventDispense(this.currentCause, entitiesToSpawn, entitySnapshots, this.getWorld());
                }
            } else {
                event = SpongeEventFactory.createSpawnEntityEvent(this.currentCause, entitiesToSpawn, entitySnapshots, this.getWorld());
            }

            if (!SpongeImpl.postEvent(event)) {
                if (entityIn instanceof EntityWeatherEffect) {
                    return addWeatherEffect(entityIn, this.currentCause);
                }
                this.getMinecraftWorld().getChunkFromChunkCoords(i, j).addEntity(entityIn);
                this.getMinecraftWorld().loadedEntityList.add(entityIn);
                this.getMixinWorld().onSpongeEntityAdded(entityIn);
                return true;
            }

            return false;
        }
    }

    public void randomTickBlock(Block block, BlockPos pos, IBlockState state, Random random) {
        this.processingBlockRandomTicks = true;
        this.preTrackBlock(state, pos);
        block.randomTick(this.getMinecraftWorld(), pos, state, random);
        this.postTrackBlock();
        this.processingBlockRandomTicks = false;
    }

    // By this point, currentPending(NextTickListEntry) should always be available
    public void updateTickBlock(Block block, BlockPos pos, IBlockState state, Random rand) {
        this.currentTickBlock = this.getMixinWorld().createSpongeBlockSnapshot(state, state.getBlock().getActualState(state, this.getMinecraftWorld(), pos), pos, 0);
        List<NamedCause> namedCauses = new ArrayList<>();
        namedCauses.add(NamedCause.source(this.currentTickBlock));
        if (this.currentPendingBlockUpdate.hasTickingBlock()) {
            namedCauses.add(NamedCause.of("SchedulerSource", this.currentPendingBlockUpdate.getCurrentTickBlock().get()));
            if (this.currentPendingBlockUpdate.hasTickingTileEntity()) {
                namedCauses.add(NamedCause.of("SchedulerParentSource", this.currentPendingBlockUpdate.getCurrentTickTileEntity().get()));
            }
        } else if (this.currentPendingBlockUpdate.hasTickingTileEntity()) {
            namedCauses.add(NamedCause.of("SchedulerSource", this.currentPendingBlockUpdate.getCurrentTickTileEntity().get()));
        }

        if (!this.currentPendingBlockUpdate.hasSourceUser()) {
            this.currentNotifier = this.trackBlockPositionCausePreTick(pos).orElse(null);
        } else {
            this.currentNotifier = this.currentPendingBlockUpdate.getSourceUser().get();
        }

        if (this.currentNotifier != null) {
            namedCauses.add(NamedCause.notifier(this.currentNotifier));
        }
        this.currentCause = Cause.of(namedCauses);
        block.updateTick(this.getMinecraftWorld(), pos, state, rand);
        this.postTrackBlock();
        this.currentPendingBlockUpdate = null;
        this.currentTickTileEntity = null;
    }

    public void notifyBlockOfStateChange(BlockPos notifyPos, final Block sourceBlock, BlockPos sourcePos) {
        if (!this.getMinecraftWorld().isRemote) {
            IBlockState iblockstate = this.getMinecraftWorld().getBlockState(notifyPos);
            if (iblockstate == Blocks.air.getDefaultState() || this.isRestoringBlocks() || this.isWorldSpawnerRunning() || this.isChunkSpawnerRunning()
                    || this.isCapturingTerrainGen()) {
                iblockstate.getBlock().onNeighborBlockChange(this.getMinecraftWorld(), notifyPos, iblockstate, sourceBlock);
                return;
            }

            try {
                if (!this.tryAndTrackActiveUser(notifyPos, PlayerTracker.Type.NOTIFIER).isPresent()) {
                    if (this.hasTickingBlock()) {
                        this.trackTargetBlockFromSource(((SpongeBlockSnapshot) this.currentTickBlock).getBlockPos(), sourcePos, iblockstate.getBlock(), notifyPos,
                                PlayerTracker.Type.NOTIFIER);
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

    public void markAndNotifyBlockPost(List<Transaction<BlockSnapshot>> transactions, CaptureType type) {
        // We have to use a proxy so that our pending changes are notified such that any accessors from block
        // classes do not fail on getting the incorrect block state from the IBlockAccess
        SpongeProxyBlockAccess proxyBlockAccess = new SpongeProxyBlockAccess(this.getMinecraftWorld(), transactions);
        for (Transaction<BlockSnapshot> transaction : transactions) {
            if (!transaction.isValid()) {
                continue; // Don't use invalidated block transactions during notifications, these only need to be restored
            }
            // Handle custom replacements
            if (transaction.getCustom().isPresent()) {
                this.setRestoringBlocks(true);
                transaction.getFinal().restore(true, false);
                this.setRestoringBlocks(false);
            }

            SpongeBlockSnapshot oldBlockSnapshot = (SpongeBlockSnapshot) transaction.getOriginal();
            SpongeBlockSnapshot newBlockSnapshot = (SpongeBlockSnapshot) transaction.getFinal();
            SpongeHooks.logBlockAction(this.currentCause, this.getMinecraftWorld(), type, transaction);
            int updateFlag = oldBlockSnapshot.getUpdateFlag();
            BlockPos pos = VecHelper.toBlockPos(oldBlockSnapshot.getPosition());
            IBlockState originalState = (IBlockState) oldBlockSnapshot.getState();
            IBlockState newState = (IBlockState) newBlockSnapshot.getState();
            BlockSnapshot currentTickingBlock = this.getCurrentTickBlock().orElse(null);
            // Containers get placed automatically
            if (originalState.getBlock() != newState.getBlock() && !SpongeImplHooks.blockHasTileEntity(newState.getBlock(), newState)) {
                this.setCurrentTickBlock(this.getMixinWorld().createSpongeBlockSnapshot(newState,
                        newState.getBlock().getActualState(newState, proxyBlockAccess, pos), pos, updateFlag));
                List<NamedCause> namedCauses = new ArrayList<>();
                namedCauses.add(NamedCause.source(this.currentTickBlock));
                namedCauses.add(NamedCause.of("ParentSource", this.currentCause.root()));
                if (this.currentNotifier != null) {
                    namedCauses.add(NamedCause.notifier(this.currentNotifier));
                } else if (StaticMixinHelper.packetPlayer != null) {
                    namedCauses.add(NamedCause.owner(StaticMixinHelper.packetPlayer));
                }
                Cause preCause = this.currentCause;
                this.currentCause = Cause.of(namedCauses);
                newState.getBlock().onBlockAdded(this.getMinecraftWorld(), pos, newState);
                // Handle any additional captures during onBlockAdded
                // This is to ensure new captures do not leak into next tick with wrong cause
                if (this.getCapturedSpongeBlockSnapshots().size() > 0) {
                    this.handlePostTickCaptures();
                }
                this.currentCause = preCause;
            }

            proxyBlockAccess.proceed();
            this.getMixinWorld().markAndNotifyNeighbors(pos, null, originalState, newState, updateFlag);
            // Handle any additional captures during notify
            // This is to ensure new captures do not leak into next tick with wrong cause
            if (this.getCapturedSpongeBlockSnapshots().size() > 0) {
                this.handlePostTickCaptures();
            }
            this.setCurrentTickBlock(currentTickingBlock);
        }
    }

    /*private boolean shouldChainCause(Cause cause) {
        return !this.isCapturingTerrainGen() && !this.isWorldSpawnerRunning() && !this.isChunkSpawnerRunning()
               && !this.isProcessingBlockRandomTicks() && !this.isCaptureCommand() && this.hasTickingBlock() && this.pluginCause == null
               && !cause.contains(this.getCurrentTickBlock().get()) && !(StaticMixinHelper.processingPacket instanceof C03PacketPlayer);

    }*/

    private Predicate<net.minecraft.entity.Entity> entityTrackerPredicate = new Predicate<net.minecraft.entity.Entity>() {

        @Override
        public boolean apply(net.minecraft.entity.Entity input) {
            if (input instanceof EntityLivingBase) {
                return false;
            }
            if (input instanceof EntityItem) {
                return false;
            }
            return true;
        }

    };
    private static AxisAlignedBB entityAABB = AxisAlignedBB.fromBounds(0, 0, 0, 0, 0, 0);
    private AxisAlignedBB getEntityAABBForBlockPos(BlockPos pos) {
        entityAABB.minX = pos.getX();
        entityAABB.minY = pos.getY();
        entityAABB.minZ = pos.getZ();
        entityAABB.maxX = pos.getX() + 0.1;
        entityAABB.maxY = pos.getY() + 0.1;
        entityAABB.maxZ = pos.getZ() + 0.1;
        return entityAABB;
    }

    public Optional<User> tryAndTrackActiveUser(BlockPos targetPos, PlayerTracker.Type type) {
        net.minecraft.world.World world = this.getMinecraftWorld();
        if (targetPos == null || !world.isBlockLoaded(targetPos)) {
            return Optional.empty();
        }

        User user = null;
        IMixinWorld spongeWorld = (IMixinWorld) world;
        IMixinChunk spongeChunk = (IMixinChunk) world.getChunkFromBlockCoords(targetPos);
        if (spongeChunk != null && !(spongeChunk instanceof EmptyChunk)) {
            final CauseTracker causeTracker = spongeWorld.getCauseTracker();
            if (StaticMixinHelper.packetPlayer != null) {
                user = (User) StaticMixinHelper.packetPlayer;
                spongeChunk.addTrackedBlockPosition(world.getBlockState(targetPos).getBlock(), targetPos, user, type);
            } else if (causeTracker.hasNotifier()) {
                user = causeTracker.getCurrentNotifier().get();
                spongeChunk.addTrackedBlockPosition(world.getBlockState(targetPos).getBlock(), targetPos, user, type);
            }
            // check if a non-living entity exists at target block position (ex. minecarts)
            if (user != null) {
                Chunk chunk = (Chunk) spongeChunk;
                List<net.minecraft.entity.Entity> entityList = new ArrayList<>();
                chunk.getEntitiesOfTypeWithinAAAB(net.minecraft.entity.Entity.class, this.getEntityAABBForBlockPos(targetPos), entityList, this.entityTrackerPredicate);
                for (net.minecraft.entity.Entity entity : entityList) {
                    ((IMixinEntity) entity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_NOTIFIER, user.getUniqueId());
                }
            }
        }

        return Optional.ofNullable(user);
    }

    public Optional<User> trackTargetBlockFromSource(Object source, BlockPos sourcePos, Block targetBlock, BlockPos targetPos, PlayerTracker.Type type) {
        // first check to see if we have an active user
        Optional<User> user = tryAndTrackActiveUser(targetPos, type);
        if (user.isPresent()) {
            return user;
        }

        net.minecraft.world.World world = this.getMinecraftWorld();
        if (sourcePos == null || !world.isBlockLoaded(sourcePos)) {
            return Optional.empty();
        }

        IMixinChunk spongeChunk = (IMixinChunk) world.getChunkFromBlockCoords(sourcePos);
        if (spongeChunk != null && !(spongeChunk instanceof EmptyChunk)) {
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

    public Optional<User> trackBlockPositionCausePreTick(BlockPos pos) {
        if (pos == null || this.currentCause == null) {
            return Optional.empty();
        }

        net.minecraft.world.World world = this.getMinecraftWorld();
        IMixinWorld spongeWorld = (IMixinWorld) world;
        IMixinChunk spongeChunk = (IMixinChunk) world.getChunkFromBlockCoords(pos);
        final CauseTracker causeTracker = spongeWorld.getCauseTracker();
        if (spongeChunk != null && !(spongeChunk instanceof EmptyChunk)) {
            Optional<User> owner = spongeChunk.getBlockOwner(pos);
            Optional<User> notifier = spongeChunk.getBlockNotifier(pos);
            if (notifier.isPresent()) {
                User user = notifier.get();
                causeTracker.setCurrentNotifier(user);
                this.currentCause = this.currentCause.merge(Cause.of(NamedCause.notifier(user)));
                return notifier;
            } else if (owner.isPresent()) {
                User user = owner.get();
                this.currentCause = this.currentCause.merge(Cause.of(NamedCause.notifier(user)));
                causeTracker.setCurrentNotifier(user);
                return owner;
            }
            if (notifier.isPresent()) {
                return notifier;
            } else {
                return owner;
            }
        }

        return Optional.empty();
    }

    public void trackEntityCausePreTick(net.minecraft.entity.Entity entity) {
        if (entity == null || this.currentCause == null) {
            return;
        }

        net.minecraft.world.World world = this.getMinecraftWorld();
        IMixinWorld spongeWorld = (IMixinWorld) world;
        IMixinEntity spongeEntity = (IMixinEntity) entity;
        final CauseTracker causeTracker = spongeWorld.getCauseTracker();
        Optional<User> owner = spongeEntity.getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR);
        if (!owner.isPresent()) {
            if (entity instanceof EntityTameable) {
                EntityTameable tameable = (EntityTameable) entity;
                if (tameable.getOwner() != null) {
                    owner = Optional.of((User) tameable.getOwner());
                }
            }
        }
        Optional<User> notifier = spongeEntity.getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_NOTIFIER);
        if (notifier.isPresent()) {
            User user = notifier.get();
            causeTracker.setCurrentNotifier(user);
            this.currentCause = this.currentCause.merge(Cause.of(NamedCause.notifier(user)));
        } else if (owner.isPresent()) {
            User user = owner.get();
            this.currentCause = this.currentCause.merge(Cause.of(NamedCause.owner(user)));
            causeTracker.setCurrentNotifier(user);
        }
    }

    // Handle entity ownership tracking
    public void trackEntityOwner(Entity entity) {
        User currentUser = StaticMixinHelper.packetPlayer != null ? (User) StaticMixinHelper.packetPlayer : this.currentNotifier;
        if (currentUser != null) {
            ((IMixinEntity) entity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, currentUser.getUniqueId());
        } else if (this.currentTickEntity != null) {
            IMixinEntity spongeEntity = (IMixinEntity) this.currentTickEntity;
            Optional<User> owner = spongeEntity.getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR);
            if (owner.isPresent()) {
                ((IMixinEntity) entity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, owner.get().getUniqueId());
            } else {
                Optional<User> notifier = spongeEntity.getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_NOTIFIER);
                if (notifier.isPresent()) {
                    ((IMixinEntity) entity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, notifier.get().getUniqueId());
                }
            }
        }
    }
}
