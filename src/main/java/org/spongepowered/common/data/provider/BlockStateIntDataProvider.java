package org.spongepowered.common.data.provider;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.IntegerProperty;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.BoundedValue;

import java.util.Optional;

public class BlockStateIntDataProvider extends BlockStateBoundedDataProvider<Integer> {

    private final IntegerProperty property;

    public BlockStateIntDataProvider(Key<? extends BoundedValue<Integer>> key,
            Class<? extends Block> blockType, IntegerProperty property) {
        super(key, blockType);
        this.property = property;
    }

    @Override
    protected Optional<Integer> getFrom(BlockState dataHolder) {
        return Optional.of(dataHolder.get(this.property));
    }

    @Override
    protected Optional<BlockState> set(BlockState dataHolder, Integer value) {
        if (!this.property.getAllowedValues().contains(value)) {
            return Optional.of(dataHolder);
        }
        return Optional.of(dataHolder.with(this.property, value));
    }
}
