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

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableList;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.BlockSpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.util.GuavaCollectors;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.EventConsumer;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.function.EntityFunction;
import org.spongepowered.common.event.tracking.phase.function.GeneralFunctions;
import org.spongepowered.common.event.tracking.phase.util.PhaseUtil;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;

import javax.annotation.Nullable;

public final class GeneralPhase extends TrackingPhase {

    public enum State implements IPhaseState {
        COMMAND {
            @Override
            public boolean canSwitchTo(IPhaseState state) {
                return state instanceof BlockPhase.State;
            }
        },
        COMPLETE {
            @Override
            public boolean canSwitchTo(IPhaseState state) {
                return true;
            }
        };

        @Override
        public GeneralPhase getPhase() {
            return TrackingPhases.GENERAL;
        }
    }

    public enum Post implements IPhaseState {
        /**
         * A specific state that is introduced for the sake of
         * preventing leaks into other phases as various phases
         * are unwound. This state is specifically to ignore any
         * transactions that may take place.
         */
        UNWINDING;


        @Override
        public TrackingPhase getPhase() {
            return TrackingPhases.GENERAL;
        }

        @Override
        public boolean canSwitchTo(IPhaseState state) {
            return false;
        }

        @Override
        public boolean tracksBlockRestores() {
            return true;
        }

    }

    GeneralPhase(@Nullable TrackingPhase parent) {
        super(parent);
    }

    @Override
    public GeneralPhase addChild(TrackingPhase child) {
        super.addChild(child);
        return this;
    }

    @Override
    public void unwind(CauseTracker causeTracker, IPhaseState state, PhaseContext phaseContext) {
        if (state == State.COMMAND) {
            final ICommand command = phaseContext.firstNamed(InternalNamedCauses.General.COMMAND, ICommand.class)
                    .orElseThrow(PhaseUtil.throwWithContext("Expected to be capturing a command, but none found!", phaseContext));
            final ICommandSender sender = phaseContext.firstNamed(NamedCause.SOURCE, ICommandSender.class)
                    .orElseThrow(PhaseUtil.throwWithContext("Expected to be capturing a Command Sender, but none found!", phaseContext));
            phaseContext.getCapturedBlockSupplier()
                    .orElseThrow(PhaseUtil.throwWithContext("Expected to be capturing block changes, but we're not!", phaseContext))
                    .ifPresentAndNotEmpty(list -> GeneralFunctions.processBlockCaptures(list, causeTracker, state, phaseContext));
            phaseContext.getCapturedEntitySupplier()
                    .orElseThrow(PhaseUtil.throwWithContext("Expected to be capturing entity spawns, but we're not!", phaseContext))
                    .ifPresentAndNotEmpty(entities -> {
                        // TODO the entity spawn causes are not likely valid, need to investigate further.
                        final Cause cause = Cause.source(SpawnCause.builder()
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
        } else if (state == Post.UNWINDING) {
            final IPhaseState unwindingState = phaseContext.firstNamed(InternalNamedCauses.Tracker.UNWINDING_STATE, IPhaseState.class)
                    .orElseThrow(PhaseUtil.throwWithContext("Expected to be unwinding a phase, but no phase found!", phaseContext));
            final PhaseContext unwindingContext = phaseContext.firstNamed(InternalNamedCauses.Tracker.UNWINDING_CONTEXT, PhaseContext.class)
                    .orElseThrow(PhaseUtil.throwWithContext("Expected to be unwinding a phase, but no context found!", phaseContext));
            unwindingState.getPhase().postDispatch(causeTracker, unwindingState, unwindingContext, phaseContext);
        }
    }

    @Override
    public boolean requiresBlockCapturing(IPhaseState currentState) {
        return currentState == Post.UNWINDING;
    }

    @Override
    public boolean ignoresBlockUpdateTick(PhaseData phaseData) {
        return phaseData.getState() == Post.UNWINDING;
    }

    @Override
    public boolean allowEntitySpawns(IPhaseState currentState) {
        return currentState != Post.UNWINDING;
    }

    @Override
    public boolean ignoresBlockEvent(IPhaseState phaseState) {
        return phaseState == Post.UNWINDING;
    }

    @Override
    public boolean alreadyCapturingBlockTicks(IPhaseState state, PhaseContext context) {
        return state == Post.UNWINDING;
    }

    @Override
    public boolean alreadyCapturingEntitySpawns(IPhaseState state) {
        return state == Post.UNWINDING;
    }

    @Override
    public boolean alreadyCapturingEntityTicks(IPhaseState state) {
        return state == Post.UNWINDING;
    }

    @Override
    public boolean alreadyCapturingTileTicks(IPhaseState state) {
        return state == Post.UNWINDING;
    }

    @Override
    public boolean alreadyCapturingItemSpawns(IPhaseState currentState) {
        return currentState == Post.UNWINDING;
    }

    @Override
    public boolean ignoresScheduledUpdates(IPhaseState phaseState) {
        return phaseState == Post.UNWINDING;
    }
}
