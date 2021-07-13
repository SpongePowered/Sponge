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
package org.spongepowered.common.event.tracking.phase.plugin;

import net.minecraft.world.entity.Entity;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.entity.SpawnType;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class BlockWorkerPhaseState extends BasicPluginState {

    @Override
    public BiConsumer<CauseStackManager.StackFrame, BasicPluginContext> getFrameModifier() {
        return super.getFrameModifier();
    }

    BlockWorkerPhaseState() {
    }

    @Override
    public void unwind(final BasicPluginContext phaseContext) {
        TrackingUtil.processBlockCaptures(phaseContext);
    }

    @Override
    public Supplier<SpawnType> getSpawnTypeForTransaction(
        final BasicPluginContext context, final Entity entityToSpawn
    ) {
        return SpawnTypes.PLUGIN;
    }

    @Override
    public boolean isApplyingStreams() {
        return true;
    }

    public @Nullable PhaseContext<@NonNull ?> switchIfNecessary(final PhaseTracker server) {
        final PhaseTracker instance = PhaseTracker.getInstance();
        if (!server.onSidedThread()) {
            return null;
        }
        final IPhaseState<@NonNull ?> currentState = instance.getCurrentState();
        if (currentState.isApplyingStreams())  {
            return null;
        }
        return this.createPhaseContext(server);
    }
}
