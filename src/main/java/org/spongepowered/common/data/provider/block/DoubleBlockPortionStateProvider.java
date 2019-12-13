package org.spongepowered.common.data.provider.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.properties.DoubleBlockHalf;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.PortionType;
import org.spongepowered.api.data.type.PortionTypes;
import org.spongepowered.common.data.provider.BlockStateDataProvider;

import java.util.Optional;

public class DoubleBlockPortionStateProvider extends BlockStateDataProvider<PortionType> {

    private final EnumProperty<DoubleBlockHalf> property;

    DoubleBlockPortionStateProvider(Class<? extends Block> blockType, EnumProperty<DoubleBlockHalf> property) {
        super(Keys.PORTION_TYPE, blockType);
        this.property = property;
    }

    @Override
    protected Optional<PortionType> getFrom(BlockState dataHolder) {
        final DoubleBlockHalf half = dataHolder.get(this.property);
        return Optional.of(half == DoubleBlockHalf.LOWER ? PortionTypes.BOTTOM : PortionTypes.TOP);
    }

    @Override
    protected Optional<BlockState> set(BlockState dataHolder, PortionType value) {
        final DoubleBlockHalf half = value == PortionTypes.TOP ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER;
        return Optional.of(dataHolder.with(this.property, half));
    }
}
