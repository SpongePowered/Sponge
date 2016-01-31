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
package org.spongepowered.common.event.spawn;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.data.manipulator.immutable.ImmutableMobSpawnerData;
import org.spongepowered.api.event.cause.entity.spawn.BlockSpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.MobSpawnerSpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.common.AbstractBlockSpawnCauseBuilder;
import org.spongepowered.api.event.cause.entity.spawn.common.AbstractSpawnCauseBuilder;

public class SpongeMobSpawnerSpawnCauseBuilder extends AbstractSpawnCauseBuilder<MobSpawnerSpawnCause, MobSpawnerSpawnCause.Builder>
        implements MobSpawnerSpawnCause.Builder {

    protected ImmutableMobSpawnerData mobSpawnerData;

    @Override
    public MobSpawnerSpawnCause.Builder spawnerData(ImmutableMobSpawnerData spawnerData) {
        this.mobSpawnerData = checkNotNull(spawnerData, "ImmutableMobSpawnerData cannot be null!");
        return this;
    }

    @Override
    public MobSpawnerSpawnCause build() {
        checkState(this.spawnType != null, "SpawnType cannot be null!");
        return new SpongeMobSpawnerSpawnCause(this);
    }

    @Override
    public MobSpawnerSpawnCause.Builder reset() {
        super.reset();
        this.mobSpawnerData = null;
        return this;
    }
}
