package org.spongepowered.common.data.provider.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.EnumProperty;
import net.minecraft.util.Direction;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.util.Axis;
import org.spongepowered.common.data.provider.BlockStateDataProvider;

import java.util.Optional;

public class AxisBlockStateProvider extends BlockStateDataProvider<Axis> {

    private final EnumProperty<Direction.Axis> property;
    private final boolean validateValue;

    AxisBlockStateProvider(Class<? extends Block> blockType, EnumProperty<Direction.Axis> property) {
        super(Keys.AXIS, blockType);
        this.property = property;
        this.validateValue = property.getAllowedValues().size() < 3;
    }

    @Override
    protected Optional<Axis> getFrom(BlockState dataHolder) {
        return Optional.of(getFor(dataHolder.get(this.property)));
    }

    @Override
    protected Optional<BlockState> set(BlockState dataHolder, Axis value) {
        final Direction.Axis axis = getFor(value);
        if (this.validateValue && !this.property.getAllowedValues().contains(axis)) {
            return Optional.of(dataHolder);
        }
        return Optional.of(dataHolder.with(this.property, axis));
    }

    private static Direction.Axis getFor(Axis axis) {
        switch (axis) {
            case X:
                return Direction.Axis.X;
            case Y:
                return Direction.Axis.Y;
            case Z:
                return Direction.Axis.Z;
        }
        throw new IllegalStateException();
    }

    private static Axis getFor(Direction.Axis axis) {
        switch (axis) {
            case X:
                return Axis.X;
            case Y:
                return Axis.Y;
            case Z:
                return Axis.Z;
        }
        throw new IllegalStateException();
    }
}
