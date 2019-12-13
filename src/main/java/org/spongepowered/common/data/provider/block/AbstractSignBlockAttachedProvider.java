package org.spongepowered.common.data.provider.block;

import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallSignBlock;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.BlockStateDataProvider;

import java.util.Optional;

public class AbstractSignBlockAttachedProvider extends BlockStateDataProvider<Boolean> {

    public AbstractSignBlockAttachedProvider() {
        super(Keys.ATTACHED, AbstractSignBlock.class);
    }

    @Override
    protected Optional<Boolean> getFrom(BlockState dataHolder) {
        return Optional.of(dataHolder.getBlock() instanceof WallSignBlock);
    }

    @Override
    protected Optional<BlockState> set(BlockState dataHolder, Boolean value) {
        return Optional.empty();
    }
}
