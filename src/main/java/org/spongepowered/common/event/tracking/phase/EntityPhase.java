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
package org.spongepowered.common.event.tracking.phase;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldServerMulti;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.Identifiable;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.EventConsumer;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.function.EntityFunction;
import org.spongepowered.common.event.tracking.phase.function.GeneralFunctions;
import org.spongepowered.common.event.tracking.phase.util.PhaseUtil;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.network.IMixinNetHandlerPlayServer;
import org.spongepowered.common.interfaces.world.IMixinTeleporter;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

public final class EntityPhase extends TrackingPhase {

    public enum State implements IPhaseState {
        DEATH() {
            @Override
            void unwind(CauseTracker causeTracker, PhaseContext context) {
                final Entity
                        dyingEntity =
                        context.firstNamed(NamedCause.SOURCE, Entity.class)
                                .orElseThrow(PhaseUtil.throwWithContext("Dying entity not found!", context));
                context.getCapturedItemsSupplier()
                        .ifPresentAndNotEmpty(items -> EntityFunction.Drops.DEATH_DROPS.process(dyingEntity, causeTracker, context, items));
                context.getCapturedEntitySupplier()
                        .ifPresentAndNotEmpty(entities -> EntityFunction.Entities.DEATH_DROPS.process(dyingEntity, causeTracker, context, entities));
                context.getCapturedEntityDropSupplier().ifPresentAndNotEmpty(map -> {
                    final Collection<ItemStack> itemStacks = map.get(dyingEntity.getUniqueId());
                    if (itemStacks.isEmpty()) {
                        return;
                    }
                    final List<ItemStack> items = new ArrayList<>();
                    final List<ItemStackSnapshot> snapshots = itemStacks.stream().map(ItemStack::createSnapshot).collect(Collectors.toList());
                    final ImmutableList<ItemStackSnapshot> originals = ImmutableList.copyOf(snapshots);
                    final DamageSource damageSource = context.firstNamed(InternalNamedCauses.General.DAMAGE_SOURCE, DamageSource.class).get();

                    final Cause preCause = Cause.source(dyingEntity).named("Attacker", damageSource).build();
                    EventConsumer.event(SpongeEventFactory.createDropItemEventPre(preCause, originals, snapshots))
                            .nonCancelled(event -> {
                                for (ItemStackSnapshot snapshot : event.getDroppedItems()) {
                                    items.add(snapshot.createStack());
                                }
                            })
                            .process();
                    if (!items.isEmpty()) {
                        final net.minecraft.entity.Entity minecraftEntity = EntityUtil.toNative(dyingEntity);
                        final List<Entity> itemEntities = items.stream()
                                .map(ItemStackUtil::toNative)
                                .map(item -> new EntityItem(causeTracker.getMinecraftWorld(), minecraftEntity.posX, minecraftEntity.posY,
                                        minecraftEntity.posZ, item))
                                .map(EntityUtil::fromNative)
                                .collect(Collectors.toList());
                        final Cause cause = Cause.source(EntitySpawnCause.builder().entity(dyingEntity)
                                .type(InternalSpawnTypes.ENTITY_DEATH)
                                .build())
                                .named(NamedCause.of("Attacker", damageSource))
                                .build();
                        EventConsumer.event(SpongeEventFactory.createDropItemEventDestruct(cause, itemEntities, causeTracker.getWorld()))
                                .nonCancelled(event -> {
                                            for (Entity entity : event.getEntities()) {
                                                TrackingUtil.associateEntityCreator(context, entity, causeTracker.getMinecraftWorld());
                                                causeTracker.getMixinWorld().forceSpawnEntity(entity);
                                            }
                                        }
                                )
                                .process();

                    }

                });
            }
        },
        DEATH_UPDATE() {
            @Override
            void unwind(CauseTracker causeTracker, PhaseContext context) {
                final Entity dyingEntity = context.firstNamed(NamedCause.SOURCE, Entity.class)
                        .orElseThrow(PhaseUtil.throwWithContext("Dying entity not found!", context));
                context.getCapturedItemsSupplier()
                        .ifPresentAndNotEmpty(items -> EntityFunction.Drops.DEATH_UPDATES.process(dyingEntity, causeTracker, context, items));
                context.getCapturedEntitySupplier()
                        .ifPresentAndNotEmpty(entities ->
                                EntityFunction.Entities.DEATH_UPDATES.process(dyingEntity, causeTracker, context, entities));
                context.getCapturedEntityDropSupplier().ifPresentAndNotEmpty(map -> {
                    if (map.isEmpty()) {
                        return;
                    }
                    final PrettyPrinter printer = new PrettyPrinter(80);
                    printer.add("Processing Entity Death Updates Spawning").centre().hr();
                    printer.add("Entity Dying: " + dyingEntity);
                    printer.add("The item stacks captured are: ");
                    for (Map.Entry<UUID, Collection<ItemStack>> entry : map.asMap().entrySet()) {
                        printer.add("  - Entity with UUID: %s", entry.getKey());
                        for (ItemStack stack : entry.getValue()) {
                            printer.add("    - %s", stack);
                        }
                    }
                    printer.trace(System.err);
                });
                context.getCapturedBlockSupplier()
                        .ifPresentAndNotEmpty(blocks -> GeneralFunctions.processBlockCaptures(blocks, causeTracker, DEATH_UPDATE, context));

            }
        },
        CHANGING_TO_DIMENSION() {
            @SuppressWarnings("unchecked")
            @Override
            void unwind(CauseTracker causeTracker, PhaseContext context) {
//                final MoveEntityEvent.Teleport.Portal portalEvent = context.firstNamed(InternalNamedCauses.Teleporting.TELEPORT_EVENT, MoveEntityEvent.Teleport.Portal.class)
//                                .orElseThrow(PhaseUtil.throwWithContext("Expected to capture a portal event!", context));
//
//                // Throw our event now
//                SpongeImpl.postEvent(portalEvent);
//
//                final IMixinEntity mixinEntity = context.firstNamed(NamedCause.SOURCE, IMixinEntity.class)
//                        .orElseThrow(PhaseUtil.throwWithContext("Expected to be teleporting an entity!", context));
//                final net.minecraft.entity.Entity minecraftEntity = EntityUtil.toNative(mixinEntity);
//
//                // Reset the player connection to allow position update packets
//                if (minecraftEntity instanceof EntityPlayerMP) {
//                    ((IMixinNetHandlerPlayServer) ((EntityPlayerMP) minecraftEntity).connection).setAllowClientLocationUpdate(true);
//                }
//
//                final Vector3i chunkPosition = mixinEntity.getLocation().getChunkPosition();
//
//
//                final Teleporter targetTeleporter = context.firstNamed(InternalNamedCauses.Teleporting.TARGET_TELEPORTER, Teleporter.class)
//                        .orElseThrow(PhaseUtil.throwWithContext("Expected to be capturing a targetTeleporter for a teleportation!", context));
//
//                final Transform<World> fromTransform = (Transform<World>) context.firstNamed(InternalNamedCauses.Teleporting.FROM_TRANSFORM, Transform.class)
//                        .orElseThrow(PhaseUtil.throwWithContext("Expected to be capturing an origination Transform!", context));
//
//                final IMixinTeleporter targetMixinTeleporter = (IMixinTeleporter) targetTeleporter;
//                if (portalEvent.isCancelled()) {
//                    targetMixinTeleporter.removePortalPositionFromCache(ChunkPos.chunkXZ2Int(chunkPosition.getX(), chunkPosition.getZ()));
//                    mixinEntity.setLocationAndAngles(fromTransform);
//                    return;
//                }
//
//                final Transform targetTransform = context.firstNamed(InternalNamedCauses.Teleporting.TARGET_TRANSFORM, Transform.class)
//                        .orElseThrow(PhaseUtil.throwWithContext("Expected to be capturing a target Transform!", context));
//
//                final WorldServer targetWorldServer = context.firstNamed(InternalNamedCauses.Teleporting.TARGET_WORLD, WorldServer.class)
//                        .orElseThrow(PhaseUtil.throwWithContext("Expected to be capturing a target WorldServer!", context));
//                final WorldServer fromWorldServer = context.firstNamed(InternalNamedCauses.Teleporting.FROM_WORLD, WorldServer.class)
//                        .orElseThrow(PhaseUtil.throwWithContext("Expected to be capturing an origination WorldServer!", context));
//                // Plugins may change transforms on us. Gotta reset the targetTeleporter cache
//                final Transform<World> eventTargetTransform = portalEvent.getToTransform();
//                if (!targetTransform.equals(eventTargetTransform)) {
//
//                    if (fromWorldServer == eventTargetTransform.getExtent()) {
//                        portalEvent.setCancelled(true);
//
//                        targetMixinTeleporter.removePortalPositionFromCache(ChunkPos.chunkXZ2Int(chunkPosition.getX(), chunkPosition.getZ()));
//                        mixinEntity.setLocationAndAngles(eventTargetTransform);
//                        if (minecraftEntity instanceof EntityPlayerMP) {
//                            final EntityPlayerMP minecraftPlayer = (EntityPlayerMP) minecraftEntity;
//                            // close any open inventory
//                            minecraftPlayer.closeScreen();
//                            // notify client
//                            minecraftPlayer.connection.setPlayerLocation(minecraftPlayer.posX, minecraftPlayer.posY, minecraftPlayer.posZ,
//                                    minecraftPlayer.rotationYaw, minecraftPlayer.rotationPitch);
//                        }
//                        return;
//                    }
//                } else {
//                    if (targetWorldServer.provider instanceof WorldProviderEnd) {
//                        final BlockPos blockpos = minecraftEntity.worldObj.getTopSolidOrLiquidBlock(targetWorldServer.getSpawnPoint());
//                        minecraftEntity.moveToBlockPosAndAngles(blockpos, minecraftEntity.rotationYaw, minecraftEntity.rotationPitch);
//                    }
//                }
//
//                final IMixinWorldServer targetMixinWorldServer = (IMixinWorldServer) targetWorldServer;
//                final List<BlockSnapshot> capturedBlocks = context.getCapturedBlocks();
//                final CauseTracker targetCauseTracker = targetMixinWorldServer.getCauseTracker();
//                if (capturedBlocks.isEmpty()
//                    || !GeneralFunctions.processBlockCaptures(capturedBlocks, targetCauseTracker, State.CHANGING_TO_DIMENSION, context)) {
//                    targetMixinTeleporter.removePortalPositionFromCache(ChunkPos.chunkXZ2Int(chunkPosition.getX(), chunkPosition.getZ()));
//                }
//
//                if (!portalEvent.getKeepsVelocity()) {
//                    minecraftEntity.motionX = 0;
//                    minecraftEntity.motionY = 0;
//                    minecraftEntity.motionZ = 0;
//                }
            }

            @Nullable
            @Override
            public net.minecraft.entity.Entity returnTeleportResult(PhaseContext context, MoveEntityEvent.Teleport.Portal event) {
                final net.minecraft.entity.Entity teleportingEntity = context.firstNamed(NamedCause.SOURCE, net.minecraft.entity.Entity.class)
                                .orElseThrow(PhaseUtil.throwWithContext("Expected to be teleporting an entity!", context));
                // The rest of this is to be handled in the phase.
                if (event.isCancelled()) {
                    return null;
                }

                teleportingEntity.worldObj.theProfiler.startSection("changeDimension");

                WorldServer toWorld = (WorldServer) event.getToTransform().getExtent();

                teleportingEntity.worldObj.removeEntity(teleportingEntity);
                teleportingEntity.isDead = false;
                teleportingEntity.worldObj.theProfiler.startSection("reposition");
                final Vector3d position = event.getToTransform().getPosition();
                teleportingEntity.setLocationAndAngles(position.getX(), position.getY(), position.getZ(), (float) event.getToTransform().getYaw(),
                        (float) event.getToTransform().getPitch());
                toWorld.spawnEntityInWorld(teleportingEntity);
                teleportingEntity.worldObj = toWorld;

                toWorld.updateEntityWithOptionalForce(teleportingEntity, false);
                teleportingEntity.worldObj.theProfiler.endStartSection("reloading");


                teleportingEntity.worldObj.theProfiler.endSection();
                teleportingEntity.worldObj.theProfiler.endSection();
                return teleportingEntity;
            }
        },
        LEAVING_DIMENSION,
        ;

