package org.spongepowered.common.data.provider.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.state.properties.DoorHingeSide;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.Hinge;
import org.spongepowered.api.data.type.Hinges;
import org.spongepowered.common.data.provider.BlockStateDataProvider;

import java.util.Optional;

public class DoorBlockHingeProvider extends BlockStateDataProvider<Hinge> {

    public DoorBlockHingeProvider() {
        super(Keys.HINGE_POSITION, DoorBlock.class);
    }

    @Override
    protected Optional<Hinge> getFrom(BlockState dataHolder) {
        final DoorHingeSide hingeSide = dataHolder.get(DoorBlock.HINGE);
        return Optional.of(hingeSide == DoorHingeSide.LEFT ? Hinges.LEFT : Hinges.RIGHT);
    }

    @Override
    protected Optional<BlockState> set(BlockState dataHolder, Hinge value) {
        final DoorHingeSide hingeSide = value == Hinges.LEFT ? DoorHingeSide.LEFT : DoorHingeSide.RIGHT;
        return Optional.of(dataHolder.with(DoorBlock.HINGE, hingeSide));
    }
}
