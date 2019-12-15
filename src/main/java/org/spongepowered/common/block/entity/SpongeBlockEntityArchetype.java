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

import com.google.common.base.MoreObjects;
import net.minecraft.block.Block;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.BlockEntityType;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.Location;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.data.AbstractArchetype;
import org.spongepowered.common.data.nbt.NBTDataType;
import org.spongepowered.common.data.nbt.NBTDataTypes;
import org.spongepowered.common.data.nbt.validation.ValidationType;
import org.spongepowered.common.data.nbt.validation.Validations;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.VecHelper;

import java.util.Objects;
import java.util.Optional;

public final class SpongeBlockEntityArchetype extends AbstractArchetype<BlockEntityType, BlockSnapshot, BlockEntity> implements
        org.spongepowered.api.block.entity.BlockEntityArchetype {

    final BlockState blockState;

    SpongeBlockEntityArchetype(SpongeBlockEntityArchetypeBuilder builder) {
        super(builder.type, NbtTranslator.getInstance().translateData(builder.data));
        this.blockState = builder.blockState;
    }

    @Override
    public BlockState getState() {
        return this.blockState;
    }

    @Override
    public BlockEntityType getBlockEntityType() {
        return this.type;
    }

    @Override
    public DataContainer getBlockEntityData() {
        return NbtTranslator.getInstance().translateFrom(this.data);
    }

    @Override
    public Optional<BlockEntity> apply(Location location) {
        final BlockState currentState = location.getBlock();
        final Block currentBlock = ((net.minecraft.block.BlockState) currentState).getBlock();
        final Block newBlock = ((net.minecraft.block.BlockState) this.blockState).getBlock();
        final World minecraftWorld = (net.minecraft.world.World) location.getWorld();

        BlockPos blockpos = VecHelper.toBlockPos(location);
        if (currentBlock != newBlock) {
            ((org.spongepowered.api.world.World) minecraftWorld).setBlock(blockpos.getX(), blockpos.getY(), blockpos.getZ(), this.blockState,
                    BlockChangeFlags.ALL);
        }
        final CompoundNBT compound = this.data.copy();

        TileEntity tileEntity = minecraftWorld.getTileEntity(blockpos);
        if (tileEntity == null) {
            return Optional.empty();
        }
        compound.putInt("x", blockpos.getX());
        compound.putInt("y", blockpos.getY());
        compound.putInt("z", blockpos.getZ());
        tileEntity.read(compound);
        tileEntity.markDirty();
        return Optional.of((org.spongepowered.api.block.entity.BlockEntity) tileEntity);
    }

    @Override
    public BlockSnapshot toSnapshot(Location location) {
        final SpongeBlockSnapshotBuilder builder = SpongeBlockSnapshotBuilder.pooled();
        builder.blockState = this.blockState;
        builder.compound = this.data.copy();
        builder.worldUuid = location.getWorldUniqueId();
        builder.coords = location.getBlockPosition();
        return builder.build();
    }

    @Override
    public int getContentVersion() {
        return Constants.Sponge.BlockEntityArchetype.BASE_VERSION;
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew()
                .set(Constants.Sponge.BlockEntityArchetype.BLOCK_ENTITY_TYPE, this.type)
                .set(Constants.Sponge.BlockEntityArchetype.BLOCK_STATE, this.blockState)
                .set(Constants.Sponge.BlockEntityArchetype.BLOCK_ENTITY_DATA, this.getBlockEntityData())
                ;
    }

    @Override
    protected NBTDataType getDataType() {
        return NBTDataTypes.TILE_ENTITY;
    }

    @Override
    protected ValidationType getValidationType() {
        return Validations.TILE_ENTITY;
    }

    @Override
    public org.spongepowered.api.block.entity.BlockEntityArchetype copy() {
        final SpongeBlockEntityArchetypeBuilder builder = new SpongeBlockEntityArchetypeBuilder();
        builder.type = this.type;
        builder.data = NbtTranslator.getInstance().translate(this.data);
        builder.blockState = this.blockState;
        return builder.build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        SpongeBlockEntityArchetype that = (SpongeBlockEntityArchetype) o;
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
