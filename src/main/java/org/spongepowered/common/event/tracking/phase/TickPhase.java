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
import net.minecraft.block.Block;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.CombatEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.Ageable;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.BlockSpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.cause.entity.teleport.EntityTeleportCause;
import org.spongepowered.api.event.cause.entity.teleport.TeleportCause;
import org.spongepowered.api.event.cause.entity.teleport.TeleportTypes;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.block.IMixinBlockEventData;
import org.spongepowered.common.interfaces.block.tile.IMixinTileEntity;
import org.spongepowered.common.interfaces.world.IMixinLocation;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.BlockChange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

public final class TickPhase extends TrackingPhase {

    public static final class Tick {

        public static final IPhaseState BLOCK = new BlockTickPhaseState("BlockTickPhase");

        public static final IPhaseState RANDOM_BLOCK = new BlockTickPhaseState("RandomBlockTickPhase");

        public static final IPhaseState ENTITY = new EntityTickPhaseState();

        public static final IPhaseState DIMENSION = new DimensionTickPhaseState();
        public static final IPhaseState TILE_ENTITY = new TileEntityTickPhaseState();
        public static final IPhaseState BLOCK_EVENT = new BlockEventTickPhaseState();
        public static final IPhaseState PLAYER = new PlayerTickPhaseState();
        public static final IPhaseState WEATHER = new WeatherTickPhaseState();

        private Tick() { // No instances for you!
        }
    }

    private static abstract class TickPhaseState implements IPhaseState {

        TickPhaseState() {
        }

        @Override
        public final TrackingPhase getPhase() {
            return TrackingPhases.WORLD;
        }

        @Override
        public boolean canSwitchTo(IPhaseState state) {
            return state instanceof BlockPhase.State || state instanceof EntityPhase.State || state == GenerationPhase.State.TERRAIN_GENERATION;
        }

        @Override
        public boolean tracksBlockSpecificDrops() {
            return true;
        }

        public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) { }

        public abstract void associateAdditionalBlockChangeCauses(PhaseContext context, Cause.Builder builder, CauseTracker causeTracker);

        public void associateBlockEventNotifier(PhaseContext context, CauseTracker causeTracker, BlockPos pos, IMixinBlockEventData blockEvent) {

        }

        public void associateNeighborBlockNotifier(PhaseContext context, @Nullable BlockPos sourcePos, Block block, BlockPos notifyPos,
                WorldServer minecraftWorld, PlayerTracker.Type notifier) {

        }

        public Cause generateTeleportCause(PhaseContext context) {
            return Cause.of(NamedCause.source(TeleportCause.builder().type(TeleportTypes.UNKNOWN).build()));
        }

        public void processPostSpawns(CauseTracker causeTracker, PhaseContext phaseContext, ArrayList<Entity> entities) {
            final SpawnEntityEvent
                    event =
                    SpongeEventFactory.createSpawnEntityEvent(InternalSpawnTypes.UNKNOWN_CAUSE, entities, causeTracker.getWorld());
            SpongeImpl.postEvent(event);
            if (!event.isCancelled()) {
                for (Entity entity : event.getEntities()) {
                    causeTracker.getMixinWorld().forceSpawnEntity(entity);
                }
            }
        }

