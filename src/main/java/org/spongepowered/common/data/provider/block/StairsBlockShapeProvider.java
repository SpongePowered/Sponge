package org.spongepowered.common.data.provider.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.state.properties.StairsShape;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.StairShape;
import org.spongepowered.common.data.provider.BlockStateDataProvider;

import java.util.Optional;

@SuppressWarnings("ConstantConditions")
public class StairsBlockShapeProvider extends BlockStateDataProvider<StairShape> {

    StairsBlockShapeProvider() {
        super(Keys.STAIR_SHAPE, StairsBlock.class);
    }

    @Override
    protected Optional<StairShape> getFrom(BlockState dataHolder) {
        final StairsShape shape = dataHolder.get(StairsBlock.SHAPE);
        return Optional.of((StairShape) (Object) shape);
    }

    @Override
    protected Optional<BlockState> set(BlockState dataHolder, StairShape value) {
        final StairsShape shape = (StairsShape) (Object) value;
        return Optional.of(dataHolder.with(StairsBlock.SHAPE, shape));
    }
}
