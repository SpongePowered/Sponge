package org.spongepowered.common.data.provider.block;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.state.IProperty;
import net.minecraft.state.properties.RailShape;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.RailDirection;
import org.spongepowered.common.data.provider.BlockStateDataProvider;

import java.util.Optional;

@SuppressWarnings("ConstantConditions")
public class AbstractRailBlockRailDirectionProvider extends BlockStateDataProvider<RailDirection> {

    AbstractRailBlockRailDirectionProvider() {
        super(Keys.RAIL_DIRECTION, AbstractRailBlock.class);
    }

    @Override
    protected Optional<RailDirection> getFrom(BlockState dataHolder) {
        final IProperty<RailShape> property = ((AbstractRailBlock) (Object) dataHolder).getShapeProperty();
        return Optional.of((RailDirection) (Object) dataHolder.get(property));
    }

    @Override
    protected Optional<BlockState> set(BlockState dataHolder, RailDirection value) {
        final RailShape shape = (RailShape) (Object) value;
        final IProperty<RailShape> property = ((AbstractRailBlock) (Object) dataHolder).getShapeProperty();
        if (!property.getAllowedValues().contains(shape)) {
            return Optional.of(dataHolder);
        }
        // TODO: Special handling for mods? Like there was before? Is this necessary?
        return Optional.of(dataHolder.with(property, shape));
    }
}
