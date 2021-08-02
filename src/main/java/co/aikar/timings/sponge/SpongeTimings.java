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
package co.aikar.timings.sponge;

import co.aikar.timings.Timing;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.common.scheduler.AsyncScheduler;
import org.spongepowered.common.scheduler.SpongeScheduledTask;
import org.spongepowered.plugin.PluginContainer;

public final class SpongeTimings {

    private SpongeTimings() {
    }

    /**
     * Gets a timer associated with a plugins tasks.
     *
     * @param task
     * @param period
     * @return
     */
    public static Timing pluginTaskTimings(ScheduledTask task, long period) {
        if (((SpongeScheduledTask) task).getScheduler() instanceof AsyncScheduler) {
            return null;
        }
        PluginContainer plugin = task.owner();

        String name = "Task: " + task.name();
        if (period > 0) {
            name += " (interval:" + period + ")";
        } else {
            name += " (Single)";
        }

        return SpongeTimingsFactory.ofSafe(plugin, name);
    }

    public static Timing entityTiming(final EntityType<?> type) {
        return SpongeTimingsFactory.ofSafe("Minecraft", "## entity - " + EntityType.getKey(type));
    }

    public static Timing blockEntityTiming(final BlockEntityType<?> type) {
        return SpongeTimingsFactory.ofSafe("Minecraft", "## blockEntity - " + BlockEntityType.getKey(type));
    }

    public static Timing pluginTimings(final PluginContainer plugin, final String context) {
        return SpongeTimingsFactory.ofSafe(plugin.metadata().id(), context, TimingsManager.PLUGIN_EVENT_HANDLER);
    }

    public static Timing pluginSchedulerTimings(final PluginContainer plugin) {
        return SpongeTimingsFactory.ofSafe(plugin.metadata().name().orElse(plugin.metadata().id()), TimingsManager.PLUGIN_SCHEDULER_HANDLER);
    }

    public static Timing blockTiming(final BlockType block) {
        final ResourceKey resourceKey = Sponge.game().registries().registry(RegistryTypes.BLOCK_TYPE).valueKey(block);
        return SpongeTimingsFactory.ofSafe("## Scheduled Block: " + resourceKey);
    }
}
