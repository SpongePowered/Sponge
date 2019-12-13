package org.spongepowered.common.data.provider.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.CropsBlock;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.common.data.provider.BlockStateBoundedDataProvider;

import java.util.Optional;

public class CropsBlockGrowthStageStateProvider extends BlockStateBoundedDataProvider<Integer> {

    public CropsBlockGrowthStageStateProvider() {
        super(Keys.GROWTH_STAGE, CropsBlock.class);
    }

    @Override
    protected BoundedValue<Integer> constructValue(BlockState dataHolder, Integer element) {
        final CropsBlock block = (CropsBlock) dataHolder.getBlock();
        return BoundedValue.immutableOf(this.getKey(), element, 0, block.getMaxAge());
    }

    @Override
    protected Optional<Integer> getFrom(BlockState dataHolder) {
        return Optional.of(dataHolder.get(((CropsBlock) dataHolder.getBlock()).getAgeProperty()));
    }

    @Override
    protected Optional<BlockState> set(BlockState dataHolder, Integer value) {
        final CropsBlock block = (CropsBlock) dataHolder.getBlock();
        return Optional.of(dataHolder.with(block.getAgeProperty(), MathHelper.clamp(value, 0, block.getMaxAge())));
    }
}
