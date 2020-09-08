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

class PlayerTickPhaseState extends TickPhaseState<PlayerTickContext> {

    @Override
    protected PlayerTickContext createNewContext(final PhaseTracker tracker) {
        return new PlayerTickContext(tracker)
                .addCaptures()
                .addEntityDropCaptures()
                ;
    }

    @Override
    public void unwind(final PlayerTickContext context) {
        final Player player = context.getSource(Player.class)
                .orElseThrow(TrackingUtil.throwWithContext("Not ticking on a Player!", context));
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(player);
//            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PASSIVE);
//            context.getCapturedEntitySupplier().acceptAndClearIfNotEmpty(entities -> {
//                SpongeCommonEventFactory.callSpawnEntity(entities, context);
//            });
            frame.addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.DROPPED_ITEM);

            TrackingUtil.processBlockCaptures(context);
        }
    }

    @Override
    public void appendContextPreExplosion(final ExplosionContext explosionContext, final PlayerTickContext context) {
        final Player player = context.getSource(Player.class)
                .orElseThrow(TrackingUtil.throwWithContext("Expected to be processing over a ticking TileEntity!", context));
        explosionContext.creator(((ServerPlayer) player).getUser());
        explosionContext.notifier(((ServerPlayer) player).getUser());
        explosionContext.source(player);
    }

    @Override
    public boolean doesDenyChunkRequests(final PlayerTickContext context) {
        return false;
    }
}
