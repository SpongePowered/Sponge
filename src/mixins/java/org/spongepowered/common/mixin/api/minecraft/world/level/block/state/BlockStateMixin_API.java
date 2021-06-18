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
package org.spongepowered.common.mixin.api.minecraft.world.level.block.state;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.block.BlockStateSerializerDeserializer;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.bridge.data.SpongeDataHolderBridge;
import org.spongepowered.common.util.Constants;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

@Mixin(net.minecraft.world.level.block.state.BlockState.class)
public abstract class BlockStateMixin_API extends BlockBehaviour_BlockStateBaseMixin_API {

    private String api$serializedState;

    @Override
    public int contentVersion() {
        return Constants.Sponge.BlockState.STATE_AS_CATALOG_ID;
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew()
            .set(Queries.CONTENT_VERSION, this.contentVersion())
            .set(Constants.Block.BLOCK_STATE, this.impl$getSerializedString());
    }

    @Override
    public BlockSnapshot snapshotFor(final ServerLocation location) {
        final SpongeBlockSnapshotBuilder builder = SpongeBlockSnapshotBuilder.pooled()
                .blockState((net.minecraft.world.level.block.state.BlockState) (Object) this)
                .position(location.blockPosition())
                .world((ServerLevel) location.world());
        if (this.shadow$getBlock().isEntityBlock() && location.block().type().equals(this.shadow$getBlock())) {
            final BlockEntity tileEntity = location.blockEntity()
                    .orElseThrow(() -> new IllegalStateException("Unable to retrieve a TileEntity for location: " + location));
            builder.add(((SpongeDataHolderBridge) tileEntity).bridge$getManipulator());
            final CompoundTag compound = new CompoundTag();
            ((net.minecraft.world.level.block.entity.BlockEntity) tileEntity).save(compound);
            builder.addUnsafeCompound(compound);
        }
        return builder.build();
    }

    @Override
    public boolean validateRawData(final DataView container) {
        return container.contains(Constants.Block.BLOCK_STATE);
    }

    @Override
    public <E> Optional<E> get(final Direction direction, final Key<? extends Value<E>> key) {
        throw new UnsupportedOperationException("Not implemented yet"); // TODO directional data
    }

    @Override
    public BlockState withRawData(final DataView container) throws InvalidDataException {
        throw new UnsupportedOperationException("Not implemented yet"); // TODO Data API
    }

    @Override
    public BlockState copy() {
        return this;
    }

    public String impl$getSerializedString() {
        if (this.api$serializedState == null) {
            this.api$serializedState = BlockStateSerializerDeserializer.serialize(this);
        }
        return this.api$serializedState;
    }

    @Override
    public List<DataHolder> impl$delegateDataHolder() {
        return Arrays.asList(this, this.type());
    }
}
