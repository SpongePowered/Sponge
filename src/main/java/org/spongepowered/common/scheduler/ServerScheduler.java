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
package org.spongepowered.common.scheduler;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.common.bridge.world.entity.player.PlayerInventoryBridge;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.plugin.BasicPluginContext;
import org.spongepowered.common.event.tracking.phase.plugin.PluginPhase;
import org.spongepowered.plugin.PluginContainer;

public final class ServerScheduler extends SyncScheduler {

    public ServerScheduler() {
        super("S");
    }

    @Override
    public void tick() {
        super.tick();

        for (final Player player : Sponge.server().onlinePlayers()) {
            if (player instanceof net.minecraft.world.entity.player.Player) {
                // Detect Changes on PlayerInventories marked as dirty.
                ((PlayerInventoryBridge) ((net.minecraft.world.entity.player.Player) player).inventory).bridge$cleanupDirty();
            }
        }
    }

    @Override
    protected void executeTaskRunnable(final SpongeScheduledTask task, final Runnable runnable) {
        try (final BasicPluginContext context = PluginPhase.State.SCHEDULED_TASK.createPhaseContext(PhaseTracker.SERVER)
                .source(task)) {
            context.buildAndSwitch();
            super.executeTaskRunnable(task, runnable);
        }
    }

    @Override
    protected PhaseContext<?> createContext(final SpongeScheduledTask task, final PluginContainer container) {
        return PluginPhase.State.SCHEDULED_TASK.createPhaseContext(PhaseTracker.SERVER)
                .source(task)
                .container(container);
    }
}
