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
package org.spongepowered.common.block.entity;

import net.minecraft.nbt.CompoundTag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.BlockEntityArchetype;
import org.spongepowered.api.block.entity.BlockEntityType;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.common.bridge.world.level.block.state.BlockStateBridge;
import org.spongepowered.common.data.persistence.NBTTranslator;
import org.spongepowered.common.util.Constants;

import java.util.Deque;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;

public final class SpongeBlockEntityArchetypeBuilder extends AbstractDataBuilder<BlockEntityArchetype> implements BlockEntityArchetype.Builder {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Deque<SpongeBlockEntityArchetypeBuilder> pool = new ConcurrentLinkedDeque<>();

    public static SpongeBlockEntityArchetypeBuilder unpooled() {
        return new SpongeBlockEntityArchetypeBuilder(false);
    }

    public static SpongeBlockEntityArchetypeBuilder pooled() {
        final @Nullable SpongeBlockEntityArchetypeBuilder builder = SpongeBlockEntityArchetypeBuilder.pool.pollFirst();
        if (builder != null) {
            return builder.reset();
        }
        return new SpongeBlockEntityArchetypeBuilder(true);
    }

    @MonotonicNonNull BlockState blockState;
    @MonotonicNonNull BlockEntityType type;
    @Nullable DataContainer data;
    private final boolean pooled;

    SpongeBlockEntityArchetypeBuilder(final boolean pooled) {
        super(BlockEntityArchetype.class, Constants.Sponge.BlockEntityArchetype.BASE_VERSION);
        this.pooled = pooled;
    }

    @Override
    public SpongeBlockEntityArchetypeBuilder reset() {
        this.blockState = null;
        this.type = null;
        this.data = null;
        return this;
    }

    @Override
    public <V> BlockEntityArchetype.Builder add(final Key<? extends Value<V>> key, final V value) {
        return null;
    }

    @Override
    public BlockEntityArchetype.Builder from(final BlockEntityArchetype value) {
        this.type = value.blockEntityType();
        this.blockState = value.state();
        this.data = value.blockEntityData();
        return this;
    }

    @Override
    public BlockEntityArchetype.Builder state(final BlockState state) {
        final net.minecraft.world.level.block.state.BlockState blockState = (net.minecraft.world.level.block.state.BlockState) state;
        if (!((BlockStateBridge) (blockState)).bridge$hasTileEntity()) {
            SpongeBlockEntityArchetypeBuilder.LOGGER.warn("BlockState {} does not provide BlockEntities!", state, new IllegalArgumentException());
        }
        if (this.blockState != state) {
            this.data = null;
        }
        this.blockState = state;
        return this;
    }

    @Override
    public BlockEntityArchetype.Builder blockEntity(final BlockEntityType blockEntityType) {
        this.type = Objects.requireNonNull(blockEntityType, "BlockEntityType cannot be null");
        return this;
    }

    @Override
    public BlockEntityArchetype.Builder from(final ServerLocation location) {
        final BlockEntity tileEntity = location.blockEntity()
                .orElseThrow(() -> new IllegalArgumentException("There is no block entity available at the provided location: " + location));

        return this.blockEntity(tileEntity);
    }

    @Override
    public BlockEntityArchetype.Builder blockEntity(final BlockEntity blockEntity) {
        if (!(Objects.requireNonNull(blockEntity, "BlockEntity cannot be null!") instanceof net.minecraft.world.level.block.entity.BlockEntity mcBlockEntity)) {
            throw new IllegalArgumentException("BlockEntity is not compatible with this implementation!");
        }
        final CompoundTag compound = mcBlockEntity.saveWithFullMetadata(mcBlockEntity.getLevel().registryAccess());
        compound.remove(Constants.Sponge.BlockSnapshot.TILE_ENTITY_POSITION_X);
        compound.remove(Constants.Sponge.BlockSnapshot.TILE_ENTITY_POSITION_Y);
        compound.remove(Constants.Sponge.BlockSnapshot.TILE_ENTITY_POSITION_Z);
        compound.remove(Constants.Item.BLOCK_ENTITY_ID);
        this.data = NBTTranslator.INSTANCE.translate(compound);
        this.blockState = blockEntity.block();
        this.type = blockEntity.type();
        return this;
    }

    @Override
    public BlockEntityArchetype.Builder blockEntityData(final DataView dataView) {
        final DataContainer copy = Objects.requireNonNull(dataView, "DataView cannot be null!").copy();
        this.data = copy;
        return this;
    }

    @Override
    public SpongeBlockEntityArchetype build() {
        if (this.blockState == null) {
            throw new IllegalStateException("BlockState cannot be null!");
        }
        if (this.type == null) {
            throw new IllegalStateException("BlockEntityType cannot be null!");
        }
        if (this.data == null) {
            this.data = DataContainer.createNew();
        }
        final SpongeBlockEntityArchetype archetype = new SpongeBlockEntityArchetype(this);
        if (this.pooled) {
            this.reset();
            SpongeBlockEntityArchetypeBuilder.pool.push(this);
        }
        return archetype;
    }

    @Override
    protected Optional<BlockEntityArchetype> buildContent(final DataView container) throws InvalidDataException {
        final SpongeBlockEntityArchetypeBuilder builder = SpongeBlockEntityArchetypeBuilder.pooled();
        if (container.contains(Constants.Sponge.BlockEntityArchetype.BLOCK_ENTITY_TYPE, Constants.Sponge.BlockEntityArchetype.BLOCK_STATE)) {
            builder.blockEntity(container.getRegistryValue(Constants.Sponge.BlockEntityArchetype.BLOCK_ENTITY_TYPE,
                RegistryTypes.BLOCK_ENTITY_TYPE)
                    .orElseThrow(() -> new InvalidDataException("Could not deserialize a BlockEntityType!")));
            builder.state(container.getSerializable(Constants.Sponge.BlockEntityArchetype.BLOCK_STATE, BlockState.class)
                    .orElseThrow(() -> new InvalidDataException("Could not deserialize a BlockState!")));
        } else {
            throw new InvalidDataException("Missing the BlockEntityType and BlockState! Cannot re-construct a BlockEntityArchetype!");
        }
        if (container.contains(Constants.Sponge.BlockEntityArchetype.BLOCK_ENTITY_DATA)) {
            builder.blockEntityData(container.getView(Constants.Sponge.BlockEntityArchetype.BLOCK_ENTITY_DATA)
                    .orElseThrow(() -> new InvalidDataException("No DataView found for the 'TileEntity' data tag!")));
        }
        return Optional.of(builder.build());
    }
}
