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

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.fluid.FluidState;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.storage.ChunkLayout;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

public class SpongeLocation<W extends World<W>> implements Location<W> {

    final WeakReference<W> worldRef;
    private final Vector3d position;
    private final Vector3i blockPosition;
    private final Vector3i chunkPosition;
    private final Vector3i biomePosition;

    SpongeLocation(final W world, final ChunkLayout chunkLayout, final Vector3d position) {
        this.worldRef = new WeakReference<>(world);
        this.position = position;
        this.blockPosition = position.toInt();
        this.chunkPosition = chunkLayout.forceToChunk(this.blockPosition);
        this.biomePosition = position.toInt().mul(1, 0, 1);
    }

    SpongeLocation(final W worldRef, final Vector3d position,
        final Vector3i chunkPosition, final Vector3i biomePosition) {
        this.worldRef = new WeakReference<>(worldRef);
        this.position = position;
        this.blockPosition = position.toInt();
        this.chunkPosition = chunkPosition;
        this.biomePosition = biomePosition;
    }

    @Override
    public W getWorld() {
        final W world = this.worldRef.get();
        if (world == null) {
            throw new IllegalStateException("World Reference is no longer valid!");
        }
        return world;
    }

    @Override
    public Optional<W> getWorldIfAvailable() {
        return Optional.ofNullable(this.worldRef.get());
    }

    @Override
    public boolean isAvailable() {
        return this.worldRef.get() != null;
    }

    @Override
    public boolean isValid() {
        return this.worldRef.get() != null;
    }

    @Override
    public Vector3d getPosition() {
        return this.position;
    }

    @Override
    public Vector3i getBlockPosition() {
        return this.blockPosition;
    }

    @Override
    public Vector3i getChunkPosition() {
        return this.chunkPosition;
    }

    @Override
    public Vector3i getBiomePosition() {
        return this.biomePosition;
    }

    @Override
    public double getX() {
        return this.position.getX();
    }

    @Override
    public double getY() {
        return this.position.getY();
    }

    @Override
    public double getZ() {
        return this.getPosition().getZ();
    }

    @Override
    public int getBlockX() {
        return this.blockPosition.getX();
    }

    @Override
    public int getBlockY() {
        return this.blockPosition.getY();
    }

    @Override
    public int getBlockZ() {
        return this.blockPosition.getZ();
    }

    @Override
    public boolean inWorld(final W world) {
        return this.worldRef.get() == world;
    }

    @Override
    public Location<W> withWorld(final W world) {
        return new SpongeLocation<W>(world, this.position, this.chunkPosition, this.biomePosition);
    }

    @Override
    public Location<W> withPosition(final Vector3d position) {
        final W world = this.worldRef.get();
        if (world == null) {
            throw new IllegalStateException("World Reference is null!");
        }
        // TODO - for now, we make the assumption we always have a server object if we're creating locations
        final ChunkLayout chunkLayout = Sponge.getServer().getChunkLayout();
        return new SpongeLocation<>(world, chunkLayout, position);
    }

    @Override
    public Location<W> withBlockPosition(final Vector3i position) {
        final W world = this.worldRef.get();
        if (world == null) {
            throw new IllegalStateException("World Reference is null!");
        }
        // TODO - for now, we make the assumption we always have a server object if we're creating locations
        final ChunkLayout chunkLayout = Sponge.getServer().getChunkLayout();
        return new SpongeLocation<>(world, chunkLayout, position.toDouble());
    }

    @Override
    public Location<W> sub(final Vector3d v) {
        return this.withPosition(this.position.sub(v));
    }

    @Override
    public Location<W> sub(final Vector3i v) {
        return this.withBlockPosition(this.blockPosition.sub(v));
    }

    @Override
    public Location<W> sub(final double x, final double y, final double z) {
        return this.withPosition(this.position.sub(x, y, z));
    }

    @Override
    public Location<W> add(final Vector3d v) {
        return this.withPosition(this.position.add(v));
    }

    @Override
    public Location<W> add(final Vector3i v) {
        return this.withBlockPosition(this.blockPosition.add(v));
    }

    @Override
    public Location<W> add(final double x, final double y, final double z) {
        return this.withPosition(this.position.add(x, y, z));
    }

    @Override
    public Location<W> relativeTo(final Direction direction) {
        return null;
    }

    @Override
    public Location<W> relativeToBlock(final Direction direction) {
        return null;
    }

    @Override
    public BiomeType getBiome() {
        return this.getWorld().getBiome(this.blockPosition);
    }

    @Override
    public boolean hasBlock() {
        return this.isValid() && this.getWorld().hasBlockState(this.blockPosition);
    }

    @Override
    public BlockState getBlock() {
        return this.getWorld().getBlock(this.blockPosition);
    }

    @Override
    public FluidState getFluid() {
        return this.getWorld().getFluid(this.blockPosition);
    }

    @Override
    public boolean hasBlockEntity() {
        return this.isValid() && this.getWorld().getBlockEntity(this.blockPosition).isPresent();
    }

    @Override
    public Optional<? extends BlockEntity> getBlockEntity() {
        return this.getWorld().getBlockEntity(this.blockPosition);
    }

    @Override
    public boolean setBlock(final BlockState state) {
        return this.getWorld().setBlock(this.blockPosition, state);
    }

    @Override
    public boolean setBlock(final BlockState state, final BlockChangeFlag flag) {
        return this.getWorld().setBlock(this.blockPosition, state, flag);
    }

    @Override
    public boolean setBlockType(final BlockType type) {
        return this.getWorld().setBlock(this.blockPosition, type.getDefaultState());
    }

    @Override
    public boolean setBlockType(final BlockType type, final BlockChangeFlag flag) {
        return this.getWorld().setBlock(this.blockPosition, type.getDefaultState(), flag);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final SpongeLocation<?> that = (SpongeLocation<?>) o;
        return this.worldRef.equals(that.worldRef) &&
                   this.position.equals(that.position) &&
                   this.blockPosition.equals(that.blockPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.worldRef, this.position, this.blockPosition);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SpongeLocation.class.getSimpleName() + "[", "]")
                   .add("worldRef=" + this.worldRef)
                   .add("position=" + this.position)
                   .toString();
    }
}
