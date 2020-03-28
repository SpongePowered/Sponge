package org.spongepowered.common.data.provider.block;

import net.minecraft.block.BlockState;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.GenericMutableDataProvider;

import java.util.Optional;

public class LightEmissionProvider extends GenericMutableDataProvider<BlockState, Integer> {

    public LightEmissionProvider() {
        super(Keys.LIGHT_EMISSION);
    }

    @Override
    protected Optional<Integer> getFrom(BlockState dataHolder) {
        return Optional.of(dataHolder.getLightValue());
    }

}
