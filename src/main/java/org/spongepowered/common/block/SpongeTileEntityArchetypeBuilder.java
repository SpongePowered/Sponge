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
package org.spongepowered.common.block;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.TileEntityArchetype;
import org.spongepowered.api.block.tileentity.TileEntityType;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.data.nbt.NbtDataTypes;
import org.spongepowered.common.data.nbt.validation.Validations;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

import javax.annotation.Nullable;

public class SpongeTileEntityArchetypeBuilder extends AbstractDataBuilder<TileEntityArchetype> implements TileEntityArchetype.Builder {

    BlockState     blockState;    // -These two fields can never be null
    @Nullable
    TileEntityType tileEntityType;
    DataContainer  tileData;      // This can be empty, but cannot be null.

    public SpongeTileEntityArchetypeBuilder() {
        super(TileEntityArchetype.class, Constants.Sponge.TileEntityArchetype.BASE_VERSION);
    }

    @Override
    public TileEntityArchetype.Builder reset() {
        this.blockState = null;
        this.tileEntityType = null;
        this.tileData = null;
        return this;
    }

    @Override
    public TileEntityArchetype.Builder from(TileEntityArchetype value) {
        this.tileEntityType = value.getTileEntityType();
        this.blockState = value.getState();
        this.tileData = value.getTileData();
        return this;
    }

    @Override
    public TileEntityArchetype.Builder state(BlockState state) {
        final IBlockState blockState = (IBlockState) state;
        if (!SpongeImplHooks.hasBlockTileEntity(blockState.func_177230_c(), blockState)) {
            new IllegalArgumentException("BlockState: "+ state + " does not provide TileEntities!").printStackTrace();
        }
        if (this.blockState != state) {
            this.tileData = null;
        }
        this.blockState = state;
        return this;
    }

    @Override
    public TileEntityArchetype.Builder tile(TileEntityType tileEntityType) {
        this.tileEntityType = checkNotNull(tileEntityType, "TileEntityType cannot be null!");
        return this;
    }

    @Override
    public TileEntityArchetype.Builder from(Location<World> location) {
        final TileEntity tileEntity = location.getTileEntity()
                .orElseThrow(() -> new IllegalArgumentException("There is no tile entity available at the provided location: " + location));

        return tile(tileEntity);
    }

    @Override
    public TileEntityArchetype.Builder tile(TileEntity tileEntity) {
        checkArgument(tileEntity instanceof net.minecraft.tileentity.TileEntity, "TileEntity is not compatible with this implementation!");
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        ((net.minecraft.tileentity.TileEntity) tileEntity).func_189515_b(nbttagcompound);
        nbttagcompound.func_82580_o("x");
        nbttagcompound.func_82580_o("y");
        nbttagcompound.func_82580_o("z");
        String tileId = nbttagcompound.func_74779_i("id");
        nbttagcompound.func_82580_o("id");
        nbttagcompound.func_74778_a(Constants.Sponge.TileEntityArchetype.TILE_ENTITY_ID, tileId);
        this.tileData = NbtTranslator.getInstance().translate(nbttagcompound);
        this.blockState = tileEntity.getBlock();
        this.tileEntityType = tileEntity.getType();
        return this;
    }

    @Override
    public TileEntityArchetype.Builder tileData(DataView dataView) {
        checkNotNull(dataView, "Provided DataView cannot be null!");
        final DataContainer copy = dataView.copy();
        DataUtil.getValidators(Validations.TILE_ENTITY).validate(copy);
        this.tileData = copy;
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public TileEntityArchetype.Builder setData(DataManipulator<?, ?> manipulator) {
        if (this.tileData == null) {
            this.tileData = DataContainer.createNew();
        }
        DataUtil.getRawNbtProcessor(NbtDataTypes.TILE_ENTITY, manipulator.getClass())
                .ifPresent(processor -> processor.storeToView(this.tileData, manipulator));
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E, V extends BaseValue<E>> TileEntityArchetype.Builder set(V value) {
        if (this.tileData == null) {
            this.tileData = DataContainer.createNew();
        }
        DataUtil.getRawNbtProcessor(NbtDataTypes.TILE_ENTITY, value.getKey())
                .ifPresent(processor -> processor.offer(this.tileData, value));
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E, V extends BaseValue<E>> TileEntityArchetype.Builder set(Key<V> key, E value) {
        if (this.tileData == null) {
            this.tileData = DataContainer.createNew();
        }
        DataUtil.getRawNbtProcessor(NbtDataTypes.TILE_ENTITY, key)
                .ifPresent(processor -> processor.offer(this.tileData, value));
        return this;
    }

    @Override
    public TileEntityArchetype build() {
        checkState(this.blockState != null, "BlockState cannot be null!");
        checkState(this.tileEntityType != null, "TileEntityType cannot be null!");
        if (this.tileData == null) {
            this.tileData = DataContainer.createNew();
        }
        return new SpongeTileEntityArchetype(this);
    }

    @Override
    protected Optional<TileEntityArchetype> buildContent(DataView container) throws InvalidDataException {
        final SpongeTileEntityArchetypeBuilder builder = new SpongeTileEntityArchetypeBuilder();
        if (container.contains(Constants.Sponge.TileEntityArchetype.TILE_TYPE, Constants.Sponge.TileEntityArchetype.BLOCK_STATE)) {
            builder.tile(container.getCatalogType(Constants.Sponge.TileEntityArchetype.TILE_TYPE, TileEntityType.class)
                    .orElseThrow(() -> new InvalidDataException("Could not deserialize a TileEntityType!")));
            builder.state(container.getCatalogType(Constants.Sponge.TileEntityArchetype.BLOCK_STATE, BlockState.class)
                    .orElseThrow(() -> new InvalidDataException("Could not deserialize a BlockState!")));
        } else {
            throw new InvalidDataException("Missing the TileEntityType and BlockState! Cannot re-construct a TileEntityArchetype!");
        }
        if (container.contains(Constants.Sponge.TileEntityArchetype.TILE_DATA)) {
            builder.tileData(container.getView(Constants.Sponge.TileEntityArchetype.TILE_DATA)
                    .orElseThrow(() -> new InvalidDataException("No DataView found for the TileEntity data tag!")));
        }
        return Optional.of(builder.build());
    }

}
