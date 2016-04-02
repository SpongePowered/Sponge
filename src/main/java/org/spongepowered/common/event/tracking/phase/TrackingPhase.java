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

import com.google.common.base.Objects;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.world.World;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public abstract class TrackingPhase {

    @Nullable private final TrackingPhase parent;

    private final List<TrackingPhase> children = new ArrayList<>();

    TrackingPhase(@Nullable TrackingPhase parent) {
        this.parent = parent;
    }

    @Nullable
    public TrackingPhase getParent() {
        return this.parent;
    }

    public List<TrackingPhase> getChildren() {
        return this.children;
    }

    public TrackingPhase addChild(TrackingPhase child) {
        this.children.add(child);
        return this;
    }

    /**
     * The exit point of any phase. Every phase should have an unwinding
     * process where if anything is captured, events should be thrown and
     * processed accordingly. The outcome of each phase is dependent on
     * the {@link IPhaseState} provded, as different states require different
     * handling.
     *
     * <p>Examples of this include: {@link PacketPhase}, {@link WorldPhase}, etc.
     * </p>
     *
     * <p>Note that the {@link CauseTracker} is only provided for easy access
     * to the {@link WorldServer}, {@link IMixinWorldServer}, and
     * {@link World} instances.</p>
     *
     * @param causeTracker The cause tracker instance
     * @param state The state
     * @param phaseContext The context of the current state being unwound
     */
    public abstract void unwind(CauseTracker causeTracker, IPhaseState state, PhaseContext phaseContext);


    /**
     * This is the post dispatch method that is automatically handled for
     * states that deem it necessary to have some post processing for
     * advanced game mechanics. This is always performed when capturing
     * has been turned on during a phases's
     * {@link #unwind(CauseTracker, IPhaseState, PhaseContext)} is
     * dispatched. The rules of post dispatch are as follows:
     * - Entering extra phases is not allowed: This is to avoid
     *  potential recursion in various corner cases.
     * - The unwinding phase context is provided solely as a root
     *  cause tracking for any nested notifications that require
     *  association of causes
     * - The unwinding phase is used with the unwinding state to
     *  further exemplify during what state that was unwinding
     *  caused notifications. This narrows down to the exact cause
     *  of the notifications.
     * - post dispatch may loop several times until no more notifications
     *  are required to be dispatched. This may include block physics for
     *  neighbor notification events.
     *
     * @param causeTracker The cause tracker responsible for the dispatch
     * @param unwindingState The state that was unwinding
     * @param unwindingContext The context of the state that was unwinding,
     *     contains the root cause for the state
     * @param postContext The post dispatch context captures containing any
     *     extra captures that took place during the state's unwinding
     */
    public void postDispatch(CauseTracker causeTracker, IPhaseState unwindingState, PhaseContext unwindingContext, PhaseContext postContext) {

    }

    // Default methods that are basic qualifiers, leaving up to the phase and state to decide
    // whether they perform capturing.

    public boolean requiresBlockCapturing(IPhaseState currentState) {
        return true;
    }

    // TODO
    public boolean ignoresBlockUpdateTick(PhaseData phaseData) {
        return false;
    }

    public boolean allowEntitySpawns(IPhaseState currentState) {
        return true;
    }

    public boolean ignoresBlockEvent(IPhaseState phaseState) {
        return false;
    }

    public boolean ignoresScheduledUpdates(IPhaseState phaseState) {
        return false;
    }

    public boolean alreadyCapturingBlockTicks(IPhaseState phaseState, PhaseContext context) {
        return false;
    }


    public boolean alreadyCapturingEntitySpawns(IPhaseState state) {
        return false;
    }

    public boolean alreadyCapturingEntityTicks(IPhaseState state) {
        return false;
    }

    public boolean alreadyCapturingTileTicks(IPhaseState state) {
        return false;
    }

    public boolean requiresPost(IPhaseState state) {
        return true;
    }

    public boolean alreadyCapturingItemSpawns(IPhaseState currentState) {
        return false;
    }

    // Actual capture methods

    /**
     * This is Step 3 of entity spawning. It is used for the sole purpose of capturing an entity spawn
     * and doesn't actually spawn an entity into the world until the current phase is unwound.
     * The method itself should technically capture entity spawns, however, in the event it
     * is required that the entity cannot be captured, returning {@code false} will mark it
     * to spawn into the world, bypassing any of the bulk spawn events or capturing.
     *
     * <p>NOTE: This method should only be called and handled if and only if {@link #allowEntitySpawns(IPhaseState)}
     * returns {@code true}. Violation of this will have unforseen consequences.</p>
     *
     * @param phaseState The current phase state
     * @param context The current context
     * @param entity The entity being captured
     * @param chunkX The chunk x position
     * @param chunkZ The chunk z position
     * @return True if the entity was successfully captured
     */
    public boolean attemptEntitySpawnCapture(IPhaseState phaseState, PhaseContext context, Entity entity, int chunkX, int chunkZ) {
        final net.minecraft.entity.Entity minecraftEntity = (net.minecraft.entity.Entity) entity;
        final WorldServer minecraftWorld = (WorldServer) minecraftEntity.worldObj;
        TrackingUtil.associateEntityCreator(context, minecraftEntity, minecraftWorld);
        if (minecraftEntity instanceof EntityItem) {
            return context.getCapturedItemsSupplier()
                    .map(supplier -> supplier.get().add(entity))
                    .orElse(false);
        } else {
            return context.getCapturedEntitySupplier()
                    .map(supplier -> supplier.get().add(entity))
                    .orElse(false);
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .toString();
    }


}
