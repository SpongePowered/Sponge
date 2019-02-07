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

import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.phase.plugin.BasicPluginContext;
import org.spongepowered.common.event.tracking.phase.plugin.PluginPhase;
import org.spongepowered.common.interfaces.entity.player.IMixinInventoryPlayer;

public class ServerScheduler extends SyncScheduler {

    public ServerScheduler() {
        super("S");
    }

    @Override
    public void tick() {
        super.tick();

        for (Player player : Sponge.getServer().getOnlinePlayers()) {
            if (player instanceof EntityPlayer) {
                // Detect Changes on PlayerInventories marked as dirty.
                ((IMixinInventoryPlayer) ((EntityPlayer) player).inventory).cleanupDirty();
            }
        }
    }

    // TODO: Also track tasks on the client scheduler?

    @Override
    protected void executeTaskRunnable(SpongeScheduledTask task, Runnable runnable) {
        try (BasicPluginContext context = PluginPhase.State.SCHEDULED_TASK.createPhaseContext()
                .source(task)) {
            context.buildAndSwitch();
            super.executeTaskRunnable(task, runnable);
        }
    }

    @Override
    protected PhaseContext<?> createContext(SpongeScheduledTask task, PluginContainer container) {
        return PluginPhase.State.SCHEDULED_TASK.createPhaseContext()
                .source(task)
                .container(container);
    }
}
