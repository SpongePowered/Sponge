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
package org.spongepowered.common.world;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.storage.ChunkLayout;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.Objects;
import java.util.Optional;

public final class SpongeServerLocationFactory implements ServerLocation.Factory {

    public static final SpongeServerLocationFactory INSTANCE = new SpongeServerLocationFactory();

    private SpongeServerLocationFactory() {}

    @Override
    public ServerLocation create(final ServerWorld world, final Vector3d position) {
        Objects.requireNonNull(world);
        Objects.requireNonNull(position);

        return new SpongeServerLocation(world, world.getEngine().getChunkLayout(), position);
    }

    @Override
    public ServerLocation create(final ServerWorld world, final Vector3i blockPosition) {
        Objects.requireNonNull(world);
        Objects.requireNonNull(blockPosition);

        final ChunkLayout chunkLayout = world.getEngine().getChunkLayout();
        final Vector3d position = blockPosition.toDouble();
        return new SpongeServerLocation(world, chunkLayout, position);
    }

    @Override
    public ServerLocation create(final ResourceKey worldKey, final Vector3d position) {
        Objects.requireNonNull(worldKey);
        Objects.requireNonNull(position);

        final Optional<ServerWorld> world = Sponge.getServer().getWorldManager().getWorld(worldKey);
        if (!world.isPresent()) {
            throw new IllegalStateException("Unknown world for key: " + worldKey.toString());
        }
        return new SpongeServerLocation(world.get(), world.get().getEngine().getChunkLayout(), position);
    }

    @Override
    public ServerLocation create(final ResourceKey worldKey, final Vector3i blockPosition) {
        Objects.requireNonNull(worldKey);
        Objects.requireNonNull(blockPosition);

        final Optional<ServerWorld> world = Sponge.getServer().getWorldManager().getWorld(worldKey);
        if (!world.isPresent()) {
            throw new IllegalStateException("Unknown world for key: " + worldKey.toString());
        }
        return new SpongeServerLocation(world.get(), world.get().getEngine().getChunkLayout(), blockPosition.toDouble());
    }
}
