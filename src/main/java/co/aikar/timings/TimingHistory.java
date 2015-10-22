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

import static co.aikar.timings.TimingsManager.FULL_SERVER_TICK;
import static co.aikar.timings.TimingsManager.MINUTE_REPORTS;
import static co.aikar.util.JSONUtil.createObject;
import static co.aikar.util.JSONUtil.pair;
import static co.aikar.util.JSONUtil.toArray;
import static co.aikar.util.JSONUtil.toArrayMapper;
import static co.aikar.util.JSONUtil.toObjectMapper;

import co.aikar.util.JSONUtil.JSONPair;
import co.aikar.util.LoadingMap;
import co.aikar.util.MRUMapCache;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.World;
import org.spongepowered.common.Sponge;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TimingHistory {

    static int idPool = 1;

    final int id = idPool++;
    public static long lastMinuteTime;
    public static long timedTicks;
    public static long playerTicks;
    public static long entityTicks;
    public static long tileEntityTicks;
    public static long activatedEntityTicks;
    static int worldIdPool = 1;
    static Map<String, Integer> worldMap = LoadingMap.newHashMap(new Function<String, Integer>() {

        @Override
        public Integer apply(String input) {
            return worldIdPool++;
        }
    });
    final long endTime;
    final long startTime;
    final long totalTicks;
    final long totalTime; // Represents all time spent running the server this
                          // history
    final MinuteReport[] minuteReports;

    final TimingHistoryEntry[] entries;
    final Set<BlockType> tileEntityTypeSet = Sets.newHashSet();
    final Set<EntityType> entityTypeSet = Sets.newHashSet();
    final Map<Object, Object> worlds;

    TimingHistory() {
        this.endTime = System.currentTimeMillis() / 1000;
        this.startTime = TimingsManager.historyStart / 1000;
        if (timedTicks % 1200 != 0) {
            this.minuteReports = MINUTE_REPORTS.toArray(new MinuteReport[MINUTE_REPORTS.size() + 1]);
            this.minuteReports[this.minuteReports.length - 1] = new MinuteReport();
        } else {
            this.minuteReports = MINUTE_REPORTS.toArray(new MinuteReport[MINUTE_REPORTS.size()]);
        }
        long ticks = 0;
        for (MinuteReport mp : this.minuteReports) {
            ticks += mp.ticksRecord.timed;
        }
        this.totalTicks = ticks;
        this.totalTime = FULL_SERVER_TICK.record.totalTime;
        this.entries = new TimingHistoryEntry[TimingsManager.HANDLERS.size()];

        int i = 0;
        for (TimingHandler handler : TimingsManager.HANDLERS) {
            this.entries[i++] = new TimingHistoryEntry(handler);
        }

        final Map<EntityType, Counter> entityCounts = MRUMapCache.of(LoadingMap.of(Maps.newHashMap(), Counter.LOADER));
        final Map<BlockType, Counter> tileEntityCounts = MRUMapCache.of(LoadingMap.of(Maps.newHashMap(), Counter.LOADER));
        // Information about all loaded chunks/entities
        this.worlds = toObjectMapper(Sponge.getGame().getServer().getWorlds(), new Function<World, JSONPair>() {

            @Override
            public JSONPair apply(World world) {
                return pair(
                        worldMap.get(world.getName()),
                        toArrayMapper(world.getLoadedChunks(), new Function<Chunk, Object>() {

                    @Override
                    public Object apply(Chunk chunk) {
                        entityCounts.clear();
                        tileEntityCounts.clear();

                        for (Entity entity : chunk.getEntities()) {
                            entityCounts.get(entity.getType()).increment();
                        }

                        for (TileEntity tileEntity : chunk.getTileEntities()) {
                            tileEntityCounts.get(tileEntity.getBlock().getType()).increment();
                        }

                        if (tileEntityCounts.isEmpty() && entityCounts.isEmpty()) {
                            return null;
                        }
                        return toArray(
                                chunk.getPosition().getX(),
                                chunk.getPosition().getZ(),
                                toObjectMapper(entityCounts.entrySet(),
                                        new Function<Map.Entry<EntityType, Counter>, JSONPair>() {

                            @Override
                            public JSONPair apply(Map.Entry<EntityType, Counter> entry) {
                                TimingHistory.this.entityTypeSet.add(entry.getKey());
                                return pair(
                                        entry.getKey().getId(),
                                        entry.getValue().count());
                            }
                        }),
                                toObjectMapper(tileEntityCounts.entrySet(),
                                        new Function<Map.Entry<BlockType, Counter>, JSONPair>() {

                            @Override
                            public JSONPair apply(Map.Entry<BlockType, Counter> entry) {
                                TimingHistory.this.tileEntityTypeSet.add(entry.getKey());
                                return pair(
                                        String.valueOf(entry.getKey().getId()),
                                        entry.getValue().count());
                            }
                        }));
                    }
                }));
            }
        });
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

    JSONPair export() {
        return pair(this.id, createObject(
                pair("s", this.startTime),
                pair("e", this.endTime),
                pair("tk", this.totalTicks),
                pair("tm", this.totalTime),
                pair("w", this.worlds),
                pair("h", toArrayMapper(this.entries, new Function<TimingHistoryEntry, Object>() {

                    @Override
                    public Object apply(TimingHistoryEntry entry) {
                        TimingData record = entry.data;
                        if (record.count == 0) {
                            return null;
                        }
                        return entry.export();
                    }
                })),
                pair("mp", toArrayMapper(this.minuteReports, new Function<MinuteReport, Object>() {

                    @Override
                    public Object apply(MinuteReport input) {
                        return input.export();
                    }
                }))));
    }

    static class MinuteReport {

        final long time = System.currentTimeMillis() / 1000;

        final TicksRecord ticksRecord = new TicksRecord();
        final PingRecord pingRecord = new PingRecord();
        final TimingData fst = TimingsManager.FULL_SERVER_TICK.minuteData.clone();
        final double tps = 1E9 / (System.nanoTime() - lastMinuteTime) * this.ticksRecord.timed;

        public List export() {
            return toArray(
                    this.time,
                    Math.round(this.tps * 100D) / 100D,
                    Math.round(this.pingRecord.avg * 100D) / 100D,
                    this.fst.export(),
                    toArray(this.ticksRecord.timed,
                            this.ticksRecord.player,
                            this.ticksRecord.entity,
                            this.ticksRecord.activatedEntity,
                            this.ticksRecord.tileEntity));
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
            final Collection<Player> onlinePlayers = Sponge.getGame().getServer().getOnlinePlayers();
            int totalPing = 0;
            for (Player player : onlinePlayers) {
                totalPing += player.getConnection().getPing();
            }
            this.avg = onlinePlayers.isEmpty() ? 0 : totalPing / onlinePlayers.size();
        }
    }

    static class Counter {

        int count = 0;
        @SuppressWarnings("rawtypes") static Function LOADER = new LoadingMap.Feeder<Counter>() {

            @Override
            public Counter apply() {
                return new Counter();
            }
        };

        public int increment() {
            return ++this.count;
        }

        public int count() {
            return this.count;
        }
    }
}
