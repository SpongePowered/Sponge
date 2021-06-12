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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.fluid.FluidType;
import org.spongepowered.api.scheduler.ScheduledUpdate;
import org.spongepowered.api.scheduler.TaskPriority;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.storage.ChunkLayout;
import org.spongepowered.common.data.holder.SpongeMutableDataHolder;
import org.spongepowered.common.util.MissingImplementationException;
import org.spongepowered.common.world.SpongeLocation;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.BiFunction;

@DefaultQualifier(NonNull.class)
public final class SpongeServerLocation extends SpongeLocation<ServerWorld, ServerLocation> implements ServerLocation, SpongeMutableDataHolder {

    SpongeServerLocation(final ServerWorld world, final ChunkLayout chunkLayout, final Vector3d position) {
        super(world, chunkLayout, position);
    }

    SpongeServerLocation(final ServerWorld worldRef, final Vector3d position, final Vector3i chunkPosition, final Vector3i biomePosition) {
        super(worldRef, position, chunkPosition, biomePosition);
    }

    @Override
    public ServerLocation withWorld(final ServerWorld world) {
        return new SpongeServerLocation(world, this.position(), this.chunkPosition(), this.biomePosition());
    }

    @Override
    public ServerLocation withPosition(final Vector3d position) {
        final ChunkLayout chunkLayout = this.world().engine().chunkLayout();
        return new SpongeServerLocation(this.world(), chunkLayout, position);
    }

    @Override
    public ServerLocation withBlockPosition(final Vector3i position) {
        final ChunkLayout chunkLayout = this.world().engine().chunkLayout();
        return new SpongeServerLocation(this.world(), chunkLayout, position.toDouble());
    }

    @Override
    public ServerLocation sub(final Vector3d v) {
        return this.withPosition(this.position().sub(v));
    }

    @Override
    public ServerLocation sub(final Vector3i v) {
        return this.withBlockPosition(this.blockPosition().sub(v));
    }

    @Override
    public ServerLocation sub(final double x, final double y, final double z) {
        return this.withPosition(this.position().sub(x, y, z));
    }

    @Override
    public ServerLocation add(final Vector3d v) {
        return this.withPosition(this.position().add(v));
    }

    @Override
    public ServerLocation add(final Vector3i v) {
        return this.withBlockPosition(this.blockPosition().add(v));
    }

    @Override
    public ServerLocation add(final double x, final double y, final double z) {
        return this.withPosition(this.position().add(x, y, z));
    }

    @Override
    public ServerLocation relativeTo(final Direction direction) {
        return this.add(direction.asOffset());
    }

    @Override
    public ServerLocation relativeToBlock(final Direction direction) {
        return this.add(direction.asBlockOffset());
    }

    @Override
    public ResourceKey worldKey() {
        return this.world().key();
    }

    @Override
    public LocatableBlock asLocatableBlock() {
        return new SpongeLocatableBlockBuilder()
                   .location(this)
                   .build();
    }

    @Override
    public <T> T map(final BiFunction<ServerWorld, Vector3d, T> mapper) {
        throw new MissingImplementationException("ServerLocation", "map");
    }

    @Override
    public <T> T mapBlock(final BiFunction<ServerWorld, Vector3i, T> mapper) {
        throw new MissingImplementationException("ServerLocation", "mapBlock");
    }

    @Override
    public <T> T mapChunk(final BiFunction<ServerWorld, Vector3i, T> mapper) {
        throw new MissingImplementationException("ServerLocation", "mapChunk");
    }

    @Override
    public <T> T mapBiome(final BiFunction<ServerWorld, Vector3i, T> mapper) {
        throw new MissingImplementationException("ServerLocation", "mapBiome");
    }

    @Override
    public boolean restoreSnapshot(final BlockSnapshot snapshot, final boolean force, final BlockChangeFlag flag) {
        return false;
    }

    @Override
    public boolean removeBlock() {
        return this.world().removeBlock(this.blockPosition());
    }

    @Override
    public <E extends Entity> E createEntity(final EntityType<E> type) {
        return this.world().createEntity(type, this.position());
    }

    @Override
    public boolean spawnEntity(final Entity entity) {
        return this.world().spawnEntity(entity);
    }

    @Override
    public Collection<Entity> spawnEntities(final Iterable<? extends Entity> entities) {
        return this.world().spawnEntities(entities);
    }

    @Override
    public ServerLocation asHighestLocation() {
        return this.withBlockPosition(this.world().highestPositionAt(this.blockPosition()));
    }

    @Override
    public BlockSnapshot createSnapshot() {
        return this.world().createSnapshot(this.blockPosition());
    }

    @Override
    public Collection<? extends ScheduledUpdate<BlockType>> scheduledBlockUpdates() {
        return this.world().scheduledBlockUpdates().scheduledAt(this.blockPosition());
    }

    @Override
    public ScheduledUpdate<BlockType> scheduleBlockUpdate(final int delay, final TemporalUnit temporalUnit) {
        return this.world().scheduledBlockUpdates().schedule(this.blockPosition(), this.blockType(), delay, temporalUnit);
    }

    @Override
    public ScheduledUpdate<BlockType> scheduleBlockUpdate(final Ticks delay, final TaskPriority priority) {
        return this.world().scheduledBlockUpdates().schedule(this.blockPosition(), this.blockType(), delay, priority);
    }

