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
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.util.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.block.BlockSnapshot;
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
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.gen.PopulatorType;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.EventConsumer;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.CaptureType;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
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
            @Override
            public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
                final Entity tickingEntity = phaseContext.firstNamed(NamedCause.SOURCE, Entity.class)
                    .orElseThrow(PhaseUtil.createIllegalStateSupplierForTicking("Not ticking on an Entity!", phaseContext));
                phaseContext.getCapturedEntitySupplier().get().ifPresent(entities -> {
                    if (((net.minecraft.entity.Entity) tickingEntity).isDead) {

                    }
                    Cause.source(EntitySpawnCause.builder().entity(tickingEntity).type(InternalSpawnTypes.PASSIVE));
                });
                phaseContext.getCapturedBlockSupplier().get().ifPresent(blockSnapshots -> {
                    final ImmutableList<Transaction<BlockSnapshot>> blockBreakTransactions;
                    final ImmutableList<Transaction<BlockSnapshot>> blockModifyTransactions;
                    final ImmutableList<Transaction<BlockSnapshot>> blockPlaceTransactions;
                    final ImmutableList<Transaction<BlockSnapshot>> blockDecayTransactions;
                    final ImmutableList<Transaction<BlockSnapshot>> blockMultiTransactions;
                    final ImmutableList.Builder<Transaction<BlockSnapshot>> breakBuilder = new ImmutableList.Builder<>();
                    final ImmutableList.Builder<Transaction<BlockSnapshot>> placeBuilder = new ImmutableList.Builder<>();
                    final ImmutableList.Builder<Transaction<BlockSnapshot>> decayBuilder = new ImmutableList.Builder<>();
                    final ImmutableList.Builder<Transaction<BlockSnapshot>> modifyBuilder = new ImmutableList.Builder<>();
                    final ImmutableList.Builder<Transaction<BlockSnapshot>> multiBuilder = new ImmutableList.Builder<>();
                    final ChangeBlockEvent.Break breakEvent;
                    final ChangeBlockEvent.Modify modifyEvent;
                    final ChangeBlockEvent.Place placeEvent;
                    final List<ChangeBlockEvent> blockEvents = new ArrayList<>();
                    final WorldServer minecraftWorld = causeTracker.getMinecraftWorld();
                    final IMixinWorldServer mixinWorld = causeTracker.getMixinWorld();

                    blockSnapshots.stream()
                            .map(originalSnapshot -> { // Create the transactions
                                final BlockPos blockPos = VecHelper.toBlockPos(originalSnapshot.getPosition());
                                final IBlockState newState = minecraftWorld.getBlockState(blockPos);
                                final IBlockState newActualState = newState.getBlock().getActualState(newState, minecraftWorld, blockPos);
                                final BlockSnapshot newSnapshot = mixinWorld.createSpongeBlockSnapshot(newState, newActualState, blockPos, 0);
                                return new Transaction<>(originalSnapshot, newSnapshot);
                            })
                            .forEach(transaction -> { // Assign the transactions as necessary
                                final BlockSnapshot replacement = transaction.getDefault();
                                final SpongeBlockSnapshot spongeReplacement = (SpongeBlockSnapshot) replacement;
                                final CaptureType captureType = spongeReplacement.captureType;
                                if (captureType == CaptureType.BREAK) {
                                    breakBuilder.add(transaction);
                                } else if (captureType == CaptureType.PLACE) {
                                    placeBuilder.add(transaction);
                                } else if (captureType == CaptureType.DECAY) {
                                    decayBuilder.add(transaction);
                                } else if (captureType == CaptureType.MODIFY) {
                                    modifyBuilder.add(transaction);
                                }
                                multiBuilder.add(transaction);
                            });

                    blockBreakTransactions = breakBuilder.build();
                    blockDecayTransactions = decayBuilder.build();
                    blockModifyTransactions = modifyBuilder.build();
                    blockPlaceTransactions = placeBuilder.build();
                    blockMultiTransactions = multiBuilder.build();
                    ChangeBlockEvent changeBlockEvent;
                    final Cause.Builder builder = Cause.source(tickingEntity);
                    final World world = causeTracker.getWorld();
                    if (blockBreakTransactions.size() > 0) {
                        changeBlockEvent = SpongeEventFactory.createChangeBlockEventBreak(builder.build(), world, blockBreakTransactions);
                        SpongeImpl.postEvent(changeBlockEvent);
                        breakEvent = (ChangeBlockEvent.Break) changeBlockEvent;
                        blockEvents.add(changeBlockEvent);
                    } else {
                        breakEvent = null;
                    }
                    if (blockModifyTransactions.size() > 0) {
                        changeBlockEvent = SpongeEventFactory.createChangeBlockEventModify(builder.build(), world, blockModifyTransactions);
                        SpongeImpl.postEvent(changeBlockEvent);
                        modifyEvent = (ChangeBlockEvent.Modify) changeBlockEvent;
                        blockEvents.add(changeBlockEvent);
                    } else {
                        modifyEvent = null;
                    }
                    if (blockPlaceTransactions.size() > 0) {
                        changeBlockEvent = SpongeEventFactory.createChangeBlockEventPlace(builder.build(), world, blockPlaceTransactions);
                        SpongeImpl.postEvent(changeBlockEvent);
                        placeEvent = (ChangeBlockEvent.Place) changeBlockEvent;
                        blockEvents.add(changeBlockEvent);
                    } else {
                        placeEvent = null;
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
                        changeBlockEvent = SpongeEventFactory.createChangeBlockEventPost(builder.build(), world, blockMultiTransactions);
                        SpongeImpl.postEvent(changeBlockEvent);
                        if (changeBlockEvent.isCancelled()) {
                            // Restore original blocks
                            // They must be restored in reverse order of which they occurred.
                            ListIterator<Transaction<BlockSnapshot>> listIterator = changeBlockEvent.getTransactions().listIterator(changeBlockEvent.getTransactions().size());
                            while (listIterator.hasPrevious()) {
                                final Transaction<BlockSnapshot> previous = listIterator.previous();
                                previous.getOriginal().restore(true, false);
                            }
                            return;
                        }
                    }

                    if (blockDecayTransactions.size() > 0) {
                        changeBlockEvent = SpongeEventFactory.createChangeBlockEventDecay(builder.build(), world, blockDecayTransactions);
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
                            for (Transaction<BlockSnapshot> transaction : Lists.reverse(blockEvent.getTransactions())) {
                                transaction.getOriginal().restore(true, false);
                            }
                            return;
                        } else {
                            final List<Transaction<BlockSnapshot>> invalid = phaseContext.getInvalidTransactions().get();
                            blockEvent.getTransactions().forEach(snapshotTransaction -> {
                                if (!snapshotTransaction.isValid()) {
                                    invalid.add(snapshotTransaction);
                                } else {
                                    phaseContext.first(User.class).ifPresent(user -> {
                                        if (captureType == CaptureType.BREAK) {
                                            final BlockPos blockPos = VecHelper.toBlockPos(snapshotTransaction.getOriginal().getPosition());
                                            EntityUtil.findHangingEntities(minecraftWorld, blockPos).stream()
                                                    .filter(entity -> entity instanceof EntityItemFrame)
                                                    .forEach(hanging -> {
                                                        final EntityItemFrame itemFrame = (EntityItemFrame) hanging;
                                                        itemFrame.dropItemOrSelf(EntityUtil.toNative(tickingEntity), true);
                                                        itemFrame.setDead();
                                                    });
                                        }
                                    });
                                }
                            });
                            for (Transaction<BlockSnapshot> transaction : blockEvent.getTransactions()) {
                                if (!transaction.isValid()) {
                                    invalid.add(transaction);
                                } else {
                                    phaseContext.first(User.class).ifPresent(user -> {
                                        if (captureType == CaptureType.BREAK) {
                                            BlockPos pos = VecHelper.toBlockPos(transaction.getOriginal().getPosition());
                                            for (EntityHanging hanging : EntityUtil.findHangingEntities(causeTracker.getMinecraftWorld(), pos)) {
                                                if (hanging instanceof EntityItemFrame) {
                                                    EntityItemFrame itemFrame = (EntityItemFrame) hanging;
                                                    itemFrame.dropItemOrSelf(((net.minecraft.entity.Entity) tickingEntity), true);
                                                    itemFrame.setDead();
                                                }
                                            }
                                        }
                                    });
                                }
                            }

                            if (invalid.size() > 0) {
                                for (Transaction<BlockSnapshot> transaction : Lists.reverse(invalid)) {
                                    transaction.getOriginal().restore(true, false);
                                }
                            }

                            TrackingUtil.dispatchPostBlockChanges(causeTracker, blockEvent.getTransactions(), captureType, builder, phaseContext);
                        }
                    }
                });
            }
        },
        TILE_ENTITY() {
            @Override
            public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
                final TileEntity tickingTile = phaseContext.firstNamed(NamedCause.SOURCE, TileEntity.class)
                        .orElseThrow(PhaseUtil.createIllegalStateSupplierForTicking("Not ticking on a TileEntity!", phaseContext));

            }
        },
        BLOCK() {
            @Override
            public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
                final BlockSnapshot tickingBlock = phaseContext.firstNamed(NamedCause.SOURCE, BlockSnapshot.class)
                        .orElseThrow(PhaseUtil.createIllegalStateSupplierForTicking("Not ticking on a Block!", phaseContext));

            }
        },
        RANDOM_BLOCK() {
            @Override
            public void processPostTick(CauseTracker causeTracker, PhaseContext phaseContext) {
                final BlockSnapshot tickingBlock = phaseContext.firstNamed(NamedCause.SOURCE, BlockSnapshot.class)
                        .orElseThrow(PhaseUtil.createIllegalStateSupplierForTicking("Not ticking on a Block!", phaseContext));

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
            return state instanceof BlockPhase.State || state instanceof EntityPhase.State || state == State.TERRAIN_GENERATION || state == GeneralPhase.Post.UNWINDING;
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
            final PopulatorType runningGenerator = context.firstNamed(TrackingUtil.CAPTURED_POPULATOR, PopulatorType.class).orElse(null);
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
