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
package org.spongepowered.common.mixin.api.mcp.tileentity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.BlockEntityArchetype;
import org.spongepowered.api.block.entity.BlockEntityType;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.SpongeLocatableBlockBuilder;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

@Mixin(net.minecraft.tileentity.TileEntity.class)
public abstract class TileEntityMixin_API implements BlockEntity {

    //@formatter:off
    @Shadow @Final private TileEntityType<?> type;
    @Shadow protected net.minecraft.world.World world;
    @Shadow protected BlockPos pos;

    @Shadow public abstract BlockPos shadow$getPos();
    @Shadow public abstract void shadow$markDirty();
    @Shadow public abstract CompoundNBT shadow$write(CompoundNBT compound);
    @Shadow protected boolean removed;
    //@formatter:on
    @Nullable private LocatableBlock api$LocatableBlock;

    @Override
    public Location getLocation() {
        return Location.of((World) this.world, VecHelper.toVector3i(this.shadow$getPos()));
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        final DataContainer container = DataContainer.createNew()
            .set(Queries.CONTENT_VERSION, this.getContentVersion())
            .set(Queries.WORLD_ID, ((World) this.world).getProperties().getUniqueId().toString())
            .set(Queries.POSITION_X, this.shadow$getPos().getX())
            .set(Queries.POSITION_Y, this.shadow$getPos().getY())
            .set(Queries.POSITION_Z, this.shadow$getPos().getZ())
            .set(Constants.TileEntity.TILE_TYPE, ((BlockEntityType) this.type).getKey());
        final CompoundNBT compound = new CompoundNBT();
        this.shadow$write(compound);
        Constants.NBT.filterSpongeCustomData(compound); // We must filter the custom data so it isn't stored twice
        container.set(Constants.Sponge.UNSAFE_NBT, NbtTranslator.getInstance().translateFrom(compound));
//        final Collection<Mutable<?, ?>> manipulators = ((CustomDataHolderBridge) this).bridge$getCustomManipulators();
//        if (!manipulators.isEmpty()) {
//            container.set(Constants.Sponge.DATA_MANIPULATORS, DataUtil.getSerializedManipulatorList(manipulators));
//        }
        return container;
    }

    @Override
    public boolean validateRawData(final DataView container) {
        return container.contains(Queries.WORLD_ID)
            && container.contains(Queries.POSITION_X)
            && container.contains(Queries.POSITION_Y)
            && container.contains(Queries.POSITION_Z)
            && container.contains(Constants.TileEntity.TILE_TYPE)
            && container.contains(Constants.Sponge.UNSAFE_NBT);
    }

    @Override
    public void setRawData(final DataView container) throws InvalidDataException {
        throw new UnsupportedOperationException(); // TODO Data API
    }

    @Override
    public boolean isValid() {
        return !this.removed;
    }

    @Override
    public void setValid(final boolean valid) {
        this.removed = valid;
    }

    @Override
    public final BlockEntityType getType() {
        return (BlockEntityType) this.type;
    }

    @Override
    public BlockState getBlock() {
        return (BlockState) this.world.getBlockState(this.shadow$getPos());
    }


    @Override
    public BlockEntityArchetype createArchetype() {
//        return new SpongeBlockEntityArchetypeBuilder().tile(this).build();
        throw new UnsupportedOperationException("lolnope");
    }

    @Override
    public LocatableBlock getLocatableBlock() {
        if (this.api$LocatableBlock == null) {
            final BlockState blockState = this.getBlock();
            this.api$LocatableBlock = new SpongeLocatableBlockBuilder()
                .world((World) this.world)
                .position(this.pos.getX(), this.pos.getY(), this.pos.getZ())
                .state(blockState)
                .build();
        }

        return this.api$LocatableBlock;
    }

    @Override
    public Set<Value.Immutable<?>> getValues() {
        // TODO: Merge custom and Vanilla values and return the merged result.
        return this.api$getVanillaValues();
    }

    protected Set<Value.Immutable<?>> api$getVanillaValues() {
        final Set<Value.Immutable<?>> values = new HashSet<>();

        return values;
    }

}
