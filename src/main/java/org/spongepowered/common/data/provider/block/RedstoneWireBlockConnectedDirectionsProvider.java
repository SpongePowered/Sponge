package org.spongepowered.common.data.provider.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.properties.RedstoneSide;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.WireAttachmentType;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.data.provider.BlockStateDataProvider;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class RedstoneWireBlockConnectedDirectionsProvider extends BlockStateDataProvider<Set<Direction>> {

    private final Map<Direction, EnumProperty<RedstoneSide>> sides;

    RedstoneWireBlockConnectedDirectionsProvider(Key<? extends Value<Set<Direction>>> key, Class<? extends Block> blockType,
            Map<Direction, EnumProperty<RedstoneSide>> sides) {
        super(key, blockType);
        this.sides = sides;
    }

    @Override
    protected Optional<Set<Direction>> getFrom(BlockState dataHolder) {
        final Set<Direction> directions = new HashSet<>();
        for (final Map.Entry<Direction, EnumProperty<RedstoneSide>> entry : this.sides.entrySet()) {
            if (dataHolder.get(entry.getValue()) != RedstoneSide.NONE) {
                directions.add(entry.getKey());
            }
        }
        return Optional.of(directions);
    }

    @Override
    protected Optional<BlockState> set(BlockState dataHolder, Set<Direction> value) {
        for (final Map.Entry<Direction, EnumProperty<RedstoneSide>> entry : this.sides.entrySet()) {
            dataHolder = RedstoneWireBlockConnectedProvider.with(dataHolder, entry.getValue(), value.contains(entry.getKey()));
        }
        return Optional.of(dataHolder);
    }
}
