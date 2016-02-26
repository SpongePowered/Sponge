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

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.effect.EntityWeatherEffect;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.client.C10PacketCreativeInventoryAction;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.action.LightningEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.phase.BlockPhase;
import org.spongepowered.common.event.tracking.phase.TrackingPhase;
import org.spongepowered.common.event.tracking.phase.WorldPhase;
import org.spongepowered.common.interfaces.IMixinMinecraftServer;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.entity.IMixinEntityLightningBolt;
import org.spongepowered.common.interfaces.entity.IMixinEntityLivingBase;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.util.StaticMixinHelper;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.CaptureType;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

/**
 * Contains all the logic that needs to move into {@link TrackingPhase}s
 * and potentially {@link IPhaseState}s. Much of the logic is simply overlap
 * that could easily be dealt with from the identity of the state
 * and making assumptions based on said state.
 */
public class MoveToPhases {

    static void handleInventoryEvents(EntityPlayerMP player, Packet<?> packetIn, Container container, IPhaseState phaseState, PhaseContext phaseContext) {
        if (player != null && player.getHealth() > 0 && container != null) {
            boolean ignoringCreative = phaseContext.firstNamed(TrackingHelper.IGNORING_CREATIVE, Boolean.class).orElse(false);
            if (packetIn instanceof C10PacketCreativeInventoryAction && !ignoringCreative) {
                SpongeCommonEventFactory.handleCreativeClickInventoryEvent(Cause.of(NamedCause.source(player)), player,
                        (C10PacketCreativeInventoryAction) packetIn, phaseState, phaseContext);
            } else {
                SpongeCommonEventFactory.handleInteractInventoryOpenCloseEvent(Cause.of(NamedCause.source(player)), player, packetIn, phaseState,
                        phaseContext);
                if (packetIn instanceof C0EPacketClickWindow) {
                    SpongeCommonEventFactory.handleClickInteractInventoryEvent(Cause.of(NamedCause.source(player)), player,
                            (C0EPacketClickWindow) packetIn, phaseState, phaseContext);
                }
            }
        }
    }

    static void handlePostPlayerBlockEvent(World minecraftWorld, @Nullable CaptureType captureType, List<Transaction<BlockSnapshot>> transactions,
            IPhaseState phaseState, PhaseContext context) {
        final Optional<EntityPlayerMP> entityPlayerMP = context.firstNamed(TrackingHelper.PACKET_PLAYER, EntityPlayerMP.class);
        if (entityPlayerMP.isPresent()) {
            return;
        }

        final EntityPlayerMP playerMP = entityPlayerMP.get();
        if (captureType == CaptureType.BREAK) {
            // Let the client know the blocks still exist
            for (Transaction<BlockSnapshot> transaction : transactions) {
                BlockSnapshot snapshot = transaction.getOriginal();
                BlockPos pos = VecHelper.toBlockPos(snapshot.getPosition());
                playerMP.playerNetServerHandler.sendPacket(new S23PacketBlockChange(minecraftWorld, pos));

                // Update any tile entity data for this block
                net.minecraft.tileentity.TileEntity tileentity = minecraftWorld.getTileEntity(pos);
                if (tileentity != null) {
                    Packet<?> pkt = tileentity.getDescriptionPacket();
                    if (pkt != null) {
                        playerMP.playerNetServerHandler.sendPacket(pkt);
                    }
                }
            }
        } else if (captureType == CaptureType.PLACE) {
            TrackingHelper.sendItemChangeToPlayer(playerMP, context);
        }
    }

