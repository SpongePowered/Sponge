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

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.cause.entity.SpawnTypes;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.general.ExplosionContext;

import java.util.function.BiConsumer;

class PlayerTickPhaseState extends TickPhaseState<PlayerTickContext> {

    private final BiConsumer<CauseStackManager.StackFrame, PlayerTickContext> FRAME_MODIFIER = super.getFrameModifier()
        .andThen((frame, context) -> {
            context.getSource(Player.class).ifPresent(frame::pushCause);
        });

    @Override
    protected PlayerTickContext createNewContext(final PhaseTracker tracker) {
        return new PlayerTickContext(tracker);
    }

    @Override
    public BiConsumer<CauseStackManager.StackFrame, PlayerTickContext> getFrameModifier() {
        return this.FRAME_MODIFIER;
    }

    @Override
    public void unwind(final PlayerTickContext context) {
        TrackingUtil.processBlockCaptures(context);
    }

    @Override
    public void appendContextPreExplosion(final ExplosionContext explosionContext, final PlayerTickContext context) {
        final Player player = context.getSource(Player.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected to be processing over a ticking TileEntity!", context));
        explosionContext.creator(((ServerPlayer) player).user());
        explosionContext.notifier(((ServerPlayer) player).user());
        explosionContext.source(player);
    }

}
