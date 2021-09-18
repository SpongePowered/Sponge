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
package org.spongepowered.common.mixin.tracker.server.players;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.context.GeneralizedContext;
import org.spongepowered.common.event.tracking.phase.block.BlockPhase;
import org.spongepowered.common.event.tracking.phase.player.PlayerPhase;
import org.spongepowered.common.event.tracking.phase.tick.PlayerTickContext;
import org.spongepowered.common.event.tracking.phase.tick.TickPhase;

@Mixin(PlayerList.class)
public class PlayerListMixin_Tracker {

    @Redirect(method = "remove",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;removePlayerImmediately(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/Entity$RemovalReason;)V"))
    private void tracker$trackPlayerLogoutThroughPhaseTracker(final ServerLevel world, final ServerPlayer player,
        final Entity.RemovalReason reason
    ) {
        try (final GeneralizedContext context = PlayerPhase.State.PLAYER_LOGOUT
                .createPhaseContext(PhaseTracker.SERVER)
                .source(player)) {
            context.buildAndSwitch();
            world.removePlayerImmediately(player, reason);
        }
    }

    @Redirect(method = "placeNewPlayer",
              at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;initInventoryMenu()V"))
    private void tracker$onInitMenu(final ServerPlayer player) {
        try (final PhaseContext<?> context = BlockPhase.State.RESTORING_BLOCKS.createPhaseContext(PhaseTracker.SERVER).source(player);) {
            context.buildAndSwitch();
            player.initInventoryMenu();
        }
    }
}
