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
package org.spongepowered.common.data.processor;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.common.data.DataTransactionBuilder.fail;
import static org.spongepowered.common.data.DataTransactionBuilder.successNoData;
import static org.spongepowered.common.data.DataTransactionBuilder.successReplaceData;
import static org.spongepowered.common.data.util.DataUtil.getData;

import com.google.common.base.Optional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.component.base.SkullComponent;
import org.spongepowered.api.data.token.Tokens;
import org.spongepowered.api.data.type.SkullType;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.SpongeBlockProcessor;
import org.spongepowered.common.data.SpongeDataProcessor;
import org.spongepowered.common.data.component.base.SpongeSkullComponent;
import org.spongepowered.common.data.type.SpongeSkullType;

import java.util.List;

public class SpongeSkullDataProcessor implements SpongeDataProcessor<SkullComponent>, SpongeBlockProcessor<SkullComponent> {

    @Override
    public Optional<SkullComponent> fromBlockPos(World world, BlockPos blockPos) {
        final IBlockState blockState = checkNotNull(world).getBlockState(checkNotNull(blockPos));
        if (blockState.getBlock() == Blocks.skull) {
            final TileEntitySkull tileEntitySkull = ((TileEntitySkull) world.getTileEntity(blockPos));
            final int skullMeta = tileEntitySkull.getSkullType();
            final SkullType skullType = ((List<SkullType>) Sponge.getGame().getRegistry().getAllOf(SkullType.class)).get(skullMeta);
            return Optional.of(create().setValue(skullType));
        }
        return Optional.absent();
    }

    @Override
    public DataTransactionResult setData(World world, BlockPos blockPos, SkullComponent manipulator, DataPriority priority) {
        final IBlockState blockState = checkNotNull(world).getBlockState(checkNotNull(blockPos));
        if (blockState.getBlock() == Blocks.skull) {
            switch (checkNotNull(priority)) {
                case COMPONENT:
                case POST_MERGE:
                    final TileEntitySkull tileEntitySkull = ((TileEntitySkull) world.getTileEntity(blockPos));
                    final int skullMeta = tileEntitySkull.getSkullType();
                    final SkullType skullType = ((List<SkullType>) Sponge.getGame().getRegistry().getAllOf(SkullType.class)).get(skullMeta);
                    final SkullComponent old = create().setValue(skullType);
                    tileEntitySkull.setType(((SpongeSkullType) manipulator.getValue()).getByteId());
                    return successReplaceData(old);
                default:
                    return successNoData();
            }
        }
        return fail(manipulator);
    }

    @Override
    public boolean remove(World world, BlockPos blockPos) {
        // we CAN remove the skull data, or "reset" it
        final IBlockState blockState = checkNotNull(world).getBlockState(checkNotNull(blockPos));
        if (blockState.getBlock() == Blocks.skull) {
            final TileEntitySkull tileEntitySkull = ((TileEntitySkull) world.getTileEntity(blockPos));
            tileEntitySkull.setType(0);
            return true;
        }
        return false;
    }

    @Override
    public Optional<BlockState> removeFrom(IBlockState blockState) {
        return Optional.absent();
    }

    @Override
    public Optional<SkullComponent> createFrom(IBlockState blockState) {
        // We can't really return the skull information from the block state, because guess what?
        // THERE IS NONE....
        return Optional.absent();
    }

    @Override
    public Optional<SkullComponent> getFrom(DataHolder dataHolder) {
        if (dataHolder instanceof ItemStack && ((ItemStack) dataHolder).getItem() instanceof ItemSkull) {
            // we have to compare damage values
            final int skullMeta = ((ItemStack) dataHolder).getMetadata();
            final SkullType skullType = ((List<SkullType>) Sponge.getGame().getRegistry().getAllOf(SkullType.class)).get(skullMeta);
            return Optional.of(create().setValue(skullType));
        }
        if (dataHolder instanceof TileEntitySkull) {
            final int skullMeta = ((TileEntitySkull) dataHolder).getSkullType();
            final SkullType skullType = ((List<SkullType>) Sponge.getGame().getRegistry().getAllOf(SkullType.class)).get(skullMeta);
            return Optional.of(create().setValue(skullType));
        }
        return Optional.absent();
    }

    @Override
    public Optional<SkullComponent> fillData(DataHolder dataHolder, SkullComponent manipulator, DataPriority priority) {
        if (dataHolder instanceof ItemStack && ((ItemStack) dataHolder).getItem() instanceof ItemSkull) {
            switch (checkNotNull(priority)) {
                case DATA_HOLDER:
                case PRE_MERGE:
                    final int skullMeta = ((ItemStack) dataHolder).getMetadata();
                    final SkullType skullType = ((List<SkullType>) Sponge.getGame().getRegistry().getAllOf(SkullType.class)).get(skullMeta);
                    return Optional.of(checkNotNull(manipulator).setValue(skullType));
                default:
                    return Optional.of(manipulator);
            }
        }
        if (dataHolder instanceof TileEntitySkull) {
            switch (checkNotNull(priority)) {
                case DATA_HOLDER:
                case PRE_MERGE:
                    final int skullMeta = ((TileEntitySkull) dataHolder).getSkullType();
                    final SkullType skullType = ((List<SkullType>) Sponge.getGame().getRegistry().getAllOf(SkullType.class)).get(skullMeta);
                    return Optional.of(checkNotNull(manipulator).setValue(skullType));
                default:
                    return Optional.of(manipulator);
            }

        }
        return Optional.absent();
    }

    @Override
    public DataTransactionResult setData(DataHolder dataHolder, SkullComponent manipulator, DataPriority priority) {
        if (dataHolder instanceof ItemStack && ((ItemStack) dataHolder).getItem() instanceof ItemSkull) {
            switch (checkNotNull(priority)) {
                case COMPONENT:
                case POST_MERGE:
                    final SkullComponent old = getFrom(dataHolder).get();
                    ((ItemStack) dataHolder).setItemDamage(((SpongeSkullType) manipulator.getValue()).getByteId());
                    return successReplaceData(old);
                default:
                    return successNoData();
            }
        }
        if (dataHolder instanceof TileEntitySkull) {
            switch (checkNotNull(priority)) {
                case COMPONENT:
                case POST_MERGE:
                    final SkullComponent old = getFrom(dataHolder).get();
                    ((TileEntitySkull) dataHolder).setType(((SpongeSkullType) manipulator.getValue()).getByteId());
                    return successReplaceData(old);
                default:
                    return successNoData();
            }
        }
        return fail(manipulator);
    }

    @Override
    public boolean remove(DataHolder dataHolder) {
        if (dataHolder instanceof ItemStack && ((ItemStack) dataHolder).getItem() == Items.skull) {
            ((ItemStack) dataHolder).setItemDamage(0);
            return true;
        }
        if (dataHolder instanceof TileEntitySkull) {
            ((TileEntitySkull) dataHolder).setType(0);
            return true;
        }
        return false;
    }

    @Override
    public Optional<SkullComponent> build(DataView container) throws InvalidDataException {
        final String skullId = getData(container, Tokens.SKULL_TYPE.getQuery(), String.class);
        final Optional<SkullType> skullTypeOptional = Sponge.getGame().getRegistry().getType(SkullType.class, skullId);
        if (skullTypeOptional.isPresent()) {
            return Optional.of(create().setValue(skullTypeOptional.get()));
        }
        return Optional.absent();
    }

    @Override
    public SkullComponent create() {
        return new SpongeSkullComponent();
    }

    @Override
    public Optional<SkullComponent> createFrom(DataHolder dataHolder) {
        return getFrom(dataHolder);
    }
}
