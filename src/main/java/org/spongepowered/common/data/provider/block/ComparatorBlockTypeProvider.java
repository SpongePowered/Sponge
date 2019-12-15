package org.spongepowered.common.data.provider.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.state.properties.ComparatorMode;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.ComparatorType;
import org.spongepowered.common.data.provider.BlockStateDataProvider;

import java.util.Optional;

@SuppressWarnings("ConstantConditions")
public class ComparatorBlockTypeProvider extends BlockStateDataProvider<ComparatorType> {

    public ComparatorBlockTypeProvider() {
        super(Keys.COMPARATOR_TYPE, ComparatorBlock.class);
    }

    @Override
    protected Optional<ComparatorType> getFrom(BlockState dataHolder) {
        return Optional.of((ComparatorType) (Object) dataHolder.get(ComparatorBlock.MODE));
    }

    @Override
    protected Optional<BlockState> set(BlockState dataHolder, ComparatorType value) {
        return Optional.of(dataHolder.with(ComparatorBlock.MODE, (ComparatorMode) (Object) value));
    }
}
