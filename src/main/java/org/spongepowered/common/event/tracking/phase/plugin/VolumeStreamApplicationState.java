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
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.entity.SpawnType;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.api.world.volume.stream.VolumeStream;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

class VolumeStreamApplicationState extends PluginPhaseState<VolumeStreamApplicationState.Context> {

    private final BiConsumer<CauseStackManager.StackFrame, Context> PLUGIN_MODIFIER = super.getFrameModifier()
        .andThen((frame, context) -> {
            if (context.stream != null) {
                frame.pushCause(context.stream);
            }
            context.getSource(Object.class).ifPresent(frame::pushCause);
        });

    @Override
    public Context createNewContext(final PhaseTracker tracker) {
        return new Context(this, tracker);
    }

    @Override
    public BiConsumer<CauseStackManager.StackFrame, Context> getFrameModifier() {
        return this.PLUGIN_MODIFIER;
    }

    @Override
    public void unwind(final Context phaseContext) {
        TrackingUtil.processBlockCaptures(phaseContext);
    }

    @Override
    public Supplier<SpawnType> getSpawnTypeForTransaction(
        final Context context, final Entity entityToSpawn
    ) {
        return context.spawnTypeSupplier;
    }

    @Override
    public boolean isApplyingStreams() {
        return true;
    }

    public static class Context extends PluginPhaseContext<Context> {

        @MonotonicNonNull VolumeStream<@NonNull ?, ?> stream;
        @MonotonicNonNull Supplier<SpawnType> spawnTypeSupplier;

        Context(final IPhaseState<Context> phaseState, final PhaseTracker tracker) {
            super(phaseState, tracker);
        }

        public Context setVolumeStream(
            final VolumeStream<@NonNull ?, ?> stream
        ) {
            this.stream = stream;
            return this;
        }

        public Context spawnType(final Supplier<@Nullable SpawnType> supplier) {
            final @Nullable SpawnType spawnType = supplier.get();
            if (spawnType == null) {
                this.spawnTypeSupplier = SpawnTypes.PLUGIN;
            } else {
                this.spawnTypeSupplier = () -> spawnType;
            }
            return this;
        }

        @Override
        protected void reset() {
            super.reset();
            this.stream = null;
            this.spawnTypeSupplier = null;
        }
    }
}
