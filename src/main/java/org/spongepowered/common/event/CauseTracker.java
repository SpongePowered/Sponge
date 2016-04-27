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
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Slot;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.client.C10PacketCreativeInventoryAction;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.stats.StatList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ReportedException;
import net.minecraft.world.chunk.EmptyChunk;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.Item;
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
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import javax.annotation.Nullable;

public final class CauseTracker {

    private final net.minecraft.world.World targetWorld;

    private boolean processingCaptureCause = false;
    private boolean processingBlockRandomTicks = false;
    private boolean captureEntitySpawns = true;
    private boolean captureBlockDecay = false;
    private boolean captureTerrainGen = false;
    private boolean captureBlocks = false;
    private boolean captureCommand = false;
    private boolean restoringBlocks = false;
    private boolean spawningDeathDrops = false;
    private List<Entity> capturedEntities = new ArrayList<>();
    private List<Entity> capturedEntityItems = new ArrayList<>();
    @Nullable private BlockSnapshot currentTickBlock;
    @Nullable private Entity currentTickEntity;
    @Nullable private TileEntity currentTickTileEntity;
    @Nullable private Cause pluginCause;
    private List<BlockSnapshot> capturedSpongeBlockSnapshots = new ArrayList<>();
    private List<Transaction<BlockSnapshot>> invalidTransactions = new ArrayList<>();
    private boolean worldSpawnerRunning;
    private boolean chunkSpawnerRunning;

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

    public boolean isProcessingCaptureCause() {
        return this.processingCaptureCause;
    }

    public void setProcessingCaptureCause(boolean processingCaptureCause) {
        this.processingCaptureCause = processingCaptureCause;
    }

    public boolean isProcessingBlockRandomTicks() {
        return this.processingBlockRandomTicks;
    }

    public void setProcessingBlockRandomTicks(boolean processingBlockRandomTicks) {
        this.processingBlockRandomTicks = processingBlockRandomTicks;
    }

    public boolean isCaptureEntitySpawns() {
        return this.captureEntitySpawns;
    }

    public void setCaptureEntitySpawns(boolean captureEntitySpawns) {
        this.captureEntitySpawns = captureEntitySpawns;
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

    public boolean isSpawningDeathDrops() {
        return this.spawningDeathDrops;
    }

    public void setSpawningDeathDrops(boolean spawningDeathDrops) {
        this.spawningDeathDrops = spawningDeathDrops;
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

    public void handleEntitySpawns(Cause cause) {
        Iterator<Entity> iter = this.capturedEntities.iterator();
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
            if (cause.first(User.class).isPresent()) {
                // store user UUID with entity to track later
                User user = cause.first(User.class).get();
                ((IMixinEntity) currentEntity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, user.getUniqueId());
            } else if (cause.first(Entity.class).isPresent()) {
                IMixinEntity spongeEntity = (IMixinEntity) cause.first(Entity.class).get();
                Optional<User> owner = spongeEntity.getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR);
                if (owner.isPresent() && !cause.containsNamed(NamedCause.OWNER)) {
                    cause = cause.with(NamedCause.of(NamedCause.OWNER, owner.get()));
                    ((IMixinEntity) currentEntity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, owner.get().getUniqueId());
                }
            }
            entitySnapshotBuilder.add(currentEntity.createSnapshot());
        }

        List<EntitySnapshot> entitySnapshots = entitySnapshotBuilder.build();
        if (entitySnapshots.isEmpty()) {
            return;
        }
        SpawnEntityEvent event;

        if (this.worldSpawnerRunning) {
            event = SpongeEventFactory.createSpawnEntityEventSpawner(cause, this.capturedEntities, entitySnapshots, this.getWorld());
        } else if (this.chunkSpawnerRunning) {
            event = SpongeEventFactory.createSpawnEntityEventChunkLoad(cause, this.capturedEntities, entitySnapshots, this.getWorld());
        } else {
            List<NamedCause> namedCauses = new ArrayList<>();
            for (Map.Entry<String, Object> entry : cause.getNamedCauses().entrySet()) {
                if (entry.getKey().equalsIgnoreCase(NamedCause.SOURCE)) {
                    if (!(entry.getValue() instanceof SpawnCause)) {
                        namedCauses.add(NamedCause.source(SpawnCause.builder().type(SpawnTypes.CUSTOM).build()));
                    } else {
                        namedCauses.add(NamedCause.source(entry.getValue()));
                    }
                } else {
                    namedCauses.add(NamedCause.of(entry.getKey(), entry.getValue()));
                }
            }
            cause = Cause.of(namedCauses);
            event = SpongeEventFactory.createSpawnEntityEvent(cause, this.capturedEntities, entitySnapshotBuilder.build(), this.getWorld());
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
            this.capturedEntities.clear();
        }
    }