        public void appendExplosionContext(PhaseContext explosionContext, PhaseContext context) {

        }
    }

    private static abstract class LocationBasedTickPhaseState extends TickPhaseState {

        LocationBasedTickPhaseState() {
        }


        abstract Location<World> getLocationSourceFromContext(PhaseContext context);

        @Override
        public void associateNeighborBlockNotifier(PhaseContext context, @Nullable BlockPos sourcePos, Block block, BlockPos notifyPos,
                WorldServer minecraftWorld, PlayerTracker.Type notifier) {
            final Location<World> location = getLocationSourceFromContext(context);
            TrackingUtil.getNotifierOrOwnerFromBlock(location)
                    .ifPresent(user -> {
                        final BlockPos blockPos = ((IMixinLocation) (Object) location).getBlockPos();
                        final IMixinChunk mixinChunk = (IMixinChunk) minecraftWorld.getChunkFromBlockCoords(blockPos);
                        mixinChunk.addTrackedBlockPosition(block, notifyPos, user, PlayerTracker.Type.NOTIFIER);
                    });
        }

        @Override
        public void handleBlockChangeWithUser(@Nullable BlockChange blockChange, WorldServer minecraftWorld, Transaction<BlockSnapshot> snapshotTransaction, PhaseContext context) {
            final Location<World> location = getLocationSourceFromContext(context);
            final Block block = (Block) snapshotTransaction.getOriginal().getState().getType();
            final Location<World> changedLocation = snapshotTransaction.getOriginal().getLocation().get();
            final Vector3d changedPosition = changedLocation.getPosition();
            final BlockPos changedBlockPos = VecHelper.toBlockPos(changedPosition);
            final IMixinChunk changedMixinChunk = (IMixinChunk) ((WorldServer) changedLocation.getExtent()).getChunkFromBlockCoords(changedBlockPos);
            TrackingUtil.getNotifierOrOwnerFromBlock(location)
                    .ifPresent(user -> changedMixinChunk.addTrackedBlockPosition(block, changedBlockPos, user, PlayerTracker.Type.NOTIFIER));
        }


        @Override
        public boolean canSwitchTo(IPhaseState state) {
            return super.canSwitchTo(state) || state == GenerationPhase.State.CHUNK_LOADING;
        }

    }

    private static class BlockTickPhaseState extends LocationBasedTickPhaseState {

        private final String name;

        BlockTickPhaseState(String name) {
            this.name = name;
        }

        @Override
        Location<World> getLocationSourceFromContext(PhaseContext context) {
            return context.getSource(BlockSnapshot.class)
                    .orElseThrow(TrackingUtil.throwWithContext("Expected to be ticking over a block!", context))
                    .getLocation()
                    .get();
        }

        @Override
        public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
            final BlockSnapshot tickingBlock = phaseContext.getSource(BlockSnapshot.class)
                    .orElseThrow(TrackingUtil.throwWithContext("Not ticking on a Block!", phaseContext));
            final Optional<User> owner = phaseContext.getOwner();
            final Optional<User> notifier = phaseContext.getNotifier();
            final User entityCreator = notifier.orElseGet(() -> owner.orElse(null));
            phaseContext.getCapturedBlockSupplier()
                    .ifPresentAndNotEmpty(blockSnapshots -> TrackingUtil.processBlockCaptures(blockSnapshots, causeTracker, this, phaseContext));
            phaseContext.getCapturedEntitySupplier()
                    .ifPresentAndNotEmpty(entities -> {
                        // Separate experience from other entities
                        final List<Entity> experience = new ArrayList<>(entities.size());
                        final List<Entity> nonExpEntities = new ArrayList<>(entities.size());
                        for (Entity entity : entities) {
                            if (entity instanceof EntityXPOrb) {
                                experience.add(entity);
                                continue;
                            }
                            nonExpEntities.add(entity);
                        }
                        if (!experience.isEmpty()) {
                            final Cause.Builder builder = Cause.builder();
                            builder.named(NamedCause.source(BlockSpawnCause.builder()
                                    .block(tickingBlock)
                                    .type(InternalSpawnTypes.EXPERIENCE)
                                    .build()));
                            notifier.ifPresent(builder::notifier);
                            owner.ifPresent(builder::owner);
                            SpongeEventFactory.createSpawnEntityEvent(builder.build(), experience, causeTracker.getWorld());
                        }
                        final Cause.Builder builder = Cause.source(BlockSpawnCause.builder()
                                .block(tickingBlock)
                                .type(InternalSpawnTypes.BLOCK_SPAWNING)
                                .build());
                        notifier.ifPresent(builder::notifier);
                        owner.ifPresent(builder::owner);
                        final Cause cause = builder.build();
                        final SpawnEntityEvent
                                spawnEntityEvent =
                                SpongeEventFactory.createSpawnEntityEvent(cause, nonExpEntities, causeTracker.getWorld());
                        SpongeImpl.postEvent(spawnEntityEvent);
                        for (Entity entity : spawnEntityEvent.getEntities()) {
                            if (entityCreator != null) {
                                EntityUtil.toMixin(entity).setCreator(entityCreator.getUniqueId());
                            }
                            causeTracker.getMixinWorld().forceSpawnEntity(entity);
                        }
                    });
            phaseContext.getCapturedItemsSupplier()
                    .ifPresentAndNotEmpty(items -> {
                        final Cause cause = Cause.source(BlockSpawnCause.builder()
                                .block(tickingBlock)
                                .type(InternalSpawnTypes.DROPPED_ITEM)
                                .build())
                                .build();
                        final ArrayList<Entity> capturedEntities = new ArrayList<>();
                        for (EntityItem entity : items) {
                            capturedEntities.add(EntityUtil.fromNative(entity));
                        }
                        final SpawnEntityEvent
                                spawnEntityEvent =
                                SpongeEventFactory.createSpawnEntityEvent(cause, capturedEntities, causeTracker.getWorld());
                        SpongeImpl.postEvent(spawnEntityEvent);
                        for (Entity entity : spawnEntityEvent.getEntities()) {
                            if (entityCreator != null) {
                                EntityUtil.toMixin(entity).setCreator(entityCreator.getUniqueId());
                            }
                            causeTracker.getMixinWorld().forceSpawnEntity(entity);
                        }
                    });
        }

        @Override
        public void associateAdditionalBlockChangeCauses(PhaseContext context, Cause.Builder builder, CauseTracker causeTracker) {
            final BlockSnapshot tickingBlock = context.getSource(BlockSnapshot.class)
                    .orElseThrow(TrackingUtil.throwWithContext("Not ticking on a Block!", context));
            builder.named(NamedCause.notifier(tickingBlock));
        }

        @Override
        public void associateBlockEventNotifier(PhaseContext context, CauseTracker causeTracker, BlockPos pos, IMixinBlockEventData blockEvent) {
            final BlockSnapshot tickingBlock = context.getSource(BlockSnapshot.class)
                    .orElseThrow(TrackingUtil.throwWithContext("Expected to be ticking a block, but found none!", context));
            blockEvent.setCurrentTickBlock(tickingBlock);
            final Location<World> blockLocation = tickingBlock.getLocation().get();
            final WorldServer worldServer = (WorldServer) blockLocation.getExtent();
            final Vector3d blockPosition = blockLocation.getPosition();
            final BlockPos blockPos = VecHelper.toBlockPos(blockPosition);
            final IMixinChunk mixinChunk = (IMixinChunk) worldServer.getChunkFromBlockCoords(blockPos);
            mixinChunk.getBlockNotifier(blockPos).ifPresent(blockEvent::setSourceUser);
            context.firstNamed(NamedCause.NOTIFIER, User.class).ifPresent(blockEvent::setSourceUser);
        }

        @Override
        public void appendExplosionContext(PhaseContext explosionContext, PhaseContext context) {
            context.getOwner().ifPresent(explosionContext::owner);
            context.getNotifier().ifPresent(explosionContext::notifier);
            final BlockSnapshot blockSnapshot = context.getSource(BlockSnapshot.class)
                    .orElseThrow(TrackingUtil.throwWithContext("Expected to be ticking a block", context));
            explosionContext.add(NamedCause.source(blockSnapshot));
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    private static class EntityTickPhaseState extends TickPhaseState {

        EntityTickPhaseState() {
        }

        @SuppressWarnings("unchecked")
        @Override
        public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
            final Entity tickingEntity = phaseContext.getSource(Entity.class)
                    .orElseThrow(TrackingUtil.throwWithContext("Not ticking on an Entity!", phaseContext));
            final Optional<User> creator = phaseContext.getOwner();
            final Optional<User> notifier = phaseContext.getNotifier();
            final User entityCreator = notifier.orElseGet(() -> creator.orElse(null));
            phaseContext.getCapturedEntitySupplier()
                    .ifPresentAndNotEmpty(entities -> {
                        final List<Entity> experience = new ArrayList<Entity>(entities.size());
                        final List<Entity> nonExp = new ArrayList<Entity>(entities.size());
                        final List<Entity> breeding = new ArrayList<Entity>(entities.size());
                        final List<Entity> projectile = new ArrayList<Entity>(entities.size());
                        for (Entity entity : entities) {
                            if (entity instanceof EntityXPOrb) {
                                experience.add(entity);
                            } else if (tickingEntity instanceof Ageable && tickingEntity.getClass() == entity.getClass()) {
                                breeding.add(entity);
                            } else if (entity instanceof Projectile) {
                                projectile.add(entity);
                            } else {
                                nonExp.add(entity);
                            }
                        }

                        if (!experience.isEmpty()) {
                            final Cause.Builder builder = Cause.source(
                                    EntitySpawnCause.builder()
                                            .entity(tickingEntity)
                                            .type(InternalSpawnTypes.EXPERIENCE)
                                            .build()
                            );
                            notifier.ifPresent(builder::notifier);
                            creator.ifPresent(builder::owner);
                            if (EntityUtil.isEntityDead(tickingEntity)) {
                                if (tickingEntity instanceof EntityLivingBase) {
                                    CombatEntry entry = ((EntityLivingBase) tickingEntity).getCombatTracker().getBestCombatEntry();
                                    if (entry != null) {
                                        if (entry.damageSrc != null) {
                                            builder.named(NamedCause.of("LastDamageSource", entry.damageSrc));
                                        }
                                    }
                                }
                            }
                            final SpawnEntityEvent
                                    event =
                                    SpongeEventFactory.createSpawnEntityEvent(builder.build(), experience, causeTracker.getWorld());
                            if (!SpongeImpl.postEvent(event)) {
                                for (Entity entity : event.getEntities()) {
                                    if (entityCreator != null) {
                                        EntityUtil.toMixin(entity).setCreator(entityCreator.getUniqueId());
                                    }
                                    causeTracker.getMixinWorld().forceSpawnEntity(entity);
                                }
                            }
                        }
                        if (!breeding.isEmpty()) {
                            final Cause.Builder builder = Cause.source(
                                    EntitySpawnCause.builder()
                                            .entity(tickingEntity)
                                            .type(InternalSpawnTypes.BREEDING)
                                            .build()
                            );
                            if (tickingEntity instanceof EntityAnimal) {
                                final EntityPlayer playerInLove = ((EntityAnimal) tickingEntity).getPlayerInLove();
                                if (playerInLove != null) {
                                    builder.named(NamedCause.of("Player", playerInLove));
                                }
                            }
                            SpawnEntityEvent event = SpongeEventFactory.createSpawnEntityEvent(builder.build(), breeding, causeTracker.getWorld());
                            if (!SpongeImpl.postEvent(event)) {
                                for (Entity entity : event.getEntities()) {
                                    if (entityCreator != null) {
                                        EntityUtil.toMixin(entity).setCreator(entityCreator.getUniqueId());
                                    }
                                    causeTracker.getMixinWorld().forceSpawnEntity(entity);
                                }
                            }
                        }
                        if (!projectile.isEmpty()) {
                            final Cause.Builder builder = Cause.source(
                                    EntitySpawnCause.builder()
                                            .entity(tickingEntity)
                                            .type(InternalSpawnTypes.PROJECTILE)
                                            .build()
                            );

                            notifier.ifPresent(builder::notifier);
                            creator.ifPresent(builder::owner);
                            final SpawnEntityEvent event = SpongeEventFactory.createSpawnEntityEvent(builder.build(), projectile, causeTracker.getWorld());
                            SpongeImpl.postEvent(event);
                            if (!event.isCancelled()) {
                                for (Entity entity : event.getEntities()) {
                                    if (entityCreator != null) {
                                        entity.setCreator(entityCreator.getUniqueId());
                                    }
                                    causeTracker.getMixinWorld().forceSpawnEntity(entity);
                                }
                            }
                        }

                        final Cause.Builder builder = Cause.source(EntitySpawnCause.builder()
                                .entity(tickingEntity)
                                .type(InternalSpawnTypes.PASSIVE)
                                .build());
                        notifier.ifPresent(builder::notifier);
                        creator.ifPresent(builder::owner);
                        final SpawnEntityEvent event = SpongeEventFactory.createSpawnEntityEvent(builder.build(), nonExp, causeTracker.getWorld());
                        SpongeImpl.postEvent(event);
                        if (!event.isCancelled()) {
                            for (Entity entity : event.getEntities()) {
                                if (entityCreator != null) {
                                    entity.setCreator(entityCreator.getUniqueId());
                                }
                                causeTracker.getMixinWorld().forceSpawnEntity(entity);
                            }
                        }
                    });
            phaseContext.getCapturedItemsSupplier()
                    .ifPresentAndNotEmpty(entities -> {
                        final ArrayList<Entity> capturedEntities = new ArrayList<>();
                        for (EntityItem entity : entities) {
                            capturedEntities.add(EntityUtil.fromNative(entity));
                        }
                        final Cause.Builder builder = Cause.source(EntitySpawnCause.builder()
                                .entity(tickingEntity)
                                .type(InternalSpawnTypes.DROPPED_ITEM)
                                .build());
                        notifier.ifPresent(user -> builder.named(NamedCause.notifier(user)));
                        creator.ifPresent(user -> builder.named(NamedCause.owner(user)));
                        final DropItemEvent.Custom event = SpongeEventFactory
                                .createDropItemEventCustom(builder.build(), capturedEntities, causeTracker.getWorld());
                        SpongeImpl.postEvent(event);
                        if (!event.isCancelled()) {
                            for (Entity entity : event.getEntities()) {
                                if (entityCreator != null) {
                                    EntityUtil.toMixin(entity).setCreator(entityCreator.getUniqueId());
                                }
                                causeTracker.getMixinWorld().forceSpawnEntity(entity);
                            }
                        }
                    });
            phaseContext.getCapturedBlockSupplier()
                    .ifPresentAndNotEmpty(blockSnapshots -> TrackingUtil.processBlockCaptures(blockSnapshots, causeTracker, this, phaseContext));
            phaseContext.getBlockItemDropSupplier()
                    .ifPresentAndNotEmpty(map -> {
                        final List<BlockSnapshot> capturedBlocks = phaseContext.getCapturedBlocks();
                        for (BlockSnapshot snapshot : capturedBlocks) {
                            final BlockPos blockPos = ((IMixinLocation) (Object) snapshot.getLocation().get()).getBlockPos();
                            final Collection<EntityItem> entityItems = map.get(blockPos);
                            if (!entityItems.isEmpty()) {
                                final Cause.Builder builder = Cause.source(BlockSpawnCause.builder()
                                        .block(snapshot)
                                        .type(InternalSpawnTypes.DROPPED_ITEM)
                                        .build());
                                notifier.ifPresent(builder::notifier);
                                creator.ifPresent(builder::owner);
                                builder.build();
                                final List<Entity> items = entityItems.stream().map(EntityUtil::fromNative).collect(Collectors.toList());
                                final DropItemEvent.Destruct event = SpongeEventFactory.createDropItemEventDestruct(builder.build(), items, causeTracker.getWorld());
                                SpongeImpl.postEvent(event);
                                if (!event.isCancelled()) {
                                    for (Entity entity : event.getEntities()) {
                                        creator.ifPresent(user -> entity.setCreator(user.getUniqueId()));
                                        causeTracker.getMixinWorld().forceSpawnEntity(entity);
                                    }
                                }
                            }
                        }

                    });
            phaseContext.getCapturedItemStackSupplier()
                    .ifPresentAndNotEmpty(drops -> {
                        final List<EntityItem> items = drops.stream()
                                .map(drop -> drop.create(causeTracker.getMinecraftWorld()))
                                .collect(Collectors.toList());
                        final Cause.Builder builder = Cause.source(
                                EntitySpawnCause.builder()
                                        .entity(tickingEntity)
                                        .type(InternalSpawnTypes.DROPPED_ITEM)
                                        .build()
                        );
                        notifier.ifPresent(user -> builder.named(NamedCause.notifier(user)));
                        creator.ifPresent(user -> builder.named(NamedCause.owner(user)));
                        final Cause cause = builder.build();
                        final List<Entity> entities = (List<Entity>) (List<?>) items;
                        if (!entities.isEmpty()) {
                            DropItemEvent.Custom event = SpongeEventFactory.createDropItemEventCustom(cause, entities, causeTracker.getWorld());
                            SpongeImpl.postEvent(event);
                            if (!event.isCancelled()) {
                                for (Entity droppedItem : event.getEntities()) {
                                    causeTracker.getMixinWorld().forceSpawnEntity(droppedItem);
                                }
                            }
                        }
                    });
            this.fireMovementEvents(EntityUtil.toNative(tickingEntity), Cause.source(tickingEntity).build());
        }

        private void fireMovementEvents(net.minecraft.entity.Entity entity, Cause cause) {
            Entity spongeEntity = (Entity) entity;

            if (entity.lastTickPosX != entity.posX
                || entity.lastTickPosY != entity.posY
                || entity.lastTickPosZ != entity.posZ
                || entity.rotationPitch != entity.prevRotationPitch
                || entity.rotationYaw != entity.prevRotationYaw) {
                // yes we have a move event.
                final double currentPosX = entity.posX;
                final double currentPosY = entity.posY;
                final double currentPosZ = entity.posZ;

                final Vector3d oldPositionVector = new Vector3d(entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ);
                final Vector3d currentPositionVector = new Vector3d(currentPosX, currentPosY, currentPosZ);

                Vector3d oldRotationVector = new Vector3d(entity.prevRotationPitch, entity.prevRotationYaw, 0);
                Vector3d currentRotationVector = new Vector3d(entity.rotationPitch, entity.rotationYaw, 0);
                final Transform<World> oldTransform = new Transform<>(spongeEntity.getWorld(), oldPositionVector, oldRotationVector,
                        spongeEntity.getScale());
                final Transform<World> newTransform = new Transform<>(spongeEntity.getWorld(), currentPositionVector, currentRotationVector,
                        spongeEntity.getScale());
                final MoveEntityEvent event = SpongeEventFactory.createMoveEntityEvent(cause, oldTransform, newTransform, spongeEntity);

                if (SpongeImpl.postEvent(event)) {
                    entity.posX = entity.lastTickPosX;
                    entity.posY = entity.lastTickPosY;
                    entity.posZ = entity.lastTickPosZ;
                    entity.rotationPitch = entity.prevRotationPitch;
                    entity.rotationYaw = entity.prevRotationYaw;

                } else {
                    Vector3d newPosition = event.getToTransform().getPosition();
                    if (!newPosition.equals(currentPositionVector)) {
                        entity.posX = newPosition.getX();
                        entity.posY = newPosition.getY();
                        entity.posZ = newPosition.getZ();
                    }
                    if (!event.getToTransform().getRotation().equals(currentRotationVector)) {
                        entity.rotationPitch = (float) currentRotationVector.getX();
                        entity.rotationYaw = (float) currentRotationVector.getY();
                    }
                    //entity.setPositionAndRotation(position.getX(), position.getY(), position.getZ(), rotation.getFloorX(), rotation.getFloorY());
                        /*
                        Some thoughts from gabizou: The interesting thing here is that while this is only called
                        in World.updateEntityWithOptionalForce, by default, it supposedly handles updating the rider entity
                        of the entity being handled here. The interesting issue is that since we are setting the transform,
                        the rider entity (and the rest of the rider entities) are being updated as well with the new position
                        and potentially world, which results in a dirty world usage (since the world transfer is handled by
                        us). Now, the thing is, the previous position is not updated either, and likewise, the current position
                        is being set by us as well. So, there's some issue I'm sure that is bound to happen with this
                        logic.
                         */
                    //((Entity) entity).setTransform(event.getToTransform());
                }
            }
        }

        @Override
        public Cause generateTeleportCause(PhaseContext context) {
            final Entity entity = context.getSource(Entity.class)
                    .orElseThrow(TrackingUtil.throwWithContext("Expected to be ticking an entity!", context));
            return Cause
                    .source(EntityTeleportCause.builder()
                            .entity(entity)
                            .type(TeleportTypes.ENTITY_TELEPORT)
                            .build()
                    )
                    .build();
        }

        @SuppressWarnings("OptionalGetWithoutIsPresent")
        @Override
        public void handleBlockChangeWithUser(@Nullable BlockChange blockChange, WorldServer minecraftWorld, Transaction<BlockSnapshot> transaction,
                PhaseContext context) {
            if (blockChange == BlockChange.BREAK) {
                final Entity tickingEntity = context.getSource(Entity.class).get();
                final BlockPos blockPos = VecHelper.toBlockPos(transaction.getOriginal().getPosition());
                for (EntityHanging entityHanging : EntityUtil.findHangingEntities(minecraftWorld, blockPos)) {
                    if (entityHanging instanceof EntityItemFrame) {
                        final EntityItemFrame frame = (EntityItemFrame) entityHanging;
                        if (tickingEntity != null) {
                            frame.dropItemOrSelf(EntityUtil.toNative(tickingEntity), true);
                        }
                        frame.setDead();
                    }
                }
            }
        }


        @Override
        public void associateAdditionalBlockChangeCauses(PhaseContext context, Cause.Builder builder, CauseTracker causeTracker) {
            final Entity tickingEntity = context.getSource(Entity.class)
                    .orElseThrow(TrackingUtil.throwWithContext("Not ticking on an Entity!", context));
            builder.named(NamedCause.owner(tickingEntity));
            context.getNotifier().ifPresent(builder::notifier);
        }


        @Override
        public void processPostSpawns(CauseTracker causeTracker, PhaseContext phaseContext, ArrayList<Entity> entities) {
            final Entity tickingEntity = phaseContext.getSource(Entity.class)
                            .orElseThrow(TrackingUtil.throwWithContext("Expected to be processing over a ticking entity!", phaseContext));
            final Optional<User> owner = phaseContext.getOwner();
            final Optional<User> notifier = phaseContext.getNotifier();
            super.processPostSpawns(causeTracker, phaseContext, entities);
        }

        @Override
        public void appendExplosionContext(PhaseContext explosionContext, PhaseContext context) {
            context.getOwner().ifPresent(explosionContext::owner);
            context.getNotifier().ifPresent(explosionContext::notifier);
            final Entity tickingEntity = context.getSource(Entity.class)
                    .orElseThrow(TrackingUtil.throwWithContext("Expected to be processing over a ticking entity!", context));
            explosionContext.add(NamedCause.source(tickingEntity));
        }

        @Override
        public String toString() {
            return "EntityTickPhase";
        }
    }

    private static class DimensionTickPhaseState extends TickPhaseState {
        DimensionTickPhaseState() {
        }

        @Override
        public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
            phaseContext.getCapturedBlockSupplier()
                    .ifPresentAndNotEmpty(blockSnapshots -> {
                        TrackingUtil.processBlockCaptures(blockSnapshots, causeTracker, this, phaseContext);
                    });

            phaseContext.getCapturedEntitySupplier()
                    .ifPresentAndNotEmpty(entities -> {
                        // TODO the entity spawn causes are not likely valid, need to investigate further.
                        final Cause cause = Cause.source(SpawnCause.builder()
                                .type(InternalSpawnTypes.PLACEMENT)
                                .build())
                                .build();
                        final SpawnEntityEvent event =
                                SpongeEventFactory.createSpawnEntityEvent(cause, entities, causeTracker.getWorld());
                        SpongeImpl.postEvent(event);
                        if (!event.isCancelled()) {
                            for (Entity entity : event.getEntities()) {
                                causeTracker.getMixinWorld().forceSpawnEntity(entity);
                            }
                        }

                    });
            phaseContext.getCapturedItemsSupplier()
                    .ifPresentAndNotEmpty(entities -> {
                        final Cause cause = Cause.source(SpawnCause.builder()
                                .type(InternalSpawnTypes.PLACEMENT)
                                .build())
                                .build();
                        final ArrayList<Entity> capturedEntities = new ArrayList<>();
                        for (EntityItem entity : entities) {
                            capturedEntities.add(EntityUtil.fromNative(entity));
                        }
                        final SpawnEntityEvent event =
                                SpongeEventFactory.createSpawnEntityEvent(cause, capturedEntities, causeTracker.getWorld());
                        SpongeImpl.postEvent(event);
                        if (!event.isCancelled()) {
                            for (Entity entity : event.getEntities()) {
                                causeTracker.getMixinWorld().forceSpawnEntity(entity);
                            }
                        }
                    });
        }
        @Override
        public void associateAdditionalBlockChangeCauses(PhaseContext context, Cause.Builder builder, CauseTracker causeTracker) {

        }
        @Override
        public String toString() {
            return "DimensionTickPhase";
        }
    }

    private static class TileEntityTickPhaseState extends LocationBasedTickPhaseState {

        TileEntityTickPhaseState() {
        }

        @Override
        Location<World> getLocationSourceFromContext(PhaseContext context) {
            return context.getSource(TileEntity.class)
                    .orElseThrow(TrackingUtil.throwWithContext("Expected to be ticking over a TileEntity!", context))
                    .getLocation();
        }

        @Override
        public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
            final TileEntity tickingTile = phaseContext.getSource(TileEntity.class)
                    .orElseThrow(TrackingUtil.throwWithContext("Not ticking on a TileEntity!", phaseContext));
            final PhaseContext.CaptureBlockSnapshotForTile capturedSnapshot = phaseContext
                    .firstNamed(InternalNamedCauses.Tracker.TILE_BLOCK_SNAPSHOT, PhaseContext.CaptureBlockSnapshotForTile.class)
                    .orElseThrow(TrackingUtil.throwWithContext("Expected to be capturing a snapshot for a ticking tile entity!", phaseContext));
            final Optional<User> notifier = phaseContext.getNotifier();
            final Optional<User> owner = phaseContext.getOwner();
            final User entityCreator = notifier.orElseGet(() -> owner.orElse(null));
            final IMixinTileEntity mixinTileEntity = (IMixinTileEntity) tickingTile;
            phaseContext.getCapturedBlockSupplier()
                    .ifPresentAndNotEmpty(blockSnapshots -> {
                        TrackingUtil.processBlockCaptures(blockSnapshots, causeTracker, this, phaseContext);
                    });

            phaseContext.getCapturedEntitySupplier()
                    .ifPresentAndNotEmpty(entities -> {
                        // Separate experience from other entities
                        final List<Entity> experience = new ArrayList<>(entities.size());
                        final List<Entity> nonExpEntities = new ArrayList<>(entities.size());
                        for (Entity entity : entities) {
                            if (entity instanceof EntityXPOrb) {
                                experience.add(entity);
                                continue;
                            }
                            nonExpEntities.add(entity);
                        }
                        if (!experience.isEmpty()) { // Experience needs to have the proper spawn type
                            final Cause.Builder builder = Cause.builder();
                            builder.named(NamedCause.source(
                                    BlockSpawnCause.builder()
                                            .block(capturedSnapshot.getSnapshot())
                                            .type(InternalSpawnTypes.EXPERIENCE)
                                            .build()
                                    )
                            );
                            notifier.ifPresent(builder::notifier);
                            owner.ifPresent(builder::owner);
                            final SpawnEntityEvent event = SpongeEventFactory.createSpawnEntityEvent(builder.build(), experience, causeTracker.getWorld());
                            SpongeImpl.postEvent(event);
                            if (!event.isCancelled()) {
                                for (Entity entity : event.getEntities()) {
                                    if (entityCreator != null) {
                                        EntityUtil.toMixin(entity).setCreator(entityCreator.getUniqueId());
                                    }
                                    causeTracker.getMixinWorld().forceSpawnEntity(entity);
                                }
                            }

                        } // Otherwise, just go ahead
                        final Cause.Builder builder = Cause.source(BlockSpawnCause.builder()
                                .block(capturedSnapshot.getSnapshot())
                                .type(mixinTileEntity.getTickedSpawnType())
                                .build());
                        notifier.ifPresent(builder::notifier);
                        owner.ifPresent(builder::owner);
                        final Cause cause = builder.build();
                        final SpawnEntityEvent
                                spawnEntityEvent =
                                SpongeEventFactory.createSpawnEntityEvent(cause, nonExpEntities, causeTracker.getWorld());
                        SpongeImpl.postEvent(spawnEntityEvent);
                        if (!spawnEntityEvent.isCancelled()) {
                            for (Entity entity : spawnEntityEvent.getEntities()) {
                                if (entityCreator != null) {
                                    EntityUtil.toMixin(entity).setCreator(entityCreator.getUniqueId());
                                }
                                causeTracker.getMixinWorld().forceSpawnEntity(entity);
                            }
                        }
                    });
            phaseContext.getCapturedItemsSupplier()
                    .ifPresentAndNotEmpty(entities -> {
                        final Cause cause = Cause.source(BlockSpawnCause.builder()
                                .block(capturedSnapshot.getSnapshot())
                                .type(InternalSpawnTypes.BLOCK_SPAWNING)
                                .build())
                                .build();
                        final ArrayList<Entity> capturedEntities = new ArrayList<>();
                        for (EntityItem entity : entities) {
                            capturedEntities.add(EntityUtil.fromNative(entity));
                        }
                        final SpawnEntityEvent
                                spawnEntityEvent =
                                SpongeEventFactory.createSpawnEntityEvent(cause, capturedEntities, causeTracker.getWorld());
                        SpongeImpl.postEvent(spawnEntityEvent);
                        if (!spawnEntityEvent.isCancelled()) {
                            for (Entity entity : spawnEntityEvent.getEntities()) {
                                if (entityCreator != null) {
                                    EntityUtil.toMixin(entity).setCreator(entityCreator.getUniqueId());
                                }
                                causeTracker.getMixinWorld().forceSpawnEntity(entity);
                            }
                        }
                    });
        }

        @Override
        public void associateAdditionalBlockChangeCauses(PhaseContext context, Cause.Builder builder, CauseTracker causeTracker) {
            final TileEntity tickingTile = context.getSource(TileEntity.class)
                    .orElseThrow(TrackingUtil.throwWithContext("Not ticking on a TileEntity!", context));
            builder.named(NamedCause.notifier(tickingTile));
        }

        @Override
        public void associateBlockEventNotifier(PhaseContext context, CauseTracker causeTracker, BlockPos pos, IMixinBlockEventData blockEvent) {
            final TileEntity tickingTile = context.getSource(TileEntity.class)
                    .orElseThrow(TrackingUtil.throwWithContext("Expected to be ticking a block, but found none!", context));
            blockEvent.setCurrentTickTileEntity(tickingTile);
            final Location<World> blockLocation = tickingTile.getLocation();
            final WorldServer worldServer = (WorldServer) blockLocation.getExtent();
            final Vector3d blockPosition = blockLocation.getPosition();
            final BlockPos blockPos = VecHelper.toBlockPos(blockPosition);
            final IMixinChunk mixinChunk = (IMixinChunk) worldServer.getChunkFromBlockCoords(blockPos);
            mixinChunk.getBlockNotifier(blockPos).ifPresent(blockEvent::setSourceUser);
            context.firstNamed(NamedCause.NOTIFIER, User.class).ifPresent(blockEvent::setSourceUser);
        }

        @Override
        public void appendExplosionContext(PhaseContext explosionContext, PhaseContext context) {
            context.getOwner().ifPresent(explosionContext::owner);
            context.getNotifier().ifPresent(explosionContext::notifier);
            final TileEntity tickingTile = context.getSource(TileEntity.class)
                    .orElseThrow(TrackingUtil.throwWithContext("Expected to be processing over a ticking TileEntity!", context));
            explosionContext.add(NamedCause.source(tickingTile));
        }

        @Override
        public String toString() {
            return "TileEntityTickPhase";
        }
    }

    private static class BlockEventTickPhaseState extends TickPhaseState {

        BlockEventTickPhaseState() {
        }

        @Override
        public void associateNeighborBlockNotifier(PhaseContext context, @Nullable BlockPos sourcePos, Block block, BlockPos notifyPos,
                WorldServer minecraftWorld, PlayerTracker.Type notifier) {
            context.getSource(BlockSnapshot.class).ifPresent(snapshot -> {
                final Location<World> location = snapshot.getLocation().get();
                TrackingUtil.getNotifierOrOwnerFromBlock(location)
                        .ifPresent(user -> {
                            final IMixinChunk mixinChunk = (IMixinChunk) minecraftWorld.getChunkFromBlockCoords(notifyPos);
                            mixinChunk.addTrackedBlockPosition(block, notifyPos, user, PlayerTracker.Type.NOTIFIER);
                        });
            });
        }

        @Override
        public void handleBlockChangeWithUser(@Nullable BlockChange blockChange, WorldServer minecraftWorld, Transaction<BlockSnapshot> snapshotTransaction, PhaseContext context) {
            final Location<World> location = Stream.<Supplier<Optional<Location<World>>>>
                    of(
                    () -> context.getSource(BlockSnapshot.class).map(snapshot -> snapshot.getLocation().get()),
                    () -> context.getSource(TileEntity.class).map(Locatable::getLocation),
                    () -> context.getSource(IMixinBlockEventData.class).map(data ->
                            new Location<>((World) minecraftWorld, VecHelper.toVector3d(data.getEventBlockPosition())))
            )
                    .map(Supplier::get)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst()
                    .orElseThrow(TrackingUtil.throwWithContext("Expected to be throwing a block event for a tile entity or a snapshot but got none!",
                            context));
            final Vector3d position = location.getPosition();
            final BlockPos sourcePos = VecHelper.toBlockPos(position);
            final Block block = (Block) snapshotTransaction.getOriginal().getState().getType();
            final Location<World> changedLocation = snapshotTransaction.getOriginal().getLocation().get();
            final Vector3d changedPosition = changedLocation.getPosition();
            final BlockPos changedBlockPos = VecHelper.toBlockPos(changedPosition);
            final IMixinChunk changedMixinChunk = (IMixinChunk) ((WorldServer) changedLocation.getExtent()).getChunkFromBlockCoords(changedBlockPos);
            TrackingUtil.getNotifierOrOwnerFromBlock(changedLocation)
                    .ifPresent(user -> changedMixinChunk.addTrackedBlockPosition(block, changedBlockPos, user, PlayerTracker.Type.NOTIFIER));
        }

        @Override
        public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
            final Optional<User> notifier = phaseContext.getNotifier();
            final Optional<User> owner = phaseContext.getOwner();
            final User entityCreator = notifier.orElseGet(() -> owner.orElse(null));
            phaseContext.getCapturedBlockSupplier()
                    .ifPresentAndNotEmpty(blockSnapshots -> TrackingUtil.processBlockCaptures(blockSnapshots, causeTracker, this, phaseContext));
            phaseContext.getCapturedEntitySupplier()
                    .ifPresentAndNotEmpty(entities -> {
                        final Cause cause = Cause.source(InternalSpawnTypes.SpawnCauses.CUSTOM_SPAWN)
                                .build();
                        final SpawnEntityEvent
                                spawnEntityEvent =
                                SpongeEventFactory.createSpawnEntityEvent(cause, entities, causeTracker.getWorld());
                        SpongeImpl.postEvent(spawnEntityEvent);
                        for (Entity entity : spawnEntityEvent.getEntities()) {
                            if (entityCreator != null) {
                                EntityUtil.toMixin(entity).setCreator(entityCreator.getUniqueId());
                            }
                            causeTracker.getMixinWorld().forceSpawnEntity(entity);
                        }
                    });
            phaseContext.getCapturedItemsSupplier()
                    .ifPresentAndNotEmpty(items -> {
                        final Cause cause = Cause.source(InternalSpawnTypes.SpawnCauses.CUSTOM_SPAWN)
                                .build();
                        final ArrayList<Entity> capturedEntities = new ArrayList<>();
                        for (EntityItem entity : items) {
                            capturedEntities.add(EntityUtil.fromNative(entity));
                        }
                        final SpawnEntityEvent
                                spawnEntityEvent =
                                SpongeEventFactory.createSpawnEntityEvent(cause, capturedEntities, causeTracker.getWorld());
                        SpongeImpl.postEvent(spawnEntityEvent);
                        for (Entity entity : spawnEntityEvent.getEntities()) {
                            if (entityCreator != null) {
                                EntityUtil.toMixin(entity).setCreator(entityCreator.getUniqueId());
                            }
                            causeTracker.getMixinWorld().forceSpawnEntity(entity);
                        }
                    });
        }

        @Override
        public void associateAdditionalBlockChangeCauses(PhaseContext context, Cause.Builder builder, CauseTracker causeTracker) {
            final Optional<BlockSnapshot> blockSnapshot = context.getSource(BlockSnapshot.class);
            final Optional<TileEntity> tileEntity = context.getSource(TileEntity.class);
            if (blockSnapshot.isPresent()) {
                builder.named(NamedCause.notifier(blockSnapshot.get()));
            } else if (tileEntity.isPresent()) {
                builder.named(NamedCause.notifier(tileEntity.get()));
            }

        }

        @Override
        public String toString() {
            return "BlockEventTickPhase";
        }
    }

    private static class PlayerTickPhaseState extends TickPhaseState {

        PlayerTickPhaseState() {
        }

        @Override
        public void associateBlockEventNotifier(PhaseContext context, CauseTracker causeTracker, BlockPos pos, IMixinBlockEventData blockEvent) {
            blockEvent.setSourceUser(context.getSource(Player.class).get());
        }

        @Override
        public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
            final Player player = phaseContext.getSource(Player.class)
                    .orElseThrow(TrackingUtil.throwWithContext("Not ticking on a Player!", phaseContext));
            phaseContext.getCapturedEntitySupplier().ifPresentAndNotEmpty(entities -> {
                final Cause.Builder builder = Cause.source(EntitySpawnCause.builder()
                        .entity(player)
                        .type(InternalSpawnTypes.PASSIVE)
                        .build());
                final SpawnEntityEvent
                        spawnEntityEvent =
                        SpongeEventFactory.createSpawnEntityEvent(builder.build(), entities, causeTracker.getWorld());
                SpongeImpl.postEvent(spawnEntityEvent);
                for (Entity entity : spawnEntityEvent.getEntities()) {
                    EntityUtil.toMixin(entity).setCreator(player.getUniqueId());
                    causeTracker.getMixinWorld().forceSpawnEntity(entity);
                }
            });
            phaseContext.getCapturedItemsSupplier().ifPresentAndNotEmpty(entities -> {
                final Cause.Builder builder = Cause.source(EntitySpawnCause.builder()
                        .entity(player)
                        .type(InternalSpawnTypes.DROPPED_ITEM)
                        .build());
                final ArrayList<Entity> capturedEntities = new ArrayList<>();
                for (EntityItem entity : entities) {
                    capturedEntities.add(EntityUtil.fromNative(entity));
                }

                final SpawnEntityEvent
                        spawnEntityEvent =
                        SpongeEventFactory.createSpawnEntityEvent(builder.build(), capturedEntities, causeTracker.getWorld());
                SpongeImpl.postEvent(spawnEntityEvent);
                for (Entity entity : spawnEntityEvent.getEntities()) {
                    EntityUtil.toMixin(entity).setCreator(player.getUniqueId());
                    causeTracker.getMixinWorld().forceSpawnEntity(entity);
                }
            });
            phaseContext.getCapturedBlockSupplier().ifPresentAndNotEmpty(blockSnapshots -> {
                TrackingUtil.processBlockCaptures(blockSnapshots, causeTracker, this, phaseContext);
            });
        }

        @Override
        public void associateAdditionalBlockChangeCauses(PhaseContext context, Cause.Builder builder, CauseTracker causeTracker) {
            builder.named(NamedCause.OWNER, context.getSource(Player.class).get());
        }

        @Override
        public void appendExplosionContext(PhaseContext explosionContext, PhaseContext context) {
            final Player player = context.getSource(Player.class)
                    .orElseThrow(TrackingUtil.throwWithContext("Expected to be processing over a ticking TileEntity!", context));
            explosionContext.owner(player);
            explosionContext.notifier(player);
            explosionContext.add(NamedCause.source(player));
        }

        @Override
        public String toString() {
            return "PlayerTickPhase";
        }
    }

    private static class WeatherTickPhaseState extends TickPhaseState {

        WeatherTickPhaseState() {
        }

        @Override
        public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
            phaseContext.getCapturedEntitySupplier().ifPresentAndNotEmpty(entities -> {
                final Cause.Builder builder = Cause.source(SpawnCause.builder()
                        .type(InternalSpawnTypes.WEATHER)
                        .build());
                final SpawnEntityEvent
                        spawnEntityEvent =
                        SpongeEventFactory.createSpawnEntityEvent(builder.build(), entities, causeTracker.getWorld());
                SpongeImpl.postEvent(spawnEntityEvent);
                for (Entity entity : spawnEntityEvent.getEntities()) {
                    causeTracker.getMixinWorld().forceSpawnEntity(entity);
                }
            });
            phaseContext.getCapturedItemsSupplier().ifPresentAndNotEmpty(entities -> {
                final Cause.Builder builder = Cause.source(SpawnCause.builder()
                        .type(InternalSpawnTypes.WEATHER)
                        .build());
                final ArrayList<Entity> capturedEntities = new ArrayList<>();
                for (EntityItem entity : entities) {
                    capturedEntities.add(EntityUtil.fromNative(entity));
                }

                final SpawnEntityEvent
                        spawnEntityEvent =
                        SpongeEventFactory.createSpawnEntityEvent(builder.build(), capturedEntities, causeTracker.getWorld());
                SpongeImpl.postEvent(spawnEntityEvent);
                for (Entity entity : spawnEntityEvent.getEntities()) {
                    causeTracker.getMixinWorld().forceSpawnEntity(entity);
                }
            });
            phaseContext.getCapturedBlockSupplier().ifPresentAndNotEmpty(blockSnapshots -> {
                TrackingUtil.processBlockCaptures(blockSnapshots, causeTracker, this, phaseContext);
            });
        }
        @Override
        public void associateAdditionalBlockChangeCauses(PhaseContext context, Cause.Builder builder, CauseTracker causeTracker) {

        }

        @Override
        public String toString() {
            return "WeatherTickPhase";
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void unwind(CauseTracker causeTracker, IPhaseState state, PhaseContext context) {
        ((TickPhaseState) state).processPostTick(causeTracker, context);
    }

    TickPhase(TrackingPhase parent) {
        super(parent);
    }

    @Override
    public TickPhase addChild(TrackingPhase child) {
        super.addChild(child);
        return this;
    }

    @Override
    public boolean spawnEntityOrCapture(IPhaseState phaseState, PhaseContext context, Entity entity, int chunkX, int chunkZ) {
        return context.getCapturedEntities().add(entity);
    }

    @Override
    protected void processPostEntitySpawns(CauseTracker causeTracker, IPhaseState unwindingState, PhaseContext phaseContext,
            ArrayList<Entity> entities) {
        ((TickPhaseState) unwindingState).processPostSpawns(causeTracker, phaseContext, entities);

    }

    @Override
    public boolean requiresBlockCapturing(IPhaseState currentState) {
        return true;
    }

    @Override
    public void associateAdditionalCauses(IPhaseState state, PhaseContext context, Cause.Builder builder, CauseTracker causeTracker) {
    }

    @Override
    public void addNotifierToBlockEvent(IPhaseState phaseState, PhaseContext context, CauseTracker causeTracker, BlockPos pos, IMixinBlockEventData blockEvent) {
        ((TickPhaseState) phaseState).associateBlockEventNotifier(context, causeTracker, pos, blockEvent);
    }

    @Override
    public void appendNotifierPreBlockTick(CauseTracker causeTracker, BlockPos pos, IPhaseState currentState, PhaseContext context,
            PhaseContext newContext) {
        if (currentState == Tick.BLOCK || currentState == Tick.RANDOM_BLOCK) {

        }
    }

    @Override
    public void associateNeighborStateNotifier(IPhaseState state, PhaseContext context, @Nullable BlockPos sourcePos, Block block, BlockPos notifyPos,
            WorldServer minecraftWorld, PlayerTracker.Type notifier) {
        ((TickPhaseState) state).associateNeighborBlockNotifier(context, sourcePos, block, notifyPos, minecraftWorld, notifier);
    }

    @Override
    public Cause generateTeleportCause(IPhaseState state, PhaseContext context) {
        return ((TickPhaseState) state).generateTeleportCause(context);
    }

    @Override
    public void appendPreBlockProtectedCheck(Cause.Builder builder, IPhaseState phaseState, PhaseContext context, CauseTracker causeTracker) {
        context.getSource(Player.class).ifPresent(player -> builder.named(NamedCause.notifier(player)));
    }

    @Override
    public boolean isTicking(IPhaseState state) {
        return true;
    }

    @Override
    public boolean alreadyCapturingItemSpawns(IPhaseState currentState) {
        return true;
    }

    @Override
    public void appendContextPreExplosion(PhaseContext phaseContext, PhaseData currentPhaseData) {
        ((TickPhaseState) currentPhaseData.state).appendExplosionContext(phaseContext, currentPhaseData.context);
    }
}
