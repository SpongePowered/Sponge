package org.spongepowered.common.data.provider.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.properties.RedstoneSide;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.common.data.provider.BlockStateDataProvider;

import java.util.Optional;

public class RedstoneWireBlockConnectedProvider extends BlockStateDataProvider<Boolean> {

    private final EnumProperty<RedstoneSide> property;

    RedstoneWireBlockConnectedProvider(Key<? extends Value<Boolean>> key,
            EnumProperty<RedstoneSide> property) {
        super(key, RedstoneWireBlock.class);
        this.property = property;
    }

    @Override
    protected Optional<Boolean> getFrom(BlockState dataHolder) {
        return Optional.of(dataHolder.get(this.property) != RedstoneSide.NONE);
    }

    @Override
    protected Optional<BlockState> set(BlockState dataHolder, Boolean value) {
        return Optional.of(with(dataHolder, this.property, value));
    }

    static BlockState with(BlockState dataHolder, EnumProperty<RedstoneSide> property, boolean value) {
        if (!value) {
            return dataHolder.with(property, RedstoneSide.NONE);
        }
        final RedstoneSide side = dataHolder.get(property);
        if (side == RedstoneSide.NONE) {
            return dataHolder.with(property, RedstoneSide.SIDE);
        }
        return dataHolder;
    }
}