    public void handlePostTickCaptures(Cause cause) {
        if (this.getMinecraftWorld().isRemote || this.restoringBlocks || this.spawningDeathDrops || cause == null) {
            return;
        } else if (this.capturedEntities.size() == 0 && this.capturedEntityItems.size() == 0 && this.capturedSpongeBlockSnapshots.size() == 0
                   && StaticMixinHelper.packetPlayer == null) {
            return; // nothing was captured, return
        }

        EntityPlayerMP player = StaticMixinHelper.packetPlayer;
        Packet<?> packetIn = StaticMixinHelper.processingPacket;

        // Attempt to find a Player cause if we do not have one
        if (!cause.first(User.class).isPresent() && !(this.capturedSpongeBlockSnapshots.size() > 0
                                                      && ((SpongeBlockSnapshot) this.capturedSpongeBlockSnapshots.get(0)).captureType
                                                         == CaptureType.DECAY)) {
            if ((cause.first(BlockSnapshot.class).isPresent() || cause.first(TileEntity.class).isPresent())) {
                // Check for player at pos of first transaction
                Optional<BlockSnapshot> snapshot = cause.first(BlockSnapshot.class);
                Optional<TileEntity> te = cause.first(TileEntity.class);
                BlockPos pos = null;
                if (snapshot.isPresent()) {
                    pos = VecHelper.toBlockPos(snapshot.get().getPosition());
                } else {
                    pos = ((net.minecraft.tileentity.TileEntity) te.get()).getPos();
                }
                net.minecraft.world.chunk.Chunk chunk = this.getMinecraftWorld().getChunkFromBlockCoords(pos);
                if (chunk != null) {
                    IMixinChunk spongeChunk = (IMixinChunk) chunk;

                    Optional<User> owner = spongeChunk.getBlockOwner(pos);
                    Optional<User> notifier = spongeChunk.getBlockNotifier(pos);
                    if (notifier.isPresent() && !cause.containsNamed(NamedCause.NOTIFIER)) {
                        cause = cause.with(NamedCause.notifier(notifier.get()));
                    }
                    if (owner.isPresent() && !cause.containsNamed(NamedCause.OWNER)) {
                        cause = cause.with(NamedCause.owner(owner.get()));
                    }
                }
            } else if (cause.first(Entity.class).isPresent()) {
                Entity entity = cause.first(Entity.class).get();
                if (entity instanceof EntityTameable) {
                    EntityTameable tameable = (EntityTameable) entity;
                    if (tameable.getOwner() != null && !cause.containsNamed(NamedCause.OWNER)) {
                        cause = cause.with(NamedCause.owner(tameable.getOwner()));
                    }
                } else {
                    Optional<User> owner = ((IMixinEntity) entity).getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR);
                    if (owner.isPresent() && !cause.contains(NamedCause.OWNER)) {
                        cause = cause.with(NamedCause.owner(owner.get()));
                    }
                }
            }
        }

        // Handle Block Captures
        handleBlockCaptures(cause);

