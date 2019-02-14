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
package org.spongepowered.common.relocate.co.aikar.timings;

import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import com.google.common.collect.EvictingQueue;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.relocate.co.aikar.util.LoadingMap;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class TimingsManager {

    static final Map<TimingIdentifier, TimingHandler> TIMING_MAP = Collections.synchronizedMap(
            LoadingMap.newHashMap((id) -> (id.protect ? new UnsafeTimingHandler(id) : new TimingHandler(id)), 256, .5F));
    public static final FullServerTickHandler FULL_SERVER_TICK = new FullServerTickHandler();
    public static final TimingHandler TIMINGS_TICK = SpongeTimingsFactory.ofSafe("Timings Tick", FULL_SERVER_TICK);
    public static final Timing DATA_GROUP_HANDLER = SpongeTimingsFactory.ofSafe("Data");
    public static final Timing MOD_EVENT_HANDLER = SpongeTimingsFactory.ofSafe("Mod Events");
    public static final Timing PLUGIN_SCHEDULER_HANDLER = SpongeTimingsFactory.ofSafe("Plugin Scheduler");
    public static final Timing PLUGIN_EVENT_HANDLER = SpongeTimingsFactory.ofSafe("Plugin Events");
    public static final Timing PLUGIN_GROUP_HANDLER = SpongeTimingsFactory.ofSafe("Plugins");
    public static List<String> hiddenConfigs = new ArrayList<>();
    public static boolean privacy = false;

    static final Collection<TimingHandler> HANDLERS = new ArrayDeque<>();
    static final ArrayDeque<TimingHistory.MinuteReport> MINUTE_REPORTS = new ArrayDeque<>();

    static EvictingQueue<TimingHistory> HISTORY = EvictingQueue.create(12);
    static TimingHandler CURRENT;
    static long timingStart = 0;
    static long historyStart = 0;
    static boolean needsFullReset = false;
    static boolean needsRecheckEnabled = false;

    private TimingsManager() {
    }

    /**
     * Resets all timing data on the next tick
     */
    static void reset() {
        needsFullReset = true;
    }

    /**
     * Counts the number of times a timer caused TPS loss.
     */
    static void tick() {
        if (Timings.isTimingsEnabled()) {
            boolean violated = FULL_SERVER_TICK.isViolated();

            for (TimingHandler handler : HANDLERS) {
                if (handler.isSpecial()) {
                    // We manually call this
                    continue;
                }
                handler.processTick(violated);
            }

            TimingHistory.playerTicks += SpongeImpl.getGame().getServer().getOnlinePlayers().size();
            TimingHistory.timedTicks++;
            // Generate TPS/Ping/Tick reports every minute
        }
    }

    static void stopServer() {
        Timings.setTimingsEnabled(false);
        recheckEnabled();
    }

    static void recheckEnabled() {
        synchronized (TIMING_MAP) {
            for (TimingHandler timings : TIMING_MAP.values()) {
                timings.checkEnabled();
            }
        }
        needsRecheckEnabled = false;
    }

    static void resetTimings() {
        if (needsFullReset) {
            // Full resets need to re-check every handlers enabled state
            // Timing map can be modified from async so we must sync on it.
            synchronized (TIMING_MAP) {
                for (TimingHandler timings : TIMING_MAP.values()) {
                    timings.reset(true);
                }
            }
            if (timingStart != 0) {
                SpongeImpl.getLogger().info("Timings reset");
            }
            HISTORY.clear();
            needsFullReset = false;
            needsRecheckEnabled = false;
            timingStart = System.currentTimeMillis();
        } else {
            // Soft resets only need to act on timings that have done something
            // Handlers can only be modified on main thread.
            for (TimingHandler timings : HANDLERS) {
                timings.reset(false);
            }
        }

        HANDLERS.clear();
        MINUTE_REPORTS.clear();

        TimingHistory.resetTicks(true);
        historyStart = System.currentTimeMillis();
    }

    static TimingHandler getHandler(String group, String name, Timing parent, boolean protect) {
        return TIMING_MAP.get(new TimingIdentifier(group, name, parent, protect));
    }

    // TODO Revise this
    public static Timing getCommandTiming(String pluginName, CommandMapping command) {
        Optional<PluginContainer> plugin = Optional.empty();
        if (!("minecraft".equals(pluginName)
                || "bukkit".equals(pluginName)
                || "Spigot".equals(pluginName))) {
            plugin = SpongeImpl.getGame().getPluginManager().getPlugin(pluginName);
        }
        if (!plugin.isPresent()) {
            return SpongeTimingsFactory.ofSafe("Command: " + pluginName + ":" + command.getPrimaryAlias());
        }

        return SpongeTimingsFactory.ofSafe(plugin.get(), "Command: " + pluginName + ":" + command.getPrimaryAlias());
    }

}
