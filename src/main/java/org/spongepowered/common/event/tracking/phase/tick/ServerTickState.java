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
package org.spongepowered.common.event.tracking.phase.tick;

import net.minecraft.server.MinecraftServer;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;

import java.lang.ref.WeakReference;
import java.util.function.BiConsumer;

public final class ServerTickState extends TickPhaseState<ServerTickState.ServerTickContext> {

    private final BiConsumer<CauseStackManager.StackFrame, ServerTickState.ServerTickContext> WORLD_MODIFIER = super.getFrameModifier()
        .andThen((frame, context) -> {
            context.getSource(Object.class).ifPresent(frame::pushCause);
            final @Nullable MinecraftServer server = context.server.get();
            if (server != null) {
                frame.pushCause(server);
            }
        });

    @Override
    protected ServerTickContext createNewContext(final PhaseTracker tracker) {
        return new ServerTickContext(this, tracker);
    }

    @Override
    public BiConsumer<CauseStackManager.StackFrame, ServerTickState.ServerTickContext> getFrameModifier() {
        return this.WORLD_MODIFIER;
    }

    @Override
    public void unwind(final ServerTickContext phaseContext) {
        TrackingUtil.processBlockCaptures(phaseContext);
    }

    public static class ServerTickContext extends TickContext<ServerTickContext> {

        @MonotonicNonNull WeakReference<MinecraftServer> server;

        public ServerTickContext server(final MinecraftServer server) {
            this.server = new WeakReference<>(server);
            return this;
        }


        ServerTickContext(final IPhaseState<? extends ServerTickContext> phaseState, final PhaseTracker tracker) {
            super(phaseState, tracker);
        }
    }
}
