package org.spongepowered.common.data.provider;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.BooleanProperty;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;

import java.util.Optional;

public class BlockStateBooleanDataProvider extends BlockStateDataProvider<Boolean> {

    private final BooleanProperty property;

    public BlockStateBooleanDataProvider(Key<? extends Value<Boolean>> key,
            Class<? extends Block> blockType, BooleanProperty property) {
        super(key, blockType);
        this.property = property;
    }

    @Override
    protected Optional<Boolean> getFrom(BlockState dataHolder) {
        return Optional.of(dataHolder.get(this.property));
    }

    @Override
    protected Optional<BlockState> set(BlockState dataHolder, Boolean value) {
        return Optional.of(dataHolder.with(this.property, value));
    }
}
