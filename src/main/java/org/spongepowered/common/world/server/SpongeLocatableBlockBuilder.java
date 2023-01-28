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

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.fluid.FluidState;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.common.util.Constants;
import org.spongepowered.math.vector.Vector3i;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public final class SpongeLocatableBlockBuilder extends AbstractDataBuilder<LocatableBlock> implements LocatableBlock.Builder {

    Supplier<? extends BlockState> blockState;
    Supplier<? extends ResourceKey> world;
    Supplier<? extends Vector3i> position;
    Supplier<? extends ServerWorld> worldReference;
    Supplier<? extends FluidState> fluidState;

    public SpongeLocatableBlockBuilder() {
        super(LocatableBlock.class, 1);
    }

    @Override
    public SpongeLocatableBlockBuilder state(final BlockState blockState) {
        Objects.requireNonNull(blockState, "BlockState cannot be null!");
        this.blockState = () -> blockState;
        return this;
    }

    @Override
    public SpongeLocatableBlockBuilder fluid(final FluidState fluid) {
        Objects.requireNonNull(fluid, "FluidState cannot be null!");
        this.fluidState = () -> fluid;
        return this;
    }

    @Override
    public SpongeLocatableBlockBuilder location(final ServerLocation location) {
        Objects.requireNonNull(location, "LocationBridge cannot be null!");
        this.blockState = location::block;
        this.fluidState = location::fluid;
        this.position = location::blockPosition;
        this.world = () -> location.world().key();
        final WeakReference<ServerWorld> worldRef = new WeakReference<>(location.world());
        this.worldReference = () -> Objects.requireNonNull(worldRef.get(), "ServerWorld refrence dereferenced");
        return this;
    }

    @Override
    public SpongeLocatableBlockBuilder position(final Vector3i position) {
        Objects.requireNonNull(position, "Position cannot be null!");
        this.position = () -> position;
        return this;
    }

    @Override
    public SpongeLocatableBlockBuilder position(final int x, final int y, final int z) {
        final Vector3i position = new Vector3i(x, y, z);
        this.position = () -> position;
        return this;
    }

    @SuppressWarnings("unchecked")
    public SpongeLocatableBlockBuilder world(final Supplier<net.minecraft.server.level.ServerLevel> worldSupplier) {
        Objects.requireNonNull(worldSupplier, "Supplier cannot be null!");
        Objects.requireNonNull(worldSupplier.get(), "ServerWorld reference cannot be null!");
        this.worldReference = (Supplier<ServerWorld>) (Supplier<?>) worldSupplier;
        this.world = () -> ((ServerWorld) worldSupplier.get()).key();
        return this;
    }

    @Override
    public SpongeLocatableBlockBuilder world(final ServerWorld world) {
        Objects.requireNonNull(world, "World cannot be null!");
        final WeakReference<ServerWorld> reference = new WeakReference<>(world);
        this.worldReference = () -> Objects.requireNonNull(reference.get(), "ServerWorld refrence dereferenced");
        this.world = () -> this.worldReference.get().key();
        return this;
    }

    @Override
    public SpongeLocatableBlockBuilder from(final LocatableBlock value) {
        Objects.requireNonNull(value, "LocatableBlock cannot be null!");
        this.position = value::blockPosition;
        this.world = () -> value.serverLocation().world().key();
        final WeakReference<ServerWorld> worldRef = new WeakReference<>(value.serverLocation().world());
        this.worldReference = () -> Objects.requireNonNull(worldRef.get(), "ServerWorld refrence dereferenced");
        return this;
    }

    @Override
    public LocatableBlock build() {
        Objects.requireNonNull(this.position, "Position cannot be null!");
        Objects.requireNonNull(this.world, "World UUID cannot be null!");
        Objects.requireNonNull(this.worldReference, "World reference cannot be null!");
        return new SpongeLocatableBlock(this);
    }

    @Override
    public SpongeLocatableBlockBuilder reset() {
        this.position = null;
        this.world = null;
        this.worldReference = null;
        this.blockState = null;
        return this;
    }

    @Override
    protected Optional<LocatableBlock> buildContent(final DataView container) throws InvalidDataException {
        final ResourceKey worldKey = container.getResourceKey(Queries.WORLD_KEY)
            .orElseThrow(() -> new InvalidDataException("Could not locate a world key"));
        final int x = container.getInt(Queries.POSITION_X)
                .orElseThrow(() -> new InvalidDataException("Could not locate an \"x\" coordinate in the container!"));
        final int y = container.getInt(Queries.POSITION_Y)
                .orElseThrow(() -> new InvalidDataException("Could not locate an \"y\" coordinate in the container!"));
        final int z = container.getInt(Queries.POSITION_Z)
                .orElseThrow(() -> new InvalidDataException("Could not locate an \"z\" coordinate in the container!"));
        final BlockState blockState = container.getSerializable(Constants.Block.BLOCK_STATE, BlockState.class)
                .orElseThrow(() -> new InvalidDataException("Could not locate a BlockState"));
        return Sponge.server().worldManager().world(worldKey)
                .map(world -> new SpongeLocatableBlockBuilder()
                        .position(x, y, z)
                        .world(world)
                        .state(blockState)
                        .build()
        );

    }
}
