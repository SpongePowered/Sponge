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
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
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
import org.spongepowered.api.util.Identifiable;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.EventConsumer;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.function.EntityListConsumer;
import org.spongepowered.common.event.tracking.phase.function.GeneralFunctions;
import org.spongepowered.common.event.tracking.phase.util.PhaseUtil;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.block.IMixinBlockEventData;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.BlockChange;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

public final class WorldPhase extends TrackingPhase {

    public enum State implements IPhaseState {
        CHUNK_LOADING,
        WORLD_SPAWNER_SPAWNING() {

        },
        POPULATOR_RUNNING(BlockPhase.State.BLOCK_DECAY, BlockPhase.State.BLOCK_DROP_ITEMS, BlockPhase.State.RESTORING_BLOCKS, State.WORLD_SPAWNER_SPAWNING, GeneralPhase.Post.UNWINDING) {
            @Override
            public boolean canSwitchTo(IPhaseState state) { // Because populators are possibly re-entrant due to mods
                return super.canSwitchTo(state) || state == POPULATOR_RUNNING;
            }
        },
        TERRAIN_GENERATION(BlockPhase.State.BLOCK_DECAY, BlockPhase.State.BLOCK_DROP_ITEMS, BlockPhase.State.RESTORING_BLOCKS, State.POPULATOR_RUNNING, State.WORLD_SPAWNER_SPAWNING,
                GeneralPhase.Post.UNWINDING);

        private final Set<IPhaseState> compatibleStates;

        State() {
            this(ImmutableSet.of());
        }

        State(ImmutableSet<IPhaseState> states) {
            this.compatibleStates = states;
        }

        State(IPhaseState... states) {
            this(ImmutableSet.copyOf(states));
        }


        @Override
        public WorldPhase getPhase() {
            return TrackingPhases.WORLD;
        }

        @Override
        public boolean canSwitchTo(IPhaseState state) {
            return this.compatibleStates.contains(state);
        }

        @Override
        public boolean isExpectedForReEntrance() {
            return true;
        }

        public boolean captureEntitySpawn(IPhaseState phaseState, PhaseContext context, Entity entity, int chunkX, int chunkZ) {
            final net.minecraft.entity.Entity minecraftEntity = EntityUtil.toNative(entity);
            if (minecraftEntity instanceof EntityItem) {
                return context.getCapturedItems().add((EntityItem) minecraftEntity);
            } else {
                return context.getCapturedEntities().add(entity);
            }
        }


    }

    public enum Tick implements ITickingState {
        ENTITY() {
            @Override
            public void assignEntityCreator(PhaseContext context, Entity entity) {
                final Entity tickingEntity = context.firstNamed(NamedCause.SOURCE, Entity.class)
                        .orElseThrow(PhaseUtil.throwWithContext("Not ticking on an Entity!", context));
                final IMixinEntity mixinEntity = EntityUtil.toMixin(tickingEntity);
                Stream.<Supplier<Optional<UUID>>>of(
                        () -> mixinEntity.getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_NOTIFIER).map(Identifiable::getUniqueId),
                        () -> mixinEntity.getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR).map(Identifiable::getUniqueId)
                )
                        .map(Supplier::get)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .findFirst()
                        .ifPresent(uuid -> EntityUtil.toMixin(entity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, uuid));

            }

