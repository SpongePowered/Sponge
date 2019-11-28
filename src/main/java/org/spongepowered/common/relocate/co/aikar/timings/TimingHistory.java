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

import org.spongepowered.common.relocate.co.aikar.timings.TimingHistory.Counter;
import org.spongepowered.common.relocate.co.aikar.timings.TimingHistory.MinuteReport;
import org.spongepowered.common.relocate.co.aikar.timings.TimingHistory.PingRecord;
import org.spongepowered.common.relocate.co.aikar.timings.TimingHistory.RegionData;
import org.spongepowered.common.relocate.co.aikar.timings.TimingHistory.RegionData.RegionId;
import org.spongepowered.common.relocate.co.aikar.timings.TimingHistory.TicksRecord;
import org.spongepowered.common.relocate.co.aikar.util.JSONUtil;
import org.spongepowered.common.relocate.co.aikar.util.LoadingMap;
import org.spongepowered.common.relocate.co.aikar.util.MRUMapCache;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.BlockEntityType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.common.SpongeImpl;

import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class TimingHistory {

    public static long lastMinuteTime;
    public static long timedTicks;
    public static long playerTicks;
    public static long entityTicks;
    public static long tileEntityTicks;
    public static long activatedEntityTicks;
    static int worldIdPool = 1;
    static Map<String, Integer> worldMap = LoadingMap.newHashMap((input) -> worldIdPool++);
    final long endTime;
    final long startTime;
    final long totalTicks;
    // Represents all time spent running the server this history
    final long totalTime;
    final MinuteReport[] minuteReports;

    final TimingHistoryEntry[] entries;
    final Set<BlockEntityType> tileEntityTypeSet = Sets.newHashSet();
    final Set<EntityType> entityTypeSet = Sets.newHashSet();
    final JsonObject worlds;

    TimingHistory() {
        this.endTime = System.currentTimeMillis() / 1000;
        this.startTime = TimingsManager.historyStart / 1000;
        if (timedTicks % 1200 != 0 || TimingsManager.MINUTE_REPORTS.isEmpty()) {
            this.minuteReports = TimingsManager.MINUTE_REPORTS.toArray(new MinuteReport[TimingsManager.MINUTE_REPORTS.size() + 1]);
            this.minuteReports[this.minuteReports.length - 1] = new MinuteReport();
        } else {
            this.minuteReports = TimingsManager.MINUTE_REPORTS.toArray(new MinuteReport[TimingsManager.MINUTE_REPORTS.size()]);
        }
        long ticks = 0;
        for (MinuteReport mp : this.minuteReports) {
            ticks += mp.ticksRecord.timed;
        }
        this.totalTicks = ticks;
        this.totalTime = TimingsManager.FULL_SERVER_TICK.record.getTotalTime();
        this.entries = new TimingHistoryEntry[TimingsManager.HANDLERS.size()];

        int i = 0;
        for (TimingHandler handler : TimingsManager.HANDLERS) {
            this.entries[i++] = new TimingHistoryEntry(handler);
        }

        // Information about all loaded chunks/entities
        this.worlds = JSONUtil.mapArrayToObject(SpongeImpl.getGame().getServer().getWorlds(), (world) -> {
            Map<RegionId, RegionData> regions = LoadingMap.newHashMap(RegionData.LOADER);
            return JSONUtil.singleObjectPair(String.valueOf(worldMap.get(world.getName())), JSONUtil.mapArray(world.getLoadedChunks(), (chunk) -> {
                RegionData data = regions.get(new RegionId(chunk.getPosition().getX(), chunk.getPosition().getZ()));

                for (Entity entity : chunk.getEntities()) {
                    if (entity.getType() == null) {
                        continue;
                    }
                    data.entityCounts.get(entity.getType()).increment();
                }

                for (BlockEntity tileEntity : chunk.getTileEntities()) {
                    if (tileEntity.getType() == null) {
                        continue;
                    }
                    data.tileEntityCounts.get(tileEntity.getType()).increment();
                }

                if (data.tileEntityCounts.isEmpty() && data.entityCounts.isEmpty()) {
                    return null;
                }
                return JSONUtil.arrayOf(
                        chunk.getPosition().getX(),
                        chunk.getPosition().getZ(),
                        JSONUtil.mapArrayToObject(data.entityCounts.entrySet(), (entry) -> {
                            if (entry.getKey() == EntityTypes.UNKNOWN) {
                                return null;
                            }
                            this.entityTypeSet.add(entry.getKey());
                            return JSONUtil.singleObjectPair(TimingsPls.getEntityId(entry.getKey()), entry.getValue().count());
                        }),
                        JSONUtil.mapArrayToObject(data.tileEntityCounts.entrySet(), (entry) -> {
                            this.tileEntityTypeSet.add(entry.getKey());
                            return JSONUtil.singleObjectPair(TimingsPls.getTileEntityId(entry.getKey()), entry.getValue().count());
                        }));
            }));
        });
    }
    static class RegionData {
        final RegionId regionId;
        @SuppressWarnings("Guava")
        static Function<RegionId, RegionData> LOADER = new Function<RegionId, RegionData>() {
            @Override
            public RegionData apply(RegionId id) {
                return new RegionData(id);
            }
        };
        RegionData(RegionId id) {
            this.regionId = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            RegionData that = (RegionData) o;

            return regionId.equals(that.regionId);

        }

        final Map<EntityType, Counter> entityCounts = MRUMapCache.of(LoadingMap.of(Maps.newHashMap(), Counter.loader()));
        final Map<BlockEntityType, Counter> tileEntityCounts = MRUMapCache.of(LoadingMap.of(Maps.newHashMap(), Counter.loader()));

        @Override
        public int hashCode() {
            return regionId.hashCode();
        }

        static class RegionId {
            final int x, z;
            final long regionId;
            RegionId(int x, int z) {
                this.x = x >> 5 << 5;
                this.z = z >> 5 << 5;
                this.regionId = ((long) (this.x) << 32) + (this.z >> 5 << 5) - Integer.MIN_VALUE;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                RegionId regionId1 = (RegionId) o;

                return regionId == regionId1.regionId;

            }

            @Override
            public int hashCode() {
                return (int) (regionId ^ (regionId >>> 32));
            }
        }
    }

    public static void resetTicks(boolean fullReset) {
        if (fullReset) {
            // Non full is simply for 1 minute reports
            timedTicks = 0;
        }
        lastMinuteTime = System.nanoTime();
        playerTicks = 0;
        tileEntityTicks = 0;
        entityTicks = 0;
        activatedEntityTicks = 0;
    }

    JsonObject export() {
        return JSONUtil.objectBuilder()
                .add("s", this.startTime)
                .add("e", this.endTime)
                .add("tk", this.totalTicks)
                .add("tm", this.totalTime)
                .add("w", this.worlds)
                .add("h", JSONUtil.mapArray(this.entries, (entry) -> entry.data.count == 0 ? null : entry.export()))
                .add("mp", JSONUtil.mapArray(this.minuteReports, MinuteReport::export))
                .build();
    }

    static class MinuteReport {

        final long time = System.currentTimeMillis() / 1000;

        final TicksRecord ticksRecord = new TicksRecord();
        final PingRecord pingRecord = new PingRecord();
        final TimingData fst = TimingsManager.FULL_SERVER_TICK.minuteData.clone();
        final double tps = 1E9 / (System.nanoTime() - lastMinuteTime) * this.ticksRecord.timed;
        final double usedMemory = TimingsManager.FULL_SERVER_TICK.avgUsedMemory;
        final double freeMemory = TimingsManager.FULL_SERVER_TICK.avgFreeMemory;
        final double loadAvg = ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();

        public JsonArray export() {
            return JSONUtil.arrayOf(
                    this.time,
                    Math.round(this.tps * 100D) / 100D,
                    Math.round(this.pingRecord.avg * 100D) / 100D,
                    this.fst.export(),
                    JSONUtil.arrayOf(this.ticksRecord.timed,
                            this.ticksRecord.player,
                            this.ticksRecord.entity,
                            this.ticksRecord.activatedEntity,
                            this.ticksRecord.tileEntity),
                    this.usedMemory,
                    this.freeMemory,
                    this.loadAvg);
        }
    }

    static class TicksRecord {

        final long timed;
        final long player;
        final long entity;
        final long tileEntity;
        final long activatedEntity;

        TicksRecord() {
            this.timed = timedTicks - (TimingsManager.MINUTE_REPORTS.size() * 1200);
            this.player = playerTicks;
            this.entity = entityTicks;
            this.tileEntity = tileEntityTicks;
            this.activatedEntity = activatedEntityTicks;
        }

    }

    static class PingRecord {

        final double avg;

        PingRecord() {
            final Collection<Player> onlinePlayers = SpongeImpl.getGame().getServer().getOnlinePlayers();
            int totalPing = 0;
            for (Player player : onlinePlayers) {
                totalPing += player.getConnection().getLatency();
            }
            this.avg = onlinePlayers.isEmpty() ? 0 : totalPing / onlinePlayers.size();
        }
    }

    static class Counter {

        int count = 0;
        private static final Function<?, Counter> LOADER = new LoadingMap.Feeder<Counter>() {

            @Override
            public Counter apply() {
                return new Counter();
            }
        };

        @SuppressWarnings("unchecked")
        static <T> Function<T, Counter> loader() {
            return (Function<T, Counter>) LOADER;
        }

        public int increment() {
            return ++this.count;
        }

        public int count() {
            return this.count;
        }
    }
}
