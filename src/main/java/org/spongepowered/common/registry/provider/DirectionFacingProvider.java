package org.spongepowered.common.registry.provider;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableBiMap;
import net.minecraft.util.EnumFacing;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.registry.TypeProvider;

import java.util.Optional;

public final class DirectionFacingProvider implements TypeProvider<Direction, EnumFacing> {

    private static final DirectionFacingProvider instance = new DirectionFacingProvider();

    public static DirectionFacingProvider getInstance() {
        return instance;
    }

    private DirectionFacingProvider() {
    }

    public static final ImmutableBiMap<Direction, EnumFacing> directionMap = ImmutableBiMap.<Direction, EnumFacing>builder()
        .put(Direction.NORTH, EnumFacing.NORTH)
        .put(Direction.EAST, EnumFacing.EAST)
        .put(Direction.SOUTH, EnumFacing.SOUTH)
        .put(Direction.WEST, EnumFacing.WEST)
        .put(Direction.UP, EnumFacing.UP)
        .put(Direction.DOWN, EnumFacing.DOWN)
        .build();

    @Override
    public Optional<EnumFacing> get(Direction key) {
        return Optional.ofNullable(directionMap.get(checkNotNull(key)));
    }

    @Override
    public Optional<Direction> getKey(EnumFacing value) {
        return Optional.ofNullable(directionMap.inverse().get(checkNotNull(value)));
    }
}
