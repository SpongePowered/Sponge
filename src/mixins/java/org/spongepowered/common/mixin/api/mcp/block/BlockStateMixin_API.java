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
package org.spongepowered.common.mixin.api.mcp.block;

import com.google.common.base.Joiner;
import net.minecraft.block.Block;
import net.minecraft.fluid.IFluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.IProperty;
import net.minecraft.util.IStringSerializable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.fluid.FluidState;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.bridge.data.CustomDataHolderBridge;
import org.spongepowered.common.mixin.api.mcp.state.StateHolderMixin_API;
import org.spongepowered.common.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(net.minecraft.block.BlockState.class)
public abstract class BlockStateMixin_API extends StateHolderMixin_API<BlockState, net.minecraft.block.BlockState> implements BlockState {

    @Shadow public abstract Block shadow$getBlock();
    @Shadow public abstract IFluidState shadow$getFluidState();

    private ResourceKey impl$key;

    @Override
    public BlockType getType() {
        return (BlockType) this.shadow$getBlock();
    }

    @Override
    public FluidState getFluidState() {
        return (FluidState) this.shadow$getFluidState();
    }

    @Override
    public int getContentVersion() {
        return Constants.Sponge.BlockState.STATE_AS_CATALOG_ID;
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew()
            .set(Queries.CONTENT_VERSION, this.getContentVersion())
            .set(Constants.Block.BLOCK_STATE, this.getKey().toString());
    }

    @Override
    public BlockSnapshot snapshotFor(ServerLocation location) {
        final SpongeBlockSnapshotBuilder builder = SpongeBlockSnapshotBuilder.pooled()
                .blockState((net.minecraft.block.BlockState) (Object) this)
                .position(location.getBlockPosition())
                .world(location.getWorld().getKey());
        if (this.shadow$getBlock().hasTileEntity() && location.getBlock().getType().equals(this.shadow$getBlock())) {
            final BlockEntity tileEntity = location.getBlockEntity()
                    .orElseThrow(() -> new IllegalStateException("Unable to retrieve a TileEntity for location: " + location));
            ((CustomDataHolderBridge) tileEntity).bridge$getCustomManipulators().forEach(builder::add);
            final CompoundNBT compound = new CompoundNBT();
            ((net.minecraft.tileentity.TileEntity) tileEntity).write(compound);
            builder.unsafeNbt(compound);
        }
        return builder.build();
    }

    @Override
    public boolean validateRawData(DataView container) {
        return container.contains(Constants.Block.BLOCK_STATE);
    }

    @Override
    public <E> Optional<E> get(Direction direction, Key<? extends Value<E>> key) {
        throw new UnsupportedOperationException("Not implemented yet"); // TODO directional data
    }

    @Override
    public BlockState withRawData(DataView container) throws InvalidDataException {
        throw new UnsupportedOperationException("Not implemented yet"); // TODO Data API
    }

    @Override
    public BlockState copy() {
        return this;
    }

    @Override
    public ResourceKey getKey() {
        if (this.impl$key == null) {
            this.impl$generateIdFromParentBlock(this.shadow$getBlock());
        }
        return this.impl$key;
    }

    private void impl$generateIdFromParentBlock(final Block block) {
        final StringBuilder builder = new StringBuilder();
        builder.append(((BlockType) block).getKey().getValue());
        if (!this.shadow$getProperties().isEmpty()) {
            builder.append('[');
            final Joiner joiner = Joiner.on(',');
            final List<String> propertyValues = new ArrayList<>();
            for (IProperty<?> property : this.shadow$getProperties()) {
                Comparable<?> value = this.shadow$get(property);
                final String stringValue = (value instanceof IStringSerializable) ? ((IStringSerializable) value).getName() : value.toString();
                propertyValues.add(property.getName() + "=" + stringValue);
            }
            builder.append(joiner.join(propertyValues));
            builder.append(']');
        }
        this.impl$key = ResourceKey.of(((BlockType) block).getKey().getNamespace(), builder.toString());
    }
}