            @SuppressWarnings("unchecked")
            @Override
            public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
                final Entity tickingEntity = phaseContext.firstNamed(NamedCause.SOURCE, Entity.class)
                        .orElseThrow(PhaseUtil.throwWithContext("Not ticking on an Entity!", phaseContext));
                phaseContext.getCapturedEntitySupplier()
                        .ifPresentAndNotEmpty(entities -> {
                            final Cause.Builder builder = Cause.source(EntitySpawnCause.builder()
                                    .entity(tickingEntity)
                                    .type(InternalSpawnTypes.PASSIVE)
                                    .build());
                            EntityUtil.toMixin(tickingEntity).getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_NOTIFIER)
                                    .ifPresent(creator -> builder.named(NamedCause.notifier(creator)));
                            EntityUtil.toMixin(tickingEntity).getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR)
                                    .ifPresent(creator -> builder.named(NamedCause.owner(creator)));
                            EventConsumer
                                    .event(SpongeEventFactory.createSpawnEntityEvent(builder.build(), entities, causeTracker.getWorld()))
                                    .nonCancelled(event -> {
                                        for (Entity entity : event.getEntities()) {
                                            Stream.<Supplier<Optional<UUID>>>of(
                                                    () -> EntityUtil.toMixin(tickingEntity)
                                                            .getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_NOTIFIER)
                                                            .map(Identifiable::getUniqueId),
                                                    () -> EntityUtil.toMixin(tickingEntity)
                                                            .getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR)
                                                            .map(Identifiable::getUniqueId)
                                            )
                                                    .map(Supplier::get)
                                                    .filter(Optional::isPresent)
                                                    .map(Optional::get)
                                                    .findFirst()
                                                    .ifPresent(uuid ->
                                                            EntityUtil.toMixin(entity)
                                                                    .trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, uuid)
                                                    );
                                            causeTracker.getMixinWorld().forceSpawnEntity(entity);
                                        }
                                    })
                                    .process();
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
                            EntityUtil.toMixin(tickingEntity).getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_NOTIFIER)
                                    .ifPresent(creator -> builder.named(NamedCause.notifier(creator)));
                            EntityUtil.toMixin(tickingEntity).getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR)
                                    .ifPresent(creator -> builder.named(NamedCause.owner(creator)));
                            EventConsumer.event(SpongeEventFactory
                                    .createDropItemEventCustom(builder.build(), capturedEntities, causeTracker.getWorld()))
                                    .nonCancelled(event -> {
                                        for (Entity entity : event.getEntities()) {
                                            TrackingUtil.associateEntityCreator(phaseContext, EntityUtil.toNative(entity),
                                                    causeTracker.getMinecraftWorld());
                                            causeTracker.getMixinWorld().forceSpawnEntity(entity);
                                        }
                                    })
                                    .process();
                        });
                phaseContext.getCapturedBlockSupplier()
                        .ifPresentAndNotEmpty(blockSnapshots -> GeneralFunctions.processBlockCaptures(blockSnapshots, causeTracker, this, phaseContext));

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
                final Entity entity = context.firstNamed(NamedCause.SOURCE, Entity.class)
                                .orElseThrow(PhaseUtil.throwWithContext("Expected to be ticking an entity!", context));
                return Cause
                        .source(EntityTeleportCause.builder()
                                .entity(entity)
                                .type(TeleportTypes.ENTITY_TELEPORT)
                                .build()
                        )
                        .build();
            }

            @Override
            public void handleBlockChangeWithUser(@Nullable BlockChange blockChange, WorldServer minecraftWorld, Transaction<BlockSnapshot> snapshotTransaction,
                    PhaseContext context) {
                GeneralFunctions.processUserBreakage(blockChange, minecraftWorld, snapshotTransaction, context.firstNamed(NamedCause.SOURCE, Entity.class).get());
            }


            @Override
            public void associateAdditionalBlockChangeCauses(PhaseContext context, Cause.Builder builder, CauseTracker causeTracker) {
                final Entity tickingEntity = context.firstNamed(NamedCause.SOURCE, Entity.class)
                        .orElseThrow(PhaseUtil.throwWithContext("Not ticking on an Entity!", context));
                builder.named(NamedCause.owner(tickingEntity));
                final IMixinEntity mixinTickingEntity = EntityUtil.toMixin(tickingEntity);
                mixinTickingEntity.getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_NOTIFIER).ifPresent(user -> builder.named(NamedCause.notifier(user)));
            }
        },
        DIMENSION() {
            @Override
            public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
                phaseContext.getCapturedBlockSupplier()
                        .ifPresentAndNotEmpty(blockSnapshots -> {
                            GeneralFunctions.processBlockCaptures(blockSnapshots, causeTracker, this, phaseContext);
                        });

                phaseContext.getCapturedEntitySupplier()
                        .ifPresentAndNotEmpty(entities -> {
                            // TODO the entity spawn causes are not likely valid, need to investigate further.
                            final Cause cause = Cause.source(SpawnCause.builder()
                                    .type(InternalSpawnTypes.PLACEMENT)
                                    .build())
                                    .build();
                            EventConsumer.event(SpongeEventFactory.createSpawnEntityEvent(cause, entities, causeTracker.getWorld()))
                                    .nonCancelled(event -> {
                                        for (Entity entity : event.getEntities()) {
                                            causeTracker.getMixinWorld().forceSpawnEntity(entity);
                                        }
                                    })
                                    .process();
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
                            EventConsumer.event(SpongeEventFactory.createSpawnEntityEvent(cause, capturedEntities, causeTracker.getWorld()))
                                    .nonCancelled(event -> {
                                        for (Entity entity : event.getEntities()) {
                                            causeTracker.getMixinWorld().forceSpawnEntity(entity);
                                        }
                                    })
                                    .process();
                        });
            }
            @Override
            public void associateAdditionalBlockChangeCauses(PhaseContext context, Cause.Builder builder, CauseTracker causeTracker) {

            }
        },
        TILE_ENTITY() {

            @Override
            public void associateNeighborBlockNotifier(PhaseContext context, @Nullable BlockPos sourcePos, Block block, BlockPos notifyPos,
                    WorldServer minecraftWorld, PlayerTracker.Type notifier) {
                context.firstNamed(NamedCause.SOURCE, TileEntity.class).ifPresent(tileEntity -> {
                    final Vector3d position = tileEntity.getLocation().getPosition();
                    final BlockPos blockPos = VecHelper.toBlockPos(position);
                    final IMixinChunk mixinChunk = (IMixinChunk) minecraftWorld.getChunkFromBlockCoords(blockPos);
                    Stream.<Supplier<Optional<User>>>of(() -> mixinChunk.getBlockNotifier(blockPos), () -> mixinChunk.getBlockOwner(blockPos))
                            .map(Supplier::get)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .findFirst()
                            .ifPresent(user -> {
                                mixinChunk.addTrackedBlockPosition(block, notifyPos, user, PlayerTracker.Type.NOTIFIER);
                            });
                });
            }

            @Override
            public void handleBlockChangeWithUser(@Nullable BlockChange blockChange, WorldServer minecraftWorld, Transaction<BlockSnapshot> snapshotTransaction, PhaseContext context) {
                final TileEntity tileEntity = context.firstNamed(NamedCause.SOURCE, TileEntity.class)
                        .orElseThrow(PhaseUtil.throwWithContext("Expected to be ticking over a tile entity, but no tile entity found!", context));
                final Location<World> location = tileEntity.getLocation();
                final Vector3d position = location.getPosition();
                final BlockPos sourcePos = VecHelper.toBlockPos(position);
                final IMixinChunk sourceChunk = (IMixinChunk) ((WorldServer) location.getExtent()).getChunkFromBlockCoords(sourcePos);
                final Block changedBlock = (Block) snapshotTransaction.getOriginal().getState().getType();
                final Location<World> changedLocation = snapshotTransaction.getOriginal().getLocation().get();
                final Vector3d changedPosition = changedLocation.getPosition();
                final BlockPos changedBlockPos = VecHelper.toBlockPos(changedPosition);
                final IMixinChunk changedMixinChunk = (IMixinChunk) ((WorldServer) changedLocation.getExtent()).getChunkFromBlockCoords(changedBlockPos);
                Stream.<Supplier<Optional<User>>>of(() -> sourceChunk.getBlockNotifier(sourcePos), () -> sourceChunk.getBlockOwner(sourcePos))
                        .map(Supplier::get)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .findFirst()
                        .ifPresent(user -> changedMixinChunk.addTrackedBlockPosition(changedBlock, changedBlockPos, user, PlayerTracker.Type.NOTIFIER));
            }

            @Override
            public void assignEntityCreator(PhaseContext context, Entity entity) {
                final TileEntity tickingTile = context.firstNamed(NamedCause.SOURCE, TileEntity.class)
                        .orElseThrow(PhaseUtil.throwWithContext("Not ticking on a TileEntity!", context));
                final Location<World> location = tickingTile.getLocation();
                final Vector3d position = location.getPosition();
                final BlockPos blockPos = VecHelper.toBlockPos(position);
                final IMixinChunk mixinChunk = (IMixinChunk) ((WorldServer) location.getExtent()).getChunkFromBlockCoords(blockPos);
                Stream.<Supplier<Optional<User>>>of(() -> mixinChunk.getBlockNotifier(blockPos), () -> mixinChunk.getBlockOwner(blockPos))
                        .map(Supplier::get)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .findFirst()
                        .ifPresent(user -> EntityUtil.toMixin(entity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, user.getUniqueId()));
            }

            @Override
            public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
                final TileEntity tickingTile = phaseContext.firstNamed(NamedCause.SOURCE, TileEntity.class)
                        .orElseThrow(PhaseUtil.throwWithContext("Not ticking on a TileEntity!", phaseContext));
                phaseContext.getCapturedBlockSupplier()
                        .ifPresentAndNotEmpty(blockSnapshots -> {
                            GeneralFunctions.processBlockCaptures(blockSnapshots, causeTracker, this, phaseContext);
                        });

                phaseContext.getCapturedEntitySupplier()
                        .ifPresentAndNotEmpty(entities -> {
                            // TODO the entity spawn causes are not likely valid, need to investigate further.
                            final Cause cause = Cause.source(BlockSpawnCause.builder()
                                    .block(tickingTile.getLocation().createSnapshot())
                                    .type(InternalSpawnTypes.PLACEMENT)
                                    .build())
                                    .build();
                            EventConsumer.event(SpongeEventFactory.createSpawnEntityEvent(cause, entities, causeTracker.getWorld()))
                                    .nonCancelled(event -> {
                                        for (Entity entity : event.getEntities()) {
                                            TrackingUtil.associateEntityCreator(phaseContext, EntityUtil.toNative(entity), causeTracker.getMinecraftWorld());
                                            causeTracker.getMixinWorld().forceSpawnEntity(entity);
                                        }
                                    })
                                    .process();
                        });
                phaseContext.getCapturedItemsSupplier()
                        .ifPresentAndNotEmpty(entities -> {
                            final Cause cause = Cause.source(BlockSpawnCause.builder()
                                    .block(tickingTile.getLocation().createSnapshot())
                                    .type(InternalSpawnTypes.PLACEMENT)
                                    .build())
                                    .build();
                            final ArrayList<Entity> capturedEntities = new ArrayList<>();
                            for (EntityItem entity : entities) {
                                capturedEntities.add(EntityUtil.fromNative(entity));
                            }
                            EventConsumer.event(SpongeEventFactory.createSpawnEntityEvent(cause, capturedEntities, causeTracker.getWorld()))
                                    .nonCancelled(event -> {
                                        for (Entity entity : event.getEntities()) {
                                            TrackingUtil.associateEntityCreator(phaseContext, EntityUtil.toNative(entity),
                                                    causeTracker.getMinecraftWorld());
                                            causeTracker.getMixinWorld().forceSpawnEntity(entity);
                                        }
                                    })
                                    .process();
                        });
            }

            @Override
            public void associateAdditionalBlockChangeCauses(PhaseContext context, Cause.Builder builder, CauseTracker causeTracker) {
                final TileEntity tickingTile = context.firstNamed(NamedCause.SOURCE, TileEntity.class)
                        .orElseThrow(PhaseUtil.throwWithContext("Not ticking on a TileEntity!", context));
                builder.named(NamedCause.notifier(tickingTile));
            }

            @Override
            public void associateBlockEventNotifier(PhaseContext context, CauseTracker causeTracker, BlockPos pos, IMixinBlockEventData blockEvent) {
                final TileEntity tickingTile = context.firstNamed(NamedCause.SOURCE, TileEntity.class)
                        .orElseThrow(PhaseUtil.throwWithContext("Expected to be ticking a block, but found none!", context));
                blockEvent.setCurrentTickTileEntity(tickingTile);
                context.firstNamed(NamedCause.NOTIFIER, User.class).ifPresent(blockEvent::setSourceUser);
                TrackingUtil.trackTargetBlockFromSource(causeTracker, tickingTile, ((net.minecraft.tileentity.TileEntity) tickingTile).getPos(),
                        blockEvent.getEventBlock(), blockEvent.getEventBlockPosition(), PlayerTracker.Type.NOTIFIER);
            }
        },
        BLOCK() {

            @Override
            public void associateNeighborBlockNotifier(PhaseContext context, @Nullable BlockPos sourcePos, Block block, BlockPos notifyPos,
                    WorldServer minecraftWorld, PlayerTracker.Type notifier) {
                context.firstNamed(NamedCause.SOURCE, BlockSnapshot.class).ifPresent(snapshot -> {
                    final Vector3d position = snapshot.getLocation().get().getPosition();
                    final BlockPos blockPos = VecHelper.toBlockPos(position);
                    final IMixinChunk mixinChunk = (IMixinChunk) minecraftWorld.getChunkFromBlockCoords(blockPos);
                    Stream.<Supplier<Optional<User>>>of(() -> mixinChunk.getBlockNotifier(blockPos), () -> mixinChunk.getBlockOwner(blockPos))
                            .map(Supplier::get)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .findFirst()
                            .ifPresent(user -> {
                                mixinChunk.addTrackedBlockPosition(block, notifyPos, user, PlayerTracker.Type.NOTIFIER);
                            });
                });
            }

            @Override
            public void handleBlockChangeWithUser(@Nullable BlockChange blockChange, WorldServer minecraftWorld, Transaction<BlockSnapshot> snapshotTransaction, PhaseContext context) {
                final BlockSnapshot snapshot = context.firstNamed(NamedCause.SOURCE, BlockSnapshot.class)
                        .orElseThrow(PhaseUtil.throwWithContext("Expected to be ticking over a tile entity, but no tile entity found!", context));
                final Location<World> location = snapshot.getLocation().get();
                final Vector3d position = location.getPosition();
                final BlockPos sourcePos = VecHelper.toBlockPos(position);
                final IMixinChunk sourceChunk = (IMixinChunk) ((WorldServer) location.getExtent()).getChunkFromBlockCoords(sourcePos);
                final Block block = (Block) snapshotTransaction.getOriginal().getState().getType();
                final Location<World> changedLocation = snapshotTransaction.getOriginal().getLocation().get();
                final Vector3d changedPosition = changedLocation.getPosition();
                final BlockPos changedBlockPos = VecHelper.toBlockPos(changedPosition);
                final IMixinChunk changedMixinChunk = (IMixinChunk) ((WorldServer) changedLocation.getExtent()).getChunkFromBlockCoords(changedBlockPos);
                Stream.<Supplier<Optional<User>>>of(() -> sourceChunk.getBlockNotifier(sourcePos), () -> sourceChunk.getBlockOwner(sourcePos))
                        .map(Supplier::get)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .findFirst()
                        .ifPresent(user -> changedMixinChunk.addTrackedBlockPosition(block, changedBlockPos, user, PlayerTracker.Type.NOTIFIER));
            }


            @Override
            public void assignEntityCreator(PhaseContext context, Entity entity) {
                final BlockSnapshot tickingBlock = context.firstNamed(NamedCause.SOURCE, BlockSnapshot.class)
                        .orElseThrow(PhaseUtil.throwWithContext("Not ticking on a Block!", context));
                final Location<World> location = tickingBlock.getLocation().get();
                final Vector3d position = location.getPosition();
                final BlockPos blockPos = VecHelper.toBlockPos(position);
                final IMixinChunk mixinChunk = (IMixinChunk) ((WorldServer) location.getExtent()).getChunkFromBlockCoords(blockPos);
                Stream.<Supplier<Optional<User>>>of(() -> mixinChunk.getBlockNotifier(blockPos), () -> mixinChunk.getBlockOwner(blockPos))
                        .map(Supplier::get)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .findFirst()
                        .ifPresent(user -> EntityUtil.toMixin(entity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, user.getUniqueId()));
            }

            @Override
            public boolean canSwitchTo(IPhaseState state) {
                return super.canSwitchTo(state) || state == State.CHUNK_LOADING;
            }

            @Override
            public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
                final BlockSnapshot tickingBlock = phaseContext.firstNamed(NamedCause.SOURCE, BlockSnapshot.class)
                        .orElseThrow(PhaseUtil.throwWithContext("Not ticking on a Block!", phaseContext));
                final WorldServer minecraftWorld = causeTracker.getMinecraftWorld();
                final World world = causeTracker.getWorld();
                phaseContext.getCapturedBlockSupplier()
                        .ifPresentAndNotEmpty(blockSnapshots -> GeneralFunctions.processBlockCaptures(blockSnapshots, causeTracker, this, phaseContext));
                phaseContext.getCapturedEntitySupplier()
                        .ifPresentAndNotEmpty(entities -> {
                            final Cause cause = Cause.source(BlockSpawnCause.builder()
                                    .block(tickingBlock)
                                    .type(InternalSpawnTypes.PLACEMENT)
                                    .build())
                                    .build();
                            EventConsumer.event(SpongeEventFactory.createSpawnEntityEvent(cause, entities, world))
                                    .nonCancelled(event -> {
                                        for (Entity entity : event.getEntities()) {
                                            TrackingUtil.associateEntityCreator(phaseContext, EntityUtil.toNative(entity), minecraftWorld);
                                            causeTracker.getMixinWorld().forceSpawnEntity(entity);
                                        }
                                    })
                                    .process();
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
                            EventConsumer.event(SpongeEventFactory.createSpawnEntityEvent(cause, capturedEntities, causeTracker.getWorld()))
                                    .nonCancelled(event -> {
                                        for (Entity entity : event.getEntities()) {
                                            TrackingUtil.associateEntityCreator(phaseContext, EntityUtil.toNative(entity), minecraftWorld);
                                            causeTracker.getMixinWorld().forceSpawnEntity(entity);
                                        }
                                    })
                                    .process();
                        });
            }

            @Override
            public void associateAdditionalBlockChangeCauses(PhaseContext context, Cause.Builder builder, CauseTracker causeTracker) {
                final BlockSnapshot tickingBlock = context.firstNamed(NamedCause.SOURCE, BlockSnapshot.class)
                        .orElseThrow(PhaseUtil.throwWithContext("Not ticking on a Block!", context));
                builder.named(NamedCause.notifier(tickingBlock));
            }

            @Override
            public void associateBlockEventNotifier(PhaseContext context, CauseTracker causeTracker, BlockPos pos, IMixinBlockEventData blockEvent) {
                final BlockSnapshot tickingBlock = context.firstNamed(NamedCause.SOURCE, BlockSnapshot.class)
                        .orElseThrow(PhaseUtil.throwWithContext("Expected to be ticking a block, but found none!", context));
                blockEvent.setCurrentTickBlock(tickingBlock);
                context.firstNamed(NamedCause.NOTIFIER, User.class).ifPresent(blockEvent::setSourceUser);
                TrackingUtil.trackTargetBlockFromSource(causeTracker, tickingBlock, ((SpongeBlockSnapshot) tickingBlock).getBlockPos(), blockEvent.getEventBlock(),
                        blockEvent.getEventBlockPosition(), PlayerTracker.Type.NOTIFIER);
            }
        },
        BLOCK_EVENT() {
            @Override
            public void associateNeighborBlockNotifier(PhaseContext context, @Nullable BlockPos sourcePos, Block block, BlockPos notifyPos,
                    WorldServer minecraftWorld, PlayerTracker.Type notifier) {
                context.firstNamed(NamedCause.SOURCE, BlockSnapshot.class).ifPresent(snapshot -> {
                    final Vector3d position = snapshot.getLocation().get().getPosition();
                    final BlockPos blockPos = VecHelper.toBlockPos(position);
                    final IMixinChunk mixinChunk = (IMixinChunk) minecraftWorld.getChunkFromBlockCoords(blockPos);
                    Stream.<Supplier<Optional<User>>>of(() -> mixinChunk.getBlockNotifier(blockPos), () -> mixinChunk.getBlockOwner(blockPos))
                            .map(Supplier::get)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .findFirst()
                            .ifPresent(user -> {
                                mixinChunk.addTrackedBlockPosition(block, notifyPos, user, PlayerTracker.Type.NOTIFIER);
                            });
                });
            }

            @Override
            public void handleBlockChangeWithUser(@Nullable BlockChange blockChange, WorldServer minecraftWorld, Transaction<BlockSnapshot> snapshotTransaction, PhaseContext context) {
                final Location location = Stream.<Supplier<Optional<Location>>>
                        of(
                        () -> context.firstNamed(NamedCause.SOURCE, BlockSnapshot.class).map(snapshot -> snapshot.getLocation().get()),
                        () -> context.firstNamed(NamedCause.SOURCE, TileEntity.class).map(tile -> tile.getLocation()),
                        () -> context.firstNamed(NamedCause.SOURCE, IMixinBlockEventData.class).map(data ->
                                new Location<>((World) minecraftWorld, VecHelper.toVector3d(data.getEventBlockPosition())))
                )
                        .map(Supplier::get)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .findFirst()
                        .orElseThrow(PhaseUtil.throwWithContext("Expected to be throwing a block event for a tile entity or a snapshot but got none!",
                                        context));
                final Vector3d position = location.getPosition();
                final BlockPos sourcePos = VecHelper.toBlockPos(position);
                final IMixinChunk sourceChunk = (IMixinChunk) ((WorldServer) location.getExtent()).getChunkFromBlockCoords(sourcePos);
                final Block block = (Block) snapshotTransaction.getOriginal().getState().getType();
                final Location<World> changedLocation = snapshotTransaction.getOriginal().getLocation().get();
                final Vector3d changedPosition = changedLocation.getPosition();
                final BlockPos changedBlockPos = VecHelper.toBlockPos(changedPosition);
                final IMixinChunk changedMixinChunk = (IMixinChunk) ((WorldServer) changedLocation.getExtent()).getChunkFromBlockCoords(changedBlockPos);
                Stream.<Supplier<Optional<User>>>of(() -> sourceChunk.getBlockNotifier(sourcePos), () -> sourceChunk.getBlockOwner(sourcePos))
                        .map(Supplier::get)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .findFirst()
                        .ifPresent(user -> changedMixinChunk.addTrackedBlockPosition(block, changedBlockPos, user, PlayerTracker.Type.NOTIFIER));
            }

            @Override
            public void assignEntityCreator(PhaseContext context, Entity entity) {
                final BlockSnapshot tickingBlock = context.firstNamed(NamedCause.SOURCE, BlockSnapshot.class)
                        .orElseThrow(PhaseUtil.throwWithContext("Not ticking on a Block!", context));
                final Location<World> location = tickingBlock.getLocation().get();
                final Vector3d position = location.getPosition();
                final BlockPos blockPos = VecHelper.toBlockPos(position);
                final IMixinChunk mixinChunk = (IMixinChunk) ((WorldServer) location.getExtent()).getChunkFromBlockCoords(blockPos);
                Stream.<Supplier<Optional<User>>>of(() -> mixinChunk.getBlockNotifier(blockPos), () -> mixinChunk.getBlockOwner(blockPos))
                        .map(Supplier::get)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .findFirst()
                        .ifPresent(user -> EntityUtil.toMixin(entity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, user.getUniqueId()));
            }

            @Override
            public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
                final WorldServer minecraftWorld = causeTracker.getMinecraftWorld();
                final World world = causeTracker.getWorld();
                phaseContext.getCapturedBlockSupplier()
                        .ifPresentAndNotEmpty(blockSnapshots -> GeneralFunctions.processBlockCaptures(blockSnapshots, causeTracker, this, phaseContext));
                phaseContext.getCapturedEntitySupplier()
                        .ifPresentAndNotEmpty(entities -> {
                            final Cause cause = Cause.source(InternalSpawnTypes.CUSTOM_SPAWN)
                                    .build();
                            EventConsumer.event(SpongeEventFactory.createSpawnEntityEvent(cause, entities, world))
                                    .nonCancelled(event -> {
                                        for (Entity entity : event.getEntities()) {
                                            TrackingUtil.associateEntityCreator(phaseContext, EntityUtil.toNative(entity), minecraftWorld);
                                            causeTracker.getMixinWorld().forceSpawnEntity(entity);
                                        }
                                    })
                                    .process();
                        });
                phaseContext.getCapturedItemsSupplier()
                        .ifPresentAndNotEmpty(items -> {
                            final Cause cause = Cause.source(InternalSpawnTypes.CUSTOM_SPAWN)
                                    .build();
                            final ArrayList<Entity> capturedEntities = new ArrayList<>();
                            for (EntityItem entity : items) {
                                capturedEntities.add(EntityUtil.fromNative(entity));
                            }
                            EventConsumer.event(SpongeEventFactory.createSpawnEntityEvent(cause, capturedEntities, causeTracker.getWorld()))
                                    .nonCancelled(event -> {
                                        for (Entity entity : event.getEntities()) {
                                            TrackingUtil.associateEntityCreator(phaseContext, EntityUtil.toNative(entity), minecraftWorld);
                                            causeTracker.getMixinWorld().forceSpawnEntity(entity);
                                        }
                                    })
                                    .process();
                        });
            }

            @Override
            public void associateAdditionalBlockChangeCauses(PhaseContext context, Cause.Builder builder, CauseTracker causeTracker) {
                final Optional<BlockSnapshot> blockSnapshot = context.firstNamed(NamedCause.SOURCE, BlockSnapshot.class);
                final Optional<TileEntity> tileEntity = context.firstNamed(NamedCause.SOURCE, TileEntity.class);
                if (blockSnapshot.isPresent()) {
                    builder.named(NamedCause.notifier(blockSnapshot.get()));
                } else if (tileEntity.isPresent()) {
                    builder.named(NamedCause.notifier(tileEntity.get()));
                }

            }
        },
        RANDOM_BLOCK() {
            @Override
            public void associateNeighborBlockNotifier(PhaseContext context, @Nullable BlockPos sourcePos, Block block, BlockPos notifyPos,
                    WorldServer minecraftWorld, PlayerTracker.Type notifier) {
                context.firstNamed(NamedCause.SOURCE, BlockSnapshot.class).ifPresent(snapshot -> {
                    final Vector3d position = snapshot.getLocation().get().getPosition();
                    final BlockPos blockPos = VecHelper.toBlockPos(position);
                    final IMixinChunk mixinChunk = (IMixinChunk) minecraftWorld.getChunkFromBlockCoords(blockPos);
                    Stream.<Supplier<Optional<User>>>of(() -> mixinChunk.getBlockNotifier(blockPos), () -> mixinChunk.getBlockOwner(blockPos))
                            .map(Supplier::get)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .findFirst()
                            .ifPresent(user -> {
                                mixinChunk.addTrackedBlockPosition(block, notifyPos, user, PlayerTracker.Type.NOTIFIER);
                            });
                });
            }

            @Override
            public void handleBlockChangeWithUser(@Nullable BlockChange blockChange, WorldServer minecraftWorld, Transaction<BlockSnapshot> snapshotTransaction, PhaseContext context) {
                final BlockSnapshot snapshot = context.firstNamed(NamedCause.SOURCE, BlockSnapshot.class)
                        .orElseThrow(PhaseUtil.throwWithContext("Expected to be ticking over a tile entity, but no tile entity found!", context));
                final Location<World> location = snapshot.getLocation().get();
                final Vector3d position = location.getPosition();
                final BlockPos sourcePos = VecHelper.toBlockPos(position);
                final IMixinChunk sourceChunk = (IMixinChunk) ((WorldServer) location.getExtent()).getChunkFromBlockCoords(sourcePos);
                final Block block = (Block) snapshotTransaction.getOriginal().getState().getType();
                final Location<World> changedLocation = snapshotTransaction.getOriginal().getLocation().get();
                final Vector3d changedPosition = changedLocation.getPosition();
                final BlockPos changedBlockPos = VecHelper.toBlockPos(changedPosition);
                final IMixinChunk changedMixinChunk = (IMixinChunk) ((WorldServer) changedLocation.getExtent()).getChunkFromBlockCoords(changedBlockPos);
                Stream.<Supplier<Optional<User>>>of(() -> sourceChunk.getBlockNotifier(sourcePos), () -> sourceChunk.getBlockOwner(sourcePos))
                        .map(Supplier::get)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .findFirst()
                        .ifPresent(user -> changedMixinChunk.addTrackedBlockPosition(block, changedBlockPos, user, PlayerTracker.Type.NOTIFIER));
            }

            @Override
            public void assignEntityCreator(PhaseContext context, Entity entity) {
                final BlockSnapshot tickingBlock = context.firstNamed(NamedCause.SOURCE, BlockSnapshot.class)
                        .orElseThrow(PhaseUtil.throwWithContext("Not ticking on a Block!", context));
                final Location<World> location = tickingBlock.getLocation().get();
                final Vector3d position = location.getPosition();
                final BlockPos blockPos = VecHelper.toBlockPos(position);
                final IMixinChunk mixinChunk = (IMixinChunk) ((WorldServer) location.getExtent()).getChunkFromBlockCoords(blockPos);
                Stream.<Supplier<Optional<User>>>of(() -> mixinChunk.getBlockNotifier(blockPos), () -> mixinChunk.getBlockOwner(blockPos))
                        .map(Supplier::get)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .findFirst()
                        .ifPresent(user -> EntityUtil.toMixin(entity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, user.getUniqueId()));
            }

            @Override
            public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
                final BlockSnapshot tickingBlock = phaseContext.firstNamed(NamedCause.SOURCE, BlockSnapshot.class)
                        .orElseThrow(PhaseUtil.throwWithContext("Not ticking on a Block!", phaseContext));
                phaseContext.getCapturedBlockSupplier()
                        .ifPresentAndNotEmpty(blockSnapshots -> GeneralFunctions.processBlockCaptures(blockSnapshots, causeTracker, this, phaseContext));
                phaseContext.getCapturedEntitySupplier()
                        .ifPresentAndNotEmpty(entities -> {
                            final Cause cause = Cause.source(BlockSpawnCause.builder()
                                        .block(tickingBlock)
                                        .type(InternalSpawnTypes.PLACEMENT)
                                        .build())
                                    .build();
                            EventConsumer.event(SpongeEventFactory.createSpawnEntityEvent(cause, entities, causeTracker.getWorld()))
                                    .nonCancelled(event -> {
                                        for (Entity entity : event.getEntities()) {
                                            TrackingUtil.associateEntityCreator(phaseContext, EntityUtil.toNative(entity),
                                                    causeTracker.getMinecraftWorld());
                                            causeTracker.getMixinWorld().forceSpawnEntity(entity);
                                        }
                                    })
                                    .process();
                        });
            }

            @Override
            public void associateAdditionalBlockChangeCauses(PhaseContext context, Cause.Builder builder, CauseTracker causeTracker) {
                final BlockSnapshot tickingBlock = context.firstNamed(NamedCause.SOURCE, BlockSnapshot.class)
                        .orElseThrow(PhaseUtil.throwWithContext("Not ticking on a Block!", context));
                builder.named(NamedCause.notifier(tickingBlock));
            }

            @Override
            public void associateBlockEventNotifier(PhaseContext context, CauseTracker causeTracker, BlockPos pos, IMixinBlockEventData blockEvent) {
                final BlockSnapshot tickingBlock = context.firstNamed(NamedCause.SOURCE, BlockSnapshot.class)
                        .orElseThrow(PhaseUtil.throwWithContext("Expected to be ticking a block, but found none!", context));
                blockEvent.setCurrentTickBlock(tickingBlock);
                context.firstNamed(NamedCause.NOTIFIER, User.class).ifPresent(blockEvent::setSourceUser);
                TrackingUtil.trackTargetBlockFromSource(causeTracker, tickingBlock, ((SpongeBlockSnapshot) tickingBlock).getBlockPos(), blockEvent.getEventBlock(),
                        blockEvent.getEventBlockPosition(), PlayerTracker.Type.NOTIFIER);
            }
        },
        PLAYER() {
            @Override
            public void assignEntityCreator(PhaseContext context, Entity entity) {
                final Player player = context.firstNamed(NamedCause.SOURCE, Player.class)
                        .orElseThrow(PhaseUtil.throwWithContext("Not ticking a player!", context));
                EntityUtil.toMixin(entity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, player.getUniqueId());
            }

            @Override
            public void associateBlockEventNotifier(PhaseContext context, CauseTracker causeTracker, BlockPos pos, IMixinBlockEventData blockEvent) {
                blockEvent.setSourceUser(context.firstNamed(NamedCause.SOURCE, Player.class).get());
            }

            @Override
            public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
                final Entity tickingEntity = phaseContext.firstNamed(NamedCause.SOURCE, Entity.class)
                        .orElseThrow(PhaseUtil.throwWithContext("Not ticking on an Entity!", phaseContext));
                phaseContext.getCapturedEntitySupplier().ifPresentAndNotEmpty(entities -> {
                    final Cause.Builder builder = Cause.source(EntitySpawnCause.builder()
                            .entity(tickingEntity)
                            .type(InternalSpawnTypes.PASSIVE)
                            .build());
                    EventConsumer.event(SpongeEventFactory.createSpawnEntityEvent(builder.build(), entities, causeTracker.getWorld()))
                            .nonCancelled(event -> EntityListConsumer.FORCE_SPAWN.apply(event.getEntities(), causeTracker))
                            .process();
                });
                phaseContext.getCapturedItemsSupplier().ifPresentAndNotEmpty(entities -> {
                    final Cause.Builder builder = Cause.source(EntitySpawnCause.builder()
                            .entity(tickingEntity)
                            .type(InternalSpawnTypes.DROPPED_ITEM)
                            .build());
                    final ArrayList<Entity> capturedEntities = new ArrayList<>();
                    for (EntityItem entity : entities) {
                        capturedEntities.add(EntityUtil.fromNative(entity));
                    }

                    EventConsumer.event(SpongeEventFactory.createDropItemEventCustom(builder.build(), capturedEntities, causeTracker.getWorld()))
                            .nonCancelled(event -> EntityListConsumer.FORCE_SPAWN.apply(event.getEntities(), causeTracker))
                            .process();
                });
                phaseContext.getCapturedBlockSupplier().ifPresentAndNotEmpty(blockSnapshots -> {
                    GeneralFunctions.processBlockCaptures(blockSnapshots, causeTracker, this, phaseContext);
                });
            }

            @Override
            public void associateAdditionalBlockChangeCauses(PhaseContext context, Cause.Builder builder, CauseTracker causeTracker) {
                builder.named(NamedCause.OWNER, context.firstNamed(NamedCause.SOURCE, Player.class).get());
            }
        },
        WEATHER {

            @Override
            public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
                phaseContext.getCapturedEntitySupplier().ifPresentAndNotEmpty(entities -> {
                    final Cause.Builder builder = Cause.source(SpawnCause.builder()
                            .type(InternalSpawnTypes.WEATHER)
                            .build());
                    EventConsumer.event(SpongeEventFactory.createSpawnEntityEvent(builder.build(), entities, causeTracker.getWorld()))
                            .nonCancelled(event -> EntityListConsumer.FORCE_SPAWN.apply(event.getEntities(), causeTracker))
                            .process();
                });
                phaseContext.getCapturedItemsSupplier().ifPresentAndNotEmpty(entities -> {
                    final Cause.Builder builder = Cause.source(SpawnCause.builder()
                            .type(InternalSpawnTypes.WEATHER)
                            .build());
                    final ArrayList<Entity> capturedEntities = new ArrayList<>();
                    for (EntityItem entity : entities) {
                        capturedEntities.add(EntityUtil.fromNative(entity));
                    }

                    EventConsumer.event(SpongeEventFactory.createDropItemEventCustom(builder.build(), capturedEntities, causeTracker.getWorld()))
                            .nonCancelled(event -> EntityListConsumer.FORCE_SPAWN.apply(event.getEntities(), causeTracker))
                            .process();
                });
                phaseContext.getCapturedBlockSupplier().ifPresentAndNotEmpty(blockSnapshots -> {
                    GeneralFunctions.processBlockCaptures(blockSnapshots, causeTracker, this, phaseContext);
                });
            }
            @Override
            public void associateAdditionalBlockChangeCauses(PhaseContext context, Cause.Builder builder, CauseTracker causeTracker) {

            }
        }
        ;


        @Override
        public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {

        }

        @Override
        public Cause generateTeleportCause(PhaseContext context) {
            return Cause.of(NamedCause.source(TeleportCause.builder().type(TeleportTypes.UNKNOWN).build()));
        }

        @Override
        public TrackingPhase getPhase() {
            return TrackingPhases.WORLD;
        }

        @Override
        public boolean canSwitchTo(IPhaseState state) {
            return state instanceof BlockPhase.State || state instanceof EntityPhase.State || state == State.TERRAIN_GENERATION;
        }


        public abstract void associateAdditionalBlockChangeCauses(PhaseContext context, Cause.Builder builder, CauseTracker causeTracker);

        public void associateBlockEventNotifier(PhaseContext context, CauseTracker causeTracker, BlockPos pos, IMixinBlockEventData blockEvent) {

        }

        public void associateNeighborBlockNotifier(PhaseContext context, @Nullable BlockPos sourcePos, Block block, BlockPos notifyPos, WorldServer minecraftWorld, PlayerTracker.Type notifier) {

        }
    }

    @Override
    public void unwind(CauseTracker causeTracker, IPhaseState state, PhaseContext context) {
        if (state instanceof ITickingState) {
            ((ITickingState) state).processPostTick(causeTracker, context);
        } else if (state == State.TERRAIN_GENERATION) {
            final List<Entity> spawnedEntities = context.getCapturedEntitySupplier().orEmptyList();
            final List<Entity> spawnedItems = context.getCapturedItemsSupplier()
                    .orEmptyList()
                    .stream()
                    .map(EntityUtil::fromNative)
                    .collect(Collectors.toList());
            if (spawnedEntities.isEmpty() && spawnedItems.isEmpty()) {
                return;
            }
            if (!spawnedEntities.isEmpty()) {
                if (!spawnedItems.isEmpty()) { // We shouldn't separate the entities whatsoever.
                    spawnedEntities.addAll(spawnedItems);
                }
                final Cause cause = Cause.source(InternalSpawnTypes.WORLD_SPAWNER_CAUSE).named("World",  causeTracker.getWorld())
                        .build();
                EventConsumer.event(SpongeEventFactory.createSpawnEntityEventSpawner(cause, spawnedEntities, causeTracker.getWorld()))
                        .nonCancelled(event -> EntityListConsumer.FORCE_SPAWN.apply(event.getEntities(), causeTracker))
                        .process();
            }
        } else if (state == State.POPULATOR_RUNNING) {
            final PopulatorType runningGenerator = context.firstNamed(InternalNamedCauses.WorldGeneration.CAPTURED_POPULATOR, PopulatorType.class).orElse(null);
            final List<Entity> spawnedEntities = context.getCapturedEntitySupplier().orEmptyList();
            final List<Entity> spawnedItems = context.getCapturedItemsSupplier()
                    .orEmptyList()
                    .stream()
                    .map(EntityUtil::fromNative)
                    .collect(Collectors.toList());
            if (spawnedEntities.isEmpty() && spawnedItems.isEmpty()) {
                return;
            }
            if (!spawnedEntities.isEmpty()) {
                if (!spawnedItems.isEmpty()) { // We shouldn't separate the entities whatsoever.
                    spawnedEntities.addAll(spawnedItems);
                }
                final Cause.Builder cause = Cause.source(InternalSpawnTypes.WORLD_SPAWNER_CAUSE).named("World",  causeTracker.getWorld());
                if (runningGenerator != null) { // There are corner cases where a populator may not have a proper type.
                    cause.named(InternalNamedCauses.WorldGeneration.CAPTURED_POPULATOR, runningGenerator);
                }
                EventConsumer.event(SpongeEventFactory.createSpawnEntityEventSpawner(cause.build(), spawnedEntities, causeTracker.getWorld()))
                        .nonCancelled(event -> EntityListConsumer.FORCE_SPAWN.apply(event.getEntities(), causeTracker))
                        .process();
            }
            // Blocks do not matter one bit.
        } else if (state == State.WORLD_SPAWNER_SPAWNING) {
            final List<Entity> spawnedEntities = context.getCapturedEntitySupplier().orEmptyList();
            final List<Entity> spawnedItems = context.getCapturedItemsSupplier()
                    .orEmptyList()
                    .stream()
                    .map(EntityUtil::fromNative)
                    .collect(Collectors.toList());;
            if (spawnedEntities.isEmpty() && spawnedItems.isEmpty()) {
                return;
            }
            if (!spawnedEntities.isEmpty()) {
                if (!spawnedItems.isEmpty()) { // We shouldn't separate the entities whatsoever.
                    spawnedEntities.addAll(spawnedItems);
                }
                final Cause cause = Cause.source(InternalSpawnTypes.WORLD_SPAWNER_CAUSE).named("World", causeTracker.getWorld()).build();
                EventConsumer.event(SpongeEventFactory.createSpawnEntityEventSpawner(cause, spawnedEntities, causeTracker.getWorld()))
                        .nonCancelled(event -> EntityListConsumer.FORCE_SPAWN.apply(event.getEntities(), causeTracker))
                        .process();
            }

            context.getCapturedBlockSupplier()
                    .ifPresentAndNotEmpty(blockSnapshots -> GeneralFunctions.processBlockCaptures(blockSnapshots, causeTracker, state, context));
        }

    }

    WorldPhase(TrackingPhase parent) {
        super(parent);
    }

    @Override
    public WorldPhase addChild(TrackingPhase child) {
        super.addChild(child);
        return this;
    }

    @Override
    public boolean requiresBlockCapturing(IPhaseState currentState) {
        return currentState instanceof Tick;
    }

    @Override
    public boolean ignoresBlockEvent(IPhaseState phaseState) {
        return phaseState instanceof State;
    }


    @Override
    public boolean ignoresBlockUpdateTick(PhaseData phaseData) {
        return phaseData.getState() instanceof State && phaseData.getState() != State.WORLD_SPAWNER_SPAWNING;
    }

    @Override
    public void associateAdditionalCauses(IPhaseState state, PhaseContext context, Cause.Builder builder, CauseTracker causeTracker) {
        if (state instanceof Tick) {
            ((Tick) state).associateAdditionalBlockChangeCauses(context, builder, causeTracker);
        }
    }

    @Override
    public void associateNotifier(IPhaseState phaseState, PhaseContext context, CauseTracker causeTracker, BlockPos pos, IMixinBlockEventData blockEvent) {
        if (phaseState instanceof Tick) {
            ((Tick) phaseState).associateBlockEventNotifier(context, causeTracker, pos, blockEvent);
        }
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
        if (state instanceof Tick) {
            ((Tick) state).associateNeighborBlockNotifier(context, sourcePos, block, notifyPos, minecraftWorld, notifier);
        }
    }

    @Override
    public boolean spawnEntityOrCapture(IPhaseState phaseState, PhaseContext context, Entity entity, int chunkX, int chunkZ) {
        if (phaseState instanceof Tick) {
            return super.spawnEntityOrCapture(phaseState, context, entity, chunkX, chunkZ);
        }
        return ((State) phaseState).captureEntitySpawn(phaseState, context, entity, chunkX, chunkZ);
    }

    @Override
    protected void processPostEntitySpawns(CauseTracker causeTracker, IPhaseState unwindingState, ArrayList<Entity> entities) {
        super.processPostEntitySpawns(causeTracker, unwindingState, entities);
    }

    @Override
    public Cause generateTeleportCause(IPhaseState state, PhaseContext context) {
        return state instanceof ITickingState ? ((ITickingState) state).generateTeleportCause(context) : super.generateTeleportCause(state, context);
    }

    /**
     * A specialized state that signifies that it is a "master" state that
     * can have multiple state side effects, such as spawning other entities,
     * changing other blocks, calling other populators, etc.
     */
    interface ITickingState extends IPhaseState {

        void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext);

        Cause generateTeleportCause(PhaseContext context);

    }
}
