package org.spongepowered.common.data.provider.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.properties.Half;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.PortionType;
import org.spongepowered.api.data.type.PortionTypes;
import org.spongepowered.common.data.provider.BlockStateDataProvider;

import java.util.Optional;

public class HalfBlockPortionProvider extends BlockStateDataProvider<PortionType> {

    private final EnumProperty<Half> property;

    HalfBlockPortionProvider(Class<? extends Block> blockType, EnumProperty<Half> property) {
        super(Keys.PORTION_TYPE, blockType);
        this.property = property;
    }

    @Override
    protected Optional<PortionType> getFrom(BlockState dataHolder) {
        final Half half = dataHolder.get(this.property);
        return Optional.of(half == Half.BOTTOM ? PortionTypes.BOTTOM : PortionTypes.TOP);
    }

    @Override
    protected Optional<BlockState> set(BlockState dataHolder, PortionType value) {
        final Half half = value == PortionTypes.TOP ? Half.TOP : Half.BOTTOM;
        return Optional.of(dataHolder.with(this.property, half));
    }
}
