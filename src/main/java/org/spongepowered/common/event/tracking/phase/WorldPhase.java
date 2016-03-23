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

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.stats.StatList;
import net.minecraft.util.BlockPos;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.entity.PlayerTracker;
import org.spongepowered.common.event.EventConsumer;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.ITickingState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingHelper;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.util.StaticMixinHelper;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.CaptureType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class WorldPhase extends TrackingPhase {

    public enum State implements IPhaseState {
        TERRAIN_GENERATION(BlockPhase.State.BLOCK_DECAY, BlockPhase.State.RESTORING_BLOCKS, State.POPULATOR_RUNNING, State.WORLD_SPAWNER_SPAWNING),
        POPULATOR_RUNNING(BlockPhase.State.BLOCK_DECAY, BlockPhase.State.RESTORING_BLOCKS, State.POPULATOR_RUNNING, State.WORLD_SPAWNER_SPAWNING),
        CHUNK_LOADING,
        WORLD_SPAWNER_SPAWNING;

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
            @Override
            public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
                final Entity tickingEntity = phaseContext.firstNamed(NamedCause.SOURCE, Entity.class)
                    .orElseThrow(
                        () -> new IllegalStateException("Not ticking on an Entity! Please analyze the current phase context: " + phaseContext));
                phaseContext.getCapturedEntitySupplier().get().ifPresent(entities -> {
                    if (((net.minecraft.entity.Entity) tickingEntity).isDead) {

                    }
                    Cause.source(EntitySpawnCause.builder().entity(tickingEntity).type(InternalSpawnTypes.PASSIVE));
                });
                phaseContext.getCapturedBlockSupplier().get().ifPresent(blockSnapshots -> {
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

                    Iterator<BlockSnapshot> iterator = blockSnapshots.iterator();
                    while (iterator.hasNext()) {
                        SpongeBlockSnapshot blockSnapshot = (SpongeBlockSnapshot) iterator.next();
                        CaptureType captureType = blockSnapshot.captureType;
                        BlockPos pos = VecHelper.toBlockPos(blockSnapshot.getPosition());
                        IBlockState currentState = causeTracker.getMinecraftWorld().getBlockState(pos);
                        Transaction<BlockSnapshot> transaction = new Transaction<>(blockSnapshot, causeTracker.getMixinWorld().createSpongeBlockSnapshot(currentState, currentState.getBlock()
                            .getActualState(currentState, causeTracker.getMinecraftWorld(), pos), pos, 0));
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
                    final Cause.Builder builder = Cause.source(tickingEntity);
                    if (blockBreakTransactions.size() > 0) {
                        changeBlockEvent = SpongeEventFactory.createChangeBlockEventBreak(builder.build(), causeTracker.getWorld(), blockBreakTransactions);
                        SpongeImpl.postEvent(changeBlockEvent);
                        breakEvent = (ChangeBlockEvent.Break) changeBlockEvent;
                        blockEvents.add(changeBlockEvent);
                    }
                    if (blockModifyTransactions.size() > 0) {
                        changeBlockEvent = SpongeEventFactory.createChangeBlockEventModify(builder.build(), causeTracker.getWorld(), blockModifyTransactions);
                        SpongeImpl.postEvent(changeBlockEvent);
                        modifyEvent = (ChangeBlockEvent.Modify) changeBlockEvent;
                        blockEvents.add(changeBlockEvent);
                    }
                    if (blockPlaceTransactions.size() > 0) {
                        changeBlockEvent = SpongeEventFactory.createChangeBlockEventPlace(builder.build(), causeTracker.getWorld(), blockPlaceTransactions);
                        SpongeImpl.postEvent(changeBlockEvent);
                        placeEvent = (ChangeBlockEvent.Place) changeBlockEvent;
                        blockEvents.add(changeBlockEvent);
                    }
                    if (blockEvents.size() > 1) {
                        if (breakEvent != null) {
                            builder.suggestNamed("BreakEvent", breakEvent);
                        }
                        if (modifyEvent != null) {
                            builder.suggestNamed("ModifyEvent", modifyEvent);
                        }
                        if (placeEvent != null) {
                            builder.suggestNamed("PlaceEvent", placeEvent);
                        }
                        changeBlockEvent = SpongeEventFactory.createChangeBlockEventPost(builder.build(), causeTracker.getWorld(), blockMultiTransactions);
                        SpongeImpl.postEvent(changeBlockEvent);
                        if (changeBlockEvent.isCancelled()) {
                            // Restore original blocks
                            ListIterator<Transaction<BlockSnapshot>> listIterator = changeBlockEvent.getTransactions().listIterator(changeBlockEvent.getTransactions().size());
                            while (listIterator.hasPrevious()) {
                                final Transaction<BlockSnapshot> previous = listIterator.previous();
                                previous.getOriginal().restore(true, false);
                            }
                            return;
                        }
                    }

                    if (blockDecayTransactions.size() > 0) {
                        changeBlockEvent = SpongeEventFactory.createChangeBlockEventDecay(builder.build(), causeTracker.getWorld(), blockDecayTransactions);
                        SpongeImpl.postEvent(changeBlockEvent);
                        blockEvents.add(changeBlockEvent);
                    }

                    for (ChangeBlockEvent blockEvent : blockEvents) {
                        final CaptureType captureType;
                        if (blockEvent instanceof ChangeBlockEvent.Break) {
                            captureType = CaptureType.BREAK;
                        } else if (blockEvent instanceof ChangeBlockEvent.Decay) {
                            captureType = CaptureType.DECAY;
                        } else if (blockEvent instanceof ChangeBlockEvent.Modify) {
                            captureType = CaptureType.MODIFY;
                        } else if (blockEvent instanceof ChangeBlockEvent.Place) {
                            captureType = CaptureType.PLACE;
                        } else {
                            captureType = null;
                        }

                        if (blockEvent.isCancelled()) {
                            // Restore original blocks
                            ListIterator<Transaction<BlockSnapshot>>
                                listIterator =
                                blockEvent.getTransactions().listIterator(blockEvent.getTransactions().size());
                            while (listIterator.hasPrevious()) {
                                Transaction<BlockSnapshot> transaction = listIterator.previous();
                                transaction.getOriginal().restore(true, false);
                            }
                            return;
                        } else {
                            final List<Transaction<BlockSnapshot>> invalid = phaseContext.getInvalidTransactions().get();
                            for (Transaction<BlockSnapshot> transaction : blockEvent.getTransactions()) {
                                if (!transaction.isValid()) {
                                    invalid.add(transaction);
                                } else {
                                    final Optional<User> user = phaseContext.first(User.class);
                                    if (captureType == CaptureType.BREAK && user.isPresent()) {
                                        BlockPos pos = VecHelper.toBlockPos(transaction.getOriginal().getPosition());
                                        for (EntityHanging hanging : SpongeHooks.findHangingEntities(causeTracker.getMinecraftWorld(), pos)) {
                                            if (hanging != null) {
                                                if (hanging instanceof EntityItemFrame) {
                                                    EntityItemFrame itemFrame = (EntityItemFrame) hanging;
                                                    itemFrame.dropItemOrSelf(((net.minecraft.entity.Entity) tickingEntity), true);
                                                    itemFrame.setDead();
                                                }
                                            }
                                        }
                                    }

                                }
                            }

                            if (invalid.size() > 0) {
                                for (Transaction<BlockSnapshot> transaction : Lists.reverse(invalid)) {
                                    transaction.getOriginal().restore(true, false);
                                }
                            }

                            TrackingHelper.markAndNotifyBlockPost(causeTracker, blockEvent.getTransactions(), captureType, builder);
                        }
                    }
                });
            }
        },
        TILE_ENTITY() {
            @Override
            public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
                final TileEntity tickingEntity = phaseContext.firstNamed(NamedCause.SOURCE, TileEntity.class)
                    .orElseThrow(
                        () -> new IllegalStateException("Not ticking on a TileEntity! Please analyze the current phase context: " + phaseContext));

            }
        },
        BLOCK() {
            @Override
            public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
                final BlockSnapshot tickingEntity = phaseContext.firstNamed(NamedCause.SOURCE, BlockSnapshot.class)
                    .orElseThrow(
                        () -> new IllegalStateException("Not ticking on an Block! Please analyze the current phase context: " + phaseContext));

            }
        },
        RANDOM_BLOCK() {
            @Override
            public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
                final BlockSnapshot tickingEntity = phaseContext.firstNamed(NamedCause.SOURCE, BlockSnapshot.class)
                    .orElseThrow(
                        () -> new IllegalStateException("Not ticking on an Block! Please analyze the current phase context: " + phaseContext));

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
        }
        if (state == State.TERRAIN_GENERATION) {
            final List<BlockSnapshot> changedBlocks = context.getCapturedBlockSupplier().get().orEmptyList();


        } else if (state == State.POPULATOR_RUNNING) {
            final PopulatorType runningGenerator = context.firstNamed(TrackingHelper.CAPTURED_POPULATOR, PopulatorType.class).orElse(null);
            final IMixinWorld mixinWorld = causeTracker.getMixinWorld();
        } else if (state instanceof Tick) {
            ((Tick) state).processPostTick(causeTracker, context);
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
                EventConsumer.supplyEvent(() -> SpongeEventFactory.createSpawnEntityEventSpawner(cause, spawnedEntities, snapshots, causeTracker.getWorld()))
                    .nonCancelled(event -> event.getEntities().forEach(entity -> causeTracker.getMixinWorld().forceSpawnEntity(entity)))
                    .buildAndPost();
            }
        }

    }

    public WorldPhase(TrackingPhase parent) {
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

}