        @Override
        public EntityPhase getPhase() {
            return TrackingPhases.ENTITY;
        }

        @Override
        public boolean tracksEntitySpecificDrops() {
            return true;
        }

        @Override
        public void assignEntityCreator(PhaseContext context, Entity entity) {
            final IMixinEntity mixinEntity = context.firstNamed(NamedCause.SOURCE, IMixinEntity.class)
                            .orElseThrow(PhaseUtil.throwWithContext("Dying Entity not found!", context));
            Stream.<Supplier<Optional<UUID>>>of(
                    () -> mixinEntity.getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_NOTIFIER).map(Identifiable::getUniqueId),
                    () -> mixinEntity.getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR).map(Identifiable::getUniqueId))
                    .map(Supplier::get)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst()
                    .ifPresent(creator -> EntityUtil.toMixin(entity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, creator));
        }

        @Nullable
        public net.minecraft.entity.Entity returnTeleportResult(PhaseContext context, MoveEntityEvent.Teleport.Portal event) {
            return null;
        }

        void unwind(CauseTracker causeTracker, PhaseContext context) {

        }
    }

    @Override
    public void unwind(CauseTracker causeTracker, IPhaseState state, PhaseContext phaseContext) {
        if (state instanceof State) {
            ((State) state).unwind(causeTracker, phaseContext);
        }

    }

    EntityPhase(TrackingPhase parent) {
        super(parent);
    }

}
