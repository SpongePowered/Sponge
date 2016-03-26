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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Items;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.CombatEntry;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.living.Ageable;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.BlockSpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.event.tracking.phase.TrackingPhase;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.entity.IMixinEntityLivingBase;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;
import org.spongepowered.common.util.StaticMixinHelper;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.CaptureType;
import org.spongepowered.common.world.gen.InternalPopulatorTypes;

import java.util.ArrayList;
import java.util.Iterator;
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

    // I'm guessing this is going to be put into PacketPhase for player block changes.
    static void handlePostPlayerBlockEvent(World minecraftWorld, @Nullable CaptureType captureType, List<Transaction<BlockSnapshot>> transactions,
            IPhaseState phaseState, PhaseContext context) {
        final Optional<EntityPlayerMP> entityPlayerMP = context.firstNamed(TrackingUtil.PACKET_PLAYER, EntityPlayerMP.class);
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
            TrackingUtil.sendItemChangeToPlayer(playerMP, context);
        }
    }

    // Don't know where this was handled previously, but it feels like it just handles death drops.
    static void preProcessItemDrops(Cause cause, List<Transaction<BlockSnapshot>> invalidTransactions, Iterator<Entity> iter,
            ImmutableList.Builder<EntitySnapshot> builder) {
        while (iter.hasNext()) {
            Entity currentEntity = iter.next();
            if (TrackingUtil.doInvalidTransactionsExist(invalidTransactions, iter, currentEntity)) {
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

    // Todo this shouldn't be done like this, but rather should be considered for entering possible phase contexts.
    // Ultimately, this is going to be delegated to the current phase to generate a cause with appropriate handling.
    public static boolean handleVanillaSpawnEntity(World nmsWorld, net.minecraft.entity.Entity nmsEntity) {
        org.spongepowered.api.world.World world = (org.spongepowered.api.world.World) nmsWorld;
        Entity entity = (Entity) nmsEntity;
        List<NamedCause> list = new ArrayList<>();
        final CauseTracker causeTracker = ((IMixinWorldServer) nmsWorld).getCauseTracker();
        final PhaseData data = causeTracker.getPhases().peek();
        final IPhaseState state = data.getState();
        final PhaseContext context = data.getContext();

        if (nmsWorld.isRemote || nmsEntity instanceof EntityPlayer) {
            return causeTracker.processSpawnEntity(entity, Cause.source(InternalSpawnTypes.FORCED_SPAWN).build());
        }
        PopulatorType type = context.firstNamed(TrackingUtil.CAPTURED_POPULATOR, PopulatorType.class).orElse(null);
        if (type != null) {
            if (InternalPopulatorTypes.ANIMAL.equals(type)) {
                list.add(NamedCause.source(InternalSpawnTypes.WORLD_SPAWNER_CAUSE));
                list.add(NamedCause.of("AnimalSpawner", type));
            } else {
                list.add(NamedCause.source(InternalSpawnTypes.STRUCTURE_SPAWNING));
                list.add(NamedCause.of("Structure", type));
            }
        } else {
            final Optional<Entity> currentTickEntity = context.firstNamed(NamedCause.SOURCE, Entity.class);
            final Optional<BlockSnapshot> currentTickBlock = context.firstNamed(NamedCause.SOURCE, BlockSnapshot.class);
            final Optional<TileEntity> currentTickTileEntity = context.firstNamed(NamedCause.SOURCE, TileEntity.class);
            if (StaticMixinHelper.dispenserDispensing) {
                if (currentTickBlock.isPresent()) {
                    BlockSpawnCause blockSpawnCause = BlockSpawnCause.builder()
                            .block(currentTickBlock.get())
                            .type(InternalSpawnTypes.DISPENSE)
                            .build();
                    list.add(NamedCause.source(blockSpawnCause));
                } else if (currentTickTileEntity.isPresent()) {
                    BlockSpawnCause blockSpawnCause = BlockSpawnCause.builder()
                            .block(currentTickTileEntity.get().getLocation().createSnapshot())
                            .type(InternalSpawnTypes.DISPENSE)
                            .build();
                    list.add(NamedCause.source(blockSpawnCause));
                } else if (currentTickEntity.isPresent()) {
                    if  (currentTickEntity.get() == entity) {
                        SpawnCause cause = InternalSpawnTypes.UNKNOWN_DISPENSE_SPAWN_CAUSE;
                        list.add(NamedCause.source(cause));
                    } else {
                        EntitySpawnCause cause = EntitySpawnCause.builder()
                                .entity(currentTickEntity.get())
                                .type(InternalSpawnTypes.DISPENSE)
                                .build();
                        list.add(NamedCause.source(cause));
                    }
                }
            } else if (nmsEntity instanceof EntityItem) {
                if (type != null) {
                    // Just default to the structures placing it.
                    list.add(NamedCause.source(InternalSpawnTypes.STRUCTURE_SPAWNING));
                    return causeTracker.processSpawnEntity(entity, Cause.of(list));
                }
                if (currentTickBlock.isPresent()) {
                    BlockSpawnCause blockSpawnCause = BlockSpawnCause.builder()
                            .block(currentTickBlock.get())
                            .type(InternalSpawnTypes.BLOCK_SPAWNING)
                            .build();
                    list.add(NamedCause.source(blockSpawnCause));
                } else if (currentTickTileEntity.isPresent()) {
                    BlockSpawnCause blockSpawnCause = BlockSpawnCause.builder()
                            .block(currentTickTileEntity.get().getLocation().createSnapshot())
                            .type(InternalSpawnTypes.BLOCK_SPAWNING)
                            .build();
                    list.add(NamedCause.source(blockSpawnCause));
                } else if (StaticMixinHelper.dropCause != null) {
                    for (Map.Entry<String, Object> entry : StaticMixinHelper.dropCause.getNamedCauses().entrySet()) {
                        list.add(NamedCause.of(entry.getKey(), entry.getValue()));
                    }
                } else if (currentTickEntity.isPresent()) {
                    if  (currentTickEntity.get() == entity) {
                        SpawnCause cause = SpawnCause.builder()
                                .type(InternalSpawnTypes.CUSTOM)
                                .build();
                        list.add(NamedCause.source(cause));
                    } else {
                        EntitySpawnCause cause = EntitySpawnCause.builder()
                                .entity(currentTickEntity.get())
                                .type(InternalSpawnTypes.PASSIVE)
                                .build();
                        list.add(NamedCause.source(cause));
                    }
                }
            } else if (nmsEntity instanceof EntityXPOrb) {
                // This is almost always ALWAYS guaranteed to be experience, otherwise, someone
                // can open a ticket to correct us with proof otherwise.
                if (currentTickEntity.isPresent()) {
                    Entity currentEntity = currentTickEntity.get();
                    EntitySpawnCause spawnCause = EntitySpawnCause.builder()
                            .entity(currentEntity)
                            .type(InternalSpawnTypes.EXPERIENCE)
                            .build();
                    list.add(NamedCause.source(spawnCause));
                    if (MoveToPhases.isEntityDead(currentEntity)) {
                        if (currentEntity instanceof EntityLivingBase) {
                            CombatEntry entry = ((EntityLivingBase) currentEntity).getCombatTracker().func_94544_f();
                            if (entry != null) {
                                if (entry.damageSrc != null) {
                                    list.add(NamedCause.of("LastDamageSource", entry.damageSrc));
                                }
                            }
                        }
                    }
                } else if (currentTickBlock.isPresent()) {
                    BlockSpawnCause spawnCause = BlockSpawnCause.builder()
                            .block(currentTickBlock.get())
                            .type(InternalSpawnTypes.EXPERIENCE)
                            .build();
                    list.add(NamedCause.source(spawnCause));
                } else if (currentTickTileEntity.isPresent()) {
                    SpawnCause spawnCause = BlockSpawnCause.builder()
                            .block(currentTickTileEntity.get().getLocation().createSnapshot())
                            .type(InternalSpawnTypes.EXPERIENCE)
                            .build();
                    list.add(NamedCause.source(spawnCause));
                } else {
                    SpawnCause spawnCause = SpawnCause.builder()
                            .type(InternalSpawnTypes.EXPERIENCE)
                            .build();
                    list.add(NamedCause.source(spawnCause));
                }
            } else {
                final Optional<ItemStack> usedItem = context.firstNamed(TrackingUtil.ITEM_USED, ItemStack.class);
                if (usedItem.isPresent()) {
                    SpawnCause cause;
                    final EntityPlayerMP packetPlayer = context.firstNamed(TrackingUtil.PACKET_PLAYER, EntityPlayerMP.class).get();
                    if (entity instanceof Projectile || entity instanceof EntityThrowable) {
                        cause = EntitySpawnCause.builder()
                                .entity(((Entity) packetPlayer))
                                .type(InternalSpawnTypes.PROJECTILE)
                                .build();
                    } else if (usedItem.get().getItem() == Items.spawn_egg) {
                        cause = EntitySpawnCause.builder()
                                .entity((Entity) packetPlayer)
                                .type(InternalSpawnTypes.SPAWN_EGG)
                                .build();
                    } else {
                        cause = EntitySpawnCause.builder()
                                .entity((Entity) packetPlayer)
                                .type(InternalSpawnTypes.PLACEMENT)
                                .build();
                    }
                    list.add(NamedCause.source(cause));
                    list.add(NamedCause.of("UsedItem", usedItem.get().createSnapshot()));
                    list.add(NamedCause.owner(packetPlayer));
                } else if (currentTickBlock.isPresent()) { // We've exhausted our possibilities, now we just associate blindly
                    BlockSpawnCause cause = BlockSpawnCause.builder().block(currentTickBlock.get())
                            .type(InternalSpawnTypes.BLOCK_SPAWNING)
                            .build();
                    list.add(NamedCause.source(cause));
                } else if (currentTickEntity.isPresent()) {
                    Entity tickingEntity = currentTickEntity.get();
                    if (tickingEntity instanceof Ageable && tickingEntity.getClass() == entity.getClass()) { // We should assume breeding
                        EntitySpawnCause spawnCause = EntitySpawnCause.builder().entity(tickingEntity).type(InternalSpawnTypes.BREEDING).build();
                        list.add(NamedCause.source(spawnCause));
                        if (tickingEntity instanceof EntityAnimal) {
                            if (((EntityAnimal) tickingEntity).getPlayerInLove() != null) {
                                list.add(NamedCause.of("Player", ((EntityAnimal) tickingEntity).getPlayerInLove()));
                            }
                        }
                    }
                    EntitySpawnCause cause = EntitySpawnCause.builder().entity(currentTickEntity.get()).type(InternalSpawnTypes.CUSTOM).build();
                    list.add(NamedCause.source(cause));
                } else {
                    list.add(NamedCause.source(SpawnCause.builder().type(InternalSpawnTypes.CUSTOM).build()));
                }
            }
        }
        if (list.isEmpty()) {
            list.add(NamedCause.source(SpawnCause.builder().type(InternalSpawnTypes.CUSTOM).build()));
        }
        return causeTracker.processSpawnEntity(entity, Cause.of(list));
    }

    private static boolean isEntityDead(Entity entity) {
        return isEntityDead((net.minecraft.entity.Entity) entity);
    }

    private static boolean isEntityDead(net.minecraft.entity.Entity entity) {
        if (entity instanceof EntityLivingBase) {
            EntityLivingBase base = (EntityLivingBase) entity;
            return base.getHealth() <= 0 || base.deathTime > 0 || base.dead;
        } else {
            return entity.isDead;
        }
    }
}
