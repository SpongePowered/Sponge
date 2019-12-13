package org.spongepowered.common.data.provider;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.DirectionProperty;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.util.Direction;

import java.util.Optional;

public class BlockStateDirectionDataProvider extends BlockStateDataProvider<Direction> {

    private final DirectionProperty property;

    public BlockStateDirectionDataProvider(Class<? extends Block> blockType, DirectionProperty property) {
        super(Keys.DIRECTION, blockType);
        this.property = property;
    }

    @Override
    protected Optional<Direction> getFrom(BlockState dataHolder) {
        final net.minecraft.util.Direction direction = dataHolder.get(this.property);
        return Optional.of(getFor(direction));
    }

    @Override
    protected Optional<BlockState> set(BlockState dataHolder, Direction value) {
        final net.minecraft.util.@Nullable Direction direction = getFor(value);
        if (direction == null || !this.property.getAllowedValues().contains(direction)) {
            return Optional.of(dataHolder);
        }
        return Optional.of(dataHolder.with(this.property, direction));
    }

    private static net.minecraft.util.@Nullable Direction getFor(final Direction direction) {
        checkNotNull(direction);
        switch (direction) {
            case UP:
                return net.minecraft.util.Direction.UP;
            case DOWN:
                return net.minecraft.util.Direction.DOWN;
            case WEST:
                return net.minecraft.util.Direction.WEST;
            case SOUTH:
                return net.minecraft.util.Direction.SOUTH;
            case EAST:
                return net.minecraft.util.Direction.EAST;
            case NORTH:
                return net.minecraft.util.Direction.NORTH;
            default:
                return null;
        }
    }

    private static Direction getFor(final net.minecraft.util.Direction facing) {
        checkNotNull(facing);
        switch (facing) {
            case UP:
                return Direction.UP;
            case DOWN:
                return Direction.DOWN;
            case WEST:
                return Direction.WEST;
            case SOUTH:
                return Direction.SOUTH;
            case EAST:
                return Direction.EAST;
            case NORTH:
                return Direction.NORTH;
            default:
                throw new IllegalStateException();
        }
    }
}
