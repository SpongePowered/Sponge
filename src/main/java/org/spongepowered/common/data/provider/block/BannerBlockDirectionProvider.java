package org.spongepowered.common.data.provider.block;

import net.minecraft.block.BannerBlock;
import net.minecraft.block.BlockState;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.data.provider.BlockStateDataProvider;

import java.util.Optional;

public class BannerBlockDirectionProvider extends BlockStateDataProvider<Direction> {

    BannerBlockDirectionProvider() {
        super(Keys.DIRECTION, BannerBlock.class);
    }

    @Override
    protected Optional<Direction> getFrom(BlockState dataHolder) {
        final int intDirection = dataHolder.get(BannerBlock.ROTATION);
        return Optional.of(Direction.values()[(intDirection + 8) % 16]);
    }

    @Override
    protected Optional<BlockState> set(BlockState dataHolder, Direction value) {
        final int intDirection = (value.ordinal() + 8) % 16;
        return Optional.of(dataHolder.with(BannerBlock.ROTATION, intDirection));
    }
}