    static void preProcessItemDrops(Cause cause, List<Transaction<BlockSnapshot>> invalidTransactions, Iterator<Entity> iter,
            ImmutableList.Builder<EntitySnapshot> builder) {
        while (iter.hasNext()) {
            Entity currentEntity = iter.next();
            if (TrackingHelper.doInvalidTransactionsExist(invalidTransactions, iter, currentEntity)) {
                continue;
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
            builder.add(currentEntity.createSnapshot());
        }
    }

    static boolean addWeatherEffect(final net.minecraft.entity.Entity entity, World minecraftWorld) {
        if (entity instanceof EntityLightningBolt) {
            LightningEvent.Pre event = SpongeEventFactory.createLightningEventPre(((IMixinEntityLightningBolt) entity).getCause());
            SpongeImpl.postEvent(event);
            if (!event.isCancelled()) {
                return minecraftWorld.addWeatherEffect(entity);
            }
        } else {
            return minecraftWorld.addWeatherEffect(entity);
        }
        return false;
    }

    static boolean completeEntitySpawn(Entity entity, Cause cause, CauseTracker causeTracker, int chunkX, int chunkZ, IPhaseState phaseState,
        PhaseContext phaseContext) {
        net.minecraft.entity.Entity entityIn = (net.minecraft.entity.Entity) entity;


        // handle actual capturing
        if (phaseState.isBusy()) {
            Optional<BlockSnapshot> currentTickingBlock = phaseContext.firstNamed(NamedCause.SOURCE, BlockSnapshot.class);
            Optional<Entity> currentTickEntity = phaseContext.firstNamed(NamedCause.SOURCE, Entity.class);
            if (currentTickingBlock.isPresent()) {
                BlockPos sourcePos = VecHelper.toBlockPos(currentTickingBlock.get().getPosition());
                Block targetBlock = causeTracker.getMinecraftWorld().getBlockState(entityIn.getPosition()).getBlock();
                SpongeHooks.tryToTrackBlockAndEntity(causeTracker.getMinecraftWorld(), currentTickingBlock.get(), entityIn, sourcePos,
                    targetBlock, entityIn.getPosition(), PlayerTracker.Type.NOTIFIER);
            }
            if (currentTickEntity.isPresent()) {
                Optional<User> creator = ((IMixinEntity) currentTickEntity.get()).getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR);
                if (creator.isPresent()) { // transfer user to next entity. This occurs with falling blocks that change into items
                    ((IMixinEntity) entityIn).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, creator.get().getUniqueId());
                }
            }
            if (entityIn instanceof EntityItem) {
                causeTracker.getCapturedEntityItems().add(entity);
            } else {
                causeTracker.getCapturedEntities().add(entity);
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
            if (entityIn instanceof EntityThrowable) {
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
            else if (entityIn instanceof EntityTNTPrimed) {
                EntityTNTPrimed tntEntity = (EntityTNTPrimed) entityIn;
                specialCause = tntEntity.getTntPlacedBy();
                causeName = NamedCause.IGNITER;

                if (specialCause instanceof Player) {
                    Player player = (Player) specialCause;
                    ((IMixinEntity) entityIn).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, player.getUniqueId());
                }
            }
            // Special case for Tameables
            else if (entityIn instanceof EntityTameable) {
                EntityTameable tameable = (EntityTameable) entityIn;
                if (tameable.getOwner() != null) {
                    specialCause = tameable.getOwner();
                    causeName = NamedCause.OWNER;
                }
            }

            if (specialCause != null && !cause.containsNamed(causeName)) {
                cause = cause.with(NamedCause.of(causeName, specialCause));
            }

            org.spongepowered.api.event.Event event;
            ImmutableList.Builder<EntitySnapshot> entitySnapshotBuilder = new ImmutableList.Builder<>();
            entitySnapshotBuilder.add(((Entity) entityIn).createSnapshot());

            if (entityIn instanceof EntityItem) {
                causeTracker.getCapturedEntityItems().add(entity);
                event = SpongeEventFactory.createDropItemEventCustom(cause, causeTracker.getCapturedEntityItems(),
                        entitySnapshotBuilder.build(), causeTracker.getWorld());
            } else {
                causeTracker.getCapturedEntities().add(entity);
                event = SpongeEventFactory.createSpawnEntityEventCustom(cause, causeTracker.getCapturedEntities(),
                        entitySnapshotBuilder.build(), causeTracker.getWorld());
            }
            if (!SpongeImpl.postEvent(event) && !entity.isRemoved()) {
                if (entityIn instanceof EntityWeatherEffect) {
                    return addWeatherEffect(entityIn, causeTracker.getMinecraftWorld());
                }

                causeTracker.getMinecraftWorld().getChunkFromChunkCoords(chunkX, chunkZ).addEntity(entityIn);
                causeTracker.getMinecraftWorld().loadedEntityList.add(entityIn);
                causeTracker.getMixinWorld().onSpongeEntityAdded(entityIn);
                if (entityIn instanceof EntityItem) {
                    causeTracker.getCapturedEntityItems().remove(entity);
                } else {
                    causeTracker.getCapturedEntities().remove(entity);
                }
                return true;
            }

            return false;
        }
    }
}
