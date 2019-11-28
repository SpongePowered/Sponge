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

import com.google.common.base.MoreObjects;
import net.minecraft.block.Block;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntityArchetype;
import org.spongepowered.api.block.tileentity.TileEntityType;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.data.AbstractArchetype;
import org.spongepowered.common.data.nbt.NbtDataType;
import org.spongepowered.common.data.nbt.NbtDataTypes;
import org.spongepowered.common.data.nbt.validation.ValidationType;
import org.spongepowered.common.data.nbt.validation.Validations;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.VecHelper;

import java.util.Objects;
import java.util.Optional;

public class SpongeTileEntityArchetype extends AbstractArchetype<TileEntityType, BlockSnapshot, org.spongepowered.api.block.tileentity.TileEntity> implements TileEntityArchetype {

    final BlockState blockState;

    SpongeTileEntityArchetype(SpongeTileEntityArchetypeBuilder builder) {
        super(builder.tileEntityType, NbtTranslator.getInstance().translateData(builder.tileData));
        this.blockState = builder.blockState;
    }

    @Override
    public BlockState getState() {
        return this.blockState;
    }

    @Override
    public TileEntityType getTileEntityType() {
        return this.type;
    }

    @Override
    public DataContainer getTileData() {
        return NbtTranslator.getInstance().translateFrom(this.data);
    }

    @Override
    public Optional<org.spongepowered.api.block.tileentity.TileEntity> apply(Location<World> location) {
        final BlockState currentState = location.getBlock();
        final Block currentBlock = ((net.minecraft.block.BlockState) currentState).func_177230_c();
        final Block newBlock = ((net.minecraft.block.BlockState) this.blockState).func_177230_c();
        final net.minecraft.world.World minecraftWorld = (net.minecraft.world.World) location.getExtent();

        BlockPos blockpos = VecHelper.toBlockPos(location);
        if (currentBlock != newBlock) {
            ((World) minecraftWorld).setBlock(blockpos.func_177958_n(), blockpos.func_177956_o(), blockpos.func_177952_p(), this.blockState, BlockChangeFlags.ALL);
        }
        final CompoundNBT compound = this.data.func_74737_b();

        TileEntity tileEntity = minecraftWorld.func_175625_s(blockpos);
        if (tileEntity == null) {
            return Optional.empty();
        }
        compound.func_74768_a("x", blockpos.func_177958_n());
        compound.func_74768_a("y", blockpos.func_177956_o());
        compound.func_74768_a("z", blockpos.func_177952_p());
        tileEntity.func_145839_a(compound);
        tileEntity.func_70296_d();
        return Optional.of((org.spongepowered.api.block.tileentity.TileEntity) tileEntity);
    }

    @Override
    public BlockSnapshot toSnapshot(Location<World> location) {
        final SpongeBlockSnapshotBuilder builder = SpongeBlockSnapshotBuilder.pooled();
        builder.blockState = this.blockState;
        builder.compound = this.data.func_74737_b();
        builder.worldUuid = location.getExtent().getUniqueId();
        builder.coords = location.getBlockPosition();
        return builder.build();
    }

    @Override
    public int getContentVersion() {
        return Constants.Sponge.TileEntityArchetype.BASE_VERSION;
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew()
                .set(Constants.Sponge.TileEntityArchetype.TILE_TYPE, this.type)
                .set(Constants.Sponge.TileEntityArchetype.BLOCK_STATE, this.blockState)
                .set(Constants.Sponge.TileEntityArchetype.TILE_DATA, getTileData())
                ;
    }

    @Override
    protected NbtDataType getDataType() {
        return NbtDataTypes.TILE_ENTITY;
    }

    @Override
    protected ValidationType getValidationType() {
        return Validations.TILE_ENTITY;
    }

    @Override
    public TileEntityArchetype copy() {
        final SpongeTileEntityArchetypeBuilder builder = new SpongeTileEntityArchetypeBuilder();
        builder.tileEntityType = this.type;
        builder.tileData = NbtTranslator.getInstance().translate(this.data);
        builder.blockState = this.blockState;
        return builder.build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        SpongeTileEntityArchetype that = (SpongeTileEntityArchetype) o;
        return this.blockState.equals(that.blockState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.blockState);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("type", this.type).add("state", this.blockState).add("data", this.data).toString();
    }
}
