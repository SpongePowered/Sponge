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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
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
import org.spongepowered.api.world.Location;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.data.nbt.validation.Validations;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.util.Constants;

import javax.annotation.Nullable;
import java.util.Optional;

@SuppressWarnings("unchecked")
public final class SpongeBlockEntityArchetypeBuilder extends AbstractDataBuilder<BlockEntityArchetype> implements BlockEntityArchetype.Builder {

    BlockState blockState;
    @Nullable BlockEntityType type;
    DataContainer data;

    public SpongeBlockEntityArchetypeBuilder() {
        super(BlockEntityArchetype.class, Constants.Sponge.BlockEntityArchetype.BASE_VERSION);
    }

    @Override
    public BlockEntityArchetype.Builder reset() {
        this.blockState = null;
        this.type = null;
        this.data = null;
        return this;
    }

    @Override
    public BlockEntityArchetype.Builder from(BlockEntityArchetype value) {
        this.type = value.getBlockEntityType();
        this.blockState = value.getState();
        this.data = value.getBlockEntityData();
        return this;
    }

    @Override
    public BlockEntityArchetype.Builder state(BlockState state) {
        final net.minecraft.block.BlockState blockState = (net.minecraft.block.BlockState) state;
        if (!SpongeImplHooks.hasBlockTileEntity(blockState)) {
            new IllegalArgumentException("BlockState: "+ state + " does not provide TileEntities!").printStackTrace();
        }
        if (this.blockState != state) {
            this.data = null;
        }
        this.blockState = state;
        return this;
    }

    @Override
    public BlockEntityArchetype.Builder blockEntity(BlockEntityType blockEntityType) {
        this.type = Preconditions.checkNotNull(blockEntityType);
        return this;
    }

    @Override
    public BlockEntityArchetype.Builder from(Location location) {
        final BlockEntity tileEntity = location.getBlockEntity()
                .orElseThrow(() -> new IllegalArgumentException("There is no block entity available at the provided location: " + location));

        return this.blockEntity(tileEntity);
    }

    @Override
    public BlockEntityArchetype.Builder blockEntity(BlockEntity blockEntity) {
        Preconditions.checkArgument(blockEntity instanceof TileEntity, "BlockEntity is not compatible with this implementation!");
        CompoundNBT compound = new CompoundNBT();
        ((TileEntity) blockEntity).write(compound);
        compound.remove("x");
        compound.remove("y");
        compound.remove("z");
        String tileId = compound.getString("id");
        compound.remove("id");
        compound.putString(Constants.Sponge.BlockEntityArchetype.TILE_ENTITY_ID, tileId);
        this.data = NbtTranslator.getInstance().translate(compound);
        this.blockState = blockEntity.getBlock();
        this.type = blockEntity.getType();
        return this;
    }

    @Override
    public BlockEntityArchetype.Builder blockEntityData(DataView dataView) {
        Preconditions.checkNotNull(dataView, "Provided DataView cannot be null!");
        final DataContainer copy = dataView.copy();
        DataUtil.getValidators(Validations.TILE_ENTITY).validate(copy);
        this.data = copy;
        return this;
    }

    @Override
    public BlockEntityArchetype.Builder setData(Mutable<?, ?> manipulator) {
        if (this.data == null) {
            this.data = DataContainer.createNew();
        }
        DataUtil.getRawNbtProcessor(NBTDataTypes.TILE_ENTITY, manipulator.getClass())
                .ifPresent(processor -> processor.storeToView(this.data, manipulator));
        return this;
    }

    @Override
    public <E, V extends Value<E>> BlockEntityArchetype.Builder set(V value) {
        if (this.data == null) {
            this.data = DataContainer.createNew();
        }
        DataUtil.getRawNbtProcessor(NBTDataTypes.TILE_ENTITY, value.getKey())
                .ifPresent(processor -> processor.offer(this.data, value));
        return this;
    }

    @Override
    public <E, V extends Value<E>> BlockEntityArchetype.Builder set(Key<V> key, E value) {
        if (this.data == null) {
            this.data = DataContainer.createNew();
        }
        DataUtil.getRawNbtProcessor(NBTDataTypes.TILE_ENTITY, key)
                .ifPresent(processor -> processor.offer(this.data, value));
        return this;
    }

    @Override
    public BlockEntityArchetype build() {
        Preconditions.checkState(this.blockState != null, "BlockState cannot be null!");
        Preconditions.checkState(this.type != null, "TileEntityType cannot be null!");
        if (this.data == null) {
            this.data = DataContainer.createNew();
        }
        return new SpongeBlockEntityArchetype(this);
    }

    @Override
    protected Optional<BlockEntityArchetype> buildContent(DataView container) throws InvalidDataException {
        final SpongeBlockEntityArchetypeBuilder builder = new SpongeBlockEntityArchetypeBuilder();
        if (container.contains(Constants.Sponge.BlockEntityArchetype.BLOCK_ENTITY_TYPE, Constants.Sponge.BlockEntityArchetype.BLOCK_STATE)) {
            builder.blockEntity(container.getCatalogType(Constants.Sponge.BlockEntityArchetype.BLOCK_ENTITY_TYPE, BlockEntityType.class)
                    .orElseThrow(() -> new InvalidDataException("Could not deserialize a BlockEntityType!")));
            builder.state(container.getCatalogType(Constants.Sponge.BlockEntityArchetype.BLOCK_STATE, BlockState.class)
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