        // Handle Player Toss
        if (player != null && packetIn instanceof C07PacketPlayerDigging) {
            C07PacketPlayerDigging digPacket = (C07PacketPlayerDigging) packetIn;
            if (digPacket.getStatus() == C07PacketPlayerDigging.Action.DROP_ITEM) {
                StaticMixinHelper.destructItemDrop = false;
            }
        }

        // Handle Player kill commands
        if (player != null && packetIn instanceof C01PacketChatMessage) {
            C01PacketChatMessage chatPacket = (C01PacketChatMessage) packetIn;
            if (chatPacket.getMessage().contains("kill")) {
                if (!cause.contains(player)) {
                    cause = cause.with(NamedCause.of("Player", player));
                }
                StaticMixinHelper.destructItemDrop = true;
            }
        }

        // Inventory Events
        if (player != null && player.getHealth() > 0 && StaticMixinHelper.lastOpenContainer != null) {
            if (packetIn instanceof C10PacketCreativeInventoryAction && !StaticMixinHelper.ignoreCreativeInventoryPacket) {
                SpongeCommonEventFactory.handleCreativeClickInventoryEvent(Cause.of(NamedCause.source(player)), player,
                    (C10PacketCreativeInventoryAction) packetIn);
            } else {
                SpongeCommonEventFactory.handleInteractInventoryOpenCloseEvent(Cause.of(NamedCause.source(player)), player, packetIn);
                if (packetIn instanceof C0EPacketClickWindow) {
                    SpongeCommonEventFactory.handleClickInteractInventoryEvent(Cause.of(NamedCause.source(player)), player,
                        (C0EPacketClickWindow) packetIn);
                }
            }
        }

        // Handle Entity captures
        if (this.capturedEntityItems.size() > 0) {
            if (StaticMixinHelper.dropCause != null) {
                cause = StaticMixinHelper.dropCause;
                StaticMixinHelper.destructItemDrop = true;
            }
            handleDroppedItems(cause);
        }
        if (this.capturedEntities.size() > 0) {
            handleEntitySpawns(cause);
        }

