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

import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.world.World;
import org.spongepowered.common.event.tracking.phase.TrackingPhase;
import org.spongepowered.common.event.tracking.phase.entity.EntityPhase;
import org.spongepowered.common.world.BlockChange;

import javax.annotation.Nullable;

/**
 * A literal phase state of which the {@link World} is currently running
 * in. The state itself is owned by {@link TrackingPhase}s as the phase
 * defines what to do upon
 * {@link TrackingPhase#unwind(CauseTracker, IPhaseState, PhaseContext)}.
 * As these should be enums, there's no data that should be stored on
 * this state. It can have control flow with {@link #canSwitchTo(IPhaseState)}
 * where preventing switching to another state is possible (likely points out
 * either errors or runaway states not being unwound).
 */
public interface IPhaseState {

    TrackingPhase getPhase();

    default boolean canSwitchTo(IPhaseState state) {
        return false;
    }

    default boolean ignoresBlockTracking() {
        return false;
    }

    default void handleBlockChangeWithUser(@Nullable BlockChange blockChange, WorldServer minecraftWorld, Transaction<BlockSnapshot> snapshotTransaction, PhaseContext context) {

    }

    default boolean tracksBlockRestores() {
        return false;
    }

    default boolean tracksBlockSpecificDrops() {
        return false;
    }

    /**
     * A simple boolean switch to whether an {@link net.minecraft.entity.EntityLivingBase#onDeath(DamageSource)}
     * should enter a specific phase to handle the destructed drops until either after this current phase
     * has completed (if returning {@code true}) or whether the entity is going to enter a specific
     * phase directly to handle entity drops (if returning {@code false}). Most all phases should
     * return true, except certain few that require it. The reasoning for a phase to return
     * {@code false} would be if it's own phase can handle entity drops with appropriate causes
     * on it's own.
     *
     * @return True if this phase is aware enough to handle entity death drops per entity, or will
     *     cause {@link EntityPhase.State#DEATH} to be entered and handle it's own drops
     */
    default boolean tracksEntitySpecificDrops() {
        return false;
    }

    default boolean ignoresEntityCollisions() {
        return false;
    }

    default boolean isExpectedForReEntrance() {
        return false;
    }

    default boolean tracksEntityDeaths() {
        return false;
    }

    default boolean shouldCaptureBlockChangeOrSkip(PhaseContext phaseContext, BlockPos pos) {
        return true;
    }
    default boolean isInteraction() {
        return false;
    }
}
