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
package co.aikar.timings;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.EvictingQueue;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.common.Sponge;

import java.util.Optional;
import java.util.Queue;

public class SpongeTimingsFactory implements TimingsFactory {

    private static final int MAX_HISTORY_FRAMES = 12;
    public final Timing NULL_HANDLER = new NullTimingHandler();
    static boolean timingsEnabled = false;
    static boolean verboseEnabled = false;
    private static int historyInterval = -1;
    private static int historyLength = -1;

    public SpongeTimingsFactory() {
    }

    private static PluginContainer checkPlugin(Object plugin) {
        Optional<PluginContainer> optPlugin = Sponge.getGame().getPluginManager().fromInstance(plugin);
        checkArgument(optPlugin.isPresent(), "Provided object is not a plugin instance");
        return optPlugin.get();
    }

    @Override
    public Timing of(Object pluginObj, String name, Timing groupHandler) {
        PluginContainer plugin = checkPlugin(pluginObj);
        if (groupHandler == null) {
            groupHandler = ofSafe(plugin.getName(), "Combined Total", TimingsManager.PLUGIN_GROUP_HANDLER);
        }
        return TimingsManager.getHandler(plugin.getName(), name, groupHandler, true);
    }

    /**
     * Returns a Timing object after starting it, useful for Java7
     * try-with-resources.
     *
     * try (Timing ignored = Timings.ofStart(plugin, someName, groupHandler)) {
     * // timed section }
     *
     * @param plugin Plugin to own the Timing
     * @param name Name of Timing
     * @param groupHandler Parent handler to mirror .start/stop calls to
     * @return Timing Handler
     */
    @Override
    public Timing ofStart(Object plugin, String name, Timing groupHandler) {
        Timing timing = of(plugin, name, groupHandler);
        timing.startTimingIfSync();
        return timing;
    }

    /**
     * Gets whether or not the Spigot Timings system is enabled
     *
     * @return Enabled or not
     */
    @Override
    public boolean isTimingsEnabled() {
        return timingsEnabled;
    }

    /**
     * Sets whether or not the Spigot Timings system should be enabled <p/>
     * Calling this will reset timing data.
     *
     * @param enabled Should timings be reported
     */
    @Override
    public void setTimingsEnabled(boolean enabled) {
        timingsEnabled = enabled;
        reset();
    }

    /**
     * Gets whether or not the Verbose level of timings is enabled. <p/> When
     * Verbose is disabled, high-frequency timings will not be available
     *
     * @return Enabled or not
     */
    @Override
    public boolean isVerboseTimingsEnabled() {
        return timingsEnabled;
    }

    /**
     * Sets whether or not the Timings should monitor at Verbose level. <p/>
     * When Verbose is disabled, high-frequency timings will not be available.
     * Calling this will reset timing data.
     *
     * @param enabled Should high-frequency timings be reported
     */
    @Override
    public void setVerboseTimingsEnabled(boolean enabled) {
        verboseEnabled = enabled;
        TimingsManager.needsRecheckEnabled = true;
    }

    /**
     * Gets the interval between Timing History report generation. <p/> Defaults
     * to 5 minutes (6000 ticks)
     *
     * @return Interval in ticks
     */
    @Override
    public int getHistoryInterval() {
        return historyInterval;
    }

    /**
     * Sets the interval between Timing History report generations. <p/>
     * Defaults to 5 minutes (6000 ticks)
     *
     * This will recheck your history length, so lowering this value will lower
     * your history length if you need more than 60 history windows.
     *
     * @param interval Interval in ticks
     */
    @Override
    public void setHistoryInterval(int interval) {
        historyInterval = Math.max(20 * 60, interval);
        // Recheck the history length with the new Interval
        if (historyLength != -1) {
            setHistoryLength(historyLength);
        }
    }

    /**
     * Gets how long in ticks Timings history is kept for the server.
     *
     * Defaults to 1 hour (72000 ticks)
     *
     * @return Duration in Ticks
     */
    @Override
    public int getHistoryLength() {
        return historyLength;
    }

    /**
     * Sets how long Timing History reports are kept for the server.
     *
     * Defaults to 1 hours(72000 ticks)
     *
     * This value is capped at a maximum of getHistoryInterval() *
     * MAX_HISTORY_FRAMES (12)
     *
     * Will not reset Timing Data but may truncate old history if the new length
     * is less than old length.
     *
     * @param length Duration in ticks
     */
    @Override
    public void setHistoryLength(int length) {
        // Cap at 12 History Frames, 1 hour at 5 minute frames.
        int maxLength = historyInterval * MAX_HISTORY_FRAMES;
        // For special cases of servers with special permission to bypass the
        // max.
        // This max helps keep data file sizes reasonable for processing on
        // Aikar's Timing parser side.
        // Setting this will not help you bypass the max unless Aikar has added
        // an exception on the API side.
        if (System.getProperty("timings.bypassMax") != null) {
            maxLength = Integer.MAX_VALUE;
        }
        historyLength = Math.max(Math.min(maxLength, length), historyInterval);
        Queue<TimingHistory> oldQueue = TimingsManager.HISTORY;
        int frames = (getHistoryLength() / getHistoryInterval());
        if (length > maxLength) {
            Sponge.getLogger().warn(
                    "Timings Length too high. Requested " + length + ", max is " + maxLength
                            + ". To get longer history, you must increase your interval. Set Interval to " + Math.ceil(length / MAX_HISTORY_FRAMES)
                            + " to achieve this length.");
        }
        TimingsManager.HISTORY = EvictingQueue.create(frames);
        TimingsManager.HISTORY.addAll(oldQueue);
    }

    /**
     * Resets all Timing Data
     */
    @Override
    public void reset() {
        TimingsManager.reset();
    }

    /**
     * Generates a report and sends it to the specified command sender.
     *
     * If sender is null, ConsoleCommandSender will be used.
     *
     * @param sender
     */
    @Override
    public void generateReport(CommandSource sender) {
        if (sender == null) {
            sender = Sponge.getGame().getServer().getConsole();
        }
        TimingsExport.reportTimings(sender);
    }

    /*
     * ================= Protected API: These are for internal use only in
     * Bukkit/CraftBukkit These do not have isPrimaryThread() checks in the
     * startTiming/stopTiming =================
     */

    static TimingHandler ofSafe(String name) {
        return ofSafe(null, name, null);
    }

    static Timing ofSafe(PluginContainer plugin, String name) {
        Timing pluginHandler = null;
        if (plugin != null) {
            pluginHandler = ofSafe(plugin.getName(), "Combined Total", TimingsManager.PLUGIN_GROUP_HANDLER);
        }
        return ofSafe(plugin != null ? plugin.getName() : "Minecraft - Invalid Plugin", name, pluginHandler);
    }

    static TimingHandler ofSafe(String name, Timing groupHandler) {
        return ofSafe(null, name, groupHandler);
    }

    static TimingHandler ofSafe(String groupName, String name, Timing groupHandler) {
        return TimingsManager.getHandler(groupName, name, groupHandler, false);
    }
}