    @Override
    public ScheduledUpdate<BlockType> scheduleBlockUpdate(final int delay, final TemporalUnit temporalUnit, final TaskPriority priority) {
        return this.world().scheduledBlockUpdates().schedule(this.blockPosition(), this.blockType(), delay, temporalUnit, priority);
    }

    @Override
    public ScheduledUpdate<BlockType> scheduleBlockUpdate(final Ticks delay) {
        return this.world().scheduledBlockUpdates().schedule(this.blockPosition(), this.blockType(), delay);
    }

    @Override
    public ScheduledUpdate<BlockType> scheduleBlockUpdate(final Duration delay) {
        return this.world().scheduledBlockUpdates().schedule(this.blockPosition(), this.blockType(), delay);
    }

    @Override
    public ScheduledUpdate<BlockType> scheduleBlockUpdate(final Duration delay, final TaskPriority priority) {
        return this.world().scheduledBlockUpdates().schedule(this.blockPosition(), this.blockType(), delay, priority);
    }

    @Override
    public Collection<? extends ScheduledUpdate<FluidType>> scheduledFluidUpdates() {
        return this.world().scheduledFluidUpdates().scheduledAt(this.blockPosition());
    }

    @Override
    public ScheduledUpdate<FluidType> scheduleFluidUpdate(final int delay, final TemporalUnit temporalUnit) {
        return this.world().scheduledFluidUpdates().schedule(this.blockPosition(), this.fluid().type(), delay, temporalUnit);
    }

    @Override
    public ScheduledUpdate<FluidType> scheduleFluidUpdate(final int delay, final TemporalUnit temporalUnit,
        final TaskPriority priority) {
        return this.world().scheduledFluidUpdates().schedule(this.blockPosition(), this.fluid().type(), delay, temporalUnit, priority);
    }

    @Override
    public ScheduledUpdate<FluidType> scheduleFluidUpdate(final Ticks ticks) {
        return this.world().scheduledFluidUpdates().schedule(this.blockPosition(), this.fluid().type(), ticks);
    }

    @Override
    public ScheduledUpdate<FluidType> scheduleFluidUpdate(final Ticks ticks, final TaskPriority priority) {
        return this.world().scheduledFluidUpdates().schedule(this.blockPosition(), this.fluid().type(), ticks, priority);
    }

    @Override
    public ScheduledUpdate<FluidType> scheduleFluidUpdate(final Duration delay) {
        return this.world().scheduledFluidUpdates().schedule(this.blockPosition(), this.fluid().type(), delay);
    }

    @Override
    public ScheduledUpdate<FluidType> scheduleFluidUpdate(final Duration delay, final TaskPriority priority) {
        return this.world().scheduledFluidUpdates().schedule(this.blockPosition(), this.fluid().type(), delay, priority);
    }

    @Override
    public <E> Optional<E> get(final Direction direction, final Key<? extends Value<E>> key) {
        // TODO direction is ignored?
        return this.get(key);
    }

    @Override
    public List<DataHolder> impl$delegateDataHolder() {
        final Optional<? extends BlockEntity> be = this.blockEntity();
        if (be.isPresent()) {
            return Arrays.asList(this, be.get(), this.block(), this.block().type());
        }
        return Arrays.asList(this, this.block(), this.block().type());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final SpongeServerLocation that = (SpongeServerLocation) o;
        return this.worldRef.get().equals(that.worldRef.get()) &&
                   this.position().equals(that.position()) &&
                   this.blockPosition().equals(that.blockPosition());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.worldRef.get(), this.position(), this.blockPosition());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SpongeServerLocation.class.getSimpleName() + "[", "]")
                   .add("worldRef=" + this.worldKey())
                   .add("position=" + this.position())
                   .toString();
    }

    public static final class Factory implements ServerLocation.Factory {

        @Override
        public ServerLocation create(final ServerWorld world, final Vector3d position) {
            Objects.requireNonNull(world);
            Objects.requireNonNull(position);

            return new SpongeServerLocation(world, world.engine().chunkLayout(), position);
        }

        @Override
        public ServerLocation create(final ServerWorld world, final Vector3i blockPosition) {
            Objects.requireNonNull(world);
            Objects.requireNonNull(blockPosition);

            final ChunkLayout chunkLayout = world.engine().chunkLayout();
            final Vector3d position = blockPosition.toDouble();
            return new SpongeServerLocation(world, chunkLayout, position);
        }

        @Override
        public ServerLocation create(final ResourceKey worldKey, final Vector3d position) {
            Objects.requireNonNull(worldKey);
            Objects.requireNonNull(position);

            final Optional<ServerWorld> world = Sponge.server().worldManager().world(worldKey);
            if (!world.isPresent()) {
                throw new IllegalStateException("Unknown world for key: " + worldKey.toString());
            }
            return new SpongeServerLocation(world.get(), world.get().engine().chunkLayout(), position);
        }

        @Override
        public ServerLocation create(final ResourceKey worldKey, final Vector3i blockPosition) {
            Objects.requireNonNull(worldKey);
            Objects.requireNonNull(blockPosition);

            final Optional<ServerWorld> world = Sponge.server().worldManager().world(worldKey);
            if (!world.isPresent()) {
                throw new IllegalStateException("Unknown world for key: " + worldKey.toString());
            }
            return new SpongeServerLocation(world.get(), world.get().engine().chunkLayout(), blockPosition.toDouble());
        }
    }
}