        StaticMixinHelper.dropCause = null;
        StaticMixinHelper.destructItemDrop = false;
        this.invalidTransactions.clear();
    }

    public void handleDroppedItems(Cause cause) {
        Iterator<Entity> iter = this.capturedEntityItems.iterator();
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
        DropItemEvent event = null;

        if (StaticMixinHelper.destructItemDrop) {
            if (!(cause.root() instanceof SpawnCause)) {
                // determine spawn cause
                Cause.Builder builder = null;
                if (cause.root() instanceof Entity) {
                    DamageSource damageSource = SpongeHooks.getEntityDamageSource((net.minecraft.entity.Entity) cause.root());
                    builder = Cause.source(EntitySpawnCause.builder().entity((Entity) cause.root()).type(SpawnTypes.DROPPED_ITEM).build());
                    if (damageSource != null) {
                        builder = builder.named(NamedCause.of("LastDamageSource", damageSource));
                    }
                } else {
                    builder = Cause.source(SpawnCause.builder().type(SpawnTypes.DROPPED_ITEM).build());
                }
    
                Cause spawnCause = builder.build();
                if (cause.root() == StaticMixinHelper.packetPlayer) {
                    cause = spawnCause.merge(Cause.of(NamedCause.owner(StaticMixinHelper.packetPlayer)));
                } else {
                    cause = spawnCause.merge(cause);
                }
            }
            event = SpongeEventFactory.createDropItemEventDestruct(cause, this.capturedEntityItems, entitySnapshots, this.getWorld());
        } else {
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
            sendItemChangeToPlayer(StaticMixinHelper.packetPlayer);
            this.capturedEntityItems.clear();
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

    public void handleBlockCaptures(Cause cause) {
        EntityPlayerMP player = StaticMixinHelper.packetPlayer;
        Packet<?> packetIn = StaticMixinHelper.processingPacket;

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
                    if (packetIn instanceof C08PacketPlayerBlockPlacement) {
                        captureType = CaptureType.PLACE;
                    } else if (packetIn instanceof C07PacketPlayerDigging) {
                        captureType = CaptureType.BREAK;
                    }
                    if (captureType != null) {
                        handlePostPlayerBlockEvent(captureType, changeBlockEvent.getTransactions());
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
                processList(listIterator);

                handlePostPlayerBlockEvent(captureType, blockEvent.getTransactions());

                // clear entity list and return to avoid spawning items
                this.capturedEntities.clear();
                this.capturedEntityItems.clear();
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

                        if (captureType == CaptureType.PLACE && player != null && packetIn instanceof C08PacketPlayerBlockPlacement) {
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
                cause = Cause.of(NamedCause.source(this));
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

    public boolean processSpawnEntity(Entity entity, Cause cause) {
        checkNotNull(entity, "Entity cannot be null!");
        checkNotNull(cause, "Cause cannot be null!");

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

        List<NamedCause> namedCauses = new ArrayList<>();
        for (Map.Entry<String, Object> entry : cause.getNamedCauses().entrySet()) {
            if (entry.getKey().equals(NamedCause.SOURCE)) {
                if (!(entry.getValue() instanceof SpawnCause)) {
                    namedCauses.add(NamedCause.source(SpawnCause.builder().type(SpawnTypes.CUSTOM).build()));
                } else {
                    namedCauses.add(NamedCause.source(entry.getValue()));
                }
            } else {
                namedCauses.add(NamedCause.of(entry.getKey(), entry.getValue()));
            }
        }
        cause = Cause.of(namedCauses);
        if (entityIn instanceof EntityPlayer) {
            flag = true;
        } else if (entityIn instanceof EntityLightningBolt) {
            ((IMixinEntityLightningBolt) entityIn).setCause(cause);
        }

        if (!flag && !this.getMinecraftWorld().isChunkLoaded(i, j, true)) {
            return false;
        } else {
            if (entityIn instanceof EntityPlayer) {
                EntityPlayer entityplayer = (EntityPlayer) entityIn;
                net.minecraft.world.World world = this.targetWorld;
                world.playerEntities.add(entityplayer);
                world.updateAllPlayersSleepingFlag();
            }

            if (this.getMinecraftWorld().isRemote || flag || this.spawningDeathDrops) {
                this.getMinecraftWorld().getChunkFromChunkCoords(i, j).addEntity(entityIn);
                this.getMinecraftWorld().loadedEntityList.add(entityIn);
                this.getMixinWorld().onSpongeEntityAdded(entityIn);
                return true;
            }

            if (!flag && this.processingCaptureCause) {
                if (this.currentTickBlock != null) {
                    BlockPos sourcePos = VecHelper.toBlockPos(this.currentTickBlock.getPosition());
                    Block targetBlock = getMinecraftWorld().getBlockState(entityIn.getPosition()).getBlock();
                    SpongeHooks
                        .tryToTrackBlockAndEntity(this.getMinecraftWorld(), this.currentTickBlock, entityIn, sourcePos, targetBlock, entityIn.getPosition(),
                            PlayerTracker.Type.NOTIFIER);
                }
                if (this.currentTickEntity != null) {
                    Optional<User> creator = ((IMixinEntity) this.currentTickEntity).getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR);
                    if (creator.isPresent()) { // transfer user to next entity. This occurs with falling blocks that change into items
                        ((IMixinEntity) entityIn).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, creator.get().getUniqueId());
                    }
                }
                if (entityIn instanceof EntityItem) {
                    this.capturedEntityItems.add((Item) entityIn);
                } else {
                    this.capturedEntities.add((Entity) entityIn);
                }
                return true;
            } else { // Custom

                if (entityIn instanceof EntityFishHook && ((EntityFishHook) entityIn).angler == null) {
                    // TODO MixinEntityFishHook.setShooter makes angler null
                    // sometimes, but that will cause NPE when ticking
                    return false;
                }

                EntityLivingBase specialCause = null;
                String causeName = "";
                // Special case for throwables
                if (!(entityIn instanceof EntityPlayer) && entityIn instanceof EntityThrowable) {
                    EntityThrowable throwable = (EntityThrowable) entityIn;
                    specialCause = throwable.getThrower();

                    if (specialCause != null) {
                        causeName = NamedCause.THROWER;
                        if (specialCause instanceof Player) {
                            Player player = (Player) specialCause;
                            ((IMixinEntity) entityIn).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, player.getUniqueId());
                        }
                    }
                }
                // Special case for TNT
                else if (!(entityIn instanceof EntityPlayer) && entityIn instanceof EntityTNTPrimed) {
                    EntityTNTPrimed tntEntity = (EntityTNTPrimed) entityIn;
                    specialCause = tntEntity.getTntPlacedBy();
                    causeName = NamedCause.IGNITER;

                    if (specialCause instanceof Player) {
                        Player player = (Player) specialCause;
                        ((IMixinEntity) entityIn).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, player.getUniqueId());
                    }
                }
                // Special case for Tameables
                else if (!(entityIn instanceof EntityPlayer) && entityIn instanceof EntityTameable) {
                    EntityTameable tameable = (EntityTameable) entityIn;
                    if (tameable.getOwner() != null) {
                        specialCause = tameable.getOwner();
                        causeName = NamedCause.OWNER;
                    }
                }

                if (specialCause != null && !cause.containsNamed(causeName)) {
                    cause = cause.with(NamedCause.of(causeName, specialCause));
                }

                org.spongepowered.api.event.Event event = null;
                ImmutableList.Builder<EntitySnapshot> entitySnapshotBuilder = new ImmutableList.Builder<>();
                entitySnapshotBuilder.add(((Entity) entityIn).createSnapshot());

                if (entityIn instanceof EntityItem) {
                    this.capturedEntityItems.add((Item) entityIn);
                    final List<NamedCause> dropCauses = new ArrayList<>();
                    for (Map.Entry<String, Object> entry : cause.getNamedCauses().entrySet()) {
                        if (entry.getKey().equals(NamedCause.SOURCE)) {
                            dropCauses.add(NamedCause.source(SpawnCause.builder().type(SpawnTypes.DROPPED_ITEM).build()));
                        } else {
                            dropCauses.add(NamedCause.of(entry.getKey(), entry.getValue()));
                        }
                    }
                    cause = Cause.of(namedCauses);
                    event = SpongeEventFactory.createDropItemEventCustom(cause, this.capturedEntityItems,
                            entitySnapshotBuilder.build(), this.getWorld());
                } else {
                    this.capturedEntities.add((Entity) entityIn);
                    final List<NamedCause> customCauses = new ArrayList<>();
                    for (Map.Entry<String, Object> entry : cause.getNamedCauses().entrySet()) {
                        if (entry.getKey().equals(NamedCause.SOURCE)) {
                            customCauses.add(NamedCause.source(SpawnCause.builder().type(SpawnTypes.CUSTOM).build()));
                        } else {
                            customCauses.add(NamedCause.of(entry.getKey(), entry.getValue()));
                        }
                    }
                    cause = Cause.of(customCauses);
                    event = SpongeEventFactory.createSpawnEntityEventCustom(cause, this.capturedEntities,
                            entitySnapshotBuilder.build(), this.getWorld());
                }
                if (!SpongeImpl.postEvent(event) && !entity.isRemoved()) {
                    if (entityIn instanceof EntityWeatherEffect) {
                        return addWeatherEffect(entityIn, cause);
                    }

                    this.getMinecraftWorld().getChunkFromChunkCoords(i, j).addEntity(entityIn);
                    this.getMinecraftWorld().loadedEntityList.add(entityIn);
                    this.getMixinWorld().onSpongeEntityAdded(entityIn);
                    if (entityIn instanceof EntityItem) {
                        this.capturedEntityItems.remove(entityIn);
                    } else {
                        this.capturedEntities.remove(entityIn);
                    }
                    return true;
                }

                return false;
            }
        }
    }

    public void randomTickBlock(Block block, BlockPos pos, IBlockState state, Random random) {
        this.processingCaptureCause = true;
        this.processingBlockRandomTicks = true;
        this.currentTickBlock = this.getMixinWorld().createSpongeBlockSnapshot(state, state.getBlock().getActualState(state, this.getMinecraftWorld(), pos), pos, 0);
        block.randomTick(this.getMinecraftWorld(), pos, state, random);
        this.handlePostTickCaptures(Cause.of(NamedCause.source(this.currentTickBlock)));
        this.currentTickBlock = null;
        this.processingCaptureCause = false;
        this.processingBlockRandomTicks = false;
    }

    public void updateTickBlock(Block block, BlockPos pos, IBlockState state, Random rand) {
        this.processingCaptureCause = true;
        this.currentTickBlock = this.getMixinWorld().createSpongeBlockSnapshot(state, state.getBlock().getActualState(state, this.getMinecraftWorld(), pos), pos, 0);
        block.updateTick(this.getMinecraftWorld(), pos, state, rand);
        this.handlePostTickCaptures(Cause.of(NamedCause.source(this.currentTickBlock)));
        this.currentTickBlock = null;
        this.processingCaptureCause = false;
    }

    public void notifyBlockOfStateChange(BlockPos notifyPos, final Block sourceBlock, BlockPos sourcePos) {
        if (!this.getMinecraftWorld().isRemote) {
            IBlockState iblockstate = this.getMinecraftWorld().getBlockState(notifyPos);
            if (iblockstate == Blocks.air.getDefaultState()) {
                iblockstate.getBlock().onNeighborBlockChange(this.getMinecraftWorld(), notifyPos, iblockstate, sourceBlock);
                return;
            }

            try {
                if (!this.getMinecraftWorld().isRemote) {
                    if (StaticMixinHelper.packetPlayer != null) {
                        IMixinChunk spongeChunk = (IMixinChunk) this.getMinecraftWorld().getChunkFromBlockCoords(notifyPos);
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
                            if (notifier.isPresent()) {
                                IMixinChunk spongeChunk = (IMixinChunk) this.getMinecraftWorld().getChunkFromBlockCoords(notifyPos);
                                spongeChunk.addTrackedBlockPosition(iblockstate.getBlock(), notifyPos, notifier.get(), PlayerTracker.Type.NOTIFIER);
                            } else if (owner.isPresent()) {
                                IMixinChunk spongeChunk = (IMixinChunk) this.getMinecraftWorld().getChunkFromBlockCoords(notifyPos);
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

    public void markAndNotifyBlockPost(List<Transaction<BlockSnapshot>> transactions, CaptureType type, Cause cause) {
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
                if (shouldChainCause(cause)) {
                    cause = cause.merge(Cause.of(NamedCause.source(this.currentTickBlock)));
                }
            }

            proxyBlockAccess.proceed();
            this.getMixinWorld().markAndNotifyNeighbors(pos, null, originalState, newState, updateFlag);

            // Handle any additional captures during notify
            // This is to ensure new captures do not leak into next tick with wrong cause
            if (this.getCapturedSpongeBlockSnapshots().size() > 0 && this.pluginCause == null) {
                this.handlePostTickCaptures(cause);
            }

            this.setCurrentTickBlock(currentTickingBlock);
        }
    }

    private boolean shouldChainCause(Cause cause) {
        return !this.isCapturingTerrainGen() && !this.isWorldSpawnerRunning() && !this.isChunkSpawnerRunning()
               && !this.isProcessingBlockRandomTicks() && !this.isCaptureCommand() && this.hasTickingBlock() && this.pluginCause == null
               && !cause.contains(this.getCurrentTickBlock().get()) && !(StaticMixinHelper.processingPacket instanceof C03PacketPlayer);

    }
}
