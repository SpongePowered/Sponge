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

import net.minecraft.core.BlockPos;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.fluid.FluidState;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.storage.ChunkLayout;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

public class SpongeLocation<W extends World<W, L>, L extends Location<W, L>> implements Location<W, L> {

    protected final WeakReference<W> worldRef;
    private final Vector3d position;
    private final Vector3i blockPosition;
    private final Vector3i chunkPosition;
    private final Vector3i biomePosition;

    private final BlockPos pos;

    protected SpongeLocation(final W world, final ChunkLayout chunkLayout, final Vector3d position) {
        this.worldRef = new WeakReference<>(world);
        this.position = position;
        this.blockPosition = position.toInt();
        this.chunkPosition = chunkLayout.forceToChunk(this.blockPosition);
        this.biomePosition = position.toInt().mul(1, 0, 1);

        this.pos = new BlockPos(position.x(), position.y(), position.z());
    }

    protected SpongeLocation(final W worldRef, final Vector3d position, final Vector3i chunkPosition, final Vector3i biomePosition) {
        this.worldRef = new WeakReference<>(worldRef);
        this.position = position;
        this.blockPosition = position.toInt();
        this.chunkPosition = chunkPosition;
        this.biomePosition = biomePosition;

        this.pos = new BlockPos(position.x(), position.y(), position.z());
    }

    @Override
    public W world() {
        final W world = this.worldRef.get();
        if (world == null) {
            throw new IllegalStateException("World Reference is no longer valid!");
        }
        return world;
    }

    @Override
    public Optional<W> worldIfAvailable() {
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
    public Vector3d position() {
        return this.position;
    }

    @Override
    public Vector3i blockPosition() {
        return this.blockPosition;
    }

    @Override
    public Vector3i chunkPosition() {
        return this.chunkPosition;
    }

    @Override
    public Vector3i biomePosition() {
        return this.biomePosition;
    }

    @Override
    public double x() {
        return this.position.x();
    }

    @Override
    public double y() {
        return this.position.y();
    }

    @Override
    public double z() {
        return this.position.z();
    }

    @Override
    public int blockX() {
        return this.blockPosition.x();
    }

    @Override
    public int blockY() {
        return this.blockPosition.y();
    }

    @Override
    public int blockZ() {
        return this.blockPosition.z();
    }

    @Override
    public boolean inWorld(final W world) {
        return this.worldRef.get() == world;
    }

    @Override
    @SuppressWarnings("unchecked")
    public L withWorld(final W world) {
        return (L) new SpongeLocation<W, L>(world, this.position, this.chunkPosition, this.biomePosition);
    }

    @Override
    @SuppressWarnings("unchecked")
    public L withPosition(final Vector3d position) {
        final W world = this.worldRef.get();
        if (world == null) {
            throw new IllegalStateException("World Reference is null!");
        }
        // TODO - for now, we make the assumption we always have a server object if we're creating locations
        final ChunkLayout chunkLayout = Sponge.server().chunkLayout();
        return (L) new SpongeLocation<>(world, chunkLayout, position);
    }

    @Override
    @SuppressWarnings("unchecked")
    public L withBlockPosition(final Vector3i position) {
        final W world = this.worldRef.get();
        if (world == null) {
            throw new IllegalStateException("World Reference is null!");
        }
        // TODO - for now, we make the assumption we always have a server object if we're creating locations
        final ChunkLayout chunkLayout = Sponge.server().chunkLayout();
        return (L) new SpongeLocation<>(world, chunkLayout, position.toDouble());
    }

    @Override
    public L sub(final Vector3d v) {
        return this.withPosition(this.position.sub(v));
    }

    @Override
    public L sub(final Vector3i v) {
        return this.withBlockPosition(this.blockPosition.sub(v));
    }

    @Override
    public L sub(final double x, final double y, final double z) {
        return this.withPosition(this.position.sub(x, y, z));
    }

    @Override
    public L add(final Vector3d v) {
        return this.withPosition(this.position.add(v));
    }

    @Override
    public L add(final Vector3i v) {
        return this.withBlockPosition(this.blockPosition.add(v));
    }

    @Override
    public L add(final double x, final double y, final double z) {
        return this.withPosition(this.position.add(x, y, z));
    }

    @Override
    public L relativeTo(final Direction direction) {
        return null;
    }

    @Override
    public L relativeToBlock(final Direction direction) {
        return null;
    }

    @Override
    public Biome biome() {
        return this.world().biome(this.blockPosition);
    }

    @Override
    public boolean hasBlock() {
        return this.isValid() && this.world().hasBlockState(this.blockPosition);
    }

    @Override
    public BlockState block() {
        return this.world().block(this.blockPosition);
    }

    @Override
    public FluidState fluid() {
        return this.world().fluid(this.blockPosition);
    }

    @Override
    public boolean hasBlockEntity() {
        return this.isValid() && this.world().blockEntity(this.blockPosition).isPresent();
    }

    @Override
    public Optional<? extends BlockEntity> blockEntity() {
        return this.world().blockEntity(this.blockPosition);
    }

    @Override
    public boolean setBlock(final BlockState state) {
        return this.world().setBlock(this.blockPosition, state);
    }

    @Override
    public boolean setBlock(final BlockState state, final BlockChangeFlag flag) {
        return this.world().setBlock(this.blockPosition, state, flag);
    }

    @Override
    public boolean setBlockType(final BlockType type) {
        return this.world().setBlock(this.blockPosition, type.defaultState());
    }

    @Override
    public boolean setBlockType(final BlockType type, final BlockChangeFlag flag) {
        return this.world().setBlock(this.blockPosition, type.defaultState(), flag);
    }

    public BlockPos asBlockPos() {
        return this.pos;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final SpongeLocation<?, ?> that = (SpongeLocation<?, ?>) o;
        return this.worldRef.get().equals(that.worldRef.get()) &&
                   this.position.equals(that.position) &&
                   this.blockPosition.equals(that.blockPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.worldRef.get(), this.position, this.blockPosition);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SpongeLocation.class.getSimpleName() + "[", "]")
                   .add("worldRef=" + this.worldRef)
                   .add("position=" + this.position)
                   .toString();
    }
}
