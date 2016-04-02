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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.BlockSpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.util.GuavaCollectors;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.EventConsumer;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.function.GeneralFunctions;
import org.spongepowered.common.event.tracking.phase.util.PhaseUtil;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;
import org.spongepowered.common.world.BlockChange;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
                    .orElseThrow(PhaseUtil.createIllegalStateSupplier("Not ticking on an Entity!", phaseContext));
                phaseContext.getCapturedEntitySupplier().get().ifPresent(entities -> {
                    if (((net.minecraft.entity.Entity) tickingEntity).isDead) {

                    }
                    Cause.source(EntitySpawnCause.builder().entity(tickingEntity).type(InternalSpawnTypes.PASSIVE));
                });
                phaseContext.getCapturedBlockSupplier().get().ifPresent(blockSnapshots -> {
                    GeneralFunctions.processBlockCaptures(blockSnapshots, causeTracker, this, phaseContext);
                });
            }

            @Override
            public void handleBlockChangeWithUser(BlockChange blockChange, WorldServer minecraftWorld, Transaction<BlockSnapshot> snapshotTransaction,
                    PhaseContext context) {
                GeneralFunctions.processUserBreakage(blockChange, minecraftWorld, snapshotTransaction, context.firstNamed(NamedCause.SOURCE, Entity.class).get());
            }
        },
        TILE_ENTITY() {
            @Override
            public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
                final TileEntity tickingTile = phaseContext.firstNamed(NamedCause.SOURCE, TileEntity.class)
                        .orElseThrow(PhaseUtil.createIllegalStateSupplier("Not ticking on a TileEntity!", phaseContext));
                phaseContext.getCapturedBlockSupplier().get().ifPresent(blockSnapshots -> {
                    GeneralFunctions.processBlockCaptures(blockSnapshots, causeTracker, this, phaseContext);
                });

                phaseContext.getCapturedEntitySupplier().get().ifPresent(entities -> {
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
        },
        BLOCK() {
            @Override
            public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
                final BlockSnapshot tickingBlock = phaseContext.firstNamed(NamedCause.SOURCE, BlockSnapshot.class)
                        .orElseThrow(PhaseUtil.createIllegalStateSupplier("Not ticking on a Block!", phaseContext));
                phaseContext.getCapturedBlockSupplier().get().ifPresent(blockSnapshots -> {
                    GeneralFunctions.processBlockCaptures(blockSnapshots, causeTracker, this, phaseContext);
                });
                phaseContext.getCapturedEntitySupplier().get().ifPresent(entities -> {
                    // TODO the entity spawn causes are not likely valid, need to investigate further.
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
        },
        RANDOM_BLOCK() {
            @Override
            public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
                final BlockSnapshot tickingBlock = phaseContext.firstNamed(NamedCause.SOURCE, BlockSnapshot.class)
                        .orElseThrow(PhaseUtil.createIllegalStateSupplier("Not ticking on a Block!", phaseContext));
                phaseContext.getCapturedBlockSupplier().get().ifPresent(blockSnapshots -> {
                    GeneralFunctions.processBlockCaptures(blockSnapshots, causeTracker, this, phaseContext);
                });
                phaseContext.getCapturedEntitySupplier().get().ifPresent(entities -> {
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
        };


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

    }

    @Override
    public void unwind(CauseTracker causeTracker, IPhaseState state, PhaseContext context) {
        if (state instanceof ITickingState) {
            ((ITickingState) state).processPostTick(causeTracker, context);
        } else if (state == State.TERRAIN_GENERATION) {
            // TODO process entity spawns
        } else if (state == State.POPULATOR_RUNNING) {
            final PopulatorType runningGenerator = context.firstNamed(TrackingUtil.CAPTURED_POPULATOR, PopulatorType.class).orElse(null);
            final IMixinWorld mixinWorld = causeTracker.getMixinWorld();
            // TODO process entity spawns
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
                    .nonCancelled(event -> event.getEntities().forEach(entity -> causeTracker.getMixinWorld().forceSpawnEntity(entity)))
                    .process();
            }

            context.getCapturedBlockSupplier().get().ifPresent(blockSnapshots -> {
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

    /**
     * A specialized state that signifies that it is a "master" state that
     * can have multiple state side effects, such as spawning other entities,
     * changing other blocks, calling other populators, etc.
     */
    interface ITickingState extends IPhaseState {

        void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext);

    }
}
