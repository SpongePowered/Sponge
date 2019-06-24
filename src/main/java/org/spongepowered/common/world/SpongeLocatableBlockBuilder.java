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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.util.Constants;

import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

public class SpongeLocatableBlockBuilder extends AbstractDataBuilder<LocatableBlock> implements LocatableBlock.Builder {

    @Nullable BlockState blockState;
    @Nullable Vector3i position;
    @Nullable UUID worldId;
    @Nullable WeakReference<World> worldReference;


    public SpongeLocatableBlockBuilder() {
        super(LocatableBlock.class, 1);
    }

    @Override
    public SpongeLocatableBlockBuilder state(BlockState blockState) {
        this.blockState = checkNotNull(blockState, "BlockState cannot be null!");
        return this;
    }

    @Override
    public SpongeLocatableBlockBuilder location(Location<World> location) {
        checkNotNull(location, "LocationBridge cannot be null!");
        this.blockState = location.getBlock();
        this.position = location.getBlockPosition();
        this.worldId = location.getExtent().getUniqueId();
        this.worldReference = new WeakReference<>(location.getExtent());
        return this;
    }

    @Override
    public SpongeLocatableBlockBuilder position(Vector3i position) {
        this.position = checkNotNull(position, "Position cannot be null!");
        return this;
    }

    @Override
    public SpongeLocatableBlockBuilder position(int x, int y, int z) {
        this.position = new Vector3i(x, y, z);
        return this;
    }

    @Override
    public SpongeLocatableBlockBuilder world(World world) {
        checkNotNull(world, "World cannot be null!");
        this.worldReference = new WeakReference<World>(world);
        this.worldId = world.getUniqueId();
        return this;
    }

    @Override
    public SpongeLocatableBlockBuilder from(LocatableBlock value) {
        this.position = checkNotNull(value, "LocatableBlock cannot be null!").getPosition();
        this.worldId = value.getLocation().getExtent().getUniqueId();
        this.worldReference = new WeakReference<World>(value.getLocation().getExtent());
        return this;
    }

    @Override
    public LocatableBlock build() {
        checkNotNull(this.position, "Position cannot be null!");
        checkNotNull(this.worldId, "World UUID cannot be null!");
        checkNotNull(this.worldReference, "World reference cannot be null!");
        if (this.blockState == null) {
            if (this.worldReference.get() != null) {
                this.blockState = this.worldReference.get().getBlock(this.position);
            } else {
                final Optional<World> world = Sponge.getServer().getWorld(this.worldId);
                checkArgument(world.isPresent(), "World is not available by the UUID: " + this.worldId);
                this.worldReference = new WeakReference<World>(world.get());
                this.blockState = this.worldReference.get().getBlock(this.position);
            }
        }
        return new SpongeLocatableBlock(this);
    }

    @Override
    public SpongeLocatableBlockBuilder reset() {
        this.position = null;
        this.worldId = null;
        this.worldReference = null;
        this.blockState = null;
        return this;
    }

    @Override
    protected Optional<LocatableBlock> buildContent(DataView container) throws InvalidDataException {
        final int x = container.getInt(Queries.POSITION_X)
                .orElseThrow(() -> new InvalidDataException("Could not locate an \"x\" coordinate in the container!"));
        final int y = container.getInt(Queries.POSITION_Y)
                .orElseThrow(() -> new InvalidDataException("Could not locate an \"y\" coordinate in the container!"));
        final int z = container.getInt(Queries.POSITION_Z)
                .orElseThrow(() -> new InvalidDataException("Could not locate an \"z\" coordinate in the container!"));
        final BlockState blockState = container.getCatalogType(Constants.Block.BLOCK_STATE, BlockState.class)
                .orElseThrow(() -> new InvalidDataException("Could not locate a BlockState"));
        final UUID worldId = container.getObject(Queries.WORLD_ID, UUID.class)
                .orElseThrow(() -> new InvalidDataException("Could not locate a UUID"));
        return Sponge.getServer().getWorld(worldId)
                .map(world -> new SpongeLocatableBlockBuilder()
                        .position(x, y, z)
                        .world(world)
                        .state(blockState)
                        .build()
        );

    }
}
