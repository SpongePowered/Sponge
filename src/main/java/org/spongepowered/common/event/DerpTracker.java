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
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.effect.EntityWeatherEffect;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldServer;
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
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.entity.IMixinEntityLightningBolt;
import org.spongepowered.common.interfaces.entity.IMixinEntityLivingBase;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.BlockChange;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

public final class DerpTracker {

    private final WorldServer targetWorld;

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

    public DerpTracker(WorldServer targetWorld) {
        this.targetWorld = targetWorld;
    }

    public World getWorld() {
        return (World) this.targetWorld;
    }

    public WorldServer getMinecraftWorld() {
        return this.targetWorld;
    }

    public IMixinWorldServer getMixinWorld() {
        return (IMixinWorldServer) this.targetWorld;
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
                if (entry.getKey().equals(NamedCause.SOURCE)) {
                    namedCauses.add(NamedCause.source(SpawnCause.builder().type(SpawnTypes.CUSTOM).build()));
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
        } else if (this.capturedEntities.size() == 0 && this.capturedEntityItems.size() == 0 && this.capturedSpongeBlockSnapshots.size() == 0) {
//                   && StaticMixinHelper.packetPlayer == null) {
            return; // nothing was captured, return
        }

//        EntityPlayerMP player = StaticMixinHelper.packetPlayer;
//        Packet<?> packetIn = StaticMixinHelper.processingPacket;

//        // Attempt to find a Player cause if we do not have one
//        if (!cause.first(User.class).isPresent() && !(this.capturedSpongeBlockSnapshots.size() > 0
//                                                      && ((SpongeBlockSnapshot) this.capturedSpongeBlockSnapshots.get(0)).captureType
//                                                         == CaptureType.DECAY)) {
//            if ((cause.first(BlockSnapshot.class).isPresent() || cause.first(TileEntity.class).isPresent())) {
//                // Check for player at pos of first transaction
//                Optional<BlockSnapshot> snapshot = cause.first(BlockSnapshot.class);
//                Optional<TileEntity> te = cause.first(TileEntity.class);
//                BlockPos pos = null;
//                if (snapshot.isPresent()) {
//                    pos = VecHelper.toBlockPos(snapshot.get().getPosition());
//                } else {
//                    pos = ((net.minecraft.tileentity.TileEntity) te.get()).getPos();
//                }
//                net.minecraft.world.chunk.Chunk chunk = this.getMinecraftWorld().getChunkFromBlockCoords(pos);
//                if (chunk != null) {
//                    IMixinChunk spongeChunk = (IMixinChunk) chunk;
//
//                    Optional<User> owner = spongeChunk.getBlockOwner(pos);
//                    Optional<User> notifier = spongeChunk.getBlockNotifier(pos);
//                    if (notifier.isPresent() && !cause.containsNamed(NamedCause.NOTIFIER)) {
//                        cause = cause.with(NamedCause.notifier(notifier.get()));
//                    }
//                    if (owner.isPresent() && !cause.containsNamed(NamedCause.OWNER)) {
//                        cause = cause.with(NamedCause.owner(owner.get()));
//                    }
//                }
//            } else if (cause.first(Entity.class).isPresent()) {
//                Entity entity = cause.first(Entity.class).get();
//                if (entity instanceof EntityTameable) {
//                    EntityTameable tameable = (EntityTameable) entity;
//                    if (tameable.getOwner() != null && !cause.containsNamed(NamedCause.OWNER)) {
//                        cause = cause.with(NamedCause.owner(tameable.getOwner()));
//                    }
//                } else {
//                    Optional<User> owner = ((IMixinEntity) entity).getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR);
//                    if (owner.isPresent() && !cause.contains(NamedCause.OWNER)) {
//                        cause = cause.with(NamedCause.owner(owner.get()));
//                    }
//                }
//            }
//        }

        // Handle Block Captures
        handleBlockCaptures(cause);

//        // Handle Player Toss
//        if (player != null && packetIn instanceof C07PacketPlayerDigging) {
//            C07PacketPlayerDigging digPacket = (C07PacketPlayerDigging) packetIn;
//            if (digPacket.getStatus() == C07PacketPlayerDigging.Action.DROP_ITEM) {
////                StaticMixinHelper.destructItemDrop = false;
//            }
//        }
//
//        // Handle Player kill commands
//        if (player != null && packetIn instanceof C01PacketChatMessage) {
//            C01PacketChatMessage chatPacket = (C01PacketChatMessage) packetIn;
//            if (chatPacket.getMessage().contains("kill")) {
//                if (!cause.contains(player)) {
//                    cause = cause.with(NamedCause.of("Player", player));
//                }
////                StaticMixinHelper.destructItemDrop = true;
//            }
//        }
//
//        // Handle Player Entity destruct
//        if (player != null && packetIn instanceof C02PacketUseEntity) {
//            C02PacketUseEntity packet = (C02PacketUseEntity) packetIn;
//            if (packet.getAction() == C02PacketUseEntity.Action.ATTACK) {
//                net.minecraft.entity.Entity entity = packet.getEntityFromWorld(this.getMinecraftWorld());
//                if (entity != null && entity.isDead && !(entity instanceof EntityLivingBase)) {
//                    Player spongePlayer = (Player) player;
//                    MessageChannel originalChannel = spongePlayer.getMessageChannel();
//
//                    DestructEntityEvent event = SpongeEventFactory.createDestructEntityEvent(
//                        cause, originalChannel, Optional.of(originalChannel), new MessageEvent.MessageFormatter(), (Entity) entity, true
//                    );
//                    SpongeImpl.getGame().getEventManager().post(event);
//                    if (!event.isMessageCancelled()) {
//                        event.getChannel().ifPresent(channel -> channel.send(entity, event.getMessage()));
//                    }
//
////                    StaticMixinHelper.lastDestroyedEntityId = entity.getEntityId();
//                }
//            }
//        }

        // Inventory Events
//        if (player != null && player.getHealth() > 0 && StaticMixinHelper.lastOpenContainer != null) {
//            if (packetIn instanceof C10PacketCreativeInventoryAction && !StaticMixinHelper.ignoreCreativeInventoryPacket) {
//                SpongeCommonEventFactory.handleCreativeClickInventoryEvent(Cause.of(NamedCause.source(player)), player,
//                    (C10PacketCreativeInventoryAction) packetIn);
//            } else {
//                SpongeCommonEventFactory.handleInteractInventoryOpenCloseEvent(Cause.of(NamedCause.source(player)), player, packetIn);
//                if (packetIn instanceof C0EPacketClickWindow) {
//                    SpongeCommonEventFactory.handleClickInteractInventoryEvent(Cause.of(NamedCause.source(player)), player,
//                        (C0EPacketClickWindow) packetIn);
//                }
//            }
//        }

        // Handle Entity captures
        if (this.capturedEntityItems.size() > 0) {
//            if (StaticMixinHelper.dropCause != null) {
//                cause = StaticMixinHelper.dropCause;
////                StaticMixinHelper.destructItemDrop = true;
//            }
            handleDroppedItems(cause);
        }
        if (this.capturedEntities.size() > 0) {
            handleEntitySpawns(cause);
        }

//        StaticMixinHelper.dropCause = null;
//        StaticMixinHelper.destructItemDrop = false;
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
                    IMixinEntityLivingBase spongeLivingEntity = (IMixinEntityLivingBase) spongeEntity;
                    DamageSource lastDamageSource = spongeLivingEntity.getLastDamageSource();
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
        DropItemEvent event;

        if ( false) { //StaticMixinHelper.destructItemDrop) {
            final Cause.Builder builder = Cause.source(SpawnCause.builder().type(SpawnTypes.DROPPED_ITEM).build());
            for (Map.Entry<String, Object> entry : cause.getNamedCauses().entrySet()) {
                builder.suggestNamed(entry.getKey(), entry.getValue());
            }
            cause = builder.build();
            event = SpongeEventFactory.createDropItemEventDestruct(cause, this.capturedEntityItems, entitySnapshots, this.getWorld());
        } else {
            final Cause.Builder builder = Cause.source(SpawnCause.builder().type(SpawnTypes.DROPPED_ITEM).build());
            for (Map.Entry<String, Object> entry : cause.getNamedCauses().entrySet()) {
                builder.suggestNamed(entry.getKey(), entry.getValue());
            }
            cause = builder.build();
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
//            if (cause.root() == StaticMixinHelper.packetPlayer) {
////                sendItemChangeToPlayer(StaticMixinHelper.packetPlayer);
//            }
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
//        EntityPlayerMP player = StaticMixinHelper.packetPlayer;
//        Packet<?> packetIn = StaticMixinHelper.processingPacket;

        ImmutableList<Transaction<BlockSnapshot>> blockBreakTransactions;
        ImmutableList<Transaction<BlockSnapshot>> blockModifyTransactions;
        ImmutableList<Transaction<BlockSnapshot>> blockPlaceTransactions;
        ImmutableList<Transaction<BlockSnapshot>> blockDecayTransactions;
        ImmutableList<Transaction<BlockSnapshot>> blockMultiTransactions;
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
//        while (iterator.hasNext()) {
//            SpongeBlockSnapshot blockSnapshot = (SpongeBlockSnapshot) iterator.next(); // This is the snapshot of the ORIGINAL block
//            CaptureType captureType = blockSnapshot.captureType;
//            BlockPos pos = VecHelper.toBlockPos(blockSnapshot.getPosition());
//            IBlockState currentState = this.getMinecraftWorld().getBlockState(pos); // Creates the snapshot for the replacement block
//            Transaction<BlockSnapshot> transaction = new Transaction<>(blockSnapshot, this.getMixinWorld().createSpongeBlockSnapshot(currentState, currentState.getBlock()
//                .getActualState(currentState, this.getMinecraftWorld(), pos), pos, 0));
//            if (captureType == CaptureType.BREAK) {
//                breakBuilder.add(transaction);
//            } else if (captureType == CaptureType.DECAY) {
//                decayBuilder.add(transaction);
//            } else if (captureType == CaptureType.PLACE) {
//                placeBuilder.add(transaction);
//            } else if (captureType == CaptureType.MODIFY) {
//                modifyBuilder.add(transaction);
//            }
//            multiBuilder.add(transaction);
//            iterator.remove();
//        }

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

//                if (player != null) {
//                    CaptureType captureType = null;
//                    if (packetIn instanceof C08PacketPlayerBlockPlacement) {
//                        captureType = CaptureType.PLACE;
//                    } else if (packetIn instanceof C07PacketPlayerDigging) {
//                        captureType = CaptureType.BREAK;
//                    }
//                    if (captureType != null) {
//                        handlePostPlayerBlockEvent(captureType, changeBlockEvent.getTransactions());
//                    }
//                }

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
            BlockChange blockChange = null;
            if (blockEvent instanceof ChangeBlockEvent.Break) {
                blockChange = BlockChange.BREAK;
            } else if (blockEvent instanceof ChangeBlockEvent.Decay) {
                blockChange = BlockChange.DECAY;
            } else if (blockEvent instanceof ChangeBlockEvent.Modify) {
                blockChange = BlockChange.MODIFY;
            } else if (blockEvent instanceof ChangeBlockEvent.Place) {
                blockChange = BlockChange.PLACE;
            }

            C08PacketPlayerBlockPlacement packet = null;

//            if (packetIn instanceof C08PacketPlayerBlockPlacement) {
//                packet = (C08PacketPlayerBlockPlacement) packetIn;
//            }

            if (blockEvent.isCancelled()) {
                // Restore original blocks
                ListIterator<Transaction<BlockSnapshot>>
                    listIterator =
                    blockEvent.getTransactions().listIterator(blockEvent.getTransactions().size());
                processList(listIterator);

                handlePostPlayerBlockEvent(blockChange, blockEvent.getTransactions());

                // clear entity list and return to avoid spawning items
                this.capturedEntities.clear();
                this.capturedEntityItems.clear();
                return;
            } else {
                for (Transaction<BlockSnapshot> transaction : blockEvent.getTransactions()) {
                    if (!transaction.isValid()) {
                        this.invalidTransactions.add(transaction);
                    } else {
                        if (blockChange == BlockChange.BREAK && cause.first(User.class).isPresent()) {
                            BlockPos pos = VecHelper.toBlockPos(transaction.getOriginal().getPosition());
                            for (EntityHanging hanging : EntityUtil.findHangingEntities(this.getMinecraftWorld(), pos)) {
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

//                        if (captureType == CaptureType.PLACE && player != null && packetIn instanceof C08PacketPlayerBlockPlacement) {
//                            BlockPos pos = VecHelper.toBlockPos(transaction.getFinal().getPosition());
//                            IMixinChunk spongeChunk = (IMixinChunk) this.getMinecraftWorld().getChunkFromBlockCoords(pos);
//                            spongeChunk.addTrackedBlockPosition((net.minecraft.block.Block) transaction.getFinal().getState().getType(), pos,
//                                (User) player, PlayerTracker.Type.OWNER);
//                            spongeChunk.addTrackedBlockPosition((net.minecraft.block.Block) transaction.getFinal().getState().getType(), pos,
//                                (User) player, PlayerTracker.Type.NOTIFIER);
//                        }
                    }
                }

                if (this.invalidTransactions.size() > 0) {
                    for (Transaction<BlockSnapshot> transaction : Lists.reverse(this.invalidTransactions)) {
                        this.restoringBlocks = true;
                        transaction.getOriginal().restore(true, false);
                        this.restoringBlocks = false;
                    }
                    handlePostPlayerBlockEvent(blockChange, this.invalidTransactions);
                }

                if (this.capturedEntityItems.size() > 0 && blockEvents.get(0) == breakEvent) {
//                    StaticMixinHelper.destructItemDrop = true;
                }

//                this.markAndNotifyBlockPost(blockEvent.getTransactions(), captureType, cause);

//                if (captureType == CaptureType.PLACE && player != null && packet != null && packet.getStack() != null) {
//                    player.addStat(StatList.objectUseStats[net.minecraft.item.Item.getIdFromItem(packet.getStack().getItem())], 1);
//                }
            }
        }
    }

    private void handlePostPlayerBlockEvent(BlockChange blockChange, List<Transaction<BlockSnapshot>> transactions) {
//        if (StaticMixinHelper.packetPlayer == null) {
//            return;
//        }

        if (blockChange == BlockChange.BREAK) {
            // Let the client know the blocks still exist
            for (Transaction<BlockSnapshot> transaction : transactions) {
                BlockSnapshot snapshot = transaction.getOriginal();
                BlockPos pos = VecHelper.toBlockPos(snapshot.getPosition());
//                StaticMixinHelper.packetPlayer.playerNetServerHandler.sendPacket(new S23PacketBlockChange(this.getMinecraftWorld(), pos));

                // Update any tile entity data for this block
                net.minecraft.tileentity.TileEntity tileentity = this.getMinecraftWorld().getTileEntity(pos);
                if (tileentity != null) {
                    Packet<?> pkt = tileentity.getDescriptionPacket();
                    if (pkt != null) {
//                        StaticMixinHelper.packetPlayer.playerNetServerHandler.sendPacket(pkt);
                    }
                }
            }
        } else if (blockChange == BlockChange.PLACE) {
//            sendItemChangeToPlayer(StaticMixinHelper.packetPlayer);
        }
    }

    private void processList(ListIterator<Transaction<BlockSnapshot>> listIterator) {
        while (listIterator.hasPrevious()) {
            Transaction<BlockSnapshot> transaction = listIterator.previous();
            this.restoringBlocks = true;
            transaction.getOriginal().restore(true, false);
            this.restoringBlocks = false;
        }
    }






    private boolean shouldChainCause(Cause cause) {
        return !this.isCapturingTerrainGen() && !this.isWorldSpawnerRunning() && !this.isChunkSpawnerRunning()
               && !this.isProcessingBlockRandomTicks() && !this.isCaptureCommand() && this.hasTickingBlock() && this.pluginCause == null;
//               && !cause.contains(this.getCurrentTickBlock().get()) && !(StaticMixinHelper.processingPacket instanceof C03PacketPlayer);

    }
}