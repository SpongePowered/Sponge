package org.spongepowered.common.data.provider;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.IntegerProperty;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.common.data.util.DataHelper;

import java.util.Optional;

public class BlockStateBoundedIntDataProvider extends BlockStateBoundedDataProvider<Integer> {

    private final IntegerProperty property;
    private final int min;
    private final int max;

    public BlockStateBoundedIntDataProvider(Key<? extends BoundedValue<Integer>> key,
            Class<? extends Block> blockType, IntegerProperty property) {
        super(key, blockType);
        this.property = property;
        this.min = DataHelper.min(property);
        this.max = DataHelper.max(property);
    }

    @Override
    protected BoundedValue<Integer> constructValue(BlockState dataHolder, Integer element) {
        return BoundedValue.immutableOf(this.getKey(), element, this.min, this.max);
    }

    @Override
    protected Optional<Integer> getFrom(BlockState dataHolder) {
        return Optional.of(dataHolder.get(this.property));
    }

    @Override
    protected Optional<BlockState> set(BlockState dataHolder, Integer value) {
        return Optional.of(dataHolder.with(this.property, MathHelper.clamp(value, this.min, this.max)));
    }
}
