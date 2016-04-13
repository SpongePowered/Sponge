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

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntityArchetype;
import org.spongepowered.api.block.tileentity.TileEntityType;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.data.AbstractArchetype;
import org.spongepowered.common.data.nbt.NbtDataType;
import org.spongepowered.common.data.nbt.NbtDataTypes;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.data.util.DataVersions;
import org.spongepowered.common.data.nbt.validation.ValidationType;
import org.spongepowered.common.data.nbt.validation.Validations;
import org.spongepowered.common.util.VecHelper;

public class SpongeTileEntityArchetype extends AbstractArchetype<TileEntityType, BlockSnapshot> implements TileEntityArchetype {

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
    public void apply(Location<World> location, Cause cause) {
        final BlockState currentState = location.getBlock();
        final Block currentBlock = BlockUtil.toBlock(currentState);
        final Block newBlock = BlockUtil.toBlock(this.blockState);
        final net.minecraft.world.World minecraftWorld = (net.minecraft.world.World) location.getExtent();

        BlockPos blockpos = VecHelper.toBlockPos(location);
        if (currentBlock != newBlock) {
            ((World) minecraftWorld).setBlock(blockpos.getX(), blockpos.getY(), blockpos.getZ(), this.blockState, true, cause);
        }
        final NBTTagCompound compound = (NBTTagCompound) this.data.copy();

        TileEntity tileEntity = minecraftWorld.getTileEntity(blockpos);
        if (tileEntity == null) {
            throw new IllegalStateException("Could not create a tile entity for this archetype, possibly there's some issues with deserialization?");
        }
        compound.setInteger("x", blockpos.getX());
        compound.setInteger("y", blockpos.getY());
        compound.setInteger("z", blockpos.getZ());
        tileEntity.readFromNBT(compound);

    }

    @Override
    public BlockSnapshot toSnapshot(Location<World> location) {
        final SpongeBlockSnapshotBuilder builder = new SpongeBlockSnapshotBuilder();
        builder.blockState = this.blockState;
        builder.compound = (NBTTagCompound) this.data.copy();
        builder.worldUuid = location.getExtent().getUniqueId();
        builder.coords = location.getBlockPosition();
        return builder.build();
    }

    @Override
    public int getContentVersion() {
        return DataVersions.TileEntitArchetype.BASE_VERSION;
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(DataQueries.TileEntityArchetype.TILE_TYPE, this.type)
                .set(DataQueries.TileEntityArchetype.BLOCK_STATE, this.blockState)
                .set(DataQueries.TileEntityArchetype.TILE_DATA, this.data)
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
}
