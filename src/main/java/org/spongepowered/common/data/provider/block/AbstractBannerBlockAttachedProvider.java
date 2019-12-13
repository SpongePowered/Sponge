package org.spongepowered.common.data.provider.block;

import net.minecraft.block.AbstractBannerBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallBannerBlock;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.BlockStateDataProvider;

import java.util.Optional;

public class AbstractBannerBlockAttachedProvider extends BlockStateDataProvider<Boolean> {

    AbstractBannerBlockAttachedProvider() {
        super(Keys.ATTACHED, AbstractBannerBlock.class);
    }

    @Override
    protected Optional<Boolean> getFrom(BlockState dataHolder) {
        return Optional.of(dataHolder.getBlock() instanceof WallBannerBlock);
    }

    @Override
    protected Optional<BlockState> set(BlockState dataHolder, Boolean value) {
        return Optional.empty();
    }
}

