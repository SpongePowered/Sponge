package org.spongepowered.common.data.provider.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SpongeBlock;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.BlockStateDataProvider;

import java.util.Optional;

public class SpongeBlockIsWetProvider extends BlockStateDataProvider<Boolean> {

    SpongeBlockIsWetProvider() {
        super(Keys.IS_WET, SpongeBlock.class);
    }

    @Override
    protected Optional<Boolean> getFrom(BlockState dataHolder) {
        return Optional.of(dataHolder.getBlock() == Blocks.WET_SPONGE);
    }

    @Override
    protected Optional<BlockState> set(BlockState dataHolder, Boolean value) {
        return Optional.of((value ? Blocks.SPONGE : Blocks.WET_SPONGE).getDefaultState());
    }
}
