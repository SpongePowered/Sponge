package org.spongepowered.common.mixin.core.data.holders;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.common.data.DataTransactionBuilder.successNoData;
import static org.spongepowered.common.data.DataTransactionBuilder.successReplaceData;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockLever;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.manipulator.block.PoweredData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.data.manipulator.block.SpongePoweredData;
import org.spongepowered.common.interfaces.block.IMixinPoweredHolder;
import org.spongepowered.common.mixin.core.block.MixinBlock;

import java.util.Collection;

@Mixin(BlockButton.class)
public abstract class MixinBlockButton extends MixinBlock implements IMixinPoweredHolder {

    @Override
    public Collection<DataManipulator<?>> getManipulators(World world, BlockPos blockPos) {
        return null;
    }

    @Override
    public ImmutableList<DataManipulator<?>> getManipulators(IBlockState blockState) {
        return null;
    }

    @Override
    public PoweredData getPoweredData(IBlockState blockState) {
        final boolean powered = (boolean) blockState.getValue(BlockButton.POWERED);
        return powered ? new SpongePoweredData() : null;
    }

    @Override
    public DataTransactionResult setPoweredData(PoweredData poweredData, World world, BlockPos blockPos, DataPriority priority) {
        final PoweredData data = getPoweredData(world.getBlockState(blockPos));
        switch (checkNotNull(priority)) {
            case DATA_MANIPULATOR:
            case POST_MERGE:
                IBlockState blockState = world.getBlockState(blockPos);
                world.setBlockState(blockPos, blockState.withProperty(BlockButton.POWERED, poweredData != null), 3);
                return successReplaceData(data);
            default:
                return successNoData();
        }
    }

    @Override
    public BlockState resetPoweredData(BlockState blockState) {
        return (BlockState) ((IBlockState) blockState).withProperty(BlockButton.POWERED, false);
    }
}
