package org.spongepowered.common.data.provider.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.block.RedstoneWallTorchBlock;
import net.minecraft.block.TorchBlock;
import net.minecraft.block.WallTorchBlock;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.BlockStateDataProvider;

import java.util.Optional;

public class TorchBlockAttachedProvider extends BlockStateDataProvider<Boolean> {

    TorchBlockAttachedProvider() {
        super(Keys.ATTACHED, TorchBlock.class);
    }

    @Override
    protected Optional<Boolean> getFrom(BlockState dataHolder) {
        return Optional.of(dataHolder.getBlock() instanceof WallTorchBlock ||
                dataHolder.getBlock() instanceof RedstoneWallTorchBlock);
    }

    @Override
    protected Optional<BlockState> set(BlockState dataHolder, Boolean value) {
        final Block block = dataHolder.getBlock();
        final boolean isWallBlock = block instanceof WallTorchBlock || block instanceof RedstoneWallTorchBlock;
        if (value == isWallBlock) {
            return Optional.of(dataHolder);
        }
        if (block instanceof RedstoneTorchBlock) {
            return Optional.of((isWallBlock ? Blocks.REDSTONE_WALL_TORCH : Blocks.REDSTONE_TORCH).getDefaultState()
                    .with(RedstoneTorchBlock.LIT, dataHolder.get(RedstoneTorchBlock.LIT)));
        }
        return Optional.of((isWallBlock ? Blocks.WALL_TORCH : Blocks.TORCH).getDefaultState());
    }
}

