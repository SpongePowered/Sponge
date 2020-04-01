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
package org.spongepowered.common.bridge.world;

import net.minecraft.entity.Entity;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.SessionLockException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.world.explosion.Explosion;
import org.spongepowered.api.world.weather.Weather;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.relocate.co.aikar.timings.WorldTimingsHandler;
import org.spongepowered.math.vector.Vector3d;

import java.util.function.Function;

public interface ServerWorldBridge {

    void bridge$setPreviousWeather(Weather weather);

    void bridge$updateRotation(Entity entityIn);

    void bridge$addEntityRotationUpdate(Entity entity, Vector3d rotation);

    boolean bridge$isLightLevel(Chunk chunk, BlockPos pos, int level);

    WorldTimingsHandler bridge$getTimingsHandler();

    int bridge$getChunkGCTickInterval();

    long bridge$getChunkUnloadDelay();

    net.minecraft.world.Explosion bridge$triggerInternalExplosion(Explosion explosion,
        Function<? super net.minecraft.world.Explosion, ? extends PhaseContext<?>> contextCreator);

    void bridge$doChunkGC();

    void bridge$incrementChunkLoadCount();

    void bridge$updateConfigCache();

    long bridge$getWeatherStartTime();

    void bridge$setWeatherStartTime(long start);

    void bridge$saveChunksAndProperties(@Nullable IProgressUpdate update, boolean flush, boolean saveChunks) throws SessionLockException;
}
