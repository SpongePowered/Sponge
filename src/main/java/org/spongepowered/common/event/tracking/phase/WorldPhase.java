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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.Humanoid;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.BlockSpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.entity.DisplaceEntityEvent;
import org.spongepowered.api.util.GuavaCollectors;
import org.spongepowered.api.util.Identifiable;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.EventConsumer;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.function.EntityListConsumer;
import org.spongepowered.common.event.tracking.phase.function.GeneralFunctions;
import org.spongepowered.common.event.tracking.phase.util.PhaseUtil;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.BlockChange;

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
        WORLD_SPAWNER_SPAWNING,
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

    }

    public enum Tick implements ITickingState {
        ENTITY() {
            @SuppressWarnings("unchecked")
            @Override
            public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
                final Entity tickingEntity = phaseContext.firstNamed(NamedCause.SOURCE, Entity.class)
                    .orElseThrow(PhaseUtil.throwWithContext("Not ticking on an Entity!", phaseContext));
                phaseContext.getCapturedEntitySupplier()
                        .orElseThrow(PhaseUtil.throwWithContext("Not capturing entity spawns!", phaseContext))
                        .ifPresentAndNotEmpty(entities -> {
                            final Cause.Builder builder = Cause.source(EntitySpawnCause.builder()
                                    .entity(tickingEntity)
                                    .type(InternalSpawnTypes.PASSIVE)
                                    .build());
                            EntityUtil.toMixin(tickingEntity).getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_NOTIFIER)
                                    .ifPresent(creator -> builder.named(NamedCause.notifier(creator)));
                            EntityUtil.toMixin(tickingEntity).getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR)
                                    .ifPresent(creator -> builder.named(NamedCause.owner(creator)));
                            final List<EntitySnapshot> snapshots = entities.stream().map(Entity::createSnapshot).collect(Collectors.toList());
                            EventConsumer
                                    .event(SpongeEventFactory.createSpawnEntityEvent(builder.build(), entities, snapshots, causeTracker.getWorld()))
                                    .nonCancelled(event ->
                                            event.getEntities().forEach(entity -> {
                                                Stream.<Supplier<Optional<UUID>>>of(
                                                    () -> EntityUtil.toMixin(tickingEntity).getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_NOTIFIER)
                                                                .map(Identifiable::getUniqueId),
                                                    () -> EntityUtil.toMixin(tickingEntity).getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR)
                                                                .map(Identifiable::getUniqueId)
                                                    )
                                                    .map(Supplier::get)
                                                    .filter(Optional::isPresent)
                                                    .map(Optional::get)
                                                    .findFirst()
                                                    .ifPresent(uuid ->
                                                            EntityUtil.toMixin(entity).trackEntityUniqueId(NbtDataUtil.SPONGE_ENTITY_CREATOR, uuid)
                                                    );
                                                causeTracker.getMixinWorld().forceSpawnEntity(entity);
                                            })
                                    )
                                    .process();
                        });
                phaseContext.getCapturedItemsSupplier()
                        .orElseThrow(PhaseUtil.throwWithContext("Not capturing item stack spawns!", phaseContext))
                        .ifPresentAndNotEmpty(entities -> {
                            final Cause.Builder builder = Cause.source(EntitySpawnCause.builder()
                                    .entity(tickingEntity)
                                    .type(InternalSpawnTypes.DROPPED_ITEM)
                                    .build());
                            EntityUtil.toMixin(tickingEntity).getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_NOTIFIER)
                                    .ifPresent(creator -> builder.named(NamedCause.notifier(creator)));
                            EntityUtil.toMixin(tickingEntity).getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR)
                                    .ifPresent(creator -> builder.named(NamedCause.owner(creator)));
                            final List<EntitySnapshot> snapshots = entities.stream().map(Entity::createSnapshot).collect(Collectors.toList());
                            EventConsumer.event(SpongeEventFactory
                                    .createDropItemEventCustom(builder.build(), entities, snapshots, causeTracker.getWorld()))
                                    .nonCancelled(event ->
                                            event.getEntities().forEach(entity -> {
                                                TrackingUtil.associateEntityCreator(phaseContext, EntityUtil.toNative(entity),
                                                        causeTracker.getMinecraftWorld());
                                                causeTracker.getMixinWorld().forceSpawnEntity(entity);
                                            })
                                    )
                                    .process();
                        });
                phaseContext.getCapturedBlockSupplier()
                        .orElseThrow(PhaseUtil.throwWithContext("Not capturing block changes!", phaseContext))
                        .ifPresentAndNotEmpty(blockSnapshots -> GeneralFunctions.processBlockCaptures(blockSnapshots, causeTracker, this, phaseContext));
                if (tickingEntity instanceof Player) {
                    return; // this is handled elsewhere
                }
                final net.minecraft.entity.Entity minecraftEntity = EntityUtil.toNative(tickingEntity);
                if (minecraftEntity.lastTickPosX != minecraftEntity.posX || minecraftEntity.lastTickPosY != minecraftEntity.posY || minecraftEntity.lastTickPosZ != minecraftEntity.posZ
                    || minecraftEntity.rotationPitch != minecraftEntity.prevRotationPitch || minecraftEntity.rotationYaw != minecraftEntity.prevRotationYaw) {
                    // yes we have a move event.
                    final double currentPosX = minecraftEntity.posX;
                    final double currentPosY = minecraftEntity.posY;
                    final double currentPosZ = minecraftEntity.posZ;
                    final Vector3d currentPositionVector = new Vector3d(currentPosX, currentPosY, currentPosZ);
                    final double currentRotPitch = minecraftEntity.rotationPitch;
                    final double currentRotYaw = minecraftEntity.rotationYaw;
                    Vector3d currentRotationVector = new Vector3d(currentRotPitch, currentRotYaw, 0);
                    DisplaceEntityEvent.Move event;
                    Transform<World> previous = new Transform<>(tickingEntity.getWorld(),
                            new Vector3d(minecraftEntity.prevPosX, minecraftEntity.prevPosY, minecraftEntity.prevPosZ), new Vector3d(minecraftEntity.prevRotationPitch, minecraftEntity.prevRotationYaw,
                            0));
                    Location<World>
                            currentLocation = new Location<>(tickingEntity.getWorld(), currentPosX, currentPosY, currentPosZ);
                    Transform<org.spongepowered.api.world.World> current = new Transform<>(currentLocation, currentRotationVector, tickingEntity.getScale());

                    if (minecraftEntity instanceof Humanoid) {
                        event = SpongeEventFactory.createDisplaceEntityEventMoveTargetHumanoid(Cause.of(NamedCause.source(minecraftEntity)), previous, current,
                                (Humanoid) minecraftEntity);
                    } else if (minecraftEntity instanceof Living) {
                        event = SpongeEventFactory.createDisplaceEntityEventMoveTargetLiving(Cause.of(NamedCause.source(minecraftEntity)), previous, current,
                                (Living) minecraftEntity);
                    } else {
                        event = SpongeEventFactory.createDisplaceEntityEventMove(Cause.of(NamedCause.source(minecraftEntity)), previous, current,
                                (Entity) minecraftEntity);
                    }
                    SpongeImpl.postEvent(event);
                    if (event.isCancelled()) {
                        minecraftEntity.posX = minecraftEntity.lastTickPosX;
                        minecraftEntity.posY = minecraftEntity.lastTickPosY;
                        minecraftEntity.posZ = minecraftEntity.lastTickPosZ;
                        minecraftEntity.rotationPitch = minecraftEntity.prevRotationPitch;
                        minecraftEntity.rotationYaw = minecraftEntity.prevRotationYaw;
                    } else {
                        Transform<org.spongepowered.api.world.World> worldTransform = event.getToTransform();
                        Vector3d eventPosition = worldTransform.getPosition();
                        Vector3d eventRotation = worldTransform.getRotation();
                        if (!eventPosition.equals(currentPositionVector)) {
                            minecraftEntity.posX = eventPosition.getX();
                            minecraftEntity.posY = eventPosition.getY();
                            minecraftEntity.posZ = eventPosition.getZ();
                        }
                        if (!eventRotation.equals(currentRotationVector)) {
                            minecraftEntity.rotationPitch = (float) currentRotationVector.getX();
                            minecraftEntity.rotationYaw = (float) currentRotationVector.getY();
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
            public void handleBlockChangeWithUser(@Nullable BlockChange blockChange, WorldServer minecraftWorld, Transaction<BlockSnapshot> snapshotTransaction,
                    PhaseContext context) {
                GeneralFunctions.processUserBreakage(blockChange, minecraftWorld, snapshotTransaction, context.firstNamed(NamedCause.SOURCE, Entity.class).get());
            }

            @Override
            public void markPostNotificationChange(@Nullable BlockChange blockChange, WorldServer minecraftWorld, PhaseContext context, Transaction<BlockSnapshot> transaction) {
                context.firstNamed(NamedCause.SOURCE, Entity.class).ifPresent(tickingEntity -> {
                    final IMixinEntity mixinEntity = EntityUtil.toMixin(tickingEntity);
                    Stream.<Supplier<Optional<User>>>of(() -> mixinEntity.getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_NOTIFIER),
                            () -> mixinEntity.getTrackedPlayer(NbtDataUtil.SPONGE_ENTITY_CREATOR))
                            .map(Supplier::get)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .findFirst()
                            .ifPresent(user -> {
                                final BlockPos notification = VecHelper.toBlockPos(transaction.getFinal().getPosition());
                                final IMixinChunk mixinChunk = (IMixinChunk) minecraftWorld.getChunkFromBlockCoords(notification);
                                final Block notificationBlock = (Block) transaction.getFinal().getState().getType();
                                mixinChunk.addTrackedBlockPosition(notificationBlock, notification, user, PlayerTracker.Type.NOTIFIER);
                            });
                });
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
        TILE_ENTITY() {
            @Override
            public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
                final TileEntity tickingTile = phaseContext.firstNamed(NamedCause.SOURCE, TileEntity.class)
                        .orElseThrow(PhaseUtil.throwWithContext("Not ticking on a TileEntity!", phaseContext));
                phaseContext.getCapturedBlockSupplier().get().ifPresentAndNotEmpty(blockSnapshots -> {
                    GeneralFunctions.processBlockCaptures(blockSnapshots, causeTracker, this, phaseContext);
                });

                phaseContext.getCapturedEntitySupplier().get().ifPresentAndNotEmpty(entities -> {
                    // TODO the entity spawn causes are not likely valid, need to investigate further.
                    final Cause cause = Cause.source(BlockSpawnCause.builder()
                                            .block(tickingTile.getLocation().createSnapshot())
                                            .type(InternalSpawnTypes.PLACEMENT)
                                            .build())
                                    .build();
                    final ImmutableList<EntitySnapshot>
                            snapshots =
                            entities.stream().map(Entity::createSnapshot).collect(GuavaCollectors.toImmutableList());
                    EventConsumer.event(SpongeEventFactory.createSpawnEntityEvent(cause, entities, snapshots, causeTracker.getWorld()))
                            .nonCancelled(event -> {
                                event.getEntities().forEach(entity -> {
                                    TrackingUtil.associateEntityCreator(phaseContext, EntityUtil.toNative(entity), causeTracker.getMinecraftWorld());
                                    causeTracker.getMixinWorld().forceSpawnEntity(entity);
                                });
                            })
                            .process();
                });
            }

            @Override
            public void markPostNotificationChange(@Nullable BlockChange blockChange, WorldServer minecraftWorld, PhaseContext context, Transaction<BlockSnapshot> transaction) {
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
                                final BlockPos notification = VecHelper.toBlockPos(transaction.getFinal().getPosition());
                                final Block notificationBlock = (Block) transaction.getFinal().getState().getType();
                                mixinChunk.addTrackedBlockPosition(notificationBlock, notification, user, PlayerTracker.Type.NOTIFIER);
                            });
                });
            }

            @Override
            public void associateAdditionalBlockChangeCauses(PhaseContext context, Cause.Builder builder, CauseTracker causeTracker) {
                final TileEntity tickingTile = context.firstNamed(NamedCause.SOURCE, TileEntity.class)
                        .orElseThrow(PhaseUtil.throwWithContext("Not ticking on a TileEntity!", context));
                builder.named(NamedCause.notifier(tickingTile));
            }
        },
        BLOCK() {
            @Override
            public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
                final BlockSnapshot tickingBlock = phaseContext.firstNamed(NamedCause.SOURCE, BlockSnapshot.class)
                        .orElseThrow(PhaseUtil.throwWithContext("Not ticking on a Block!", phaseContext));
                final WorldServer minecraftWorld = causeTracker.getMinecraftWorld();
                final World world = causeTracker.getWorld();
                phaseContext.getCapturedBlockSupplier()
                        .orElseThrow(PhaseUtil.throwWithContext("Intended to capture block changes but couldn't!", phaseContext))
                        .ifPresentAndNotEmpty(blockSnapshots -> GeneralFunctions.processBlockCaptures(blockSnapshots, causeTracker, this, phaseContext));
                phaseContext.getCapturedEntitySupplier()
                        .orElseThrow(PhaseUtil.throwWithContext("Intended to capture entity spawns couldn't!", phaseContext))
                        .ifPresentAndNotEmpty(entities -> {
                            final Cause cause = Cause.source(BlockSpawnCause.builder()
                                        .block(tickingBlock)
                                        .type(InternalSpawnTypes.PLACEMENT)
                                        .build())
                                    .build();
                            final ImmutableList<EntitySnapshot>
                                    snapshots =
                                    entities.stream().map(Entity::createSnapshot).collect(GuavaCollectors.toImmutableList());
                            EventConsumer.event(SpongeEventFactory.createSpawnEntityEvent(cause, entities, snapshots, world))
                                    .nonCancelled(event ->
                                            event.getEntities().forEach(entity -> {
                                                TrackingUtil.associateEntityCreator(phaseContext, EntityUtil.toNative(entity), minecraftWorld);
                                                causeTracker.getMixinWorld().forceSpawnEntity(entity);
                                            })
                                    )
                                    .process();
                        });
                phaseContext.getCapturedItemsSupplier()
                        .orElseThrow(PhaseUtil.throwWithContext("Intended to capture items but couldn't!", phaseContext))
                        .ifPresentAndNotEmpty(items -> {
                            final Cause cause = Cause.source(BlockSpawnCause.builder()
                                        .block(tickingBlock)
                                        .type(InternalSpawnTypes.DROPPED_ITEM)
                                        .build())
                                    .build();
                            final ImmutableList<EntitySnapshot> snapshots =
                                    items.stream().map(Entity::createSnapshot).collect(GuavaCollectors.toImmutableList());
                            EventConsumer.event(SpongeEventFactory.createSpawnEntityEvent(cause, items, snapshots, world))
                                    .nonCancelled(event ->
                                            event.getEntities().forEach(entity -> {
                                                TrackingUtil.associateEntityCreator(phaseContext, EntityUtil.toNative(entity), minecraftWorld);
                                                causeTracker.getMixinWorld().forceSpawnEntity(entity);
                                            })
                                    )
                                    .process();
                        });
            }
            @Override
            public void markPostNotificationChange(@Nullable BlockChange blockChange, WorldServer minecraftWorld, PhaseContext context, Transaction<BlockSnapshot> transaction) {
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
                                final BlockPos notification = VecHelper.toBlockPos(transaction.getFinal().getPosition());
                                final Block notificationBlock = (Block) transaction.getFinal().getState().getType();
                                mixinChunk.addTrackedBlockPosition(notificationBlock, notification, user, PlayerTracker.Type.NOTIFIER);
                            });
                });
            }

            @Override
            public void associateAdditionalBlockChangeCauses(PhaseContext context, Cause.Builder builder, CauseTracker causeTracker) {
                final BlockSnapshot tickingBlock = context.firstNamed(NamedCause.SOURCE, BlockSnapshot.class)
                        .orElseThrow(PhaseUtil.throwWithContext("Not ticking on a Block!", context));
                builder.named(NamedCause.notifier(tickingBlock));
            }
        },
        RANDOM_BLOCK() {
            @Override
            public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
                final BlockSnapshot tickingBlock = phaseContext.firstNamed(NamedCause.SOURCE, BlockSnapshot.class)
                        .orElseThrow(PhaseUtil.throwWithContext("Not ticking on a Block!", phaseContext));
                phaseContext.getCapturedBlockSupplier().get().ifPresentAndNotEmpty(blockSnapshots -> {
                    GeneralFunctions.processBlockCaptures(blockSnapshots, causeTracker, this, phaseContext);
                });
                phaseContext.getCapturedEntitySupplier().get().ifPresentAndNotEmpty(entities -> {
                    final Cause cause = Cause.source(BlockSpawnCause.builder().block(tickingBlock).type(InternalSpawnTypes.PLACEMENT).build()).build();
                    final ImmutableList<EntitySnapshot>
                            snapshots =
                            entities.stream().map(Entity::createSnapshot).collect(GuavaCollectors.toImmutableList());
                    EventConsumer.event(SpongeEventFactory.createSpawnEntityEvent(cause, entities, snapshots, causeTracker.getWorld()))
                            .nonCancelled(event -> {
                                event.getEntities().forEach(entity -> {
                                    TrackingUtil.associateEntityCreator(phaseContext, EntityUtil.toNative(entity), causeTracker.getMinecraftWorld());
                                    causeTracker.getMixinWorld().forceSpawnEntity(entity);
                                });
                            })
                            .process();
                });
            }
            @Override
            public void markPostNotificationChange(@Nullable BlockChange blockChange, WorldServer minecraftWorld, PhaseContext context, Transaction<BlockSnapshot> transaction) {
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
                                final BlockPos notification = VecHelper.toBlockPos(transaction.getFinal().getPosition());
                                final Block notificationBlock = (Block) transaction.getFinal().getState().getType();
                                mixinChunk.addTrackedBlockPosition(notificationBlock, notification, user, PlayerTracker.Type.NOTIFIER);
                            });
                });
            }

            @Override
            public void associateAdditionalBlockChangeCauses(PhaseContext context, Cause.Builder builder, CauseTracker causeTracker) {
                final BlockSnapshot tickingBlock = context.firstNamed(NamedCause.SOURCE, BlockSnapshot.class)
                        .orElseThrow(PhaseUtil.throwWithContext("Not ticking on a Block!", context));
                builder.named(NamedCause.notifier(tickingBlock));
            }
        },
        PLAYER() {
            @Override
            public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
                final Entity tickingEntity = phaseContext.firstNamed(NamedCause.SOURCE, Entity.class)
                        .orElseThrow(PhaseUtil.throwWithContext("Not ticking on an Entity!", phaseContext));
                phaseContext.getCapturedEntitySupplier().get().ifPresentAndNotEmpty(entities -> {
                    final Cause.Builder builder = Cause.source(EntitySpawnCause.builder()
                            .entity(tickingEntity)
                            .type(InternalSpawnTypes.PASSIVE)
                            .build());
                    final List<EntitySnapshot> snapshots = entities.stream().map(Entity::createSnapshot).collect(Collectors.toList());
                    EventConsumer.event(SpongeEventFactory.createSpawnEntityEvent(builder.build(), entities, snapshots, causeTracker.getWorld()))
                            .nonCancelled(event -> EntityListConsumer.FORCE_SPAWN.apply(event.getEntities(), causeTracker))
                            .process();
                });
                phaseContext.getCapturedItemsSupplier().get().ifPresentAndNotEmpty(entities -> {
                    final Cause.Builder builder = Cause.source(EntitySpawnCause.builder()
                            .entity(tickingEntity)
                            .type(InternalSpawnTypes.DROPPED_ITEM)
                            .build());
                    final List<EntitySnapshot> snapshots = entities.stream().map(Entity::createSnapshot).collect(Collectors.toList());
                    EventConsumer.event(SpongeEventFactory.createDropItemEventCustom(builder.build(), entities, snapshots, causeTracker.getWorld()))
                            .nonCancelled(event -> EntityListConsumer.FORCE_SPAWN.apply(event.getEntities(), causeTracker))
                            .process();
                });
                phaseContext.getCapturedBlockSupplier().get().ifPresentAndNotEmpty(blockSnapshots -> {
                    GeneralFunctions.processBlockCaptures(blockSnapshots, causeTracker, this, phaseContext);
                });
            }

            @Override
            public void markPostNotificationChange(@Nullable BlockChange blockChange, WorldServer minecraftWorld, PhaseContext context,
                    Transaction<BlockSnapshot> transaction) {
                final BlockPos notification = VecHelper.toBlockPos(transaction.getFinal().getPosition());
                final IMixinChunk mixinChunk = (IMixinChunk) minecraftWorld.getChunkFromBlockCoords(notification);
                final Block notificationBlock = (Block) transaction.getFinal().getState().getType();
                mixinChunk.addTrackedBlockPosition(notificationBlock, notification, context.first(Player.class).get(), PlayerTracker.Type.NOTIFIER);
            }

            @Override
            public void associateAdditionalBlockChangeCauses(PhaseContext context, Cause.Builder builder, CauseTracker causeTracker) {
                builder.named(NamedCause.OWNER, context.firstNamed(NamedCause.SOURCE, Player.class).get());
            }
        }
        ;


        @Override
        public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {

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

    }

    @Override
    public void unwind(CauseTracker causeTracker, IPhaseState state, PhaseContext context) {
        if (state instanceof ITickingState) {
            ((ITickingState) state).processPostTick(causeTracker, context);
        } else if (state == State.TERRAIN_GENERATION) {
            final List<Entity> spawnedEntities = context.getCapturedEntitySupplier().get().orEmptyList();
            final List<Entity> spawnedItems = context.getCapturedItemsSupplier().get().orEmptyList();
            if (spawnedEntities.isEmpty() && spawnedItems.isEmpty()) {
                return;
            }
            if (!spawnedEntities.isEmpty()) {
                if (!spawnedItems.isEmpty()) { // We shouldn't separate the entities whatsoever.
                    spawnedEntities.addAll(spawnedItems);
                }
                final List<EntitySnapshot> snapshots = spawnedEntities.stream().map(Entity::createSnapshot).collect(Collectors.toList());
                final Cause cause = Cause.source(InternalSpawnTypes.WORLD_SPAWNER_CAUSE).named("World",  causeTracker.getWorld())
                        .build();
                EventConsumer.event(SpongeEventFactory.createSpawnEntityEventSpawner(cause, spawnedEntities, snapshots, causeTracker.getWorld()))
                        .nonCancelled(event -> EntityListConsumer.FORCE_SPAWN.apply(event.getEntities(), causeTracker))
                        .process();
            }
        } else if (state == State.POPULATOR_RUNNING) {
            final PopulatorType runningGenerator = context.firstNamed(InternalNamedCauses.WorldGeneration.CAPTURED_POPULATOR, PopulatorType.class).orElse(null);
            final List<Entity> spawnedEntities = context.getCapturedEntitySupplier().get().orEmptyList();
            final List<Entity> spawnedItems = context.getCapturedItemsSupplier().get().orEmptyList();
            if (spawnedEntities.isEmpty() && spawnedItems.isEmpty()) {
                return;
            }
            if (!spawnedEntities.isEmpty()) {
                if (!spawnedItems.isEmpty()) { // We shouldn't separate the entities whatsoever.
                    spawnedEntities.addAll(spawnedItems);
                }
                final List<EntitySnapshot> snapshots = spawnedEntities.stream().map(Entity::createSnapshot).collect(Collectors.toList());
                final Cause cause = Cause.source(InternalSpawnTypes.WORLD_SPAWNER_CAUSE).named("World",  causeTracker.getWorld())
                        .named(InternalNamedCauses.WorldGeneration.CAPTURED_POPULATOR, runningGenerator)
                        .build();
                EventConsumer.event(SpongeEventFactory.createSpawnEntityEventSpawner(cause, spawnedEntities, snapshots, causeTracker.getWorld()))
                        .nonCancelled(event -> EntityListConsumer.FORCE_SPAWN.apply(event.getEntities(), causeTracker))
                        .process();
            }
            // Blocks do not matter one bit.
        } else if (state == State.WORLD_SPAWNER_SPAWNING) {
            final List<Entity> spawnedEntities = context.getCapturedEntitySupplier().get().orEmptyList();
            final List<Entity> spawnedItems = context.getCapturedItemsSupplier().get().orEmptyList();
            if (spawnedEntities.isEmpty() && spawnedItems.isEmpty()) {
                return;
            }
            if (!spawnedEntities.isEmpty()) {
                if (!spawnedItems.isEmpty()) { // We shouldn't separate the entities whatsoever.
                    spawnedEntities.addAll(spawnedItems);
                }
                final List<EntitySnapshot> snapshots = spawnedEntities.stream().map(Entity::createSnapshot).collect(Collectors.toList());
                final Cause cause = Cause.source(InternalSpawnTypes.WORLD_SPAWNER_CAUSE).named("World", causeTracker.getWorld()).build();
                EventConsumer.event(SpongeEventFactory.createSpawnEntityEventSpawner(cause, spawnedEntities, snapshots, causeTracker.getWorld()))
                        .nonCancelled(event -> EntityListConsumer.FORCE_SPAWN.apply(event.getEntities(), causeTracker))
                        .process();
            }

            context.getCapturedBlockSupplier().get().ifPresentAndNotEmpty(blockSnapshots -> {
                GeneralFunctions.processBlockCaptures(blockSnapshots, causeTracker, state, context);
            });
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
    public void associateAdditionalCauses(IPhaseState state, PhaseContext context, Cause.Builder builder, CauseTracker causeTracker) {
        if (state instanceof Tick) {
            ((Tick) state).associateAdditionalBlockChangeCauses(context, builder, causeTracker);
        }
    }

    /**
     * A specialized state that signifies that it is a "master" state that
     * can have multiple state side effects, such as spawning other entities,
     * changing other blocks, calling other populators, etc.
     */
    interface ITickingState extends IPhaseState {

        void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext);

    }
}
