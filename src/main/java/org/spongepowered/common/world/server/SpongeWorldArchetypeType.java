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
package org.spongepowered.common.world.server;

import net.minecraft.core.Holder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import org.spongepowered.api.world.WorldType;
import org.spongepowered.api.world.generation.ChunkGenerator;
import org.spongepowered.api.world.server.WorldArchetypeType;

public final class SpongeWorldArchetypeType implements WorldArchetypeType.Builder {

    private WorldType worldType;
    private ChunkGenerator chunkGenerator;

    public SpongeWorldArchetypeType() {
        this.reset();
    }

    @Override
    public WorldArchetypeType.Builder worldType(final WorldType worldType) {
        this.worldType = worldType;
        return this;
    }

    @Override
    public WorldArchetypeType.Builder chunkGenerator(final ChunkGenerator chunkGenerator) {
        this.chunkGenerator = chunkGenerator;
        return this;
    }

    @Override
    public WorldArchetypeType.Builder from(final WorldArchetypeType value) {
        this.worldType = value.worldType();
        this.chunkGenerator = value.chunkGenerator();
        return this;
    }

    @Override
    public WorldArchetypeType.Builder reset() {
        this.worldType = null;
        this.chunkGenerator = null;
        return this;
    }

    @Override
    public WorldArchetypeType build() {
        return (WorldArchetypeType) (Object) new LevelStem(Holder.direct((DimensionType) (Object) this.worldType), (net.minecraft.world.level.chunk.ChunkGenerator) this.chunkGenerator);
    }
}
